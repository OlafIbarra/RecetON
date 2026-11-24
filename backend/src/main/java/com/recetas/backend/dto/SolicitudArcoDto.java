package com.recetas.backend.dto;

import java.time.LocalDateTime;

/** Vista de una solicitud ARCO; incluye datos del solicitante solo en las consultas de administración. */
public record SolicitudArcoDto(
        Long idSolicitud,
        Integer idUsuario,
        String nombreUsuario,
        String correo,
        String derecho,
        String descripcion,
        LocalDateTime fechaSolicitud,
        String estado,
        String respuesta,
        LocalDateTime fechaResolucion,
        String nombreResponsable
) { }
