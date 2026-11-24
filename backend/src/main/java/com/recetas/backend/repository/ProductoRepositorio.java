package com.recetas.backend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.recetas.backend.dto.ProductoDto;
import com.recetas.backend.dto.SolicitudProducto;

@Repository
public class ProductoRepositorio {

    private static final String CONSULTA_BASE = """
            SELECT p.id_producto,
                   p.id_receta,
                   p.nombre_producto,
                   p.descripcion,
                   p.precio,
                   p.stock,
                   r.titulo AS nombre_receta,
                   img.url_imagen AS imagen
            FROM productos p
            LEFT JOIN recetas r ON r.id_receta = p.id_receta
            LEFT JOIN LATERAL (
                SELECT ir.url_imagen
                FROM imagenes_receta ir
                WHERE ir.id_receta = p.id_receta
                ORDER BY ir.es_portada DESC NULLS LAST, ir.id_imagen DESC
                LIMIT 1
            ) img ON TRUE
            """;

    private final JdbcTemplate jdbc;

    public ProductoRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ProductoDto> listar() {
        return jdbc.query(CONSULTA_BASE + " ORDER BY p.nombre_producto, p.id_producto", this::mapear);
    }

    public Optional<ProductoDto> buscarPorId(int id) {
        List<ProductoDto> filas = jdbc.query(CONSULTA_BASE + " WHERE p.id_producto = ?", this::mapear, id);
        return filas.stream().findFirst();
    }

    public int insertar(SolicitudProducto solicitud) {
        Integer id = jdbc.queryForObject("SELECT nextval('seq_productos')", Integer.class);
        jdbc.update("""
                INSERT INTO productos (id_producto, id_receta, nombre_producto, descripcion, precio, stock)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, solicitud.idReceta(), solicitud.nombreProducto().trim(), solicitud.descripcion(),
                solicitud.precio(), solicitud.stock());
        return id;
    }

    public boolean actualizar(int id, SolicitudProducto solicitud) {
        return jdbc.update("""
                UPDATE productos
                SET id_receta = ?, nombre_producto = ?, descripcion = ?, precio = ?, stock = ?
                WHERE id_producto = ?
                """, solicitud.idReceta(), solicitud.nombreProducto().trim(), solicitud.descripcion(),
                solicitud.precio(), solicitud.stock(), id) > 0;
    }

    public boolean estaEnCarrito(int id) {
        Boolean valor = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM carrito_item WHERE id_producto = ?)", Boolean.class, id);
        return Boolean.TRUE.equals(valor);
    }

    public boolean estaEnPedidos(int id) {
        Boolean valor = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pedido_item WHERE id_producto = ?)", Boolean.class, id);
        return Boolean.TRUE.equals(valor);
    }

    public boolean eliminar(int id) {
        return jdbc.update("DELETE FROM productos WHERE id_producto = ?", id) > 0;
    }

    private ProductoDto mapear(java.sql.ResultSet rs, int fila) throws java.sql.SQLException {
        return new ProductoDto(
                rs.getInt("id_producto"),
                (Integer) rs.getObject("id_receta"),
                rs.getString("nombre_producto"),
                rs.getString("descripcion"),
                rs.getBigDecimal("precio"),
                (Integer) rs.getObject("stock"),
                rs.getString("nombre_receta"),
                rs.getString("imagen"));
    }
}
