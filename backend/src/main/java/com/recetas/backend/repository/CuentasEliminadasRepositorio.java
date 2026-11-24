package com.recetas.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Operaciones de archivo y purga de registros de bajas ya vencidos. */
@Repository
public class CuentasEliminadasRepositorio {

    private final JdbcTemplate jdbc;

    public CuentasEliminadasRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Cuenta únicamente los registros cuyo plazo de conservación ya venció. */
    public int contarVencidas() {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM cuentas_eliminadas WHERE fecha_purga <= CURRENT_TIMESTAMP",
                Integer.class);
        return total == null ? 0 : total;
    }

    /**
     * Elimina solamente filas del archivo de bajas cuya fecha_purga ya venció.
     * No elimina usuarios, pedidos, recetas ni solicitudes ARCO.
     */
    public int purgarVencidas() {
        return jdbc.update("DELETE FROM cuentas_eliminadas WHERE fecha_purga <= CURRENT_TIMESTAMP");
    }
}
