package com.recetas.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.recetas.backend.dto.SolicitudUsuario;
import com.recetas.backend.dto.UsuarioCredencial;
import com.recetas.backend.dto.UsuarioDto;

@Repository
public class UsuarioRepositorio {

    private final JdbcTemplate jdbc;

    public UsuarioRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<UsuarioDto> listar() {
        return jdbc.query("""
                SELECT id_usuario, nombre_usuario, correo, nombre_completo, fecha_registro, activo
                FROM usuarios ORDER BY id_usuario
                """, (rs, fila) -> new UsuarioDto(
                rs.getInt("id_usuario"), rs.getString("nombre_usuario"), rs.getString("correo"),
                rs.getString("nombre_completo"),
                rs.getTimestamp("fecha_registro") == null ? null : rs.getTimestamp("fecha_registro").toLocalDateTime(),
                (Boolean) rs.getObject("activo")));
    }

    public Optional<UsuarioDto> buscarPorId(int id) {
        List<UsuarioDto> resultados = jdbc.query("""
                SELECT id_usuario, nombre_usuario, correo, nombre_completo, fecha_registro, activo
                FROM usuarios WHERE id_usuario = ?
                """, (rs, fila) -> new UsuarioDto(
                rs.getInt("id_usuario"), rs.getString("nombre_usuario"), rs.getString("correo"),
                rs.getString("nombre_completo"),
                rs.getTimestamp("fecha_registro") == null ? null : rs.getTimestamp("fecha_registro").toLocalDateTime(),
                (Boolean) rs.getObject("activo")), id);
        return resultados.stream().findFirst();
    }

    public Optional<UsuarioCredencial> buscarCredenciales(String usuarioOCorreo) {
        List<UsuarioCredencial> resultados = jdbc.query("""
                SELECT id_usuario, nombre_usuario, nombre_completo, contrasena_hash, activo
                FROM usuarios
                WHERE LOWER(nombre_usuario) = LOWER(?) OR LOWER(COALESCE(correo, '')) = LOWER(?)
                ORDER BY id_usuario
                LIMIT 1
                """, (rs, fila) -> new UsuarioCredencial(
                rs.getInt("id_usuario"), rs.getString("nombre_usuario"), rs.getString("nombre_completo"),
                rs.getString("contrasena_hash"), (Boolean) rs.getObject("activo")), usuarioOCorreo, usuarioOCorreo);
        return resultados.stream().findFirst();
    }


    public List<String> listarRoles(int idUsuario) {
        return jdbc.query("""
                SELECT r.nombre_rol
                FROM usuario_rol ur
                JOIN roles r ON r.id_rol = ur.id_rol
                WHERE ur.id_usuario = ?
                ORDER BY r.id_rol
                """, (rs, fila) -> rs.getString("nombre_rol"), idUsuario);
    }

    /** Los usuarios nuevos reciben únicamente el rol común; nunca pueden enviarse un rol desde el JSON. */
    public void asignarRolUsuarioPredeterminado(int idUsuario) {
        List<Integer> rolesComunes = jdbc.query("""
                SELECT id_rol FROM roles
                WHERE LOWER(BTRIM(nombre_rol)) IN ('usuario', 'user', 'cliente', 'usuario comun', 'usuario común')
                ORDER BY CASE WHEN LOWER(BTRIM(nombre_rol)) = 'usuario' THEN 0 ELSE 1 END, id_rol
                LIMIT 1
                """, (rs, fila) -> rs.getInt("id_rol"));

        int idRol;
        if (rolesComunes.isEmpty()) {
            Integer siguienteId = jdbc.queryForObject("SELECT COALESCE(MAX(id_rol), 0) + 1 FROM roles", Integer.class);
            idRol = siguienteId == null ? 1 : siguienteId;
            jdbc.update("INSERT INTO roles (id_rol, nombre_rol) VALUES (?, 'Usuario')", idRol);
        } else {
            idRol = rolesComunes.get(0);
        }

        jdbc.update("INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (?, ?) ON CONFLICT DO NOTHING",
                idUsuario, idRol);
    }

    public boolean existeNombreOCorreo(String nombre, String correo, Integer excluirId) {
        Integer cantidad = jdbc.queryForObject("""
                SELECT COUNT(*) FROM usuarios
                WHERE (LOWER(nombre_usuario) = LOWER(?) OR (? <> '' AND LOWER(COALESCE(correo, '')) = LOWER(?)))
                  AND (CAST(? AS INTEGER) IS NULL OR id_usuario <> CAST(? AS INTEGER))
                """, Integer.class, nombre, correo == null ? "" : correo,
                correo == null ? "" : correo, excluirId, excluirId);
        return cantidad != null && cantidad > 0;
    }

    public int insertar(SolicitudUsuario solicitud, String hashContrasena) {
        Integer id = jdbc.queryForObject("SELECT nextval('seq_usuarios')", Integer.class);
        jdbc.update("""
                INSERT INTO usuarios
                    (id_usuario, nombre_usuario, correo, contrasena_hash, nombre_completo, fecha_registro, activo)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, COALESCE(?, TRUE))
                """, id, solicitud.username().trim(), solicitud.email(), hashContrasena,
                solicitud.fullName().trim(), solicitud.active());
        return id;
    }

    /** Correo de contacto de la cuenta, usado como valor predeterminado para solicitudes ARCO. */
    public String buscarCorreo(int idUsuario) {
        List<String> correos = jdbc.query(
                "SELECT correo FROM usuarios WHERE id_usuario = ? AND activo IS TRUE",
                (rs, fila) -> rs.getString("correo"), idUsuario);
        return correos.isEmpty() ? null : correos.get(0);
    }

    /** Actualiza datos de perfil sin tocar el hash, el estado de la cuenta ni los roles. */
    public boolean actualizarPerfil(int id, String username, String email, String fullName) {
        return jdbc.update("""
                UPDATE usuarios SET
                    nombre_usuario = ?,
                    correo = ?,
                    nombre_completo = ?
                WHERE id_usuario = ? AND activo IS TRUE
                """, username, email, fullName, id) > 0;
    }

    public Optional<UsuarioCredencial> buscarCredencialesPorId(int idUsuario) {
        List<UsuarioCredencial> resultados = jdbc.query("""
                SELECT id_usuario, nombre_usuario, nombre_completo, contrasena_hash, activo
                FROM usuarios
                WHERE id_usuario = ? AND activo IS TRUE
                """, (rs, fila) -> new UsuarioCredencial(
                rs.getInt("id_usuario"), rs.getString("nombre_usuario"), rs.getString("nombre_completo"),
                rs.getString("contrasena_hash"), (Boolean) rs.getObject("activo")), idUsuario);
        return resultados.stream().findFirst();
    }

    public boolean actualizarContrasena(int idUsuario, String hashNuevo) {
        return jdbc.update("""
                UPDATE usuarios SET contrasena_hash = ?
                WHERE id_usuario = ? AND activo IS TRUE
                """, hashNuevo, idUsuario) > 0;
    }

    public boolean actualizar(int id, SolicitudUsuario solicitud, String hashContrasena) {
        return jdbc.update("""
                UPDATE usuarios SET
                    nombre_usuario = COALESCE(?, nombre_usuario),
                    correo = COALESCE(?, correo),
                    contrasena_hash = COALESCE(?, contrasena_hash),
                    nombre_completo = COALESCE(?, nombre_completo),
                    activo = COALESCE(?, activo)
                WHERE id_usuario = ?
                """, solicitud.username(), solicitud.email(), hashContrasena,
                solicitud.fullName(), solicitud.active(), id) > 0;
    }

    /** Guarda los datos mínimos definidos para el registro de baja durante dos años. */
    public void archivarBaja(int id) {
        jdbc.update("""
                INSERT INTO cuentas_eliminadas
                    (id_usuario_original, nombre_usuario, nombre_completo, correo, fecha_registro_original,
                     fecha_eliminacion, fecha_purga, motivo)
                SELECT id_usuario, nombre_usuario, nombre_completo, correo, fecha_registro,
                       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '2 years', 'Solicitud del titular'
                FROM usuarios WHERE id_usuario = ?
                """, id);
    }

    /** Anonimiza la cuenta original para preservar integridad referencial de pedidos/recetas. */
    public boolean anonimizarCuenta(int id) {
        String identificador = "cuenta_eliminada_" + id;
        return jdbc.update("""
                UPDATE usuarios SET nombre_usuario = ?, correo = NULL, nombre_completo = 'Cuenta eliminada',
                    contrasena_hash = NULL, activo = FALSE
                WHERE id_usuario = ? AND activo = TRUE
                """, identificador, id) > 0;
    }

    public boolean eliminar(int id) {
        Integer existe = jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE id_usuario = ?", Integer.class, id);
        if (existe == null || existe == 0) {
            return false;
        }

        Boolean tieneRecetas = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM recetas WHERE id_usuario = ?)", Boolean.class, id);
        Boolean tienePedidos = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM pedidos WHERE id_usuario = ?)", Boolean.class, id);
        if (Boolean.TRUE.equals(tieneRecetas) || Boolean.TRUE.equals(tienePedidos)) {
            throw new com.recetas.backend.exception.ConflictoDatosException(
                    "No se puede eliminar el usuario porque tiene recetas publicadas o pedidos relacionados.");
        }

        jdbc.update("DELETE FROM usuario_rol WHERE id_usuario = ?", id);
        jdbc.update("DELETE FROM valoraciones WHERE id_usuario = ?", id);
        jdbc.update("DELETE FROM favoritos WHERE id_usuario = ?", id);
        jdbc.update("DELETE FROM carrito_item WHERE id_carrito IN (SELECT id_carrito FROM carritos WHERE id_usuario = ?)", id);
        jdbc.update("DELETE FROM carritos WHERE id_usuario = ?", id);
        jdbc.update("DELETE FROM direcciones WHERE id_usuario = ?", id);
        jdbc.update("DELETE FROM metodos_pago WHERE id_usuario = ?", id);
        jdbc.update("DELETE FROM usuarios WHERE id_usuario = ?", id);
        return true;
    }
}
