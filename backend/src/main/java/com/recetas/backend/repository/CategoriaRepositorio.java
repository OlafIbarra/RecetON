package com.recetas.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.recetas.backend.dto.CategoriaDto;
import com.recetas.backend.dto.SolicitudCategoria;

@Repository
public class CategoriaRepositorio {

    private final JdbcTemplate jdbc;

    public CategoriaRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CategoriaDto> listar() {
        return jdbc.query("SELECT id_categoria, nombre_categoria, imagen_url FROM categorias ORDER BY id_categoria",
                (rs, fila) -> new CategoriaDto(rs.getInt("id_categoria"), rs.getString("nombre_categoria"), rs.getString("imagen_url")));
    }

    public Optional<CategoriaDto> buscarPorId(int id) {
        List<CategoriaDto> resultados = jdbc.query(
                "SELECT id_categoria, nombre_categoria, imagen_url FROM categorias WHERE id_categoria = ?",
                (rs, fila) -> new CategoriaDto(rs.getInt("id_categoria"), rs.getString("nombre_categoria"), rs.getString("imagen_url")), id);
        return resultados.stream().findFirst();
    }

    public int insertar(SolicitudCategoria solicitud) {
        Integer id = jdbc.queryForObject("SELECT nextval('seq_categorias')", Integer.class);
        jdbc.update("INSERT INTO categorias (id_categoria, nombre_categoria, imagen_url) VALUES (?, ?, ?)",
                id, solicitud.name().trim(), solicitud.image());
        return id;
    }

    public boolean actualizar(int id, SolicitudCategoria solicitud) {
        return jdbc.update("UPDATE categorias SET nombre_categoria = COALESCE(?, nombre_categoria), imagen_url = COALESCE(?, imagen_url) WHERE id_categoria = ?",
                solicitud.name(), solicitud.image(), id) > 0;
    }

    public boolean eliminar(int id) {
        Integer existe = jdbc.queryForObject("SELECT COUNT(*) FROM categorias WHERE id_categoria = ?", Integer.class, id);
        if (existe == null || existe == 0) {
            return false;
        }
        jdbc.update("DELETE FROM receta_categoria WHERE id_categoria = ?", id);
        jdbc.update("DELETE FROM categorias WHERE id_categoria = ?", id);
        return true;
    }

    public boolean existeNombre(String nombre, Integer excluirId) {
        Integer cantidad = jdbc.queryForObject("""
                SELECT COUNT(*) FROM categorias
                WHERE LOWER(nombre_categoria) = LOWER(?) AND (CAST(? AS INTEGER) IS NULL OR id_categoria <> CAST(? AS INTEGER))
                """, Integer.class, nombre, excluirId, excluirId);
        return cantidad != null && cantidad > 0;
    }
}
