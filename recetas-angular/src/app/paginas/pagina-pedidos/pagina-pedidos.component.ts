import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { PedidosApiServicio } from '../../servicios/pedidos-api.servicio';
import { HistorialNavegacionServicio } from '../../servicios/historial-navegacion.servicio';
import { PedidoDetalle, PedidoResumen } from '../../interfaces/pedidos.interface';

@Component({
  selector: 'app-pagina-pedidos',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './pagina-pedidos.component.html',
  styleUrl: './pagina-pedidos.component.css',
})
export class PaginaPedidosComponent implements OnInit {
  private readonly api = inject(PedidosApiServicio);
  private readonly autenticacion = inject(AutenticacionServicio);
  private readonly historial = inject(HistorialNavegacionServicio);
  idUsuario: number | null = null;
  pedidos: PedidoResumen[] = [];
  detalle: PedidoDetalle | null = null;
  cargando = true;
  cargandoDetalle = false;
  error: string | null = null;
  mensaje: string | null = null;

  ngOnInit(): void {
    this.idUsuario = this.autenticacion.obtenerUsuarioActual()?.id ?? null;
    const estado = history.state as { mensajePedido?: string };
    this.mensaje = estado?.mensajePedido ?? null;
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    if (this.idUsuario === null) { this.error = 'No se encontró tu sesión.'; this.cargando = false; return; }
    this.cargando = true;
    this.api.listarPedidosUsuario(this.idUsuario).subscribe({
      next: (datos) => { this.pedidos = datos; this.cargando = false; },
      error: (e: HttpErrorResponse) => { this.error = this.mensajeError(e); this.cargando = false; },
    });
  }

  verDetalle(pedido: PedidoResumen): void {
    if (this.idUsuario === null) return;
    if (this.detalle?.idPedido === pedido.idPedido) { this.detalle = null; return; }
    this.cargandoDetalle = true;
    this.error = null;
    this.api.obtenerPedidoUsuario(this.idUsuario, pedido.idPedido).subscribe({
      next: (detalle) => { this.detalle = detalle; this.cargandoDetalle = false; },
      error: (e: HttpErrorResponse) => { this.error = this.mensajeError(e); this.cargandoDetalle = false; },
    });
  }

  regresarPantallaAnterior(): void { void this.historial.regresar(); }

  formatoPrecio(precio: number): string { return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(precio); }
  claseEstado(estado: string): string { return estado.toLowerCase().replaceAll(' ', '-'); }
  private mensajeError(error: HttpErrorResponse): string {
    const cuerpo = error.error as { mensaje?: string } | null;
    return cuerpo?.mensaje ?? (error.status === 0 ? 'No se pudo conectar con Spring Boot.' : 'No se pudo cargar el historial.');
  }
}
