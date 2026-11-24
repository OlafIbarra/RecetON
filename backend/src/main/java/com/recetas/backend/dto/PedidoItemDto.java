package com.recetas.backend.dto;

import java.math.BigDecimal;

public record PedidoItemDto(
        Integer idProducto,
        String nombreProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) { }
