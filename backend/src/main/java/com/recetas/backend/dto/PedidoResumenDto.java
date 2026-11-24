package com.recetas.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResumenDto(
        Integer idPedido,
        Integer idUsuario,
        String usuario,
        LocalDateTime fechaPedido,
        String estado,
        BigDecimal total,
        Integer cantidadArticulos) { }
