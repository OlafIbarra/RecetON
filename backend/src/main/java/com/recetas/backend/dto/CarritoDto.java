package com.recetas.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarritoDto(
        Integer idCarrito,
        Integer idUsuario,
        List<ItemCarritoDto> items,
        BigDecimal subtotal
) {
}
