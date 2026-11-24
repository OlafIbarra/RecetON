package com.recetas.backend.dto;

public record RespuestaLoginDto(boolean exito, String mensaje, UsuarioSesionDto usuario, String token) {
    public static RespuestaLoginDto error(String mensaje) {
        return new RespuestaLoginDto(false, mensaje, null, null);
    }

    public static RespuestaLoginDto correcto(UsuarioSesionDto usuario, String token) {
        return new RespuestaLoginDto(true, "Sesión iniciada correctamente.", usuario, token);
    }
}
