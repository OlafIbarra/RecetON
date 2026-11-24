package com.recetas.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recetas.backend.dto.DetalleRecetaDto;
import com.recetas.backend.dto.RecetaDto;
import com.recetas.backend.dto.SolicitudReceta;
import com.recetas.backend.exception.RecursoNoEncontradoException;
import com.recetas.backend.exception.SolicitudInvalidaException;
import com.recetas.backend.repository.RecetaRepositorio;

@Service
public class RecetaServicio {

    private static final Pattern MINUTOS = Pattern.compile("(\\d+)");
    private final RecetaRepositorio repositorio;

    public RecetaServicio(RecetaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public List<RecetaDto> listar() {
        return repositorio.listar();
    }

    public RecetaDto buscarPorId(int id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la receta con ID " + id + "."));
    }

    public DetalleRecetaDto buscarDetalle(int id) {
        RecetaDto receta = buscarPorId(id);
        return repositorio.buscarDetalle(id, receta);
    }

    @Transactional
    public RecetaDto crear(SolicitudReceta solicitud) {
        validarSolicitud(solicitud, true);
        int id = repositorio.insertar(solicitud, convertirPrecio(solicitud.price(), true), convertirMinutos(solicitud.time()));
        return buscarPorId(id);
    }

    @Transactional
    public RecetaDto actualizar(int id, SolicitudReceta solicitud) {
        validarSolicitud(solicitud, false);
        boolean actualizada = repositorio.actualizar(id, solicitud,
                convertirPrecio(solicitud.price(), false), convertirMinutos(solicitud.time()));
        if (!actualizada) {
            throw new RecursoNoEncontradoException("No se encontró la receta con ID " + id + ".");
        }
        return buscarPorId(id);
    }

    @Transactional
    public void eliminar(int id) {
        if (!repositorio.eliminar(id)) {
            throw new RecursoNoEncontradoException("No se encontró la receta con ID " + id + ".");
        }
    }

    private void validarSolicitud(SolicitudReceta solicitud, boolean esNueva) {
        if (solicitud == null) {
            throw new SolicitudInvalidaException("Debes enviar los datos de la receta.");
        }
        if (esNueva && (solicitud.name() == null || solicitud.name().isBlank())) {
            throw new SolicitudInvalidaException("El campo 'name' es obligatorio.");
        }
        if (!esNueva && solicitud.name() != null && solicitud.name().isBlank()) {
            throw new SolicitudInvalidaException("El campo 'name' no puede estar vacío.");
        }
        if (solicitud.price() != null) {
            convertirPrecio(solicitud.price(), esNueva);
        }
        if (solicitud.time() != null && !solicitud.time().isBlank() && convertirMinutos(solicitud.time()) == null) {
            throw new SolicitudInvalidaException("El campo 'time' debe incluir los minutos, por ejemplo: '25 min'.");
        }
    }

    private BigDecimal convertirPrecio(String valor, boolean porDefectoCero) {
        if (valor == null || valor.isBlank()) {
            return porDefectoCero ? BigDecimal.ZERO : null;
        }
        try {
            BigDecimal precio = new BigDecimal(valor.trim());
            if (precio.signum() < 0) {
                throw new SolicitudInvalidaException("El precio no puede ser negativo.");
            }
            return precio;
        } catch (NumberFormatException ex) {
            throw new SolicitudInvalidaException("El campo 'price' debe ser un número, por ejemplo: '120.00'.");
        }
    }

    private Integer convertirMinutos(String tiempo) {
        if (tiempo == null || tiempo.isBlank()) {
            return null;
        }
        Matcher matcher = MINUTOS.matcher(tiempo);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.valueOf(matcher.group(1));
        } catch (NumberFormatException ex) {
            throw new SolicitudInvalidaException("El tiempo de preparación no es válido.");
        }
    }
}
