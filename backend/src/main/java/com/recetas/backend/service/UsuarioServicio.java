package com.recetas.backend.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recetas.backend.dto.SolicitudUsuario;
import com.recetas.backend.dto.SolicitudActualizacionCuenta;
import com.recetas.backend.dto.SolicitudCambioContrasena;
import com.recetas.backend.dto.UsuarioCredencial;
import com.recetas.backend.dto.UsuarioDto;
import com.recetas.backend.exception.ConflictoDatosException;
import com.recetas.backend.exception.RecursoNoEncontradoException;
import com.recetas.backend.exception.SolicitudInvalidaException;
import com.recetas.backend.repository.UsuarioRepositorio;

@Service
public class UsuarioServicio {

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder passwordEncoder;
    public UsuarioServicio(UsuarioRepositorio repositorio, PasswordEncoder passwordEncoder) {
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioDto> listar() {
        return repositorio.listar();
    }

    public UsuarioDto buscarPorId(int id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario con ID " + id + "."));
    }

    @Transactional
    public UsuarioDto crear(SolicitudUsuario solicitud) {
        validar(solicitud, true);
        if (repositorio.existeNombreOCorreo(solicitud.username().trim(), solicitud.email(), null)) {
            throw new ConflictoDatosException("Ya existe un usuario con ese nombre de usuario o correo electrónico.");
        }
        String hash = passwordEncoder.encode(solicitud.password());
        int idUsuario = repositorio.insertar(solicitud, hash);
        repositorio.asignarRolUsuarioPredeterminado(idUsuario);
        return buscarPorId(idUsuario);
    }

    @Transactional
    public UsuarioDto actualizar(int id, SolicitudUsuario solicitud) {
        validar(solicitud, false);
        UsuarioDto actual = buscarPorId(id);
        String nombre = solicitud.username() == null ? actual.username() : solicitud.username().trim();
        String correo = solicitud.email() == null ? actual.email() : solicitud.email();
        if (repositorio.existeNombreOCorreo(nombre, correo, id)) {
            throw new ConflictoDatosException("Ya existe otro usuario con ese nombre de usuario o correo electrónico.");
        }
        String hash = solicitud.password() == null || solicitud.password().isBlank()
                ? null
                : passwordEncoder.encode(solicitud.password());
        if (!repositorio.actualizar(id, solicitud, hash)) {
            throw new RecursoNoEncontradoException("No se encontró el usuario con ID " + id + ".");
        }
        return buscarPorId(id);
    }

    /** Devuelve el correo asociado a la cuenta; utilizado por las solicitudes ARCO. */
    public String buscarCorreo(int idUsuario) {
        return repositorio.buscarCorreo(idUsuario);
    }

    /** Actualiza únicamente los campos editables del perfil propio. */
    @Transactional
    public UsuarioDto actualizarMiCuenta(int id, SolicitudActualizacionCuenta solicitud) {
        if (solicitud == null) {
            throw new SolicitudInvalidaException("Debes enviar los datos de tu cuenta.");
        }
        UsuarioDto actual = buscarPorId(id);
        String nombre = solicitud.username() == null ? actual.username() : solicitud.username().trim();
        String correo = solicitud.email() == null ? actual.email() : solicitud.email().trim();
        String nombreCompleto = solicitud.fullName() == null ? actual.fullName() : solicitud.fullName().trim();

        if (nombre == null || nombre.isBlank()) {
            throw new SolicitudInvalidaException("El nombre de usuario no puede estar vacío.");
        }
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new SolicitudInvalidaException("El nombre completo no puede estar vacío.");
        }
        if (correo != null && correo.isBlank()) correo = null;
        if (correo != null && !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new SolicitudInvalidaException("Escribe un correo electrónico válido o deja el campo vacío.");
        }
        if (repositorio.existeNombreOCorreo(nombre, correo, id)) {
            throw new ConflictoDatosException("Ya existe otra cuenta con ese nombre de usuario o correo electrónico.");
        }
        if (!repositorio.actualizarPerfil(id, nombre, correo, nombreCompleto)) {
            throw new RecursoNoEncontradoException("La cuenta no existe o está inactiva.");
        }
        return buscarPorId(id);
    }

    /** Verifica la contraseña actual antes de guardar una nueva. Los JWT de acceso expiran en 20 minutos. */
    @Transactional
    public void cambiarContrasena(int idUsuario, SolicitudCambioContrasena solicitud) {
        if (solicitud == null || solicitud.contrasenaActual() == null || solicitud.contrasenaActual().isBlank()) {
            throw new SolicitudInvalidaException("Escribe tu contraseña actual.");
        }
        String nueva = solicitud.contrasenaNueva();
        if (nueva == null || nueva.length() < 8) {
            throw new SolicitudInvalidaException("La contraseña nueva debe contener al menos 8 caracteres.");
        }
        if (nueva.equals(solicitud.contrasenaActual())) {
            throw new SolicitudInvalidaException("La contraseña nueva debe ser diferente de la actual.");
        }
        UsuarioCredencial credencial = repositorio.buscarCredencialesPorId(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("La cuenta no existe o está inactiva."));
        boolean actualCorrecta;
        try {
            actualCorrecta = credencial.contrasenaHash() != null
                    && passwordEncoder.matches(solicitud.contrasenaActual(), credencial.contrasenaHash());
        } catch (IllegalArgumentException ex) {
            actualCorrecta = false;
        }
        if (!actualCorrecta) {
            throw new SolicitudInvalidaException("La contraseña actual no es correcta.");
        }
        if (!repositorio.actualizarContrasena(idUsuario, passwordEncoder.encode(nueva))) {
            throw new RecursoNoEncontradoException("No se pudo actualizar la contraseña de la cuenta.");
        }
    }

    @Transactional
    public void eliminar(int id) {
        if (!repositorio.eliminar(id)) {
            throw new RecursoNoEncontradoException("No se encontró el usuario con ID " + id + ".");
        }
    }

    /**
     * Baja de cuenta para usuario común: archiva el registro por dos años y anonimiza la fila original.
     * No elimina pedidos ni recetas para evitar romper historial y claves foráneas.
     */
    @Transactional
    public void eliminarMiCuenta(int idUsuario) {
        UsuarioDto actual = buscarPorId(idUsuario);
        if (!Boolean.TRUE.equals(actual.active())) {
            throw new RecursoNoEncontradoException("La cuenta ya está desactivada.");
        }
        List<String> roles = repositorio.listarRoles(idUsuario);
        boolean administrador = roles.stream().map(r -> r == null ? "" : r.trim().toLowerCase())
                .anyMatch(r -> r.equals("admin") || r.equals("administrador") || r.equals("administrator"));
        if (administrador) {
            throw new SolicitudInvalidaException("La eliminación de cuenta desde Mi cuenta solo está disponible para usuarios normales.");
        }
        repositorio.archivarBaja(idUsuario);
        if (!repositorio.anonimizarCuenta(idUsuario)) {
            throw new RecursoNoEncontradoException("No se pudo completar la baja de la cuenta.");
        }
    }

    private void validar(SolicitudUsuario solicitud, boolean esNuevo) {
        if (solicitud == null) {
            throw new SolicitudInvalidaException("Debes enviar los datos del usuario.");
        }
        if (esNuevo) {
            if (solicitud.username() == null || solicitud.username().isBlank()) {
                throw new SolicitudInvalidaException("El campo 'username' es obligatorio.");
            }
            if (solicitud.password() == null || solicitud.password().isBlank()) {
                throw new SolicitudInvalidaException("El campo 'password' es obligatorio al registrar un usuario.");
            }
            if (solicitud.fullName() == null || solicitud.fullName().isBlank()) {
                throw new SolicitudInvalidaException("El campo 'fullName' es obligatorio.");
            }
        } else {
            if (solicitud.username() != null && solicitud.username().isBlank()) {
                throw new SolicitudInvalidaException("El nombre de usuario no puede estar vacío.");
            }
            if (solicitud.fullName() != null && solicitud.fullName().isBlank()) {
                throw new SolicitudInvalidaException("El nombre completo no puede estar vacío.");
            }
            if (solicitud.password() != null && !solicitud.password().isBlank() && solicitud.password().length() < 8) {
                throw new SolicitudInvalidaException("La contraseña debe contener al menos 8 caracteres.");
            }
        }
        if (solicitud.password() != null && esNuevo && solicitud.password().length() < 8) {
            throw new SolicitudInvalidaException("La contraseña debe contener al menos 8 caracteres.");
        }
    }
}
