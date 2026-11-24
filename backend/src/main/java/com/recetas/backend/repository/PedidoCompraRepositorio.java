package com.recetas.backend.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.recetas.backend.dto.DireccionDto;
import com.recetas.backend.dto.MetodoPagoDto;
import com.recetas.backend.dto.PedidoDetalleDto;
import com.recetas.backend.dto.PedidoItemDto;
import com.recetas.backend.dto.PedidoResumenDto;

@Repository
public class PedidoCompraRepositorio {

    private final JdbcTemplate jdbc;

    public PedidoCompraRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DireccionDto> listarDirecciones(int idUsuario) {
        return jdbc.query("""
                SELECT id_direccion, id_usuario, calle, numero, colonia, ciudad, estado, codigo_postal, es_principal
                FROM direcciones WHERE id_usuario = ?
                ORDER BY es_principal DESC NULLS LAST, id_direccion DESC
                """, (rs, fila) -> new DireccionDto(
                rs.getInt("id_direccion"), rs.getInt("id_usuario"), rs.getString("calle"),
                rs.getString("numero"), rs.getString("colonia"), rs.getString("ciudad"),
                rs.getString("estado"), rs.getString("codigo_postal"), (Boolean) rs.getObject("es_principal")), idUsuario);
    }

    public Optional<DireccionDto> buscarDireccion(int idUsuario, int idDireccion) {
        List<DireccionDto> resultados = jdbc.query("""
                SELECT id_direccion, id_usuario, calle, numero, colonia, ciudad, estado, codigo_postal, es_principal
                FROM direcciones WHERE id_usuario = ? AND id_direccion = ?
                """, (rs, fila) -> new DireccionDto(
                rs.getInt("id_direccion"), rs.getInt("id_usuario"), rs.getString("calle"),
                rs.getString("numero"), rs.getString("colonia"), rs.getString("ciudad"),
                rs.getString("estado"), rs.getString("codigo_postal"), (Boolean) rs.getObject("es_principal")), idUsuario, idDireccion);
        return resultados.stream().findFirst();
    }

    public int crearDireccion(int idUsuario, String calle, String numero, String colonia, String ciudad,
            String estado, String codigoPostal, boolean principal) {
        Integer id = jdbc.queryForObject("SELECT nextval('seq_direcciones')", Integer.class);
        jdbc.update("""
                INSERT INTO direcciones (id_direccion, id_usuario, calle, numero, colonia, ciudad, estado, codigo_postal, es_principal)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, idUsuario, calle, numero, colonia, ciudad, estado, codigoPostal, principal);
        return id;
    }

    public boolean actualizarDireccion(int idUsuario, int idDireccion, String calle, String numero,
            String colonia, String ciudad, String estado, String codigoPostal, boolean principal) {
        return jdbc.update("""
                UPDATE direcciones SET calle = ?, numero = ?, colonia = ?, ciudad = ?, estado = ?,
                       codigo_postal = ?, es_principal = ?
                WHERE id_usuario = ? AND id_direccion = ?
                """, calle, numero, colonia, ciudad, estado, codigoPostal, principal, idUsuario, idDireccion) > 0;
    }

    public void quitarPrincipalDirecciones(int idUsuario) {
        jdbc.update("UPDATE direcciones SET es_principal = FALSE WHERE id_usuario = ?", idUsuario);
    }

    public int contarDirecciones(int idUsuario) {
        Integer cuenta = jdbc.queryForObject("SELECT COUNT(*) FROM direcciones WHERE id_usuario = ?", Integer.class, idUsuario);
        return cuenta == null ? 0 : cuenta;
    }

    public boolean direccionUsadaEnPedido(int idUsuario, int idDireccion) {
        Boolean usada = jdbc.queryForObject("""
                SELECT EXISTS (SELECT 1 FROM pedidos WHERE id_usuario = ? AND id_direccion = ?)
                """, Boolean.class, idUsuario, idDireccion);
        return Boolean.TRUE.equals(usada);
    }

    public boolean eliminarDireccion(int idUsuario, int idDireccion) {
        return jdbc.update("DELETE FROM direcciones WHERE id_usuario = ? AND id_direccion = ?", idUsuario, idDireccion) > 0;
    }

    public List<MetodoPagoDto> listarMetodosPago(int idUsuario) {
        return jdbc.query("""
                SELECT id_metodo_pago, id_usuario, tipo, ultimos_4_digitos, es_principal
                FROM metodos_pago WHERE id_usuario = ?
                ORDER BY es_principal DESC NULLS LAST, id_metodo_pago DESC
                """, (rs, fila) -> new MetodoPagoDto(
                rs.getInt("id_metodo_pago"), rs.getInt("id_usuario"), rs.getString("tipo"),
                rs.getString("ultimos_4_digitos"), (Boolean) rs.getObject("es_principal")), idUsuario);
    }

    public Optional<MetodoPagoDto> buscarMetodoPago(int idUsuario, int idMetodoPago) {
        List<MetodoPagoDto> resultados = jdbc.query("""
                SELECT id_metodo_pago, id_usuario, tipo, ultimos_4_digitos, es_principal
                FROM metodos_pago WHERE id_usuario = ? AND id_metodo_pago = ?
                """, (rs, fila) -> new MetodoPagoDto(
                rs.getInt("id_metodo_pago"), rs.getInt("id_usuario"), rs.getString("tipo"),
                rs.getString("ultimos_4_digitos"), (Boolean) rs.getObject("es_principal")), idUsuario, idMetodoPago);
        return resultados.stream().findFirst();
    }

    public int crearMetodoPago(int idUsuario, String tipo, String ultimos4Digitos, boolean principal) {
        Integer id = jdbc.queryForObject("SELECT nextval('seq_metodos_pago')", Integer.class);
        jdbc.update("""
                INSERT INTO metodos_pago (id_metodo_pago, id_usuario, tipo, ultimos_4_digitos, es_principal)
                VALUES (?, ?, ?, ?, ?)
                """, id, idUsuario, tipo, ultimos4Digitos, principal);
        return id;
    }

    public boolean actualizarMetodoPago(int idUsuario, int idMetodoPago, String tipo,
            String ultimos4Digitos, boolean principal) {
        return jdbc.update("""
                UPDATE metodos_pago SET tipo = ?, ultimos_4_digitos = ?, es_principal = ?
                WHERE id_usuario = ? AND id_metodo_pago = ?
                """, tipo, ultimos4Digitos, principal, idUsuario, idMetodoPago) > 0;
    }

    public void quitarPrincipalMetodosPago(int idUsuario) {
        jdbc.update("UPDATE metodos_pago SET es_principal = FALSE WHERE id_usuario = ?", idUsuario);
    }

    public int contarMetodosPago(int idUsuario) {
        Integer cuenta = jdbc.queryForObject("SELECT COUNT(*) FROM metodos_pago WHERE id_usuario = ?", Integer.class, idUsuario);
        return cuenta == null ? 0 : cuenta;
    }

    public boolean metodoPagoUsadoEnPedido(int idUsuario, int idMetodoPago) {
        Boolean usado = jdbc.queryForObject("""
                SELECT EXISTS (SELECT 1 FROM pedidos WHERE id_usuario = ? AND id_metodo_pago = ?)
                """, Boolean.class, idUsuario, idMetodoPago);
        return Boolean.TRUE.equals(usado);
    }

    public boolean eliminarMetodoPago(int idUsuario, int idMetodoPago) {
        return jdbc.update("DELETE FROM metodos_pago WHERE id_usuario = ? AND id_metodo_pago = ?", idUsuario, idMetodoPago) > 0;
    }

    public Optional<Integer> buscarCarritoParaCheckout(int idUsuario) {
        List<Integer> resultados = jdbc.query("""
                SELECT id_carrito FROM carritos WHERE id_usuario = ?
                ORDER BY fecha_creacion DESC NULLS LAST, id_carrito DESC LIMIT 1 FOR UPDATE
                """, (rs, fila) -> rs.getInt("id_carrito"), idUsuario);
        return resultados.stream().findFirst();
    }

    public List<LineaCompra> bloquearYListarLineas(int idCarrito) {
        return jdbc.query("""
                SELECT p.id_producto, p.nombre_producto, p.precio, p.stock, ci.cantidad
                FROM carrito_item ci JOIN productos p ON p.id_producto = ci.id_producto
                WHERE ci.id_carrito = ? ORDER BY p.id_producto FOR UPDATE OF p
                """, (rs, fila) -> new LineaCompra(
                rs.getInt("id_producto"), rs.getString("nombre_producto"), rs.getBigDecimal("precio"),
                (Integer) rs.getObject("stock"), rs.getInt("cantidad")), idCarrito);
    }

    public int crearPedido(int idUsuario, int idDireccion, int idMetodoPago, BigDecimal total) {
        Integer id = jdbc.queryForObject("SELECT nextval('seq_pedidos')", Integer.class);
        jdbc.update("""
                INSERT INTO pedidos (id_pedido, id_usuario, id_direccion, id_metodo_pago, fecha_pedido, estado, total)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 'Registrado', ?)
                """, id, idUsuario, idDireccion, idMetodoPago, total);
        return id;
    }

    public void agregarItemPedido(int idPedido, LineaCompra linea) {
        jdbc.update("""
                INSERT INTO pedido_item (id_pedido, id_producto, cantidad, precio_unitario)
                VALUES (?, ?, ?, ?)
                """, idPedido, linea.idProducto(), linea.cantidad(), linea.precio());
    }

    public boolean descontarStock(int idProducto, int cantidad) {
        return jdbc.update("UPDATE productos SET stock = stock - ? WHERE id_producto = ? AND stock >= ?",
                cantidad, idProducto, cantidad) > 0;
    }

    public void vaciarCarrito(int idCarrito) {
        jdbc.update("DELETE FROM carrito_item WHERE id_carrito = ?", idCarrito);
    }

    public List<PedidoResumenDto> listarPedidosUsuario(int idUsuario) {
        return jdbc.query(consultaPedidos() + " WHERE p.id_usuario = ? GROUP BY p.id_pedido, u.nombre_usuario ORDER BY p.fecha_pedido DESC, p.id_pedido DESC",
                this::mapResumen, idUsuario);
    }

    public List<PedidoResumenDto> listarTodosLosPedidos() {
        return jdbc.query(consultaPedidos() + " GROUP BY p.id_pedido, u.nombre_usuario ORDER BY p.fecha_pedido DESC, p.id_pedido DESC",
                this::mapResumen);
    }

    private String consultaPedidos() {
        return """
                SELECT p.id_pedido, p.id_usuario, u.nombre_usuario, p.fecha_pedido, p.estado, p.total,
                       COALESCE(SUM(pi.cantidad), 0)::integer AS cantidad_articulos
                FROM pedidos p JOIN usuarios u ON u.id_usuario = p.id_usuario
                LEFT JOIN pedido_item pi ON pi.id_pedido = p.id_pedido
                """;
    }

    private PedidoResumenDto mapResumen(java.sql.ResultSet rs, int fila) throws java.sql.SQLException {
        Timestamp fecha = rs.getTimestamp("fecha_pedido");
        return new PedidoResumenDto(rs.getInt("id_pedido"), rs.getInt("id_usuario"), rs.getString("nombre_usuario"),
                fecha == null ? null : fecha.toLocalDateTime(), rs.getString("estado"), rs.getBigDecimal("total"),
                rs.getInt("cantidad_articulos"));
    }

    public Optional<String> obtenerEstado(int idPedido) {
        List<String> estados = jdbc.query("SELECT estado FROM pedidos WHERE id_pedido = ? FOR UPDATE",
                (rs, fila) -> rs.getString("estado"), idPedido);
        return estados.stream().findFirst();
    }

    public boolean actualizarEstado(int idPedido, String estado) {
        return jdbc.update("UPDATE pedidos SET estado = ? WHERE id_pedido = ?", estado, idPedido) > 0;
    }

    public void devolverStockPedido(int idPedido) {
        jdbc.update("""
                UPDATE productos p SET stock = COALESCE(p.stock, 0) + pi.cantidad
                FROM pedido_item pi
                WHERE pi.id_pedido = ? AND pi.id_producto = p.id_producto
                """, idPedido);
    }

    public Optional<PedidoDetalleDto> buscarDetallePedido(Integer idUsuario, int idPedido) {
        String condicionUsuario = idUsuario == null ? "" : " AND p.id_usuario = ?";
        String sql = """
                SELECT p.id_pedido, p.id_usuario, u.nombre_usuario, p.fecha_pedido, p.estado, p.total,
                       d.id_direccion, d.calle, d.numero, d.colonia, d.ciudad, d.estado AS estado_direccion,
                       d.codigo_postal, d.es_principal AS direccion_principal,
                       mp.id_metodo_pago, mp.tipo AS tipo_pago, mp.ultimos_4_digitos, mp.es_principal AS pago_principal
                FROM pedidos p
                JOIN usuarios u ON u.id_usuario = p.id_usuario
                JOIN direcciones d ON d.id_direccion = p.id_direccion
                JOIN metodos_pago mp ON mp.id_metodo_pago = p.id_metodo_pago
                WHERE p.id_pedido = ?
                """ + condicionUsuario;
        List<PedidoDetalleDto> encontrados = idUsuario == null
                ? jdbc.query(sql, (rs, fila) -> mapDetalleCabecera(rs, idPedido), idPedido)
                : jdbc.query(sql, (rs, fila) -> mapDetalleCabecera(rs, idPedido), idPedido, idUsuario);
        if (encontrados.isEmpty()) return Optional.empty();
        PedidoDetalleDto cabecera = encontrados.get(0);
        List<PedidoItemDto> items = listarItemsPedido(idPedido);
        return Optional.of(new PedidoDetalleDto(cabecera.idPedido(), cabecera.idUsuario(), cabecera.usuario(),
                cabecera.fechaPedido(), cabecera.estado(), cabecera.total(), cabecera.direccionEnvio(), cabecera.metodoPago(), items));
    }

    private PedidoDetalleDto mapDetalleCabecera(java.sql.ResultSet rs, int fila) throws java.sql.SQLException {
        Timestamp fecha = rs.getTimestamp("fecha_pedido");
        DireccionDto direccion = new DireccionDto(rs.getInt("id_direccion"), rs.getInt("id_usuario"),
                rs.getString("calle"), rs.getString("numero"), rs.getString("colonia"), rs.getString("ciudad"),
                rs.getString("estado_direccion"), rs.getString("codigo_postal"), (Boolean) rs.getObject("direccion_principal"));
        MetodoPagoDto metodo = new MetodoPagoDto(rs.getInt("id_metodo_pago"), rs.getInt("id_usuario"),
                rs.getString("tipo_pago"), rs.getString("ultimos_4_digitos"), (Boolean) rs.getObject("pago_principal"));
        return new PedidoDetalleDto(rs.getInt("id_pedido"), rs.getInt("id_usuario"), rs.getString("nombre_usuario"),
                fecha == null ? null : fecha.toLocalDateTime(), rs.getString("estado"), rs.getBigDecimal("total"),
                direccion, metodo, List.of());
    }

    private List<PedidoItemDto> listarItemsPedido(int idPedido) {
        return jdbc.query("""
                SELECT pi.id_producto, p.nombre_producto, pi.cantidad, pi.precio_unitario,
                       (pi.precio_unitario * pi.cantidad)::numeric(12,2) AS subtotal
                FROM pedido_item pi LEFT JOIN productos p ON p.id_producto = pi.id_producto
                WHERE pi.id_pedido = ? ORDER BY p.nombre_producto NULLS LAST, pi.id_producto
                """, (rs, fila) -> new PedidoItemDto(
                rs.getInt("id_producto"), rs.getString("nombre_producto"), rs.getInt("cantidad"),
                rs.getBigDecimal("precio_unitario"), rs.getBigDecimal("subtotal")), idPedido);
    }

    public record LineaCompra(int idProducto, String nombreProducto, BigDecimal precio, Integer stock, int cantidad) { }
}
