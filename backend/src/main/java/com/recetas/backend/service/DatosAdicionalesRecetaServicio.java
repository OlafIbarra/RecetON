package com.recetas.backend.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recetas.backend.dto.SolicitudEquipoReceta;
import com.recetas.backend.dto.SolicitudInformacionNutricional;
import com.recetas.backend.dto.SolicitudValoracionReceta;
import com.recetas.backend.exception.RecursoNoEncontradoException;
import com.recetas.backend.exception.SolicitudInvalidaException;
import com.recetas.backend.repository.DatosAdicionalesRecetaRepositorio;

/** Validación y coordinación de equipo, nutrición y valoraciones. */
@Service
public class DatosAdicionalesRecetaServicio {

    private final DatosAdicionalesRecetaRepositorio repositorio;
    private final RecetaServicio recetaServicio;

    public DatosAdicionalesRecetaServicio(
            DatosAdicionalesRecetaRepositorio repositorio,
            RecetaServicio recetaServicio) {
        this.repositorio = repositorio;
        this.recetaServicio = recetaServicio;
    }

    @Transactional
    public void agregarEquipo(int idReceta, SolicitudEquipoReceta solicitud) {
        recetaServicio.buscarPorId(idReceta);
        if (solicitud == null || solicitud.nombreEquipo() == null || solicitud.nombreEquipo().isBlank()) {
            throw new SolicitudInvalidaException("El nombre del equipo de cocina es obligatorio.");
        }
        repositorio.agregarEquipo(idReceta, solicitud.nombreEquipo());
    }

    @Transactional
    public void eliminarEquipo(int idReceta, int idEquipo) {
        recetaServicio.buscarPorId(idReceta);
        if (!repositorio.eliminarEquipo(idReceta, idEquipo)) {
            throw new RecursoNoEncontradoException("Ese equipo no está asociado a la receta indicada.");
        }
    }

    @Transactional
    public void guardarInformacionNutricional(int idReceta, SolicitudInformacionNutricional solicitud) {
        recetaServicio.buscarPorId(idReceta);
        validarNutricion(solicitud);
        repositorio.guardarInformacionNutricional(idReceta, solicitud);
    }

    @Transactional
    public void eliminarInformacionNutricional(int idReceta) {
        recetaServicio.buscarPorId(idReceta);
        if (!repositorio.eliminarInformacionNutricional(idReceta)) {
            throw new RecursoNoEncontradoException("La receta todavía no tiene información nutricional registrada.");
        }
    }

    @Transactional
    public void agregarValoracion(int idReceta, SolicitudValoracionReceta solicitud) {
        recetaServicio.buscarPorId(idReceta);
        if (solicitud == null || solicitud.idUsuario() == null || solicitud.idUsuario() < 1) {
            throw new SolicitudInvalidaException("Debes iniciar sesión con un usuario válido para valorar la receta.");
        }
        if (!repositorio.existeUsuario(solicitud.idUsuario())) {
            throw new RecursoNoEncontradoException("No se encontró el usuario indicado.");
        }
        if (solicitud.puntuacion() == null || solicitud.puntuacion() < 1 || solicitud.puntuacion() > 5) {
            throw new SolicitudInvalidaException("La puntuación debe ser un número entre 1 y 5.");
        }
        if (solicitud.comentario() != null && solicitud.comentario().length() > 5000) {
            throw new SolicitudInvalidaException("El comentario no debe superar los 5000 caracteres.");
        }
        repositorio.agregarValoracion(idReceta, solicitud);
    }

    private void validarNutricion(SolicitudInformacionNutricional solicitud) {
        if (solicitud == null) {
            throw new SolicitudInvalidaException("Debes enviar los datos nutricionales.");
        }
        boolean hayDato = solicitud.calorias() != null || solicitud.proteinasG() != null
                || solicitud.carbohidratosG() != null || solicitud.grasasG() != null
                || solicitud.azucaresG() != null || solicitud.sodioMg() != null;
        if (!hayDato) {
            throw new SolicitudInvalidaException("Ingresa al menos un dato nutricional.");
        }
        validarNoNegativo("calorías", solicitud.calorias());
        validarNoNegativo("proteínas", solicitud.proteinasG());
        validarNoNegativo("carbohidratos", solicitud.carbohidratosG());
        validarNoNegativo("grasas", solicitud.grasasG());
        validarNoNegativo("azúcares", solicitud.azucaresG());
        validarNoNegativo("sodio", solicitud.sodioMg());
    }

    private void validarNoNegativo(String campo, BigDecimal valor) {
        if (valor != null && valor.signum() < 0) {
            throw new SolicitudInvalidaException("El valor de " + campo + " no puede ser negativo.");
        }
    }
}
