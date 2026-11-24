package com.recetas.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recetas.backend.dto.CarritoDto;
import com.recetas.backend.dto.ProductoDto;
import com.recetas.backend.dto.SolicitudItemCarrito;
import com.recetas.backend.exception.RecursoNoEncontradoException;
import com.recetas.backend.exception.SolicitudInvalidaException;
import com.recetas.backend.repository.CarritoRepositorio;

@Service
public class CarritoServicio {

    private final CarritoRepositorio repositorio;
    private final UsuarioServicio usuarioServicio;
    private final ProductoServicio productoServicio;

    public CarritoServicio(CarritoRepositorio repositorio, UsuarioServicio usuarioServicio, ProductoServicio productoServicio) {
        this.repositorio = repositorio;
        this.usuarioServicio = usuarioServicio;
        this.productoServicio = productoServicio;
    }

    public CarritoDto obtener(int idUsuario) {
        validarUsuario(idUsuario);
        return repositorio.buscarIdCarrito(idUsuario)
                .map(idCarrito -> repositorio.construirDto(idCarrito, idUsuario))
                .orElseGet(() -> new CarritoDto(null, idUsuario, List.of(), BigDecimal.ZERO));
    }

    @Transactional
    public CarritoDto agregar(int idUsuario, SolicitudItemCarrito solicitud) {
        validarUsuario(idUsuario);
        if (solicitud == null || solicitud.idProducto() == null || solicitud.idProducto() < 1) {
            throw new SolicitudInvalidaException("Debes indicar un producto válido.");
        }
        validarCantidad(solicitud.cantidad());
        ProductoDto producto = productoServicio.buscarPorId(solicitud.idProducto());
        int idCarrito = repositorio.buscarIdCarrito(idUsuario).orElseGet(() -> repositorio.crear(idUsuario));
        Integer cantidadActual = repositorio.cantidadProducto(idCarrito, solicitud.idProducto());
        int nuevaCantidad = (cantidadActual == null ? 0 : cantidadActual) + solicitud.cantidad();
        validarStock(producto, nuevaCantidad);
        repositorio.guardarCantidad(idCarrito, solicitud.idProducto(), nuevaCantidad);
        return repositorio.construirDto(idCarrito, idUsuario);
    }

    @Transactional
    public CarritoDto actualizarCantidad(int idUsuario, int idProducto, Integer cantidad) {
        validarUsuario(idUsuario);
        validarCantidad(cantidad);
        int idCarrito = repositorio.buscarIdCarrito(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario todavía no tiene un carrito."));
        if (!repositorio.existeItem(idCarrito, idProducto)) {
            throw new RecursoNoEncontradoException("El producto no está en el carrito.");
        }
        ProductoDto producto = productoServicio.buscarPorId(idProducto);
        validarStock(producto, cantidad);
        repositorio.guardarCantidad(idCarrito, idProducto, cantidad);
        return repositorio.construirDto(idCarrito, idUsuario);
    }

    @Transactional
    public CarritoDto eliminarItem(int idUsuario, int idProducto) {
        validarUsuario(idUsuario);
        Integer idCarrito = repositorio.buscarIdCarrito(idUsuario).orElse(null);
        if (idCarrito != null) repositorio.eliminarItem(idCarrito, idProducto);
        return idCarrito == null
                ? new CarritoDto(null, idUsuario, List.of(), BigDecimal.ZERO)
                : repositorio.construirDto(idCarrito, idUsuario);
    }

    @Transactional
    public CarritoDto vaciar(int idUsuario) {
        validarUsuario(idUsuario);
        Integer idCarrito = repositorio.buscarIdCarrito(idUsuario).orElse(null);
        if (idCarrito == null) return new CarritoDto(null, idUsuario, List.of(), BigDecimal.ZERO);
        repositorio.vaciar(idCarrito);
        return repositorio.construirDto(idCarrito, idUsuario);
    }

    private void validarUsuario(int idUsuario) {
        if (idUsuario < 1) throw new SolicitudInvalidaException("El identificador del usuario debe ser positivo.");
        if (!Boolean.TRUE.equals(usuarioServicio.buscarPorId(idUsuario).active())) {
            throw new SolicitudInvalidaException("El usuario no está activo.");
        }
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new SolicitudInvalidaException("La cantidad debe ser un entero mayor que cero.");
        }
    }

    private void validarStock(ProductoDto producto, int cantidad) {
        if (producto.stock() == null || cantidad > producto.stock()) {
            throw new SolicitudInvalidaException("No hay stock suficiente. Stock disponible: " + producto.stock() + ".");
        }
    }
}
