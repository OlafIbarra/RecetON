package com.recetas.backend.dto;

/** Datos para crear o actualizar un paso de preparación. */
public record SolicitudPasoReceta(
        Integer numeroPaso,
        String descripcion,
        Integer tiempoEstimadoMin
) {
}
