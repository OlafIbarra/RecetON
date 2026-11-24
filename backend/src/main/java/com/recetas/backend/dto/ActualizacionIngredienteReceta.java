package com.recetas.backend.dto;

import java.math.BigDecimal;

/** Datos que se pueden modificar sin cambiar el nombre global del ingrediente. */
public record ActualizacionIngredienteReceta(
        BigDecimal cantidad,
        String nombreUnidad,
        String abreviaturaUnidad
) {
}
