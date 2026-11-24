package com.recetas.backend.dto;

/** Valoración asociada a una receta y a un usuario existente. */
public record SolicitudValoracionReceta(
        Integer idUsuario,
        Integer puntuacion,
        String comentario) {
}
