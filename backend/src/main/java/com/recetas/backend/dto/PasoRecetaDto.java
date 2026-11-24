package com.recetas.backend.dto;

/** Paso de preparación de una receta. idPaso identifica el registro en pasos_receta. */
public record PasoRecetaDto(
        Integer idPaso,
        Integer numeroPaso,
        String descripcion,
        Integer tiempoEstimadoMin
) {
}
