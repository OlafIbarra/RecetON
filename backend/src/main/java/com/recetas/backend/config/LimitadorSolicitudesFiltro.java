package com.recetas.backend.config;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;

import com.recetas.backend.service.LimitadorSolicitudesServicio;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Limita endpoints sensibles antes de que los procese Spring Security.
 * El POST /login solo consulta el contador: los fallos se registran en el
 * AuthenticationFailureHandler, evitando contar rechazos CSRF como contraseñas
 * incorrectas.
 */
public class LimitadorSolicitudesFiltro extends OncePerRequestFilter {

    private final LimitadorSolicitudesServicio limitador;
    private final int maxFallosLogin;
    private final Duration ventanaLogin;
    private final int maxTokensOAuth;
    private final Duration ventanaTokenOAuth;
    private final int maxAutorizacionesOAuth;
    private final Duration ventanaAutorizacionOAuth;

    public LimitadorSolicitudesFiltro(
            LimitadorSolicitudesServicio limitador,
            int maxFallosLogin, Duration ventanaLogin,
            int maxTokensOAuth, Duration ventanaTokenOAuth,
            int maxAutorizacionesOAuth, Duration ventanaAutorizacionOAuth) {
        this.limitador = limitador;
        this.maxFallosLogin = maxFallosLogin;
        this.ventanaLogin = ventanaLogin;
        this.maxTokensOAuth = maxTokensOAuth;
        this.ventanaTokenOAuth = ventanaTokenOAuth;
        this.maxAutorizacionesOAuth = maxAutorizacionesOAuth;
        this.ventanaAutorizacionOAuth = ventanaAutorizacionOAuth;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest solicitud) {
        String ruta = solicitud.getRequestURI();
        String metodo = solicitud.getMethod();
        return !(
                ("POST".equalsIgnoreCase(metodo) && "/login".equals(ruta))
                || ("POST".equalsIgnoreCase(metodo) && "/oauth2/token".equals(ruta))
                || ("GET".equalsIgnoreCase(metodo) && "/oauth2/authorize".equals(ruta)));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest solicitud,
            HttpServletResponse respuesta,
            FilterChain cadena) throws ServletException, IOException {

        String ruta = solicitud.getRequestURI();
        String ipCliente = obtenerIpCliente(solicitud);

        if ("/login".equals(ruta)) {
            String clave = claveLogin(solicitud);
            LimitadorSolicitudesServicio.Resultado resultado =
                    limitador.comprobar(clave, maxFallosLogin, ventanaLogin);
            if (!resultado.permitido()) {
                responderLimiteLogin(respuesta, resultado.segundosEspera());
                return;
            }
        } else if ("/oauth2/token".equals(ruta)) {
            String clave = "oauth-token:" + ipCliente;
            LimitadorSolicitudesServicio.Resultado resultado =
                    limitador.consumir(clave, maxTokensOAuth, ventanaTokenOAuth);
            if (!resultado.permitido()) {
                responderLimiteOAuth(respuesta, resultado.segundosEspera());
                return;
            }
        } else if ("/oauth2/authorize".equals(ruta)) {
            String clave = "oauth-authorize:" + ipCliente;
            LimitadorSolicitudesServicio.Resultado resultado =
                    limitador.consumir(clave, maxAutorizacionesOAuth, ventanaAutorizacionOAuth);
            if (!resultado.permitido()) {
                responderLimiteAutorizacion(respuesta, resultado.segundosEspera());
                return;
            }
        }

        cadena.doFilter(solicitud, respuesta);
    }

    /** Misma clave usada por el filtro y los handlers de autenticación. */
    public static String claveLogin(HttpServletRequest solicitud) {
        String usuario = solicitud.getParameter("username");
        String normalizado = usuario == null ? "<vacio>" : usuario.trim().toLowerCase(Locale.ROOT);
        if (normalizado.length() > 100) {
            normalizado = normalizado.substring(0, 100);
        }
        return "login:" + obtenerIpCliente(solicitud) + ":" + normalizado;
    }

    private static String obtenerIpCliente(HttpServletRequest solicitud) {
        // server.forward-headers-strategy=framework procesa los encabezados del
        // proxy confiable antes de este filtro. No se lee X-Forwarded-For manualmente.
        String ip = solicitud.getRemoteAddr();
        return ip == null || ip.isBlank() ? "desconocida" : ip;
    }

    private static void responderLimiteLogin(HttpServletResponse respuesta, long espera) throws IOException {
        prepararRespuesta(respuesta, espera);
        respuesta.setContentType("text/html;charset=UTF-8");
        String html = """
                <!doctype html><html lang="es"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Espera un momento · RecetON</title></head>
                <body style="font-family:system-ui,sans-serif;background:#f4f8f2;color:#183b2c;padding:2rem">
                <main style="max-width:34rem;margin:8vh auto;background:white;padding:2rem;border-radius:1rem">
                <h1>Demasiados intentos</h1>
                <p>Por seguridad, el inicio de sesión se ha pausado temporalmente.
                Espera %d segundos y vuelve a intentarlo.</p>
                <a href="/login">Volver al inicio de sesión</a>
                </main></body></html>
                """.formatted(espera);
        respuesta.getWriter().write(html);
    }

    private static void responderLimiteOAuth(HttpServletResponse respuesta, long espera) throws IOException {
        prepararRespuesta(respuesta, espera);
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta.getWriter().write(
                "{\"error\":\"temporarily_unavailable\",\"error_description\":\"Demasiadas solicitudes al endpoint OAuth. Inténtalo más tarde.\"}");
    }

    private static void responderLimiteAutorizacion(HttpServletResponse respuesta, long espera) throws IOException {
        prepararRespuesta(respuesta, espera);
        respuesta.setContentType("text/plain;charset=UTF-8");
        respuesta.getWriter().write(
                "Demasiadas solicitudes de autorización. Espera " + espera + " segundos y vuelve a intentarlo.");
    }

    private static void prepararRespuesta(HttpServletResponse respuesta, long espera) {
        respuesta.setStatus(429);
        respuesta.setHeader("Retry-After", Long.toString(Math.max(1L, espera)));
        respuesta.setHeader("Cache-Control", "no-store");
        respuesta.setHeader("Pragma", "no-cache");
        respuesta.setCharacterEncoding("UTF-8");
    }
}
