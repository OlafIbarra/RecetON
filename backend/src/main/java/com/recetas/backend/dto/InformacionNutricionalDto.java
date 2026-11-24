package com.recetas.backend.dto;

import java.math.BigDecimal;

/** Información nutricional almacenada en informacion_nutricional. */
public record InformacionNutricionalDto(
        BigDecimal calorias,
        BigDecimal proteinasG,
        BigDecimal carbohidratosG,
        BigDecimal grasasG,
        BigDecimal azucaresG,
        BigDecimal sodioMg
) {
}
