package com.recetas.backend.dto;

import java.time.LocalDateTime;

/** Valoración y comentario vinculados a una receta. */
public record ValoracionRecetaDto(
        Integer idValoracion,
        String usuario,
        Integer puntuacion,
        String comentario,
        LocalDateTime fecha
) {
}
