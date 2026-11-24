package com.recetas.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.recetas.backend.repository.CuentasEliminadasRepositorio;

/**
 * Depura diariamente los registros de baja cuyo plazo académico de dos años venció.
 * La purga se limita a cuentas_eliminadas; no elimina las cuentas anonimizadas ni sus
 * relaciones comerciales, recetas, pedidos o expedientes ARCO.
 */
@Service
public class DepuracionCuentasEliminadasServicio {

    private static final Logger LOG = LoggerFactory.getLogger(DepuracionCuentasEliminadasServicio.class);

    private final CuentasEliminadasRepositorio repositorio;

    public DepuracionCuentasEliminadasServicio(CuentasEliminadasRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /** Por defecto se ejecuta todos los días a las 03:00 en hora de Ciudad de México. */
    @Scheduled(
            cron = "${receton.retencion.cuentas-eliminadas.cron:0 0 3 * * *}",
            zone = "${receton.retencion.zona-horaria:America/Mexico_City}")
    public void depurarRegistrosVencidos() {
        try {
            int eliminados = repositorio.purgarVencidas();
            if (eliminados > 0) {
                LOG.info("Depuración de cuentas eliminadas completada: {} registro(s) vencido(s) eliminado(s).", eliminados);
            } else {
                LOG.debug("Depuración de cuentas eliminadas completada: no hay registros vencidos.");
            }
        } catch (DataAccessException ex) {
            // No se registran datos personales. La próxima ejecución programada lo reintentará.
            LOG.error("No se pudo depurar el archivo de bajas vencidas. Se volverá a intentar en la próxima ejecución.", ex);
        }
    }
}
