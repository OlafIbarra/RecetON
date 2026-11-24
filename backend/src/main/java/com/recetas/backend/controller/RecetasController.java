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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import com.recetas.backend.dto.ActualizacionIngredienteReceta;
import com.recetas.backend.dto.DetalleRecetaDto;
import com.recetas.backend.dto.RecetaDto;
import com.recetas.backend.dto.SolicitudIngredienteReceta;
import com.recetas.backend.dto.SolicitudEquipoReceta;
import com.recetas.backend.dto.SolicitudInformacionNutricional;
import com.recetas.backend.dto.SolicitudValoracionReceta;
import com.recetas.backend.dto.SolicitudPasoReceta;
import com.recetas.backend.dto.SolicitudReceta;
import com.recetas.backend.service.ContenidoRecetaServicio;
import com.recetas.backend.service.DatosAdicionalesRecetaServicio;
import com.recetas.backend.service.RecetaServicio;

@RestController
@RequestMapping("/api/recetas")
public class RecetasController {

    private final RecetaServicio servicio;
    private final ContenidoRecetaServicio contenidoServicio;
    private final DatosAdicionalesRecetaServicio datosAdicionalesServicio;

    public RecetasController(RecetaServicio servicio,
            ContenidoRecetaServicio contenidoServicio,
            DatosAdicionalesRecetaServicio datosAdicionalesServicio) {
        this.servicio = servicio;
        this.contenidoServicio = contenidoServicio;
        this.datosAdicionalesServicio = datosAdicionalesServicio;
    }

    @GetMapping
    public List<RecetaDto> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public RecetaDto buscarPorId(@PathVariable int id) {
        return servicio.buscarPorId(id);
    }

    /** Devuelve la receta y sus datos relacionados desde form_recetas. */
    @GetMapping("/{id}/detalle")
    public DetalleRecetaDto buscarDetalle(@PathVariable int id) {
        return servicio.buscarDetalle(id);
    }

    @PostMapping
    public ResponseEntity<RecetaDto> crear(@RequestBody SolicitudReceta solicitud) {
        RecetaDto creada = servicio.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/recetas/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public RecetaDto actualizar(@PathVariable int id, @RequestBody SolicitudReceta solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------- INGREDIENTES DE UNA RECETA --------------------

    @PostMapping("/{id}/ingredientes")
    public DetalleRecetaDto agregarIngrediente(@PathVariable int id,
            @RequestBody SolicitudIngredienteReceta solicitud) {
        contenidoServicio.agregarIngrediente(id, solicitud);
        return servicio.buscarDetalle(id);
    }

    @PutMapping("/{id}/ingredientes/{idIngrediente}")
    public DetalleRecetaDto actualizarIngrediente(@PathVariable int id,
            @PathVariable int idIngrediente,
            @RequestBody ActualizacionIngredienteReceta solicitud) {
        contenidoServicio.actualizarIngrediente(id, idIngrediente, solicitud);
        return servicio.buscarDetalle(id);
    }

    @DeleteMapping("/{id}/ingredientes/{idIngrediente}")
    public DetalleRecetaDto eliminarIngrediente(@PathVariable int id, @PathVariable int idIngrediente) {
        contenidoServicio.eliminarIngrediente(id, idIngrediente);
        return servicio.buscarDetalle(id);
    }

    // -------------------- PASOS DE PREPARACIÓN --------------------

    @PostMapping("/{id}/pasos")
    public DetalleRecetaDto agregarPaso(@PathVariable int id, @RequestBody SolicitudPasoReceta solicitud) {
        contenidoServicio.agregarPaso(id, solicitud);
        return servicio.buscarDetalle(id);
    }

    @PutMapping("/{id}/pasos/{idPaso}")
    public DetalleRecetaDto actualizarPaso(@PathVariable int id, @PathVariable int idPaso,
            @RequestBody SolicitudPasoReceta solicitud) {
        contenidoServicio.actualizarPaso(id, idPaso, solicitud);
        return servicio.buscarDetalle(id);
    }

    @DeleteMapping("/{id}/pasos/{idPaso}")
    public DetalleRecetaDto eliminarPaso(@PathVariable int id, @PathVariable int idPaso) {
        contenidoServicio.eliminarPaso(id, idPaso);
        return servicio.buscarDetalle(id);
    }

    // -------------------- EQUIPO DE COCINA --------------------

    @PostMapping("/{id}/equipos")
    public DetalleRecetaDto agregarEquipo(@PathVariable int id,
            @RequestBody SolicitudEquipoReceta solicitud) {
        datosAdicionalesServicio.agregarEquipo(id, solicitud);
        return servicio.buscarDetalle(id);
    }

    @DeleteMapping("/{id}/equipos/{idEquipo}")
    public DetalleRecetaDto eliminarEquipo(@PathVariable int id, @PathVariable int idEquipo) {
        datosAdicionalesServicio.eliminarEquipo(id, idEquipo);
        return servicio.buscarDetalle(id);
    }

    // -------------------- INFORMACIÓN NUTRICIONAL --------------------

    @PutMapping("/{id}/informacion-nutricional")
    public DetalleRecetaDto guardarInformacionNutricional(@PathVariable int id,
            @RequestBody SolicitudInformacionNutricional solicitud) {
        datosAdicionalesServicio.guardarInformacionNutricional(id, solicitud);
        return servicio.buscarDetalle(id);
    }

    @DeleteMapping("/{id}/informacion-nutricional")
    public DetalleRecetaDto eliminarInformacionNutricional(@PathVariable int id) {
        datosAdicionalesServicio.eliminarInformacionNutricional(id);
        return servicio.buscarDetalle(id);
    }

    // -------------------- VALORACIONES --------------------

    @PostMapping("/{id}/valoraciones")
    public DetalleRecetaDto agregarValoracion(@PathVariable int id,
            @RequestBody SolicitudValoracionReceta solicitud,
            @RequestAttribute(name = "idUsuarioAutenticado") int idUsuarioAutenticado) {
        if (solicitud == null) {
            throw new com.recetas.backend.exception.SolicitudInvalidaException("Debes enviar la puntuación de la receta.");
        }
        // Se usa el ID derivado del token, nunca el ID que envíe el navegador.
        SolicitudValoracionReceta valoracionPropia = new SolicitudValoracionReceta(
                idUsuarioAutenticado, solicitud.puntuacion(), solicitud.comentario());
        datosAdicionalesServicio.agregarValoracion(id, valoracionPropia);
        return servicio.buscarDetalle(id);
    }

}
