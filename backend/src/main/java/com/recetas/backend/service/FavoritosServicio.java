package com.recetas.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recetas.backend.dto.EstadoFavoritoDto;
import com.recetas.backend.dto.RecetaDto;
import com.recetas.backend.dto.UsuarioDto;
import com.recetas.backend.exception.SolicitudInvalidaException;
import com.recetas.backend.repository.FavoritosRepositorio;

@Service
public class FavoritosServicio {

    private final FavoritosRepositorio repositorio;
    private final UsuarioServicio usuarioServicio;
    private final RecetaServicio recetaServicio;

    public FavoritosServicio(FavoritosRepositorio repositorio,
            UsuarioServicio usuarioServicio,
            RecetaServicio recetaServicio) {
        this.repositorio = repositorio;
        this.usuarioServicio = usuarioServicio;
        this.recetaServicio = recetaServicio;
    }

    public List<RecetaDto> listarPorUsuario(int idUsuario) {
        usuarioActivo(idUsuario);
        return repositorio.listarRecetasPorUsuario(idUsuario);
    }

    public EstadoFavoritoDto consultarEstado(int idUsuario, int idReceta) {
        usuarioActivo(idUsuario);
        recetaServicio.buscarPorId(idReceta);
        return repositorio.consultarEstado(idUsuario, idReceta);
    }

    @Transactional
    public EstadoFavoritoDto agregar(int idUsuario, int idReceta) {
        usuarioActivo(idUsuario);
        recetaServicio.buscarPorId(idReceta);
        repositorio.agregar(idUsuario, idReceta);
        return repositorio.consultarEstado(idUsuario, idReceta);
    }

    @Transactional
    public EstadoFavoritoDto eliminar(int idUsuario, int idReceta) {
        usuarioActivo(idUsuario);
        recetaServicio.buscarPorId(idReceta);
        repositorio.eliminar(idUsuario, idReceta);
        return repositorio.consultarEstado(idUsuario, idReceta);
    }

    private UsuarioDto usuarioActivo(int idUsuario) {
        if (idUsuario < 1) {
            throw new SolicitudInvalidaException("El identificador del usuario debe ser positivo.");
        }
        UsuarioDto usuario = usuarioServicio.buscarPorId(idUsuario);
        if (!Boolean.TRUE.equals(usuario.active())) {
            throw new SolicitudInvalidaException("El usuario no está activo y no puede administrar favoritos.");
        }
        return usuario;
    }
}
