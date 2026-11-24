package com.recetas.backend.dto;

/** Equipo o utensilio asociado a la receta. */
public record EquipoCocinaRecetaDto(
        Integer idEquipo,
        String nombre
) {
}
