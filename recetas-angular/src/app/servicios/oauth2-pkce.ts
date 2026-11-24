/**
 * Utilidades PKCE para OAuth 2.0 Authorization Code.
 * Incluye SHA-256 en TypeScript porque WebCrypto.subtle no está disponible en todos
 * los orígenes HTTP de una LAN (por ejemplo, http://172.20.10.2:4200).
 */

const CONSTANTES_SHA256 = new Uint32Array([
  0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
  0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
  0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
  0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
  0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
  0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
  0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
  0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2,
]);

const ESTADO_INICIAL = new Uint32Array([
  0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
  0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19,
]);

function rotarDerecha(valor: number, cantidad: number): number {
  return (valor >>> cantidad) | (valor << (32 - cantidad));
}

function calcularSha256(mensaje: Uint8Array): Uint8Array {
  const longitudOriginal = mensaje.length;
  const longitudConMarca = longitudOriginal + 1;
  const longitudRellenada = Math.ceil((longitudConMarca + 8) / 64) * 64;
  const datos = new Uint8Array(longitudRellenada);
  datos.set(mensaje);
  datos[longitudOriginal] = 0x80;

  const vista = new DataView(datos.buffer);
  const bitsAltos = Math.floor((longitudOriginal * 8) / 0x100000000);
  const bitsBajos = (longitudOriginal * 8) >>> 0;
  vista.setUint32(longitudRellenada - 8, bitsAltos);
  vista.setUint32(longitudRellenada - 4, bitsBajos);

  const estado = new Uint32Array(ESTADO_INICIAL);
  const palabras = new Uint32Array(64);

  for (let bloque = 0; bloque < longitudRellenada; bloque += 64) {
    for (let i = 0; i < 16; i++) {
      palabras[i] = vista.getUint32(bloque + i * 4);
    }
    for (let i = 16; i < 64; i++) {
      const x = palabras[i - 15];
      const y = palabras[i - 2];
      const sigma0 = rotarDerecha(x, 7) ^ rotarDerecha(x, 18) ^ (x >>> 3);
      const sigma1 = rotarDerecha(y, 17) ^ rotarDerecha(y, 19) ^ (y >>> 10);
      palabras[i] = (palabras[i - 16] + sigma0 + palabras[i - 7] + sigma1) >>> 0;
    }

    let a = estado[0]; let b = estado[1]; let c = estado[2]; let d = estado[3];
    let e = estado[4]; let f = estado[5]; let g = estado[6]; let h = estado[7];

    for (let i = 0; i < 64; i++) {
      const suma1 = rotarDerecha(e, 6) ^ rotarDerecha(e, 11) ^ rotarDerecha(e, 25);
      const eleccion = (e & f) ^ (~e & g);
      const temporal1 = (h + suma1 + eleccion + CONSTANTES_SHA256[i] + palabras[i]) >>> 0;
      const suma0 = rotarDerecha(a, 2) ^ rotarDerecha(a, 13) ^ rotarDerecha(a, 22);
      const mayoria = (a & b) ^ (a & c) ^ (b & c);
      const temporal2 = (suma0 + mayoria) >>> 0;
      h = g; g = f; f = e; e = (d + temporal1) >>> 0;
      d = c; c = b; b = a; a = (temporal1 + temporal2) >>> 0;
    }

    estado[0] = (estado[0] + a) >>> 0;
    estado[1] = (estado[1] + b) >>> 0;
    estado[2] = (estado[2] + c) >>> 0;
    estado[3] = (estado[3] + d) >>> 0;
    estado[4] = (estado[4] + e) >>> 0;
    estado[5] = (estado[5] + f) >>> 0;
    estado[6] = (estado[6] + g) >>> 0;
    estado[7] = (estado[7] + h) >>> 0;
  }

  const salida = new Uint8Array(32);
  const salidaVista = new DataView(salida.buffer);
  estado.forEach((valor, indice) => salidaVista.setUint32(indice * 4, valor));
  return salida;
}

function base64Url(datos: Uint8Array): string {
  let binario = '';
  datos.forEach((byte) => { binario += String.fromCharCode(byte); });
  return btoa(binario).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
}

function aleatorioBase64Url(tamano: number): string {
  const bytes = new Uint8Array(tamano);
  crypto.getRandomValues(bytes);
  return base64Url(bytes);
}

export function crearParametrosPkce(): { state: string; verifier: string; challenge: string } {
  const state = aleatorioBase64Url(32);
  const verifier = aleatorioBase64Url(32);
  const challenge = base64Url(calcularSha256(new TextEncoder().encode(verifier)));
  return { state, verifier, challenge };
}
