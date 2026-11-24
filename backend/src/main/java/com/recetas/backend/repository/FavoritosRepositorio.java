package com.recetas.backend.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.recetas.backend.dto.EstadoFavoritoDto;
import com.recetas.backend.dto.RecetaDto;

@Repository
public class FavoritosRepositorio {

    private final JdbcTemplate jdbc;
    private final RecetaRepositorio recetaRepositorio;

    public FavoritosRepositorio(JdbcTemplate jdbc, RecetaRepositorio recetaRepositorio) {
        this.jdbc = jdbc;
        this.recetaRepositorio = recetaRepositorio;
    }

    public EstadoFavoritoDto consultarEstado(int idUsuario, int idReceta) {
        List<EstadoFavoritoDto> resultados = jdbc.query("""
                SELECT fecha_agregado
                FROM favoritos
                WHERE id_usuario = ? AND id_receta = ?
                """, (rs, fila) -> {
            Timestamp fecha = rs.getTimestamp("fecha_agregado");
            LocalDateTime fechaLocal = fecha == null ? null : fecha.toLocalDateTime();
            return new EstadoFavoritoDto(true, fechaLocal);
        }, idUsuario, idReceta);
        return resultados.isEmpty() ? new EstadoFavoritoDto(false, null) : resultados.get(0);
    }

    public void agregar(int idUsuario, int idReceta) {
        jdbc.update("""
                INSERT INTO favoritos (id_usuario, id_receta, fecha_agregado)
                VALUES (?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT (id_usuario, id_receta) DO NOTHING
                """, idUsuario, idReceta);
    }

    public void eliminar(int idUsuario, int idReceta) {
        jdbc.update("DELETE FROM favoritos WHERE id_usuario = ? AND id_receta = ?", idUsuario, idReceta);
    }

    public List<RecetaDto> listarRecetasPorUsuario(int idUsuario) {
        return recetaRepositorio.listarFavoritosPorUsuario(idUsuario);
    }
}
