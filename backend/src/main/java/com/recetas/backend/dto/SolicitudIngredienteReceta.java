package com.recetas.backend.dto;

import java.math.BigDecimal;

/** Datos para asociar un ingrediente existente o nuevo a una receta. */
public record SolicitudIngredienteReceta(
        String nombreIngrediente,
        BigDecimal cantidad,
        String nombreUnidad,
        String abreviaturaUnidad
) {
}
