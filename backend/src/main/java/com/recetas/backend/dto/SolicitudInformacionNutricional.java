package com.recetas.backend.dto;

import java.math.BigDecimal;

/** Datos opcionales de la tabla informacion_nutricional. */
public record SolicitudInformacionNutricional(
        BigDecimal calorias,
        BigDecimal proteinasG,
        BigDecimal carbohidratosG,
        BigDecimal grasasG,
        BigDecimal azucaresG,
        BigDecimal sodioMg) {
}
