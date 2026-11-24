package com.recetas.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recetas.backend.dto.ImagenRecetaDto;
import com.recetas.backend.dto.SolicitudImagenReceta;
import com.recetas.backend.exception.ConflictoDatosException;
import com.recetas.backend.exception.RecursoNoEncontradoException;
import com.recetas.backend.exception.SolicitudInvalidaException;
import com.recetas.backend.repository.ImagenRecetaRepositorio;

@Service
public class ImagenRecetaServicio {

    private final ImagenRecetaRepositorio repositorio;
    private final RecetaServicio recetaServicio;

    public ImagenRecetaServicio(ImagenRecetaRepositorio repositorio, RecetaServicio recetaServicio) {
        this.repositorio = repositorio;
        this.recetaServicio = recetaServicio;
    }

    public List<ImagenRecetaDto> listar(int idReceta) {
        recetaServicio.buscarPorId(idReceta);
        return repositorio.listarPorReceta(idReceta);
    }

    @Transactional
    public ImagenRecetaDto agregar(int idReceta, SolicitudImagenReceta solicitud) {
        recetaServicio.buscarPorId(idReceta);
        validar(solicitud);
        String url = solicitud.urlImagen().trim();
        if (repositorio.existeUrl(idReceta, url)) {
            throw new ConflictoDatosException("Esta URL de imagen ya está registrada para la receta.");
        }

        boolean esPrimera = repositorio.contarPorReceta(idReceta) == 0;
        boolean esPortada = esPrimera || Boolean.TRUE.equals(solicitud.esPortada());
        if (esPortada) {
            repositorio.desmarcarPortadas(idReceta);
        }
        int idImagen = repositorio.insertar(idReceta, url, esPortada);
        return repositorio.buscarPorIdYReceta(idImagen, idReceta)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se pudo recuperar la imagen recién agregada."));
    }

    @Transactional
    public List<ImagenRecetaDto> establecerPortada(int idReceta, int idImagen) {
        recetaServicio.buscarPorId(idReceta);
        if (repositorio.buscarPorIdYReceta(idImagen, idReceta).isEmpty()) {
            throw new RecursoNoEncontradoException("La imagen indicada no pertenece a esta receta.");
        }
        repositorio.desmarcarPortadas(idReceta);
        repositorio.marcarPortada(idReceta, idImagen);
        return repositorio.listarPorReceta(idReceta);
    }

    @Transactional
    public List<ImagenRecetaDto> eliminar(int idReceta, int idImagen) {
        recetaServicio.buscarPorId(idReceta);
        ImagenRecetaDto imagen = repositorio.buscarPorIdYReceta(idImagen, idReceta)
                .orElseThrow(() -> new RecursoNoEncontradoException("La imagen indicada no pertenece a esta receta."));
        if (!repositorio.eliminar(idReceta, idImagen)) {
            throw new RecursoNoEncontradoException("No se pudo eliminar la imagen indicada.");
        }
        if (imagen.esPortada() && repositorio.contarPorReceta(idReceta) > 0) {
            repositorio.desmarcarPortadas(idReceta);
            repositorio.elegirPortadaMasReciente(idReceta);
        }
        return repositorio.listarPorReceta(idReceta);
    }

    private void validar(SolicitudImagenReceta solicitud) {
        if (solicitud == null || solicitud.urlImagen() == null || solicitud.urlImagen().isBlank()) {
            throw new SolicitudInvalidaException("La URL de la imagen es obligatoria.");
        }
        String url = solicitud.urlImagen().trim();
        if (url.length() > 255) {
            throw new SolicitudInvalidaException("La URL no puede superar los 255 caracteres, conforme a la columna url_imagen.");
        }
        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            throw new SolicitudInvalidaException("La URL de la imagen debe comenzar con http:// o https://.");
        }
    }
}
