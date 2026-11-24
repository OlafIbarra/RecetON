package com.recetas.backend.dto;

import java.math.BigDecimal;

/** Ingrediente y cantidad asociados a una receta, obtenidos de las tablas existentes. */
public record IngredienteRecetaDto(
        Integer idIngrediente,
        String nombre,
        BigDecimal cantidad,
        String unidad,
        String abreviatura
) {
}
