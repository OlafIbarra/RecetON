package com.recetas.backend.dto;

public record SolicitudUsuario(
        String username,
        String email,
        String password,
        String fullName,
        Boolean active
) {
}
