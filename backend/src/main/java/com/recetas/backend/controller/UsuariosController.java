package com.recetas.backend.controller;

import java.net.URI;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recetas.backend.dto.RespuestaLoginDto;
import com.recetas.backend.dto.SolicitudUsuario;
import com.recetas.backend.dto.UsuarioDto;
import com.recetas.backend.service.UsuarioServicio;

/** CRUD administrativo de usuarios y autenticación; MiCuentaController gestiona el autoservicio. */
@RestController
@RequestMapping("/api")
public class UsuariosController {
    private final UsuarioServicio servicio;
    private final SecurityContextLogoutHandler manejadorCierreSesion = new SecurityContextLogoutHandler();

    public UsuariosController(UsuarioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/usuarios")
    public List<UsuarioDto> listar() { return servicio.listar(); }

    @GetMapping("/usuarios/{id}")
    public UsuarioDto buscarPorId(@PathVariable int id) { return servicio.buscarPorId(id); }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDto> crear(@RequestBody SolicitudUsuario solicitud) {
        UsuarioDto creado = servicio.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/usuarios/" + creado.id())).body(creado);
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioDto actualizar(@PathVariable int id, @RequestBody SolicitudUsuario solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/autenticacion/cerrar-sesion")
    public ResponseEntity<Void> cerrarSesion(
            HttpServletRequest solicitud,
            HttpServletResponse respuesta,
            Authentication autenticacion) {
        // Invalida la sesión HTTP que Spring Authorization Server creó al autenticar
        // el formulario /login. Así, el siguiente /oauth2/authorize ya no reutiliza
        // silenciosamente la sesión anterior y vuelve a solicitar credenciales.
        manejadorCierreSesion.logout(solicitud, respuesta, autenticacion);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/autenticacion/iniciar-sesion")
    public ResponseEntity<RespuestaLoginDto> iniciarSesion() {
        return ResponseEntity.status(HttpStatus.GONE).body(RespuestaLoginDto.error(
                "El inicio de sesión directo fue sustituido por OAuth 2.0 Authorization Code + PKCE. "
                + "Usa /oauth2/authorize con el cliente receton-angular."));
    }
}
