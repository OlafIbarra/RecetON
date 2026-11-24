package com.recetas.backend.dto;

import java.math.BigDecimal;

/** Producto del catálogo; imagen y nombre de receta se obtienen de sus relaciones existentes. */
public record ProductoDto(
        Integer idProducto,
        Integer idReceta,
        String nombreProducto,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        String nombreReceta,
        String imagen
) {
}
