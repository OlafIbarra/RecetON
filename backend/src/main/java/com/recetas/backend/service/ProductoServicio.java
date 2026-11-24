package com.recetas.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recetas.backend.dto.ProductoDto;
import com.recetas.backend.dto.SolicitudProducto;
import com.recetas.backend.exception.ConflictoDatosException;
import com.recetas.backend.exception.RecursoNoEncontradoException;
import com.recetas.backend.exception.SolicitudInvalidaException;
import com.recetas.backend.repository.ProductoRepositorio;

@Service
public class ProductoServicio {

    private final ProductoRepositorio repositorio;
    private final RecetaServicio recetaServicio;

    public ProductoServicio(ProductoRepositorio repositorio, RecetaServicio recetaServicio) {
        this.repositorio = repositorio;
        this.recetaServicio = recetaServicio;
    }

    public List<ProductoDto> listar() {
        return repositorio.listar();
    }

    public ProductoDto buscarPorId(int id) {
        if (id < 1) throw new SolicitudInvalidaException("El identificador del producto debe ser positivo.");
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto con ID " + id + "."));
    }

    @Transactional
    public ProductoDto crear(SolicitudProducto solicitud) {
        validar(solicitud);
        if (solicitud.idReceta() != null) recetaServicio.buscarPorId(solicitud.idReceta());
        int id = repositorio.insertar(solicitud);
        return buscarPorId(id);
    }

    @Transactional
    public ProductoDto actualizar(int id, SolicitudProducto solicitud) {
        buscarPorId(id);
        validar(solicitud);
        if (solicitud.idReceta() != null) recetaServicio.buscarPorId(solicitud.idReceta());
        if (!repositorio.actualizar(id, solicitud)) {
            throw new RecursoNoEncontradoException("No se encontró el producto con ID " + id + ".");
        }
        return buscarPorId(id);
    }

    @Transactional
    public void eliminar(int id) {
        buscarPorId(id);
        if (repositorio.estaEnCarrito(id)) {
            throw new ConflictoDatosException("No se puede eliminar el producto porque está incluido en uno o más carritos. Retíralo de los carritos primero.");
        }
        if (repositorio.estaEnPedidos(id)) {
            throw new ConflictoDatosException("No se puede eliminar el producto porque forma parte del historial de pedidos.");
        }
        repositorio.eliminar(id);
    }

    private void validar(SolicitudProducto solicitud) {
        if (solicitud == null) throw new SolicitudInvalidaException("Debes enviar los datos del producto.");
        if (solicitud.nombreProducto() == null || solicitud.nombreProducto().isBlank()) {
            throw new SolicitudInvalidaException("El nombre del producto es obligatorio.");
        }
        if (solicitud.precio() == null || solicitud.precio().compareTo(BigDecimal.ZERO) < 0) {
            throw new SolicitudInvalidaException("El precio debe ser un número mayor o igual a cero.");
        }
        if (solicitud.stock() == null || solicitud.stock() < 0) {
            throw new SolicitudInvalidaException("El stock debe ser un entero mayor o igual a cero.");
        }
    }
}
