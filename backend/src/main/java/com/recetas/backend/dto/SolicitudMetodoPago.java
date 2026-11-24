package com.recetas.backend.dto;

public record SolicitudMetodoPago(
        String tipo,
        String ultimos4Digitos,
        Boolean esPrincipal) { }
