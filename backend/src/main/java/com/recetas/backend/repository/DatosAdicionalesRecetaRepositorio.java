package com.recetas.backend.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.recetas.backend.dto.SolicitudInformacionNutricional;
import com.recetas.backend.dto.SolicitudValoracionReceta;

/** Persistencia para equipo, nutrición y valoraciones usando el esquema existente. */
@Repository
public class DatosAdicionalesRecetaRepositorio {

    private final JdbcTemplate jdbc;

    public DatosAdicionalesRecetaRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Busca un equipo existente o lo crea en el catálogo y asocia su ID a la receta. */
    public void agregarEquipo(int idReceta, String nombreEquipo) {
        List<Integer> ids = jdbc.query("""
                SELECT id_equipo
                FROM equipos_cocina
                WHERE LOWER(nombre_equipo) = LOWER(?)
                ORDER BY id_equipo
                LIMIT 1
                """, (rs, fila) -> rs.getInt("id_equipo"), nombreEquipo.trim());

        int idEquipo;
        if (ids.isEmpty()) {
            Integer nuevoId = jdbc.queryForObject("SELECT nextval('seq_equipos_cocina')", Integer.class);
            if (nuevoId == null) {
                throw new IllegalStateException("No se pudo generar el identificador del equipo.");
            }
            idEquipo = nuevoId;
            jdbc.update("INSERT INTO equipos_cocina (id_equipo, nombre_equipo) VALUES (?, ?)",
                    idEquipo, nombreEquipo.trim());
        } else {
            idEquipo = ids.get(0);
        }

        jdbc.update("""
                INSERT INTO receta_equipo (id_receta, id_equipo)
                VALUES (?, ?)
                ON CONFLICT (id_receta, id_equipo) DO NOTHING
                """, idReceta, idEquipo);
    }

    /** Quita solo la asociación y conserva el equipo en el catálogo. */
    public boolean eliminarEquipo(int idReceta, int idEquipo) {
        return jdbc.update("DELETE FROM receta_equipo WHERE id_receta = ? AND id_equipo = ?",
                idReceta, idEquipo) > 0;
    }

    /** La tabla tiene id_receta como PK, así que se actualiza una única fila por receta. */
    public void guardarInformacionNutricional(int idReceta, SolicitudInformacionNutricional solicitud) {
        jdbc.update("""
                INSERT INTO informacion_nutricional
                    (id_receta, calorias, proteinas_g, carbohidratos_g, grasas_g, azucares_g, sodio_mg)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id_receta) DO UPDATE SET
                    calorias = EXCLUDED.calorias,
                    proteinas_g = EXCLUDED.proteinas_g,
                    carbohidratos_g = EXCLUDED.carbohidratos_g,
                    grasas_g = EXCLUDED.grasas_g,
                    azucares_g = EXCLUDED.azucares_g,
                    sodio_mg = EXCLUDED.sodio_mg
                """, idReceta, solicitud.calorias(), solicitud.proteinasG(), solicitud.carbohidratosG(),
                solicitud.grasasG(), solicitud.azucaresG(), solicitud.sodioMg());
    }

    public boolean eliminarInformacionNutricional(int idReceta) {
        return jdbc.update("DELETE FROM informacion_nutricional WHERE id_receta = ?", idReceta) > 0;
    }

    public boolean existeUsuario(int idUsuario) {
        Integer cantidad = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE id_usuario = ?", Integer.class, idUsuario);
        return cantidad != null && cantidad > 0;
    }

    public void agregarValoracion(int idReceta, SolicitudValoracionReceta solicitud) {
        Integer idValoracion = jdbc.queryForObject("SELECT nextval('seq_valoraciones')", Integer.class);
        if (idValoracion == null) {
            throw new IllegalStateException("No se pudo generar el identificador de la valoración.");
        }
        jdbc.update("""
                INSERT INTO valoraciones
                    (id_valoracion, id_usuario, id_receta, puntuacion, comentario_breve, fecha)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """, idValoracion, solicitud.idUsuario(), idReceta,
                solicitud.puntuacion(), limpiar(solicitud.comentario()));
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
