package com.recetas.backend.dto;

import java.time.LocalDateTime;

/** Estado de la relación entre un usuario y una receta en favoritos. */
public record EstadoFavoritoDto(
        boolean favorito,
        LocalDateTime fechaAgregado
) {
}
