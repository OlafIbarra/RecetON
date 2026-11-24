package com.recetas.backend.dto;

/** Datos necesarios para asociar una imagen a una receta. */
public record SolicitudImagenReceta(
        String urlImagen,
        Boolean esPortada
) {
}
