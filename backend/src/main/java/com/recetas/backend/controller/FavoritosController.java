package com.recetas.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recetas.backend.dto.EstadoFavoritoDto;
import com.recetas.backend.dto.RecetaDto;
import com.recetas.backend.service.FavoritosServicio;

@RestController
@RequestMapping("/api/usuarios/{idUsuario}/favoritos")
public class FavoritosController {

    private final FavoritosServicio servicio;

    public FavoritosController(FavoritosServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<RecetaDto> listar(@PathVariable int idUsuario) {
        return servicio.listarPorUsuario(idUsuario);
    }

    @GetMapping("/{idReceta}")
    public EstadoFavoritoDto consultarEstado(
            @PathVariable int idUsuario,
            @PathVariable int idReceta) {
        return servicio.consultarEstado(idUsuario, idReceta);
    }

    @PutMapping("/{idReceta}")
    public EstadoFavoritoDto agregar(
            @PathVariable int idUsuario,
            @PathVariable int idReceta) {
        return servicio.agregar(idUsuario, idReceta);
    }

    @DeleteMapping("/{idReceta}")
    public EstadoFavoritoDto eliminar(
            @PathVariable int idUsuario,
            @PathVariable int idReceta) {
        return servicio.eliminar(idUsuario, idReceta);
    }
}
