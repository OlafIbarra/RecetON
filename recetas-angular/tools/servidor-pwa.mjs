import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const WEB_ROOT = path.resolve(__dirname, '..', 'dist', 'recetas-angular', 'browser');
const BACKEND_HOST = process.env.RECETON_BACKEND_HOST || '127.0.0.1';
const BACKEND_PORT = Number(process.env.RECETON_BACKEND_PORT || 8080);
const PORT = Number(process.env.PORT || 4200);
const HOST = process.env.HOST || '0.0.0.0';

const MIME_TYPES = new Map([
  ['.html', 'text/html; charset=utf-8'], ['.js', 'text/javascript; charset=utf-8'],
  ['.mjs', 'text/javascript; charset=utf-8'], ['.css', 'text/css; charset=utf-8'],
  ['.json', 'application/json; charset=utf-8'], ['.webmanifest', 'application/manifest+json; charset=utf-8'],
  ['.svg', 'image/svg+xml'], ['.png', 'image/png'], ['.jpg', 'image/jpeg'], ['.jpeg', 'image/jpeg'],
  ['.webp', 'image/webp'], ['.gif', 'image/gif'], ['.ico', 'image/x-icon'],
  ['.woff', 'font/woff'], ['.woff2', 'font/woff2'], ['.ttf', 'font/ttf'],
]);

const BACKEND_PREFIXES = ['/api', '/oauth2', '/.well-known', '/login', '/logout', '/error', '/saludo', '/adios'];
const esRutaBackend = (pathname) => BACKEND_PREFIXES.some((prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`));

function responderError(res, status, message) {
  if (res.headersSent) return res.destroy();
  res.writeHead(status, { 'Content-Type': 'text/plain; charset=utf-8', 'Cache-Control': 'no-store' });
  res.end(message);
}

function reenviarBackend(req, res) {
  const headers = { ...req.headers, host: `${BACKEND_HOST}:${BACKEND_PORT}` };
  delete headers.connection;
  const proxyReq = http.request({ hostname: BACKEND_HOST, port: BACKEND_PORT, method: req.method, path: req.url, headers }, (proxyRes) => {
    res.writeHead(proxyRes.statusCode || 502, proxyRes.statusMessage, proxyRes.headers);
    proxyRes.pipe(res);
  });
  proxyReq.on('error', () => responderError(res, 502, 'No fue posible conectar con el backend. Comprueba Spring Boot en el puerto 8080.'));
  req.on('aborted', () => proxyReq.destroy());
  req.pipe(proxyReq);
}

function obtenerArchivoSeguro(pathname) {
  let decoded;
  try { decoded = decodeURIComponent(pathname); } catch { return null; }
  const candidato = path.resolve(WEB_ROOT, `.${decoded}`);
  const relativo = path.relative(WEB_ROOT, candidato);
  return relativo.startsWith('..') || path.isAbsolute(relativo) ? null : candidato;
}

function responderArchivo(req, res, pathname) {
  let filePath = obtenerArchivoSeguro(pathname);
  if (!filePath) return responderError(res, 400, 'Ruta no válida.');
  if (pathname === '/') filePath = path.join(WEB_ROOT, 'index.html');

  fs.stat(filePath, (error, stats) => {
    if (!error && stats.isDirectory()) filePath = path.join(filePath, 'index.html');
    fs.stat(filePath, (fileError, fileStats) => {
      if (fileError || !fileStats.isFile()) {
        const aceptaHtml = (req.headers.accept || '').includes('text/html');
        if (aceptaHtml && path.extname(pathname).length === 0) filePath = path.join(WEB_ROOT, 'index.html');
        else return responderError(res, 404, 'Recurso no encontrado.');
      }

      const relative = path.relative(WEB_ROOT, filePath).replaceAll(path.sep, '/');
      const noCache = ['index.html', 'manifest.webmanifest', 'ngsw.json', 'ngsw-worker.js', 'safety-worker.js', 'assets/receton-config.json'].includes(relative);
      res.writeHead(200, {
        'Content-Type': MIME_TYPES.get(path.extname(filePath).toLowerCase()) || 'application/octet-stream',
        'Cache-Control': noCache ? 'no-store' : 'no-cache',
        'X-Content-Type-Options': 'nosniff',
        'Referrer-Policy': 'strict-origin-when-cross-origin',
      });
      if (req.method === 'HEAD') return res.end();
      fs.createReadStream(filePath).on('error', () => responderError(res, 500, 'No fue posible leer el recurso.')).pipe(res);
    });
  });
}

const server = http.createServer((req, res) => {
  let parsedUrl;
  try { parsedUrl = new URL(req.url || '/', 'http://localhost'); }
  catch { return responderError(res, 400, 'Solicitud no válida.'); }
  if (esRutaBackend(parsedUrl.pathname)) return reenviarBackend(req, res);
  if (req.method !== 'GET' && req.method !== 'HEAD') return responderError(res, 405, 'Método no permitido.');
  responderArchivo(req, res, parsedUrl.pathname);
});

server.listen(PORT, HOST, () => {
  if (!fs.existsSync(path.join(WEB_ROOT, 'index.html'))) {
    console.error(`No se encontró el build de RecetON en: ${WEB_ROOT}`);
    console.error('Ejecuta primero: npm run build:pwa');
    process.exitCode = 1;
    server.close();
    return;
  }
  console.log(`RecetON PWA disponible en http://localhost:${PORT}`);
  console.log(`Archivos estáticos: ${WEB_ROOT}`);
  console.log(`Rutas API/OAuth reenviadas a http://${BACKEND_HOST}:${BACKEND_PORT}`);
  console.log('Mantén esta terminal abierta mientras uses la PWA.');
});
