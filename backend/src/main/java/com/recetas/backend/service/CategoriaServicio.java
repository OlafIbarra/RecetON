package com.recetas.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recetas.backend.dto.CategoriaDto;
import com.recetas.backend.dto.SolicitudCategoria;
import com.recetas.backend.exception.RecursoNoEncontradoException;
import com.recetas.backend.exception.SolicitudInvalidaException;
import com.recetas.backend.repository.CategoriaRepositorio;

@Service
public class CategoriaServicio {

    private final CategoriaRepositorio repositorio;

    public CategoriaServicio(CategoriaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<CategoriaDto> listar() {
        return repositorio.listar();
    }

    public CategoriaDto buscarPorId(int id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la categoría con ID " + id + "."));
    }

    @Transactional
    public CategoriaDto crear(SolicitudCategoria solicitud) {
        validar(solicitud, true);
        if (repositorio.existeNombre(solicitud.name().trim(), null)) {
            throw new com.recetas.backend.exception.ConflictoDatosException("Ya existe una categoría con ese nombre.");
        }
        return buscarPorId(repositorio.insertar(solicitud));
    }

    @Transactional
    public CategoriaDto actualizar(int id, SolicitudCategoria solicitud) {
        validar(solicitud, false);
        if (repositorio.buscarPorId(id).isEmpty()) {
            throw new RecursoNoEncontradoException("No se encontró la categoría con ID " + id + ".");
        }
        if (solicitud.name() != null && repositorio.existeNombre(solicitud.name().trim(), id)) {
            throw new com.recetas.backend.exception.ConflictoDatosException("Ya existe otra categoría con ese nombre.");
        }
        repositorio.actualizar(id, solicitud);
        return buscarPorId(id);
    }

    @Transactional
    public void eliminar(int id) {
        if (!repositorio.eliminar(id)) {
            throw new RecursoNoEncontradoException("No se encontró la categoría con ID " + id + ".");
        }
    }

    private void validar(SolicitudCategoria solicitud, boolean esNueva) {
        if (solicitud == null || (esNueva && (solicitud.name() == null || solicitud.name().isBlank()))) {
            throw new SolicitudInvalidaException("El campo 'name' es obligatorio para registrar una categoría.");
        }
        if (!esNueva && solicitud.name() != null && solicitud.name().isBlank()) {
            throw new SolicitudInvalidaException("El campo 'name' no puede estar vacío.");
        }
    }
}
