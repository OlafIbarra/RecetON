package com.recetas.backend.dto;

public record MetodoPagoDto(
        Integer idMetodoPago,
        Integer idUsuario,
        String tipo,
        String ultimos4Digitos,
        Boolean esPrincipal) { }
