package com.recetas.backend.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class LimitadorSolicitudesServicioTest {

    @Test
    void bloqueaDespuesDelMaximoDeIntentosFallidos() {
        LimitadorSolicitudesServicio servicio = new LimitadorSolicitudesServicio();
        Duration ventana = Duration.ofMinutes(10);

        assertTrue(servicio.registrarFallo("login:127.0.0.1:olaf", 2, ventana).permitido());
        assertFalse(servicio.registrarFallo("login:127.0.0.1:olaf", 2, ventana).permitido());
        assertFalse(servicio.comprobar("login:127.0.0.1:olaf", 2, ventana).permitido());
    }

    @Test
    void limpiarRestableceElContadorAlAutenticarse() {
        LimitadorSolicitudesServicio servicio = new LimitadorSolicitudesServicio();
        String clave = "login:127.0.0.1:olaf";
        Duration ventana = Duration.ofMinutes(10);

        servicio.registrarFallo(clave, 1, ventana);
        assertFalse(servicio.comprobar(clave, 1, ventana).permitido());

        servicio.limpiar(clave);
        assertTrue(servicio.comprobar(clave, 1, ventana).permitido());
    }

    @Test
    void limitaSolicitudesOAuthPorVentana() {
        LimitadorSolicitudesServicio servicio = new LimitadorSolicitudesServicio();
        String clave = "oauth-token:127.0.0.1";
        Duration ventana = Duration.ofMinutes(1);

        assertTrue(servicio.consumir(clave, 2, ventana).permitido());
        assertTrue(servicio.consumir(clave, 2, ventana).permitido());
        assertFalse(servicio.consumir(clave, 2, ventana).permitido());
    }
}
