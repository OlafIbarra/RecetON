package com.recetas.backend.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recetas.backend.dto.ActualizacionCantidadCarrito;
import com.recetas.backend.dto.CarritoDto;
import com.recetas.backend.dto.SolicitudItemCarrito;
import com.recetas.backend.service.CarritoServicio;

@RestController
@RequestMapping("/api/usuarios/{idUsuario}/carrito")
public class CarritoController {

    private final CarritoServicio servicio;

    public CarritoController(CarritoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public CarritoDto obtener(@PathVariable int idUsuario) {
        return servicio.obtener(idUsuario);
    }

    @PostMapping("/items")
    public CarritoDto agregar(@PathVariable int idUsuario, @RequestBody SolicitudItemCarrito solicitud) {
        return servicio.agregar(idUsuario, solicitud);
    }

    @PutMapping("/items/{idProducto}")
    public CarritoDto actualizarCantidad(@PathVariable int idUsuario, @PathVariable int idProducto,
            @RequestBody ActualizacionCantidadCarrito solicitud) {
        return servicio.actualizarCantidad(idUsuario, idProducto, solicitud == null ? null : solicitud.cantidad());
    }

    @DeleteMapping("/items/{idProducto}")
    public CarritoDto eliminarItem(@PathVariable int idUsuario, @PathVariable int idProducto) {
        return servicio.eliminarItem(idUsuario, idProducto);
    }

    @DeleteMapping
    public CarritoDto vaciar(@PathVariable int idUsuario) {
        return servicio.vaciar(idUsuario);
    }
}
