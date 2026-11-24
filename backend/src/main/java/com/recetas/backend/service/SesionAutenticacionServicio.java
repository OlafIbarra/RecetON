package com.recetas.backend.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

/**
 * LEGADO: el flujo de inicio de sesión de RecetON se migró a OAuth 2.0 Authorization Code + PKCE
 * y access tokens JWT. Esta clase se conserva temporalmente para que el parche sea incremental,
 * pero el controlador y los servicios actuales ya no la utilizan para autenticar peticiones.
 */
@Deprecated(forRemoval = false)
@Service
public class SesionAutenticacionServicio {

    private static final long DURACION_SESION_MS = Duration.ofHours(8).toMillis();
    private final SecureRandom generadorSeguro = new SecureRandom();
    private final ConcurrentMap<String, SesionActiva> sesiones = new ConcurrentHashMap<>();

    public String crearSesion(int idUsuario) {
        byte[] bytes = new byte[32];
        generadorSeguro.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sesiones.put(token, new SesionActiva(idUsuario, System.currentTimeMillis() + DURACION_SESION_MS));
        return token;
    }

    public Optional<Integer> obtenerIdUsuario(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        SesionActiva sesion = sesiones.get(token);
        if (sesion == null) {
            return Optional.empty();
        }
        if (sesion.expiraEnMillis() <= System.currentTimeMillis()) {
            sesiones.remove(token, sesion);
            return Optional.empty();
        }
        return Optional.of(sesion.idUsuario());
    }

    public void cerrarSesion(String token) {
        if (token != null && !token.isBlank()) {
            sesiones.remove(token);
        }
    }

    /** Invalida todas las sesiones de una cuenta al cambiar la contraseña o eliminarla. */
    public void cerrarTodasLasSesiones(int idUsuario) {
        sesiones.entrySet().removeIf(entrada -> entrada.getValue().idUsuario() == idUsuario);
    }

    public static String extraerToken(String encabezadoAutorizacion) {
        if (encabezadoAutorizacion == null || !encabezadoAutorizacion.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = encabezadoAutorizacion.substring(7).trim();
        return token.isBlank() ? null : token;
    }

    private record SesionActiva(int idUsuario, long expiraEnMillis) { }
}
