package com.recetas.backend.config;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Autentica cada petición API y aplica permisos según los roles de usuario_rol/roles. */
@Component
public class AutorizacionApiInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_ID_USUARIO = "idUsuarioAutenticado";
    public static final String ATRIBUTO_ES_ADMINISTRADOR = "esAdministrador";

    private static final Pattern RUTA_PROPIETARIA = Pattern.compile("^/api/usuarios/(\\d+)/(favoritos|carrito|direcciones|metodos-pago|pedidos)(?:/.*)?$");
    private static final Pattern RUTA_VALORACION = Pattern.compile("^/api/recetas/\\d+/valoraciones$");
    private final JdbcTemplate jdbc;

    public AutorizacionApiInterceptor(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean preHandle(HttpServletRequest solicitud, HttpServletResponse respuesta, Object manejador) throws IOException {
        String metodo = solicitud.getMethod();
        String ruta = solicitud.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(metodo)
                || !ruta.startsWith("/api/")
                || ("POST".equalsIgnoreCase(metodo) && "/api/autenticacion/iniciar-sesion".equals(ruta))
                || "/api/saludo".equals(ruta)
                || "/api/adios".equals(ruta)) {
            return true;
        }

        // Spring Security ya validó la firma, el emisor y la expiración del JWT.
        // Aquí extraemos el identificador de usuario que el servidor de autorización colocó en el token.
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (!(autenticacion instanceof JwtAuthenticationToken autenticacionJwt)) {
            return responder(respuesta, HttpServletResponse.SC_UNAUTHORIZED,
                    "Falta un access token JWT válido. Inicia sesión mediante OAuth 2.0.");
        }
        Object idClaim = autenticacionJwt.getToken().getClaim("id_usuario");
        if (!(idClaim instanceof Number idNumerico)) {
            return responder(respuesta, HttpServletResponse.SC_UNAUTHORIZED,
                    "El JWT no contiene el identificador de usuario requerido.");
        }
        int id = idNumerico.intValue();
        Boolean usuarioActivo = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM usuarios WHERE id_usuario = ? AND activo IS TRUE)",
                Boolean.class, id);
        if (!Boolean.TRUE.equals(usuarioActivo)) {
            return responder(respuesta, HttpServletResponse.SC_UNAUTHORIZED,
                    "La cuenta está inactiva. Inicia sesión con una cuenta activa.");
        }

        Boolean administradorConsulta = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM usuario_rol ur
                    JOIN roles r ON r.id_rol = ur.id_rol
                    WHERE ur.id_usuario = ?
                      AND LOWER(BTRIM(r.nombre_rol)) IN ('administrador', 'admin', 'administrator')
                )
                """, Boolean.class, id);
        boolean esAdministrador = Boolean.TRUE.equals(administradorConsulta);
        solicitud.setAttribute(ATRIBUTO_ID_USUARIO, id);
        solicitud.setAttribute(ATRIBUTO_ES_ADMINISTRADOR, esAdministrador);

        // Autoservicio de cuenta y solicitudes ARCO: el controlador obtiene el id de la sesión,
        // nunca de un parámetro enviado por el navegador.
        if (ruta.equals("/api/mi-cuenta")) {
            if ("GET".equalsIgnoreCase(metodo) || "PUT".equalsIgnoreCase(metodo)
                    || "DELETE".equalsIgnoreCase(metodo)) return true;
        }
        if (ruta.equals("/api/mi-cuenta/contrasena") && "PUT".equalsIgnoreCase(metodo)) {
            return true;
        }
        if (ruta.equals("/api/solicitudes-arco")) {
            if ("GET".equalsIgnoreCase(metodo) || "POST".equalsIgnoreCase(metodo)) return true;
        }
        if (ruta.startsWith("/api/administracion/solicitudes-arco")) {
            if (!esAdministrador) {
                return responder(respuesta, HttpServletResponse.SC_FORBIDDEN,
                        "La gestión de solicitudes ARCO requiere el rol Administrador.");
            }
            return true;
        }

        // El panel de pedidos global es exclusivamente administrativo, incluidas las consultas.
        if (ruta.startsWith("/api/administracion/pedidos") && !esAdministrador) {
            return responder(respuesta, HttpServletResponse.SC_FORBIDDEN,
                    "El panel de pedidos requiere el rol Administrador.");
        }

        // Favoritos, carrito, direcciones, métodos de pago e historial pertenecen a un usuario.
        Matcher coincidenciaPropietaria = RUTA_PROPIETARIA.matcher(ruta);
        if (coincidenciaPropietaria.matches()) {
            int idPropietario = Integer.parseInt(coincidenciaPropietaria.group(1));
            if (idPropietario != id && !esAdministrador) {
                return responder(respuesta, HttpServletResponse.SC_FORBIDDEN,
                        "Solo puedes consultar o modificar tus propios favoritos y carrito.");
            }
            return true;
        }

        // Los usuarios comunes pueden publicar su propia valoración, no suplantar otro id_usuario.
        if ("POST".equalsIgnoreCase(metodo) && RUTA_VALORACION.matcher(ruta).matches()) {
            return true;
        }

        // El CRUD de usuarios no está disponible para usuarios comunes.
        if (ruta.matches("^/api/usuarios(?:/\\d+)?$")) {
            if (!esAdministrador) {
                return responder(respuesta, HttpServletResponse.SC_FORBIDDEN,
                        "Esta operación requiere el rol Administrador.");
            }
            return true;
        }

        boolean esEscritura = "POST".equalsIgnoreCase(metodo)
                || "PUT".equalsIgnoreCase(metodo)
                || "PATCH".equalsIgnoreCase(metodo)
                || "DELETE".equalsIgnoreCase(metodo);
        if (esEscritura && "/api/autenticacion/cerrar-sesion".equals(ruta)) {
            return true;
        }
        if (esEscritura && !esAdministrador) {
            return responder(respuesta, HttpServletResponse.SC_FORBIDDEN,
                    "Esta operación requiere el rol Administrador.");
        }
        return true;
    }

    private boolean responder(HttpServletResponse respuesta, int estado, String mensaje) throws IOException {
        respuesta.setStatus(estado);
        respuesta.setCharacterEncoding("UTF-8");
        respuesta.setContentType("application/json");
        String textoSeguro = mensaje.replace("\\", "\\\\").replace("\"", "\\\"");
        respuesta.getWriter().write("{\"mensaje\":\"" + textoSeguro + "\"}");
        return false;
    }
}
