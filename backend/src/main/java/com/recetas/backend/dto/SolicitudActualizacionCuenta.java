package com.recetas.backend.dto;

/** Campos editables del perfil propio. No acepta roles ni estado de la cuenta. */
public record SolicitudActualizacionCuenta(String username, String email, String fullName) { }
