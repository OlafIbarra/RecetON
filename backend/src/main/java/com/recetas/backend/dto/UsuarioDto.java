package com.recetas.backend.dto;

import java.time.LocalDateTime;

/** Nunca expone el hash de contraseña. */
public record UsuarioDto(
        Integer id,
        String username,
        String email,
        String fullName,
        LocalDateTime dateRegistered,
        Boolean active
) {
}
