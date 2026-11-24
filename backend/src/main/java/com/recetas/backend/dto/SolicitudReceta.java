package com.recetas.backend.dto;

/** Campos admitidos al crear o modificar una receta desde la API. */
public record SolicitudReceta(
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
