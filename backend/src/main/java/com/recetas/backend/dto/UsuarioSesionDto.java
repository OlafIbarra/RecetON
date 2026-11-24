package com.recetas.backend.dto;

import java.util.List;

public record UsuarioSesionDto(Integer id, String nombreUsuario, String nombreCompleto, List<String> roles) {
}
