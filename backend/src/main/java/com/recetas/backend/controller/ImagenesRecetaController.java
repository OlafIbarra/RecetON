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

import com.recetas.backend.dto.ImagenRecetaDto;
import com.recetas.backend.dto.SolicitudImagenReceta;
import com.recetas.backend.service.ImagenRecetaServicio;

@RestController
@RequestMapping("/api/recetas/{idReceta}/imagenes")
public class ImagenesRecetaController {

    private final ImagenRecetaServicio servicio;

    public ImagenesRecetaController(ImagenRecetaServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ImagenRecetaDto> listar(@PathVariable int idReceta) {
        return servicio.listar(idReceta);
    }

    @PostMapping
    public ResponseEntity<ImagenRecetaDto> agregar(
            @PathVariable int idReceta,
            @RequestBody SolicitudImagenReceta solicitud) {
        ImagenRecetaDto creada = servicio.agregar(idReceta, solicitud);
        return ResponseEntity.created(URI.create(
                "/api/recetas/" + idReceta + "/imagenes/" + creada.idImagen())).body(creada);
    }

    @PutMapping("/{idImagen}/portada")
    public List<ImagenRecetaDto> establecerPortada(
            @PathVariable int idReceta,
            @PathVariable int idImagen) {
        return servicio.establecerPortada(idReceta, idImagen);
    }

    @DeleteMapping("/{idImagen}")
    public ResponseEntity<List<ImagenRecetaDto>> eliminar(
            @PathVariable int idReceta,
            @PathVariable int idImagen) {
        return ResponseEntity.ok(servicio.eliminar(idReceta, idImagen));
    }
}
