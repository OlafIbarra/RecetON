package com.recetas.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoDetalleDto(
        Integer idPedido,
        Integer idUsuario,
        String usuario,
        LocalDateTime fechaPedido,
        String estado,
        BigDecimal total,
        DireccionDto direccionEnvio,
        MetodoPagoDto metodoPago,
        List<PedidoItemDto> items) { }
