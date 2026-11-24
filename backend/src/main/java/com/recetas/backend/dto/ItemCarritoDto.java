package com.recetas.backend.dto;

import java.math.BigDecimal;

public record ItemCarritoDto(
        Integer idProducto,
        String nombreProducto,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        Integer cantidad,
        BigDecimal subtotal,
        Integer idReceta,
        String nombreReceta,
        String imagen
) {
}
