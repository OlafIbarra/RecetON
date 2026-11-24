import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { PedidosApiServicio } from '../../servicios/pedidos-api.servicio';
import { PedidoDetalle, PedidoResumen } from '../../interfaces/pedidos.interface';

@Component({
  selector: 'app-pagina-administracion-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pagina-administracion-pedidos.component.html',
  styleUrl: './pagina-administracion-pedidos.component.css',
})
export class PaginaAdministracionPedidosComponent implements OnInit {
  private readonly api = inject(PedidosApiServicio);
  private readonly autenticacion = inject(AutenticacionServicio);
  private readonly router = inject(Router);

  pedidos: PedidoResumen[] = [];
  detalle: PedidoDetalle | null = null;
  estadosSeleccionados: Record<number, string> = {};
  cargando = true;
  guardandoId: number | null = null;
  error: string | null = null;
  mensaje: string | null = null;

  ngOnInit(): void {
    if (!this.autenticacion.esAdministrador()) {
      void this.router.navigate(['/inicio']);
      return;
    }
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = null;
    this.api.listarPedidosAdministracion().subscribe({
      next: (datos) => {
        this.pedidos = datos;
        this.estadosSeleccionados = {};
        for (const pedido of datos) this.estadosSeleccionados[pedido.idPedido] = pedido.estado;
        this.cargando = false;
      },
      error: (e: HttpErrorResponse) => { this.error = this.mensajeError(e, 'No se pudieron cargar los pedidos.'); this.cargando = false; },
    });
  }

  opcionesEstado(estado: string): string[] {
    switch (estado) {
      case 'Registrado': return ['En preparación', 'Cancelado'];
      case 'En preparación': return ['Enviado', 'Cancelado'];
      case 'Enviado': return ['Entregado'];
      default: return [];
    }
  }

  actualizarEstado(pedido: PedidoResumen): void {
    const nuevo = this.estadosSeleccionados[pedido.idPedido];
    if (!nuevo || nuevo === pedido.estado || this.guardandoId !== null) return;
    if (!window.confirm(`¿Cambiar el pedido #${pedido.idPedido} a “${nuevo}”?`)) {
      this.estadosSeleccionados[pedido.idPedido] = pedido.estado;
      return;
    }
    this.guardandoId = pedido.idPedido;
    this.error = null;
    this.mensaje = null;
    this.api.actualizarEstadoPedido(pedido.idPedido, { estado: nuevo }).subscribe({
      next: (actualizado) => {
        this.guardandoId = null;
        this.mensaje = `El pedido #${actualizado.idPedido} ahora está en estado “${actualizado.estado}”.`;
        this.cargar();
        if (this.detalle?.idPedido === actualizado.idPedido) this.detalle = actualizado;
      },
      error: (e: HttpErrorResponse) => {
        this.guardandoId = null;
        this.estadosSeleccionados[pedido.idPedido] = pedido.estado;
        this.error = this.mensajeError(e, 'No se pudo actualizar el estado del pedido.');
      },
    });
  }

  verDetalle(pedido: PedidoResumen): void {
    if (this.detalle?.idPedido === pedido.idPedido) { this.detalle = null; return; }
    this.api.obtenerPedidoAdministracion(pedido.idPedido).subscribe({
      next: (detalle) => this.detalle = detalle,
      error: (e: HttpErrorResponse) => this.error = this.mensajeError(e, 'No se pudo cargar el detalle del pedido.'),
    });
  }

  formatoPrecio(precio: number): string { return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(precio); }
  private mensajeError(error: HttpErrorResponse, alternativo: string): string {
    const cuerpo = error.error as { mensaje?: string } | null;
    if (cuerpo?.mensaje) return cuerpo.mensaje;
    if (error.status === 401) return 'Tu sesión venció. Inicia sesión nuevamente.';
    if (error.status === 403) return 'Esta sección requiere el rol Administrador.';
    return error.status === 0 ? 'No se pudo conectar con Spring Boot.' : alternativo;
  }
}
