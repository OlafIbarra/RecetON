package com.recetas.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recetas.backend.dto.ProductoDto;
import com.recetas.backend.dto.SolicitudProducto;
import com.recetas.backend.service.ProductoServicio;

@RestController
@RequestMapping("/api/productos")
public class ProductosController {

    private final ProductoServicio servicio;

    public ProductosController(ProductoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ProductoDto> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public ProductoDto buscarPorId(@PathVariable int id) {
        return servicio.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ProductoDto> crear(@RequestBody SolicitudProducto solicitud) {
        ProductoDto creado = servicio.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/productos/" + creado.idProducto())).body(creado);
    }

    @PutMapping("/{id}")
    public ProductoDto actualizar(@PathVariable int id, @RequestBody SolicitudProducto solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
