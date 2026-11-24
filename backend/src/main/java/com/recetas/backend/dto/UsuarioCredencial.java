package com.recetas.backend.dto;

public record UsuarioCredencial(
        Integer id,
        String nombreUsuario,
        String nombreCompleto,
        String contrasenaHash,
        Boolean activo
) {
}
