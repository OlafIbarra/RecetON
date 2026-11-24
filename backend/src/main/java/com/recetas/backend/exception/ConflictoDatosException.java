package com.recetas.backend.exception;

public class ConflictoDatosException extends RuntimeException {
    public ConflictoDatosException(String mensaje) {
        super(mensaje);
    }
}
