package com.recetas.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recetas.backend.dto.DireccionDto;
import com.recetas.backend.dto.EstadoPedidoSolicitud;
import com.recetas.backend.dto.MetodoPagoDto;
import com.recetas.backend.dto.PedidoDetalleDto;
import com.recetas.backend.dto.PedidoResumenDto;
import com.recetas.backend.dto.SolicitudCrearPedido;
import com.recetas.backend.dto.SolicitudDireccion;
import com.recetas.backend.dto.SolicitudMetodoPago;
import com.recetas.backend.exception.ConflictoDatosException;
import com.recetas.backend.exception.RecursoNoEncontradoException;
import com.recetas.backend.exception.SolicitudInvalidaException;
import com.recetas.backend.repository.PedidoCompraRepositorio;
import com.recetas.backend.repository.PedidoCompraRepositorio.LineaCompra;

@Service
public class PedidoCompraServicio {

    private static final Set<String> TIPOS_METODO_PAGO = Set.of(
            "efectivo al recibir", "tarjeta de crédito", "tarjeta de débito", "transferencia");
    private static final Set<String> ESTADOS = Set.of("Registrado", "En preparación", "Enviado", "Entregado", "Cancelado");

    private final PedidoCompraRepositorio repositorio;
    private final JdbcTemplate jdbc;

    public PedidoCompraServicio(PedidoCompraRepositorio repositorio, JdbcTemplate jdbc) {
        this.repositorio = repositorio;
        this.jdbc = jdbc;
    }

    public List<DireccionDto> listarDirecciones(int idUsuario) {
        validarUsuarioActivo(idUsuario);
        return repositorio.listarDirecciones(idUsuario);
    }

    @Transactional
    public DireccionDto crearDireccion(int idUsuario, SolicitudDireccion solicitud) {
        validarUsuarioActivo(idUsuario);
        validarDireccion(solicitud);
        boolean principal = Boolean.TRUE.equals(solicitud.esPrincipal()) || repositorio.contarDirecciones(idUsuario) == 0;
        if (principal) repositorio.quitarPrincipalDirecciones(idUsuario);
        int id = repositorio.crearDireccion(idUsuario, limpiar(solicitud.calle()), limpiar(solicitud.numero()),
                limpiar(solicitud.colonia()), limpiar(solicitud.ciudad()), limpiar(solicitud.estado()),
                limpiar(solicitud.codigoPostal()), principal);
        return repositorio.buscarDireccion(idUsuario, id).orElseThrow();
    }

    @Transactional
    public DireccionDto actualizarDireccion(int idUsuario, int idDireccion, SolicitudDireccion solicitud) {
        validarUsuarioActivo(idUsuario);
        validarDireccion(solicitud);
        if (repositorio.buscarDireccion(idUsuario, idDireccion).isEmpty()) {
            throw new RecursoNoEncontradoException("No se encontró esa dirección en tu cuenta.");
        }
        if (repositorio.direccionUsadaEnPedido(idUsuario, idDireccion)) {
            throw new ConflictoDatosException("Esta dirección ya está vinculada a un pedido. Agrega una dirección nueva para futuras compras y conserva el historial.");
        }
        boolean principal = Boolean.TRUE.equals(solicitud.esPrincipal());
        if (principal) repositorio.quitarPrincipalDirecciones(idUsuario);
        if (!repositorio.actualizarDireccion(idUsuario, idDireccion, limpiar(solicitud.calle()),
                limpiar(solicitud.numero()), limpiar(solicitud.colonia()), limpiar(solicitud.ciudad()),
                limpiar(solicitud.estado()), limpiar(solicitud.codigoPostal()), principal)) {
            throw new RecursoNoEncontradoException("No se encontró esa dirección.");
        }
        return repositorio.buscarDireccion(idUsuario, idDireccion).orElseThrow();
    }

    @Transactional
    public void eliminarDireccion(int idUsuario, int idDireccion) {
        validarUsuarioActivo(idUsuario);
        DireccionDto existente = repositorio.buscarDireccion(idUsuario, idDireccion)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró esa dirección en tu cuenta."));
        if (repositorio.direccionUsadaEnPedido(idUsuario, idDireccion)) {
            throw new ConflictoDatosException("No puedes eliminar una dirección asociada a un pedido. Puedes conservarla en el historial y agregar otra dirección.");
        }
        if (!repositorio.eliminarDireccion(idUsuario, idDireccion)) {
            throw new RecursoNoEncontradoException("No se encontró esa dirección.");
        }
        if (Boolean.TRUE.equals(existente.esPrincipal())) marcarPrimeraDireccionComoPrincipal(idUsuario);
    }

    public List<MetodoPagoDto> listarMetodosPago(int idUsuario) {
        validarUsuarioActivo(idUsuario);
        return repositorio.listarMetodosPago(idUsuario);
    }

    @Transactional
    public MetodoPagoDto crearMetodoPago(int idUsuario, SolicitudMetodoPago solicitud) {
        validarUsuarioActivo(idUsuario);
        DatosMetodoPago datos = validarMetodoPago(solicitud);
        boolean principal = Boolean.TRUE.equals(solicitud.esPrincipal()) || repositorio.contarMetodosPago(idUsuario) == 0;
        if (principal) repositorio.quitarPrincipalMetodosPago(idUsuario);
        int id = repositorio.crearMetodoPago(idUsuario, datos.tipo(), datos.ultimos4Digitos(), principal);
        return repositorio.buscarMetodoPago(idUsuario, id).orElseThrow();
    }

    @Transactional
    public MetodoPagoDto actualizarMetodoPago(int idUsuario, int idMetodoPago, SolicitudMetodoPago solicitud) {
        validarUsuarioActivo(idUsuario);
        DatosMetodoPago datos = validarMetodoPago(solicitud);
        if (repositorio.buscarMetodoPago(idUsuario, idMetodoPago).isEmpty()) {
            throw new RecursoNoEncontradoException("No se encontró ese método de pago en tu cuenta.");
        }
        if (repositorio.metodoPagoUsadoEnPedido(idUsuario, idMetodoPago)) {
            throw new ConflictoDatosException("Este método está relacionado con un pedido. Agrega otro método para futuras compras y conserva el historial.");
        }
        boolean principal = Boolean.TRUE.equals(solicitud.esPrincipal());
        if (principal) repositorio.quitarPrincipalMetodosPago(idUsuario);
        if (!repositorio.actualizarMetodoPago(idUsuario, idMetodoPago, datos.tipo(), datos.ultimos4Digitos(), principal)) {
            throw new RecursoNoEncontradoException("No se encontró ese método de pago.");
        }
        return repositorio.buscarMetodoPago(idUsuario, idMetodoPago).orElseThrow();
    }

    @Transactional
    public void eliminarMetodoPago(int idUsuario, int idMetodoPago) {
        validarUsuarioActivo(idUsuario);
        MetodoPagoDto existente = repositorio.buscarMetodoPago(idUsuario, idMetodoPago)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ese método de pago en tu cuenta."));
        if (repositorio.metodoPagoUsadoEnPedido(idUsuario, idMetodoPago)) {
            throw new ConflictoDatosException("No puedes eliminar un método relacionado con un pedido. Puedes conservarlo en el historial y agregar otro método.");
        }
        if (!repositorio.eliminarMetodoPago(idUsuario, idMetodoPago)) {
            throw new RecursoNoEncontradoException("No se encontró ese método de pago.");
        }
        if (Boolean.TRUE.equals(existente.esPrincipal())) marcarPrimerMetodoComoPrincipal(idUsuario);
    }

    public List<PedidoResumenDto> listarPedidosUsuario(int idUsuario) {
        validarUsuarioActivo(idUsuario);
        return repositorio.listarPedidosUsuario(idUsuario);
    }

    public PedidoDetalleDto buscarPedidoUsuario(int idUsuario, int idPedido) {
        validarUsuarioActivo(idUsuario);
        return repositorio.buscarDetallePedido(idUsuario, idPedido)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró ese pedido en tu historial."));
    }

    @Transactional
    public PedidoDetalleDto crearPedido(int idUsuario, SolicitudCrearPedido solicitud) {
        validarUsuarioActivo(idUsuario);
        if (solicitud == null || solicitud.idDireccion() == null || solicitud.idMetodoPago() == null) {
            throw new SolicitudInvalidaException("Selecciona una dirección de envío y un método de pago.");
        }
        if (repositorio.buscarDireccion(idUsuario, solicitud.idDireccion()).isEmpty()) {
            throw new SolicitudInvalidaException("La dirección seleccionada no pertenece a tu cuenta.");
        }
        if (repositorio.buscarMetodoPago(idUsuario, solicitud.idMetodoPago()).isEmpty()) {
            throw new SolicitudInvalidaException("El método seleccionado no pertenece a tu cuenta.");
        }
        int idCarrito = repositorio.buscarCarritoParaCheckout(idUsuario)
                .orElseThrow(() -> new SolicitudInvalidaException("Tu carrito está vacío."));
        List<LineaCompra> lineas = repositorio.bloquearYListarLineas(idCarrito);
        if (lineas.isEmpty()) throw new SolicitudInvalidaException("Agrega al menos un producto antes de confirmar el pedido.");

        BigDecimal total = BigDecimal.ZERO;
        for (LineaCompra linea : lineas) {
            if (linea.cantidad() < 1) throw new SolicitudInvalidaException("El carrito tiene una cantidad inválida.");
            if (linea.stock() == null || linea.stock() < linea.cantidad()) {
                throw new ConflictoDatosException("El stock de '" + linea.nombreProducto() + "' cambió. Actualiza el carrito antes de confirmar.");
            }
            if (linea.precio() == null || linea.precio().signum() < 0) {
                throw new ConflictoDatosException("El producto '" + linea.nombreProducto() + "' no tiene un precio válido.");
            }
            total = total.add(linea.precio().multiply(BigDecimal.valueOf(linea.cantidad())));
        }
        total = total.setScale(2, RoundingMode.HALF_UP);

        int idPedido = repositorio.crearPedido(idUsuario, solicitud.idDireccion(), solicitud.idMetodoPago(), total);
        for (LineaCompra linea : lineas) {
            repositorio.agregarItemPedido(idPedido, linea);
            if (!repositorio.descontarStock(linea.idProducto(), linea.cantidad())) {
                throw new ConflictoDatosException("No se pudo reservar el stock de '" + linea.nombreProducto() + "'. No se confirmó el pedido.");
            }
        }
        repositorio.vaciarCarrito(idCarrito);
        return repositorio.buscarDetallePedido(idUsuario, idPedido).orElseThrow();
    }

    public List<PedidoResumenDto> listarTodosLosPedidos() {
        return repositorio.listarTodosLosPedidos();
    }

    public PedidoDetalleDto buscarPedidoAdministracion(int idPedido) {
        return repositorio.buscarDetallePedido(null, idPedido)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el pedido."));
    }

    @Transactional
    public PedidoDetalleDto actualizarEstadoPedido(int idPedido, EstadoPedidoSolicitud solicitud) {
        if (solicitud == null || solicitud.estado() == null || solicitud.estado().isBlank()) {
            throw new SolicitudInvalidaException("Selecciona un estado para el pedido.");
        }
        String nuevo = normalizarEstado(solicitud.estado());
        if (!ESTADOS.contains(nuevo)) throw new SolicitudInvalidaException("El estado indicado no es válido.");
        String actual = repositorio.obtenerEstado(idPedido)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el pedido."));
        if (actual.equals(nuevo)) return buscarPedidoAdministracion(idPedido);
        boolean transicionPermitida = switch (actual) {
            case "Registrado" -> nuevo.equals("En preparación") || nuevo.equals("Cancelado");
            case "En preparación" -> nuevo.equals("Enviado") || nuevo.equals("Cancelado");
            case "Enviado" -> nuevo.equals("Entregado");
            default -> false;
        };
        if (!transicionPermitida) {
            throw new ConflictoDatosException("No se permite cambiar el pedido de '" + actual + "' a '" + nuevo + "'.");
        }
        if (nuevo.equals("Cancelado")) repositorio.devolverStockPedido(idPedido);
        repositorio.actualizarEstado(idPedido, nuevo);
        return buscarPedidoAdministracion(idPedido);
    }

    private void validarUsuarioActivo(int idUsuario) {
        if (idUsuario < 1) throw new SolicitudInvalidaException("El identificador del usuario no es válido.");
        Boolean activo = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM usuarios WHERE id_usuario = ? AND activo IS TRUE)", Boolean.class, idUsuario);
        if (!Boolean.TRUE.equals(activo)) throw new SolicitudInvalidaException("La cuenta no existe o está inactiva.");
    }

    private void validarDireccion(SolicitudDireccion s) {
        if (s == null) throw new SolicitudInvalidaException("Completa los datos de la dirección.");
        validarTexto(s.calle(), "calle", 255);
        validarTexto(s.numero(), "número exterior", 255);
        validarTexto(s.colonia(), "colonia", 255);
        validarTexto(s.ciudad(), "ciudad", 255);
        validarTexto(s.estado(), "estado", 255);
        validarTexto(s.codigoPostal(), "código postal", 255);
        if (!s.codigoPostal().trim().matches("\\d{5}")) {
            throw new SolicitudInvalidaException("Escribe un código postal mexicano de cinco dígitos.");
        }
    }

    private DatosMetodoPago validarMetodoPago(SolicitudMetodoPago s) {
        if (s == null || s.tipo() == null || s.tipo().isBlank()) {
            throw new SolicitudInvalidaException("Selecciona un tipo de método de pago.");
        }
        String tipo = s.tipo().trim();
        if (!TIPOS_METODO_PAGO.contains(tipo.toLowerCase(Locale.ROOT))) {
            throw new SolicitudInvalidaException("El tipo de método de pago no está permitido.");
        }
        boolean tarjeta = tipo.toLowerCase(Locale.ROOT).startsWith("tarjeta");
        String ultimos = s.ultimos4Digitos() == null ? null : s.ultimos4Digitos().trim();
        if (tarjeta && (ultimos == null || !ultimos.matches("\\d{4}"))) {
            throw new SolicitudInvalidaException("Para una tarjeta solo registra sus últimos cuatro dígitos.");
        }
        if (!tarjeta) ultimos = null;
        return new DatosMetodoPago(tipo, ultimos);
    }

    private void validarTexto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) throw new SolicitudInvalidaException("El campo " + campo + " es obligatorio.");
        if (valor.trim().length() > maximo) throw new SolicitudInvalidaException("El campo " + campo + " excede la longitud permitida.");
    }

    private String limpiar(String texto) { return texto == null ? null : texto.trim(); }
    private String normalizarEstado(String estado) {
        return ESTADOS.stream().filter(e -> e.equalsIgnoreCase(estado.trim())).findFirst().orElse(estado.trim());
    }

    private void marcarPrimeraDireccionComoPrincipal(int idUsuario) {
        List<DireccionDto> direcciones = repositorio.listarDirecciones(idUsuario);
        if (direcciones.isEmpty()) return;
        repositorio.quitarPrincipalDirecciones(idUsuario);
        DireccionDto primera = direcciones.get(0);
        repositorio.actualizarDireccion(idUsuario, primera.idDireccion(), primera.calle(), primera.numero(),
                primera.colonia(), primera.ciudad(), primera.estado(), primera.codigoPostal(), true);
    }

    private void marcarPrimerMetodoComoPrincipal(int idUsuario) {
        List<MetodoPagoDto> metodos = repositorio.listarMetodosPago(idUsuario);
        if (metodos.isEmpty()) return;
        repositorio.quitarPrincipalMetodosPago(idUsuario);
        MetodoPagoDto primero = metodos.get(0);
        repositorio.actualizarMetodoPago(idUsuario, primero.idMetodoPago(), primero.tipo(), primero.ultimos4Digitos(), true);
    }

    private record DatosMetodoPago(String tipo, String ultimos4Digitos) { }
}
