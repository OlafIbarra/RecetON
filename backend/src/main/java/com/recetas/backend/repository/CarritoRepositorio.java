package com.recetas.backend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.recetas.backend.dto.CarritoDto;
import com.recetas.backend.dto.ItemCarritoDto;

@Repository
public class CarritoRepositorio {

    private final JdbcTemplate jdbc;

    public CarritoRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Integer> buscarIdCarrito(int idUsuario) {
        List<Integer> ids = jdbc.query("""
                SELECT id_carrito FROM carritos
                WHERE id_usuario = ?
                ORDER BY fecha_creacion DESC NULLS LAST, id_carrito DESC
                LIMIT 1
                """, (rs, fila) -> rs.getInt("id_carrito"), idUsuario);
        return ids.stream().findFirst();
    }

    public int crear(int idUsuario) {
        Integer id = jdbc.queryForObject("SELECT nextval('seq_carritos')", Integer.class);
        jdbc.update("INSERT INTO carritos (id_carrito, id_usuario, fecha_creacion) VALUES (?, ?, CURRENT_TIMESTAMP)", id, idUsuario);
        return id;
    }

    public List<ItemCarritoDto> listarItems(int idCarrito) {
        return jdbc.query("""
                SELECT p.id_producto, p.nombre_producto, p.descripcion, p.precio, p.stock,
                       ci.cantidad, (p.precio * ci.cantidad)::numeric(12,2) AS subtotal,
                       p.id_receta, r.titulo AS nombre_receta, img.url_imagen AS imagen
                FROM carrito_item ci
                JOIN productos p ON p.id_producto = ci.id_producto
                LEFT JOIN recetas r ON r.id_receta = p.id_receta
                LEFT JOIN LATERAL (
                    SELECT ir.url_imagen FROM imagenes_receta ir
                    WHERE ir.id_receta = p.id_receta
                    ORDER BY ir.es_portada DESC NULLS LAST, ir.id_imagen DESC
                    LIMIT 1
                ) img ON TRUE
                WHERE ci.id_carrito = ?
                ORDER BY p.nombre_producto, p.id_producto
                """, (rs, fila) -> new ItemCarritoDto(
                rs.getInt("id_producto"), rs.getString("nombre_producto"), rs.getString("descripcion"),
                rs.getBigDecimal("precio"), (Integer) rs.getObject("stock"), rs.getInt("cantidad"),
                rs.getBigDecimal("subtotal"), (Integer) rs.getObject("id_receta"),
                rs.getString("nombre_receta"), rs.getString("imagen")), idCarrito);
    }

    public Integer cantidadProducto(int idCarrito, int idProducto) {
        List<Integer> cantidades = jdbc.query("SELECT cantidad FROM carrito_item WHERE id_carrito = ? AND id_producto = ?",
                (rs, fila) -> rs.getInt("cantidad"), idCarrito, idProducto);
        return cantidades.isEmpty() ? null : cantidades.get(0);
    }

    public void guardarCantidad(int idCarrito, int idProducto, int cantidad) {
        Integer existente = cantidadProducto(idCarrito, idProducto);
        if (existente == null) {
            jdbc.update("INSERT INTO carrito_item (id_carrito, id_producto, cantidad) VALUES (?, ?, ?)", idCarrito, idProducto, cantidad);
        } else {
            jdbc.update("UPDATE carrito_item SET cantidad = ? WHERE id_carrito = ? AND id_producto = ?", cantidad, idCarrito, idProducto);
        }
    }

    public boolean existeItem(int idCarrito, int idProducto) {
        Boolean existe = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM carrito_item WHERE id_carrito = ? AND id_producto = ?)",
                Boolean.class, idCarrito, idProducto);
        return Boolean.TRUE.equals(existe);
    }

    public void eliminarItem(int idCarrito, int idProducto) {
        jdbc.update("DELETE FROM carrito_item WHERE id_carrito = ? AND id_producto = ?", idCarrito, idProducto);
    }

    public void vaciar(int idCarrito) {
        jdbc.update("DELETE FROM carrito_item WHERE id_carrito = ?", idCarrito);
    }

    public BigDecimal subtotal(int idCarrito) {
        BigDecimal total = jdbc.queryForObject("""
                SELECT COALESCE(SUM(p.precio * ci.cantidad), 0)::numeric(12,2)
                FROM carrito_item ci JOIN productos p ON p.id_producto = ci.id_producto
                WHERE ci.id_carrito = ?
                """, BigDecimal.class, idCarrito);
        return total == null ? BigDecimal.ZERO : total;
    }

    public CarritoDto construirDto(int idCarrito, int idUsuario) {
        return new CarritoDto(idCarrito, idUsuario, listarItems(idCarrito), subtotal(idCarrito));
    }
}
