package com.recetas.backend.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.recetas.backend.dto.SolicitudArcoDto;

/** Acceso a solicitudes ARCO usando los nombres de columnas del esquema real de RecetON. */
@Repository
public class SolicitudesArcoRepositorio {
    private final JdbcTemplate jdbc;

    public SolicitudesArcoRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /*
     * La tabla existente public.solicitudes_arco utiliza:
     * id_solicitud_arco, tipo_derecho, correo_contacto, etc.
     * No contiene id_responsable ni fecha_actualizacion; no se inventan columnas.
     */
    private static final String SELECT = """
            SELECT s.id_solicitud_arco AS id_solicitud,
                   s.id_usuario,
                   u.nombre_usuario,
                   s.correo_contacto AS correo,
                   s.tipo_derecho AS derecho,
                   s.descripcion,
                   s.fecha_solicitud,
                   s.estado,
                   s.respuesta,
                   s.fecha_resolucion,
                   CAST(NULL AS VARCHAR) AS nombre_responsable
            FROM public.solicitudes_arco s
            LEFT JOIN public.usuarios u ON u.id_usuario = s.id_usuario
            """;

    /** Obtiene el correo activo de la cuenta para guardarlo como dato de contacto de la solicitud. */
    public Optional<String> obtenerCorreoContacto(int idUsuario) {
        List<String> correos = jdbc.query("""
                SELECT NULLIF(BTRIM(correo), '') AS correo
                FROM public.usuarios
                WHERE id_usuario = ? AND activo IS TRUE
                """, (rs, fila) -> rs.getString("correo"), idUsuario);
        return correos.stream()
                .filter(correo -> correo != null && !correo.isBlank())
                .map(String::trim)
                .findFirst();
    }

    public Long insertar(int idUsuario, String derecho, String descripcion, String correoContacto) {
        return jdbc.queryForObject("""
                INSERT INTO public.solicitudes_arco
                    (id_usuario, tipo_derecho, descripcion, correo_contacto)
                VALUES (?, ?, ?, ?)
                RETURNING id_solicitud_arco
                """, Long.class, idUsuario, derecho, descripcion, correoContacto);
    }

    public List<SolicitudArcoDto> listarPorUsuario(int idUsuario) {
        return jdbc.query(SELECT + " WHERE s.id_usuario = ? ORDER BY s.fecha_solicitud DESC, s.id_solicitud_arco DESC",
                (rs, fila) -> mapear(rs), idUsuario);
    }

    public List<SolicitudArcoDto> listarTodas() {
        return jdbc.query(SELECT + " ORDER BY s.fecha_solicitud DESC, s.id_solicitud_arco DESC",
                (rs, fila) -> mapear(rs));
    }

    public Optional<SolicitudArcoDto> buscarPorId(long idSolicitud) {
        List<SolicitudArcoDto> resultados = jdbc.query(
                SELECT + " WHERE s.id_solicitud_arco = ?",
                (rs, fila) -> mapear(rs), idSolicitud);
        return resultados.stream().findFirst();
    }

    /**
     * La tabla actual registra estado, respuesta y fecha_resolucion.
     * No registra qué administrador atendió el caso ni una fecha de actualización independiente.
     */
    public boolean actualizarEstado(long idSolicitud, String estado, String respuesta) {
        return jdbc.update("""
                UPDATE public.solicitudes_arco
                SET estado = ?,
                    respuesta = NULLIF(BTRIM(?), ''),
                    fecha_resolucion = CASE
                        WHEN ? IN ('Atendida', 'No procedente') THEN CURRENT_TIMESTAMP
                        ELSE NULL
                    END
                WHERE id_solicitud_arco = ?
                """, estado, respuesta, estado, idSolicitud) > 0;
    }

    private SolicitudArcoDto mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new SolicitudArcoDto(
                rs.getLong("id_solicitud"),
                rs.getInt("id_usuario"),
                rs.getString("nombre_usuario"),
                rs.getString("correo"),
                rs.getString("derecho"),
                rs.getString("descripcion"),
                aLocalDateTime(rs.getTimestamp("fecha_solicitud")),
                rs.getString("estado"),
                rs.getString("respuesta"),
                aLocalDateTime(rs.getTimestamp("fecha_resolucion")),
                rs.getString("nombre_responsable"));
    }

    private LocalDateTime aLocalDateTime(Timestamp fecha) {
        return fecha == null ? null : fecha.toLocalDateTime();
    }
}
