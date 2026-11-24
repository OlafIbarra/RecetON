package com.recetas.backend.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.recetas.backend.dto.ActualizacionIngredienteReceta;
import com.recetas.backend.dto.SolicitudIngredienteReceta;
import com.recetas.backend.dto.SolicitudPasoReceta;

/** Operaciones para las tablas existentes de ingredientes y pasos de receta. */
@Repository
public class ContenidoRecetaRepositorio {

    private final JdbcTemplate jdbc;

    public ContenidoRecetaRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Busca o crea el ingrediente del catálogo y lo relaciona con la receta.
     * Si ya estaba relacionado, actualiza la cantidad y la unidad en lugar de
     * duplicar la clave compuesta de receta_ingrediente.
     */
    public void guardarIngrediente(int idReceta, SolicitudIngredienteReceta solicitud) {
        int idIngrediente = buscarOCrearIngrediente(solicitud.nombreIngrediente());
        Integer idUnidad = buscarOCrearUnidad(solicitud.nombreUnidad(), solicitud.abreviaturaUnidad());

        jdbc.update("""
                INSERT INTO receta_ingrediente (id_receta, id_ingrediente, cantidad, id_unidad)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (id_receta, id_ingrediente)
                DO UPDATE SET cantidad = EXCLUDED.cantidad, id_unidad = EXCLUDED.id_unidad
                """, idReceta, idIngrediente, solicitud.cantidad(), idUnidad);
    }

    public boolean actualizarIngrediente(int idReceta, int idIngrediente,
            ActualizacionIngredienteReceta solicitud) {
        Integer idUnidad = buscarOCrearUnidad(solicitud.nombreUnidad(), solicitud.abreviaturaUnidad());
        return jdbc.update("""
                UPDATE receta_ingrediente
                SET cantidad = ?, id_unidad = ?
                WHERE id_receta = ? AND id_ingrediente = ?
                """, solicitud.cantidad(), idUnidad, idReceta, idIngrediente) > 0;
    }

    /** Elimina solo la relación receta-ingrediente; conserva el catálogo. */
    public boolean eliminarIngrediente(int idReceta, int idIngrediente) {
        return jdbc.update("DELETE FROM receta_ingrediente WHERE id_receta = ? AND id_ingrediente = ?",
                idReceta, idIngrediente) > 0;
    }

    public int guardarPaso(int idReceta, SolicitudPasoReceta solicitud) {
        Integer numeroPaso = solicitud.numeroPaso();
        if (numeroPaso == null) {
            numeroPaso = jdbc.queryForObject(
                    "SELECT COALESCE(MAX(numero_paso), 0) + 1 FROM pasos_receta WHERE id_receta = ?",
                    Integer.class, idReceta);
        }
        Integer idPaso = jdbc.queryForObject("SELECT nextval('seq_pasos_receta')", Integer.class);
        jdbc.update("""
                INSERT INTO pasos_receta (id_paso, id_receta, numero_paso, descripcion, tiempo_estimado_min)
                VALUES (?, ?, ?, ?, ?)
                """, idPaso, idReceta, numeroPaso, solicitud.descripcion().trim(), solicitud.tiempoEstimadoMin());
        return idPaso;
    }

    public boolean actualizarPaso(int idReceta, int idPaso, SolicitudPasoReceta solicitud) {
        return jdbc.update("""
                UPDATE pasos_receta
                SET numero_paso = COALESCE(?, numero_paso),
                    descripcion = ?,
                    tiempo_estimado_min = ?
                WHERE id_paso = ? AND id_receta = ?
                """, solicitud.numeroPaso(), solicitud.descripcion().trim(), solicitud.tiempoEstimadoMin(),
                idPaso, idReceta) > 0;
    }

    public boolean eliminarPaso(int idReceta, int idPaso) {
        return jdbc.update("DELETE FROM pasos_receta WHERE id_paso = ? AND id_receta = ?",
                idPaso, idReceta) > 0;
    }

    private int buscarOCrearIngrediente(String nombre) {
        List<Integer> ids = jdbc.query("""
                SELECT id_ingrediente
                FROM ingredientes
                WHERE LOWER(nombre_ingrediente) = LOWER(?)
                ORDER BY id_ingrediente
                LIMIT 1
                """, (rs, fila) -> rs.getInt("id_ingrediente"), nombre.trim());
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        Integer id = jdbc.queryForObject("SELECT nextval('seq_ingredientes')", Integer.class);
        jdbc.update("INSERT INTO ingredientes (id_ingrediente, nombre_ingrediente) VALUES (?, ?)", id, nombre.trim());
        return id;
    }

    /** La unidad es opcional; cuando se proporciona, se busca o crea en su catálogo. */
    private Integer buscarOCrearUnidad(String nombreUnidad, String abreviaturaUnidad) {
        String nombre = limpiar(nombreUnidad);
        String abreviatura = limpiar(abreviaturaUnidad);
        if (nombre == null && abreviatura == null) {
            return null;
        }
        if (nombre == null) {
            nombre = abreviatura;
        }
        if (abreviatura == null) {
            abreviatura = nombre;
        }

        List<Integer> ids = jdbc.query("""
                SELECT id_unidad
                FROM unidades_medida
                WHERE LOWER(nombre_unidad) = LOWER(?) OR LOWER(abreviatura) = LOWER(?)
                ORDER BY id_unidad
                LIMIT 1
                """, (rs, fila) -> rs.getInt("id_unidad"), nombre, abreviatura);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        Integer id = jdbc.queryForObject("SELECT nextval('seq_unidades_medida')", Integer.class);
        jdbc.update("INSERT INTO unidades_medida (id_unidad, nombre_unidad, abreviatura) VALUES (?, ?, ?)",
                id, nombre, abreviatura);
        return id;
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
