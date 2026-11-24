package com.recetas.backend.dto;

public record SolicitudDireccion(
        String calle,
        String numero,
        String colonia,
        String ciudad,
        String estado,
        String codigoPostal,
        Boolean esPrincipal) { }
