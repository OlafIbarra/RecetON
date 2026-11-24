package com.recetas.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recetas.backend.dto.ActualizacionIngredienteReceta;
import com.recetas.backend.dto.SolicitudIngredienteReceta;
import com.recetas.backend.dto.SolicitudPasoReceta;
import com.recetas.backend.exception.RecursoNoEncontradoException;
import com.recetas.backend.exception.SolicitudInvalidaException;
import com.recetas.backend.repository.ContenidoRecetaRepositorio;

/** Valida y coordina las operaciones del contenido relacionado con recetas. */
@Service
public class ContenidoRecetaServicio {

    private final ContenidoRecetaRepositorio repositorio;
    private final RecetaServicio recetaServicio;

    public ContenidoRecetaServicio(ContenidoRecetaRepositorio repositorio, RecetaServicio recetaServicio) {
        this.repositorio = repositorio;
        this.recetaServicio = recetaServicio;
    }

    @Transactional
    public void agregarIngrediente(int idReceta, SolicitudIngredienteReceta solicitud) {
        recetaServicio.buscarPorId(idReceta);
        validarIngrediente(solicitud);
        repositorio.guardarIngrediente(idReceta, solicitud);
    }

    @Transactional
    public void actualizarIngrediente(int idReceta, int idIngrediente,
            ActualizacionIngredienteReceta solicitud) {
        recetaServicio.buscarPorId(idReceta);
        if (solicitud == null) {
            throw new SolicitudInvalidaException("Debes enviar los datos del ingrediente.");
        }
        validarCantidad(solicitud.cantidad());
        if (!repositorio.actualizarIngrediente(idReceta, idIngrediente, solicitud)) {
            throw new RecursoNoEncontradoException(
                    "Ese ingrediente no está asociado a la receta indicada.");
        }
    }

    @Transactional
    public void eliminarIngrediente(int idReceta, int idIngrediente) {
        recetaServicio.buscarPorId(idReceta);
        if (!repositorio.eliminarIngrediente(idReceta, idIngrediente)) {
            throw new RecursoNoEncontradoException(
                    "Ese ingrediente no está asociado a la receta indicada.");
        }
    }

    @Transactional
    public void agregarPaso(int idReceta, SolicitudPasoReceta solicitud) {
        recetaServicio.buscarPorId(idReceta);
        validarPaso(solicitud, true);
        repositorio.guardarPaso(idReceta, solicitud);
    }

    @Transactional
    public void actualizarPaso(int idReceta, int idPaso, SolicitudPasoReceta solicitud) {
        recetaServicio.buscarPorId(idReceta);
        validarPaso(solicitud, false);
        if (!repositorio.actualizarPaso(idReceta, idPaso, solicitud)) {
            throw new RecursoNoEncontradoException("No se encontró ese paso en la receta indicada.");
        }
    }

    @Transactional
    public void eliminarPaso(int idReceta, int idPaso) {
        recetaServicio.buscarPorId(idReceta);
        if (!repositorio.eliminarPaso(idReceta, idPaso)) {
            throw new RecursoNoEncontradoException("No se encontró ese paso en la receta indicada.");
        }
    }

    private void validarIngrediente(SolicitudIngredienteReceta solicitud) {
        if (solicitud == null || solicitud.nombreIngrediente() == null
                || solicitud.nombreIngrediente().isBlank()) {
            throw new SolicitudInvalidaException("El nombre del ingrediente es obligatorio.");
        }
        validarCantidad(solicitud.cantidad());
    }

    private void validarCantidad(java.math.BigDecimal cantidad) {
        if (cantidad != null && cantidad.signum() < 0) {
            throw new SolicitudInvalidaException("La cantidad del ingrediente no puede ser negativa.");
        }
    }

    private void validarPaso(SolicitudPasoReceta solicitud, boolean esNuevo) {
        if (solicitud == null) {
            throw new SolicitudInvalidaException("Debes enviar los datos del paso.");
        }
        if (solicitud.descripcion() == null || solicitud.descripcion().isBlank()) {
            throw new SolicitudInvalidaException("La descripción del paso es obligatoria.");
        }
        if (solicitud.numeroPaso() != null && solicitud.numeroPaso() < 1) {
            throw new SolicitudInvalidaException("El número del paso debe ser mayor que cero.");
        }
        if (solicitud.tiempoEstimadoMin() != null && solicitud.tiempoEstimadoMin() < 0) {
            throw new SolicitudInvalidaException("El tiempo estimado no puede ser negativo.");
        }
        if (!esNuevo && solicitud.numeroPaso() != null && solicitud.numeroPaso() < 1) {
            throw new SolicitudInvalidaException("El número del paso debe ser mayor que cero.");
        }
    }
}
