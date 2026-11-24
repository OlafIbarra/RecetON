package com.recetas.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recetas.backend.config.AutorizacionApiInterceptor;
import com.recetas.backend.dto.SolicitudActualizacionCuenta;
import com.recetas.backend.dto.SolicitudArco;
import com.recetas.backend.dto.SolicitudArcoDto;
import com.recetas.backend.dto.SolicitudCambioContrasena;
import com.recetas.backend.dto.UsuarioDto;
import com.recetas.backend.service.SolicitudesArcoServicio;
import com.recetas.backend.service.UsuarioServicio;

/** Autoservicio: el identificador siempre procede de la sesión validada, nunca del cuerpo. */
@RestController
@RequestMapping("/api")
public class MiCuentaController {
    private final UsuarioServicio usuarios;
    private final SolicitudesArcoServicio arco;

    public MiCuentaController(UsuarioServicio usuarios, SolicitudesArcoServicio arco) {
        this.usuarios = usuarios;
        this.arco = arco;
    }

    @GetMapping("/mi-cuenta")
    public UsuarioDto miCuenta(@RequestAttribute(AutorizacionApiInterceptor.ATRIBUTO_ID_USUARIO) int idUsuario) {
        return usuarios.buscarPorId(idUsuario);
    }

    @PutMapping("/mi-cuenta")
    public UsuarioDto actualizarMiCuenta(
            @RequestAttribute(AutorizacionApiInterceptor.ATRIBUTO_ID_USUARIO) int idUsuario,
            @RequestBody SolicitudActualizacionCuenta solicitud) {
        return usuarios.actualizarMiCuenta(idUsuario, solicitud);
    }

    @PutMapping("/mi-cuenta/contrasena")
    public ResponseEntity<Void> cambiarContrasena(
            @RequestAttribute(AutorizacionApiInterceptor.ATRIBUTO_ID_USUARIO) int idUsuario,
            @RequestBody SolicitudCambioContrasena solicitud) {
        usuarios.cambiarContrasena(idUsuario, solicitud);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/mi-cuenta")
    public ResponseEntity<Void> eliminarMiCuenta(
            @RequestAttribute(AutorizacionApiInterceptor.ATRIBUTO_ID_USUARIO) int idUsuario) {
        usuarios.eliminarMiCuenta(idUsuario);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/solicitudes-arco")
    public ResponseEntity<SolicitudArcoDto> crearSolicitudArco(
            @RequestAttribute(AutorizacionApiInterceptor.ATRIBUTO_ID_USUARIO) int idUsuario,
            @RequestBody SolicitudArco solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(arco.crear(idUsuario, solicitud));
    }

    @GetMapping("/solicitudes-arco")
    public List<SolicitudArcoDto> listarSolicitudesArco(
            @RequestAttribute(AutorizacionApiInterceptor.ATRIBUTO_ID_USUARIO) int idUsuario) {
        return arco.listar(idUsuario);
    }
}
