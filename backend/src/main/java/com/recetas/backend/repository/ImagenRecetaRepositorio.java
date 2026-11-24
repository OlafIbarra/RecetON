package com.recetas.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.recetas.backend.dto.ImagenRecetaDto;

@Repository
public class ImagenRecetaRepositorio {

    private final JdbcTemplate jdbc;

    public ImagenRecetaRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ImagenRecetaDto> listarPorReceta(int idReceta) {
        return jdbc.query("""
                SELECT id_imagen, id_receta, url_imagen, COALESCE(es_portada, FALSE) AS es_portada
                FROM imagenes_receta
                WHERE id_receta = ?
                ORDER BY COALESCE(es_portada, FALSE) DESC, id_imagen ASC
                """, (rs, fila) -> new ImagenRecetaDto(
                rs.getInt("id_imagen"),
                rs.getInt("id_receta"),
                rs.getString("url_imagen"),
                rs.getBoolean("es_portada")), idReceta);
    }

    public Optional<ImagenRecetaDto> buscarPorIdYReceta(int idImagen, int idReceta) {
        List<ImagenRecetaDto> resultados = jdbc.query("""
                SELECT id_imagen, id_receta, url_imagen, COALESCE(es_portada, FALSE) AS es_portada
                FROM imagenes_receta
                WHERE id_imagen = ? AND id_receta = ?
                """, (rs, fila) -> new ImagenRecetaDto(
                rs.getInt("id_imagen"),
                rs.getInt("id_receta"),
                rs.getString("url_imagen"),
                rs.getBoolean("es_portada")), idImagen, idReceta);
        return resultados.stream().findFirst();
    }

    public boolean existeUrl(int idReceta, String urlImagen) {
        Integer cuenta = jdbc.queryForObject("""
                SELECT COUNT(*) FROM imagenes_receta
                WHERE id_receta = ? AND url_imagen = ?
                """, Integer.class, idReceta, urlImagen);
        return cuenta != null && cuenta > 0;
    }

    public int contarPorReceta(int idReceta) {
        Integer cuenta = jdbc.queryForObject(
                "SELECT COUNT(*) FROM imagenes_receta WHERE id_receta = ?", Integer.class, idReceta);
        return cuenta == null ? 0 : cuenta;
    }

    public int insertar(int idReceta, String urlImagen, boolean esPortada) {
        Integer idImagen = jdbc.queryForObject("SELECT nextval('seq_imagenes_receta')", Integer.class);
        jdbc.update("""
                INSERT INTO imagenes_receta (id_imagen, id_receta, url_imagen, es_portada)
                VALUES (?, ?, ?, ?)
                """, idImagen, idReceta, urlImagen, esPortada);
        return idImagen;
    }

    public void desmarcarPortadas(int idReceta) {
        jdbc.update("UPDATE imagenes_receta SET es_portada = FALSE WHERE id_receta = ?", idReceta);
    }

    public boolean marcarPortada(int idReceta, int idImagen) {
        return jdbc.update("""
                UPDATE imagenes_receta SET es_portada = TRUE
                WHERE id_receta = ? AND id_imagen = ?
                """, idReceta, idImagen) > 0;
    }

    public boolean eliminar(int idReceta, int idImagen) {
        return jdbc.update("DELETE FROM imagenes_receta WHERE id_receta = ? AND id_imagen = ?", idReceta, idImagen) > 0;
    }

    public void elegirPortadaMasReciente(int idReceta) {
        jdbc.update("""
                UPDATE imagenes_receta SET es_portada = TRUE
                WHERE id_imagen = (
                    SELECT id_imagen FROM imagenes_receta
                    WHERE id_receta = ?
                    ORDER BY id_imagen DESC
                    LIMIT 1
                )
                """, idReceta);
    }
}
