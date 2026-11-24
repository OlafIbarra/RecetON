package com.recetas.backend.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recetas.backend.dto.ActualizacionSolicitudArco;
import com.recetas.backend.dto.SolicitudArco;
import com.recetas.backend.dto.SolicitudArcoDto;
import com.recetas.backend.exception.RecursoNoEncontradoException;
import com.recetas.backend.exception.SolicitudInvalidaException;
import com.recetas.backend.repository.SolicitudesArcoRepositorio;

@Service
public class SolicitudesArcoServicio {
    private static final Map<String, String> DERECHOS = Map.of(
            "acceso", "Acceso",
            "rectificacion", "Rectificación",
            "cancelacion", "Cancelación",
            "oposicion", "Oposición");
    private static final Set<String> ESTADOS = Set.of(
            "Recibida", "En revisión", "Pendiente de información", "Atendida", "No procedente");

    private final SolicitudesArcoRepositorio repositorio;

    public SolicitudesArcoServicio(SolicitudesArcoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public SolicitudArcoDto crear(int idUsuario, SolicitudArco solicitud) {
        if (solicitud == null || solicitud.derecho() == null || solicitud.descripcion() == null) {
            throw new SolicitudInvalidaException("Selecciona un derecho y describe la solicitud.");
        }
        String derecho = normalizarDerecho(solicitud.derecho());
        String descripcion = solicitud.descripcion().trim();
        if (descripcion.length() < 10) {
            throw new SolicitudInvalidaException("Describe la solicitud con al menos 10 caracteres.");
        }
        if (descripcion.length() > 2000) {
            throw new SolicitudInvalidaException("La descripción no puede superar los 2000 caracteres.");
        }

        String correoContacto = repositorio.obtenerCorreoContacto(idUsuario)
                .orElseThrow(() -> new SolicitudInvalidaException(
                        "Tu cuenta necesita un correo electrónico registrado para recibir seguimiento de la solicitud. Actualízalo en Mi cuenta e inténtalo de nuevo."));
        if (correoContacto.length() > 255) {
            throw new SolicitudInvalidaException("El correo de contacto supera el límite permitido de 255 caracteres.");
        }

        Long id = repositorio.insertar(idUsuario, derecho, descripcion, correoContacto);
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La solicitud se guardó, pero no se pudo consultar."));
    }

    public List<SolicitudArcoDto> listar(int idUsuario) {
        return repositorio.listarPorUsuario(idUsuario);
    }

    public List<SolicitudArcoDto> listarTodas() {
        return repositorio.listarTodas();
    }

    @Transactional
    public SolicitudArcoDto actualizar(long idSolicitud, int idResponsable, ActualizacionSolicitudArco solicitud) {
        // idResponsable ya fue autenticado y autorizado como administrador por el backend.
        // El esquema actual no tiene una columna para persistir ese identificador.
        if (idResponsable <= 0) {
            throw new SolicitudInvalidaException("No se pudo identificar al administrador responsable.");
        }
        if (solicitud == null || solicitud.estado() == null || !ESTADOS.contains(solicitud.estado())) {
            throw new SolicitudInvalidaException("Selecciona un estado válido para la solicitud.");
        }
        String respuesta = solicitud.respuesta() == null ? "" : solicitud.respuesta().trim();
        if (("Atendida".equals(solicitud.estado()) || "No procedente".equals(solicitud.estado())) && respuesta.isBlank()) {
            throw new SolicitudInvalidaException("Registra una respuesta antes de cerrar la solicitud.");
        }
        // public.solicitudes_arco.respuesta es VARCHAR(2000) en el esquema proporcionado.
        if (respuesta.length() > 2000) {
            throw new SolicitudInvalidaException("La respuesta no puede superar los 2000 caracteres.");
        }
        if (!repositorio.actualizarEstado(idSolicitud, solicitud.estado(), respuesta)) {
            throw new RecursoNoEncontradoException("No se encontró la solicitud ARCO.");
        }
        return repositorio.buscarPorId(idSolicitud)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la solicitud ARCO."));
    }

    private String normalizarDerecho(String valor) {
        String llave = valor.trim().toLowerCase(Locale.ROOT)
                .replace('á', 'a').replace('é', 'e').replace('í', 'i')
                .replace('ó', 'o').replace('ú', 'u');
        String derecho = DERECHOS.get(llave);
        if (derecho == null) {
            throw new SolicitudInvalidaException("El derecho debe ser Acceso, Rectificación, Cancelación u Oposición.");
        }
        return derecho;
    }
}
