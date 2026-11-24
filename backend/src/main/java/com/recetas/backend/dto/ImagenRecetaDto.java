package com.recetas.backend.dto;

/** Representa una imagen registrada para una receta existente. */
public record ImagenRecetaDto(
        int idImagen,
        int idReceta,
        String urlImagen,
        boolean esPortada
) {
}
