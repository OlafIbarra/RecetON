import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { TiendaApiServicio } from '../../servicios/tienda-api.servicio';
import { HistorialNavegacionServicio } from '../../servicios/historial-navegacion.servicio';
import { Carrito, ItemCarrito } from '../../interfaces/tienda.interface';

@Component({
  selector: 'app-pagina-carrito',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pagina-carrito.component.html',
  styleUrl: './pagina-carrito.component.css',
})
export class PaginaCarritoComponent implements OnInit {
  private readonly tienda: TiendaApiServicio = inject(TiendaApiServicio);
  private readonly autenticacion: AutenticacionServicio = inject(AutenticacionServicio);
  private readonly router: Router = inject(Router);
  private readonly historial = inject(HistorialNavegacionServicio);

  carrito: Carrito | null = null;
  cantidades: Record<number, number> = {};
  cargando: boolean = true;
  error: string | null = null;
  mensaje: string | null = null;
  ocupado: boolean = false;

  ngOnInit(): void { this.cargarCarrito(); }

  cargarCarrito(): void {
    const idUsuario = this.autenticacion.obtenerUsuarioActual()?.id;
    if (idUsuario === undefined) {
      this.error = 'No se encontró la sesión. Inicia sesión nuevamente.';
      this.cargando = false;
      return;
    }
    this.cargando = true;
    this.tienda.obtenerCarrito(idUsuario).subscribe({
      next: (carrito: Carrito) => {
        this.carrito = carrito;
        for (const item of carrito.items) this.cantidades[item.idProducto] = item.cantidad;
        this.cargando = false;
      },
      error: (error: HttpErrorResponse) => {
        this.error = this.mensajeError(error, 'No se pudo cargar el carrito.');
        this.cargando = false;
      },
    });
  }

  actualizarCantidad(item: ItemCarrito): void {
    const idUsuario = this.autenticacion.obtenerUsuarioActual()?.id;
    const cantidad = this.cantidades[item.idProducto];
    if (idUsuario === undefined) return;
    if (!Number.isInteger(cantidad) || cantidad < 1) {
      this.error = 'La cantidad debe ser un entero mayor que cero.';
      return;
    }
    this.ocupado = true;
    this.error = null;
    this.tienda.actualizarCantidadCarrito(idUsuario, item.idProducto, cantidad).subscribe({
      next: (carrito: Carrito) => { this.carrito = carrito; this.ocupado = false; this.mensaje = 'Cantidad actualizada.'; },
      error: (error: HttpErrorResponse) => { this.error = this.mensajeError(error, 'No se pudo actualizar la cantidad.'); this.ocupado = false; },
    });
  }

  quitar(item: ItemCarrito): void {
    const idUsuario = this.autenticacion.obtenerUsuarioActual()?.id;
    if (idUsuario === undefined) return;
    this.ocupado = true;
    this.error = null;
    this.tienda.quitarDelCarrito(idUsuario, item.idProducto).subscribe({
      next: (carrito: Carrito) => { this.carrito = carrito; this.ocupado = false; this.mensaje = `Se quitó ${item.nombreProducto} del carrito.`; },
      error: (error: HttpErrorResponse) => { this.error = this.mensajeError(error, 'No se pudo quitar el producto.'); this.ocupado = false; },
    });
  }

  vaciar(): void {
    const idUsuario = this.autenticacion.obtenerUsuarioActual()?.id;
    if (idUsuario === undefined || !this.carrito?.items.length) return;
    this.ocupado = true;
    this.error = null;
    this.tienda.vaciarCarrito(idUsuario).subscribe({
      next: (carrito: Carrito) => { this.carrito = carrito; this.ocupado = false; this.mensaje = 'El carrito quedó vacío.'; },
      error: (error: HttpErrorResponse) => { this.error = this.mensajeError(error, 'No se pudo vaciar el carrito.'); this.ocupado = false; },
    });
  }

  formatoPrecio(precio: number): string {
    return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(precio);
  }

  regresarPantallaAnterior(): void { void this.historial.regresar(); }

  continuarTienda(): void { this.router.navigate(['/productos']); }

  private mensajeError(error: HttpErrorResponse, alternativo: string): string {
    const cuerpo = error.error as { mensaje?: string } | null;
    return cuerpo?.mensaje ?? (error.status === 0 ? 'No se pudo conectar con Spring Boot.' : alternativo);
  }
}
