package com.recetas.backend.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.recetas.backend.dto.CategoriaDto;
import com.recetas.backend.dto.DetalleRecetaDto;
import com.recetas.backend.dto.EquipoCocinaRecetaDto;
import com.recetas.backend.dto.InformacionNutricionalDto;
import com.recetas.backend.dto.IngredienteRecetaDto;
import com.recetas.backend.dto.PasoRecetaDto;
import com.recetas.backend.dto.RecetaDto;
import com.recetas.backend.dto.SolicitudReceta;
import com.recetas.backend.dto.ValoracionRecetaDto;

@Repository
public class RecetaRepositorio {

    private static final String CONSULTA_BASE = """
            SELECT
                r.id_receta AS id,
                r.titulo AS name,
                COALESCE((
                    SELECT ir.url_imagen
                    FROM imagenes_receta ir
                    WHERE ir.id_receta = r.id_receta
                    ORDER BY ir.es_portada DESC NULLS LAST, ir.id_imagen DESC
                    LIMIT 1
                ), '') AS image,
                COALESCE(r.precio, 0)::text AS price,
                COALESCE((
                    SELECT AVG(v.puntuacion)::double precision
                    FROM valoraciones v
                    WHERE v.id_receta = r.id_receta
                ), 0.0) AS rating,
                COALESCE(r.tiempo_preparacion_min::text || ' min', '') AS time,
                (
                    SELECT c.nombre_categoria
                    FROM receta_categoria rc
                    JOIN categorias c ON c.id_categoria = rc.id_categoria
                    WHERE rc.id_receta = r.id_receta
                    ORDER BY c.id_categoria
                    LIMIT 1
                ) AS tag,
                CASE WHEN r.id_receta % 3 = 0 THEN 'plum'
                     WHEN r.id_receta % 3 = 1 THEN 'sage'
                     ELSE 'squash' END AS "tagColor",
                r.descripcion AS description,
                r.id_usuario AS "idUsuario",
                r.id_pais AS "idPais",
                r.porciones AS servings,
                r.dificultad AS difficulty,
                r.activo AS active
            FROM recetas r
            """;

    private final JdbcTemplate jdbc;

    public RecetaRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<RecetaDto> mapper = this::mapear;

    public List<RecetaDto> listar() {
        return jdbc.query(CONSULTA_BASE + " ORDER BY r.id_receta DESC", mapper);
    }

    public Optional<RecetaDto> buscarPorId(int id) {
        List<RecetaDto> resultados = jdbc.query(CONSULTA_BASE + " WHERE r.id_receta = ?", mapper, id);
        return resultados.stream().findFirst();
    }

    /** Lista las recetas que un usuario agregó a favoritos. */
    public List<RecetaDto> listarFavoritosPorUsuario(int idUsuario) {
        return jdbc.query(CONSULTA_BASE
                + " WHERE EXISTS (SELECT 1 FROM favoritos f WHERE f.id_receta = r.id_receta AND f.id_usuario = ?)"
                + " ORDER BY (SELECT f.fecha_agregado FROM favoritos f WHERE f.id_receta = r.id_receta AND f.id_usuario = ?) DESC NULLS LAST, r.id_receta DESC",
                mapper, idUsuario, idUsuario);
    }

    /**
     * Consulta el detalle completo usando exclusivamente las tablas existentes
     * en form_recetas. Las relaciones opcionales se devuelven vacías o nulas
     * cuando aún no tienen datos.
     */
    public DetalleRecetaDto buscarDetalle(int id, RecetaDto receta) {
        List<String> nombresPais = jdbc.query("""
                SELECT p.nombre_pais
                FROM recetas r
                LEFT JOIN paises p ON p.id_pais = r.id_pais
                WHERE r.id_receta = ?
                """, (rs, fila) -> rs.getString("nombre_pais"), id);
        // LEFT JOIN puede devolver una fila cuyo nombre_pais sea NULL.
        // No usar Stream.findFirst() directamente: no admite un primer elemento NULL.
        String paisOrigen = nombresPais.isEmpty() ? null : nombresPais.get(0);

        List<CategoriaDto> categorias = jdbc.query("""
                SELECT c.id_categoria AS id, c.nombre_categoria AS name, c.imagen_url AS image
                FROM receta_categoria rc
                JOIN categorias c ON c.id_categoria = rc.id_categoria
                WHERE rc.id_receta = ?
                ORDER BY c.nombre_categoria
                """, (rs, fila) -> new CategoriaDto(
                        rs.getInt("id"), rs.getString("name"), rs.getString("image")), id);

        List<String> imagenes = jdbc.query("""
                SELECT url_imagen
                FROM imagenes_receta
                WHERE id_receta = ?
                ORDER BY es_portada DESC NULLS LAST, id_imagen
                """, (rs, fila) -> rs.getString("url_imagen"), id);

        List<IngredienteRecetaDto> ingredientes = jdbc.query("""
                SELECT i.id_ingrediente,
                       i.nombre_ingrediente,
                       ri.cantidad,
                       um.nombre_unidad,
                       um.abreviatura
                FROM receta_ingrediente ri
                JOIN ingredientes i ON i.id_ingrediente = ri.id_ingrediente
                LEFT JOIN unidades_medida um ON um.id_unidad = ri.id_unidad
                WHERE ri.id_receta = ?
                ORDER BY i.nombre_ingrediente
                """, (rs, fila) -> new IngredienteRecetaDto(
                        rs.getInt("id_ingrediente"),
                        rs.getString("nombre_ingrediente"),
                        rs.getBigDecimal("cantidad"),
                        rs.getString("nombre_unidad"),
                        rs.getString("abreviatura")), id);

        List<PasoRecetaDto> pasos = jdbc.query("""
                SELECT id_paso, numero_paso, descripcion, tiempo_estimado_min
                FROM pasos_receta
                WHERE id_receta = ?
                ORDER BY numero_paso, id_paso
                """, (rs, fila) -> new PasoRecetaDto(
                        rs.getInt("id_paso"),
                        (Integer) rs.getObject("numero_paso"),
                        rs.getString("descripcion"),
                        (Integer) rs.getObject("tiempo_estimado_min")), id);

        List<EquipoCocinaRecetaDto> equipos = jdbc.query("""
                SELECT e.id_equipo, e.nombre_equipo
                FROM receta_equipo re
                JOIN equipos_cocina e ON e.id_equipo = re.id_equipo
                WHERE re.id_receta = ?
                ORDER BY e.nombre_equipo
                """, (rs, fila) -> new EquipoCocinaRecetaDto(
                        rs.getInt("id_equipo"), rs.getString("nombre_equipo")), id);

        List<InformacionNutricionalDto> datosNutricionales = jdbc.query("""
                SELECT calorias, proteinas_g, carbohidratos_g, grasas_g, azucares_g, sodio_mg
                FROM informacion_nutricional
                WHERE id_receta = ?
                """, (rs, fila) -> new InformacionNutricionalDto(
                        rs.getBigDecimal("calorias"),
                        rs.getBigDecimal("proteinas_g"),
                        rs.getBigDecimal("carbohidratos_g"),
                        rs.getBigDecimal("grasas_g"),
                        rs.getBigDecimal("azucares_g"),
                        rs.getBigDecimal("sodio_mg")), id);

        List<ValoracionRecetaDto> valoraciones = jdbc.query("""
                SELECT v.id_valoracion,
                       COALESCE(NULLIF(u.nombre_usuario, ''), NULLIF(u.nombre_completo, ''), 'Usuario') AS usuario,
                       v.puntuacion,
                       v.comentario_breve,
                       v.fecha
                FROM valoraciones v
                LEFT JOIN usuarios u ON u.id_usuario = v.id_usuario
                WHERE v.id_receta = ?
                ORDER BY v.fecha DESC NULLS LAST, v.id_valoracion DESC
                """, (rs, fila) -> {
                    Timestamp marcaTiempo = rs.getTimestamp("fecha");
                    LocalDateTime fecha = marcaTiempo == null ? null : marcaTiempo.toLocalDateTime();
                    return new ValoracionRecetaDto(
                            rs.getInt("id_valoracion"),
                            rs.getString("usuario"),
                            (Integer) rs.getObject("puntuacion"),
                            rs.getString("comentario_breve"),
                            fecha);
                }, id);

        return new DetalleRecetaDto(
                receta,
                paisOrigen,
                categorias,
                imagenes,
                ingredientes,
                pasos,
                equipos,
                datosNutricionales.stream().findFirst().orElse(null),
                valoraciones);
    }

    public int insertar(SolicitudReceta solicitud, BigDecimal precio, Integer minutos) {
        Integer id = jdbc.queryForObject("SELECT nextval('seq_recetas')", Integer.class);
        jdbc.update("""
                INSERT INTO recetas
                    (id_receta, id_usuario, id_pais, titulo, descripcion,
                     tiempo_preparacion_min, porciones, dificultad, precio,
                     fecha_publicacion, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, COALESCE(?, TRUE))
                """,
                id, solicitud.idUsuario(), solicitud.idPais(), requerido(solicitud.name()),
                solicitud.description(), minutos, solicitud.servings(), solicitud.difficulty(), precio,
                solicitud.active());

        guardarImagenPortada(id, solicitud.image());
        asignarCategoria(id, solicitud.tag(), false);
        return id;
    }

    public boolean actualizar(int id, SolicitudReceta solicitud, BigDecimal precio, Integer minutos) {
        int filas = jdbc.update("""
                UPDATE recetas SET
                    id_usuario = COALESCE(?, id_usuario),
                    id_pais = COALESCE(?, id_pais),
                    titulo = COALESCE(?, titulo),
                    descripcion = COALESCE(?, descripcion),
                    tiempo_preparacion_min = COALESCE(?, tiempo_preparacion_min),
                    porciones = COALESCE(?, porciones),
                    dificultad = COALESCE(?, dificultad),
                    precio = COALESCE(?, precio),
                    activo = COALESCE(?, activo)
                WHERE id_receta = ?
                """,
                solicitud.idUsuario(), solicitud.idPais(), solicitud.name(), solicitud.description(),
                minutos, solicitud.servings(), solicitud.difficulty(), precio, solicitud.active(), id);

        if (filas == 0) {
            return false;
        }
        if (solicitud.image() != null && !solicitud.image().isBlank()) {
            guardarImagenPortada(id, solicitud.image());
        }
        if (solicitud.tag() != null && !solicitud.tag().isBlank()) {
            asignarCategoria(id, solicitud.tag(), true);
        }
        return true;
    }

    public boolean eliminar(int id) {
        Integer existe = jdbc.queryForObject("SELECT COUNT(*) FROM recetas WHERE id_receta = ?", Integer.class, id);
        if (existe == null || existe == 0) {
            return false;
        }

        Boolean productoConMovimiento = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM productos p
                    WHERE p.id_receta = ? AND (
                        EXISTS (SELECT 1 FROM pedido_item pi WHERE pi.id_producto = p.id_producto)
                        OR EXISTS (SELECT 1 FROM carrito_item ci WHERE ci.id_producto = p.id_producto)
                    )
                )
                """, Boolean.class, id);
        if (Boolean.TRUE.equals(productoConMovimiento)) {
            throw new com.recetas.backend.exception.ConflictoDatosException(
                    "No se puede eliminar esta receta porque sus productos aparecen en carritos o pedidos.");
        }

        jdbc.update("DELETE FROM receta_categoria WHERE id_receta = ?", id);
        jdbc.update("DELETE FROM receta_equipo WHERE id_receta = ?", id);
        jdbc.update("DELETE FROM receta_ingrediente WHERE id_receta = ?", id);
        jdbc.update("DELETE FROM imagenes_receta WHERE id_receta = ?", id);
        jdbc.update("DELETE FROM pasos_receta WHERE id_receta = ?", id);
        jdbc.update("DELETE FROM informacion_nutricional WHERE id_receta = ?", id);
        jdbc.update("DELETE FROM favoritos WHERE id_receta = ?", id);
        jdbc.update("DELETE FROM valoraciones WHERE id_receta = ?", id);
        jdbc.update("DELETE FROM productos WHERE id_receta = ?", id);
        jdbc.update("DELETE FROM recetas WHERE id_receta = ?", id);
        return true;
    }

    private void guardarImagenPortada(int idReceta, String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        Integer idImagenExistente = buscarImagenPortada(idReceta);
        if (idImagenExistente != null) {
            jdbc.update("UPDATE imagenes_receta SET url_imagen = ?, es_portada = TRUE WHERE id_imagen = ?", url, idImagenExistente);
        } else {
            Integer idImagen = jdbc.queryForObject("SELECT nextval('seq_imagenes_receta')", Integer.class);
            jdbc.update("INSERT INTO imagenes_receta (id_imagen, id_receta, url_imagen, es_portada) VALUES (?, ?, ?, TRUE)",
                    idImagen, idReceta, url);
        }
    }

    private Integer buscarImagenPortada(int idReceta) {
        try {
            return jdbc.queryForObject("SELECT id_imagen FROM imagenes_receta WHERE id_receta = ? AND es_portada = TRUE ORDER BY id_imagen LIMIT 1",
                    Integer.class, idReceta);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private void asignarCategoria(int idReceta, String nombreCategoria, boolean reemplazar) {
        if (nombreCategoria == null || nombreCategoria.isBlank()) {
            return;
        }
        if (reemplazar) {
            jdbc.update("DELETE FROM receta_categoria WHERE id_receta = ?", idReceta);
        }
        List<Integer> ids = jdbc.query("SELECT id_categoria FROM categorias WHERE LOWER(nombre_categoria) = LOWER(?) ORDER BY id_categoria LIMIT 1",
                (rs, fila) -> rs.getInt("id_categoria"), nombreCategoria.trim());
        if (!ids.isEmpty()) {
            jdbc.update("INSERT INTO receta_categoria (id_receta, id_categoria) VALUES (?, ?) ON CONFLICT DO NOTHING",
                    idReceta, ids.get(0));
        }
    }

    private RecetaDto mapear(ResultSet rs, int fila) throws SQLException {
        return new RecetaDto(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("image"),
                rs.getString("price"),
                rs.getDouble("rating"),
                rs.getString("time"),
                rs.getString("tag"),
                rs.getString("tagColor"),
                rs.getString("description"),
                (Integer) rs.getObject("idUsuario"),
                (Integer) rs.getObject("idPais"),
                (Integer) rs.getObject("servings"),
                rs.getString("difficulty"),
                (Boolean) rs.getObject("active")
        );
    }

    private String requerido(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
