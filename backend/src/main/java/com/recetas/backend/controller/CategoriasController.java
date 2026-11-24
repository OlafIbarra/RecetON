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

import com.recetas.backend.dto.CategoriaDto;
import com.recetas.backend.dto.SolicitudCategoria;
import com.recetas.backend.service.CategoriaServicio;

@RestController
@RequestMapping("/api/categorias")
public class CategoriasController {

    private final CategoriaServicio servicio;

    public CategoriasController(CategoriaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<CategoriaDto> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public CategoriaDto buscarPorId(@PathVariable int id) {
        return servicio.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<CategoriaDto> crear(@RequestBody SolicitudCategoria solicitud) {
        CategoriaDto creada = servicio.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/categorias/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public CategoriaDto actualizar(@PathVariable int id, @RequestBody SolicitudCategoria solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
