package com.recetas.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recetas.backend.dto.EstadoPedidoSolicitud;
import com.recetas.backend.dto.PedidoDetalleDto;
import com.recetas.backend.dto.PedidoResumenDto;
import com.recetas.backend.service.PedidoCompraServicio;

@RestController
@RequestMapping("/api/administracion/pedidos")
public class PedidoAdministracionController {

    private final PedidoCompraServicio servicio;

    public PedidoAdministracionController(PedidoCompraServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<PedidoResumenDto> listarTodos() {
        return servicio.listarTodosLosPedidos();
    }

    @GetMapping("/{idPedido}")
    public PedidoDetalleDto buscar(@PathVariable int idPedido) {
        return servicio.buscarPedidoAdministracion(idPedido);
    }

    @PutMapping("/{idPedido}/estado")
    public PedidoDetalleDto actualizarEstado(@PathVariable int idPedido, @RequestBody EstadoPedidoSolicitud solicitud) {
        return servicio.actualizarEstadoPedido(idPedido, solicitud);
    }
}
