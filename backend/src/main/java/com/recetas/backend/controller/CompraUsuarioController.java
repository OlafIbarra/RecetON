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

import com.recetas.backend.dto.DireccionDto;
import com.recetas.backend.dto.MetodoPagoDto;
import com.recetas.backend.dto.PedidoDetalleDto;
import com.recetas.backend.dto.PedidoResumenDto;
import com.recetas.backend.dto.SolicitudCrearPedido;
import com.recetas.backend.dto.SolicitudDireccion;
import com.recetas.backend.dto.SolicitudMetodoPago;
import com.recetas.backend.service.PedidoCompraServicio;

@RestController
@RequestMapping("/api/usuarios/{idUsuario}")
public class CompraUsuarioController {

    private final PedidoCompraServicio servicio;

    public CompraUsuarioController(PedidoCompraServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/direcciones")
    public List<DireccionDto> listarDirecciones(@PathVariable int idUsuario) {
        return servicio.listarDirecciones(idUsuario);
    }

    @PostMapping("/direcciones")
    public ResponseEntity<DireccionDto> crearDireccion(@PathVariable int idUsuario,
            @RequestBody SolicitudDireccion solicitud) {
        DireccionDto creada = servicio.crearDireccion(idUsuario, solicitud);
        return ResponseEntity.created(URI.create("/api/usuarios/" + idUsuario + "/direcciones/" + creada.idDireccion())).body(creada);
    }

    @PutMapping("/direcciones/{idDireccion}")
    public DireccionDto actualizarDireccion(@PathVariable int idUsuario, @PathVariable int idDireccion,
            @RequestBody SolicitudDireccion solicitud) {
        return servicio.actualizarDireccion(idUsuario, idDireccion, solicitud);
    }

    @DeleteMapping("/direcciones/{idDireccion}")
    public ResponseEntity<Void> eliminarDireccion(@PathVariable int idUsuario, @PathVariable int idDireccion) {
        servicio.eliminarDireccion(idUsuario, idDireccion);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metodos-pago")
    public List<MetodoPagoDto> listarMetodosPago(@PathVariable int idUsuario) {
        return servicio.listarMetodosPago(idUsuario);
    }

    @PostMapping("/metodos-pago")
    public ResponseEntity<MetodoPagoDto> crearMetodoPago(@PathVariable int idUsuario,
            @RequestBody SolicitudMetodoPago solicitud) {
        MetodoPagoDto creado = servicio.crearMetodoPago(idUsuario, solicitud);
        return ResponseEntity.created(URI.create("/api/usuarios/" + idUsuario + "/metodos-pago/" + creado.idMetodoPago())).body(creado);
    }

    @PutMapping("/metodos-pago/{idMetodoPago}")
    public MetodoPagoDto actualizarMetodoPago(@PathVariable int idUsuario, @PathVariable int idMetodoPago,
            @RequestBody SolicitudMetodoPago solicitud) {
        return servicio.actualizarMetodoPago(idUsuario, idMetodoPago, solicitud);
    }

    @DeleteMapping("/metodos-pago/{idMetodoPago}")
    public ResponseEntity<Void> eliminarMetodoPago(@PathVariable int idUsuario, @PathVariable int idMetodoPago) {
        servicio.eliminarMetodoPago(idUsuario, idMetodoPago);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pedidos")
    public List<PedidoResumenDto> listarPedidos(@PathVariable int idUsuario) {
        return servicio.listarPedidosUsuario(idUsuario);
    }

    @GetMapping("/pedidos/{idPedido}")
    public PedidoDetalleDto buscarPedido(@PathVariable int idUsuario, @PathVariable int idPedido) {
        return servicio.buscarPedidoUsuario(idUsuario, idPedido);
    }

    @PostMapping("/pedidos")
    public ResponseEntity<PedidoDetalleDto> crearPedido(@PathVariable int idUsuario,
            @RequestBody SolicitudCrearPedido solicitud) {
        PedidoDetalleDto pedido = servicio.crearPedido(idUsuario, solicitud);
        return ResponseEntity.created(URI.create("/api/usuarios/" + idUsuario + "/pedidos/" + pedido.idPedido())).body(pedido);
    }
}
