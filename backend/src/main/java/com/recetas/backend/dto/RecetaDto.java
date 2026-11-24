package com.recetas.backend.dto;

/** Respuesta de recetas con los nombres que espera el frontend Angular. */
public record RecetaDto(
        Integer id,
        String name,
        String image,
        String price,
        Double rating,
        String time,
        String tag,
        String tagColor,
        String description,
        Integer idUsuario,
        Integer idPais,
        Integer servings,
        String difficulty,
        Boolean active
) {
}
