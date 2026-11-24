package com.recetas.backend.dto;

import java.math.BigDecimal;

/** Campos de productos. No incluye una columna de imagen, porque productos no la tiene. */
public record SolicitudProducto(
        Integer idReceta,
        String nombreProducto,
        String descripcion,
        BigDecimal precio,
        Integer stock
) {
}
