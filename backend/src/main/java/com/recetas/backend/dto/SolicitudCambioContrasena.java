package com.recetas.backend.dto;

/** Cambio de contraseña autenticado: siempre exige la contraseña actual. */
public record SolicitudCambioContrasena(String contrasenaActual, String contrasenaNueva) { }
