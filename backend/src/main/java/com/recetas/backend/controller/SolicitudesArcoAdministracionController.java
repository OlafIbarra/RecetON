package com.recetas.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recetas.backend.config.AutorizacionApiInterceptor;
import com.recetas.backend.dto.ActualizacionSolicitudArco;
import com.recetas.backend.dto.SolicitudArcoDto;
import com.recetas.backend.service.SolicitudesArcoServicio;

/** Administración de solicitudes; el interceptor también exige rol administrador en servidor. */
@RestController
@RequestMapping("/api/administracion/solicitudes-arco")
public class SolicitudesArcoAdministracionController {
    private final SolicitudesArcoServicio servicio;

    public SolicitudesArcoAdministracionController(SolicitudesArcoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<SolicitudArcoDto> listar() {
        return servicio.listarTodas();
    }

    @PutMapping("/{idSolicitud}")
    public SolicitudArcoDto actualizar(
            @PathVariable long idSolicitud,
            @RequestAttribute(AutorizacionApiInterceptor.ATRIBUTO_ID_USUARIO) int idResponsable,
            @RequestBody ActualizacionSolicitudArco solicitud) {
        return servicio.actualizar(idSolicitud, idResponsable, solicitud);
    }
}
