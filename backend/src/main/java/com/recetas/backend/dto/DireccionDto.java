package com.recetas.backend.dto;

public record DireccionDto(
        Integer idDireccion,
        Integer idUsuario,
        String calle,
        String numero,
        String colonia,
        String ciudad,
        String estado,
        String codigoPostal,
        Boolean esPrincipal) { }
