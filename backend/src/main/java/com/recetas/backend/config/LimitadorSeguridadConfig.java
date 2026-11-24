package com.recetas.backend.config;

import java.time.Duration;

import com.recetas.backend.service.LimitadorSolicitudesServicio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/** Configura límites para OAuth y fallos reales de contraseña. */
@Configuration
public class LimitadorSeguridadConfig {

    @Bean
    FilterRegistrationBean<LimitadorSolicitudesFiltro> registroLimitadorSolicitudes(
            LimitadorSolicitudesServicio limitador,
            @Value("${receton.security.rate-limit.login.max-attempts:8}") int maxFallosLogin,
            @Value("${receton.security.rate-limit.login.window-seconds:600}") long ventanaLoginSegundos,
            @Value("${receton.security.rate-limit.oauth-token.max-attempts:30}") int maxTokensOAuth,
            @Value("${receton.security.rate-limit.oauth-token.window-seconds:60}") long ventanaTokenSegundos,
            @Value("${receton.security.rate-limit.oauth-authorize.max-attempts:60}") int maxAutorizacionesOAuth,
            @Value("${receton.security.rate-limit.oauth-authorize.window-seconds:60}") long ventanaAutorizacionSegundos) {

        validarLimite("login", maxFallosLogin, ventanaLoginSegundos);
        validarLimite("oauth-token", maxTokensOAuth, ventanaTokenSegundos);
        validarLimite("oauth-authorize", maxAutorizacionesOAuth, ventanaAutorizacionSegundos);

        LimitadorSolicitudesFiltro filtro = new LimitadorSolicitudesFiltro(
                limitador,
                maxFallosLogin, Duration.ofSeconds(ventanaLoginSegundos),
                maxTokensOAuth, Duration.ofSeconds(ventanaTokenSegundos),
                maxAutorizacionesOAuth, Duration.ofSeconds(ventanaAutorizacionSegundos));

        FilterRegistrationBean<LimitadorSolicitudesFiltro> registro = new FilterRegistrationBean<>();
        registro.setFilter(filtro);
        registro.setName("limitadorSolicitudesReceton");
        registro.addUrlPatterns("/*");
        // Se ejecuta después de ForwardedHeaderFilter y antes de Spring Security.
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registro;
    }

    @Bean
    AuthenticationFailureHandler recetonLoginFailureHandler(
            LimitadorSolicitudesServicio limitador,
            @Value("${receton.security.rate-limit.login.max-attempts:8}") int maxFallosLogin,
            @Value("${receton.security.rate-limit.login.window-seconds:600}") long ventanaLoginSegundos) {
        SimpleUrlAuthenticationFailureHandler redireccion =
                new SimpleUrlAuthenticationFailureHandler("/login?error");

        return (solicitud, respuesta, excepcion) -> {
            String clave = LimitadorSolicitudesFiltro.claveLogin(solicitud);
            LimitadorSolicitudesServicio.Resultado resultado = limitador.registrarFallo(
                    clave, maxFallosLogin, Duration.ofSeconds(ventanaLoginSegundos));

            if (!resultado.permitido()) {
                responderDemasiadosIntentos(respuesta, resultado.segundosEspera());
                return;
            }
            redireccion.onAuthenticationFailure(solicitud, respuesta, excepcion);
        };
    }

    @Bean
    AuthenticationSuccessHandler recetonLoginSuccessHandler(LimitadorSolicitudesServicio limitador) {
        SavedRequestAwareAuthenticationSuccessHandler redireccion =
                new SavedRequestAwareAuthenticationSuccessHandler();

        return (solicitud, respuesta, autenticacion) -> {
            limitador.limpiar(LimitadorSolicitudesFiltro.claveLogin(solicitud));
            redireccion.onAuthenticationSuccess(solicitud, respuesta, autenticacion);
        };
    }

    private static void validarLimite(String nombre, int maximo, long segundos) {
        if (maximo < 1 || segundos < 1) {
            throw new IllegalStateException(
                    "La configuración de rate limiting para " + nombre + " debe usar valores positivos.");
        }
    }

    private static void responderDemasiadosIntentos(
            jakarta.servlet.http.HttpServletResponse respuesta, long espera) throws java.io.IOException {
        respuesta.setStatus(429);
        respuesta.setHeader("Retry-After", Long.toString(Math.max(1L, espera)));
        respuesta.setHeader("Cache-Control", "no-store");
        respuesta.setContentType("text/html;charset=UTF-8");
        respuesta.setCharacterEncoding("UTF-8");
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
                """.formatted(Math.max(1L, espera));
        respuesta.getWriter().write(html);
    }
}
