package com.recetas.backend.dto;

/** La forma JSON que consume receta-api.servicio.ts. */
public record CategoriaDto(Integer id, String name, String image) {
}
