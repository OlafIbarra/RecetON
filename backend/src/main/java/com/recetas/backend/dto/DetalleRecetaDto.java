package com.recetas.backend.dto;

import java.util.List;

/**
 * Vista de detalle construida desde recetas y sus tablas relacionadas.
 * No agrega ni presupone columnas nuevas en el esquema de PostgreSQL.
 */
public record DetalleRecetaDto(
        RecetaDto receta,
        String paisOrigen,
        List<CategoriaDto> categorias,
        List<String> imagenes,
        List<IngredienteRecetaDto> ingredientes,
        List<PasoRecetaDto> pasos,
        List<EquipoCocinaRecetaDto> equipos,
        InformacionNutricionalDto informacionNutricional,
        List<ValoracionRecetaDto> valoraciones
) {
}
