import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { PedidosApiServicio } from '../../servicios/pedidos-api.servicio';
import { TiendaApiServicio } from '../../servicios/tienda-api.servicio';
import { Carrito } from '../../interfaces/tienda.interface';
import { DireccionEnvio, MetodoPagoGuardado, SolicitudDireccion, SolicitudMetodoPago } from '../../interfaces/pedidos.interface';

@Component({
  selector: 'app-pagina-finalizar-compra',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pagina-finalizar-compra.component.html',
  styleUrl: './pagina-finalizar-compra.component.css',
})
export class PaginaFinalizarCompraComponent implements OnInit {
  private readonly api = inject(PedidosApiServicio);
  private readonly tienda = inject(TiendaApiServicio);
  private readonly autenticacion = inject(AutenticacionServicio);
  private readonly router = inject(Router);

  idUsuario: number | null = null;
  carrito: Carrito | null = null;
  direcciones: DireccionEnvio[] = [];
  metodosPago: MetodoPagoGuardado[] = [];
  idDireccionSeleccionada: number | null = null;
  idMetodoSeleccionado: number | null = null;
  cargando = true;
  guardando = false;
  error: string | null = null;
  mensaje: string | null = null;
  direccionEditandoId: number | null = null;
  metodoEditandoId: number | null = null;
  mostrarFormularioDireccion = false;
  mostrarFormularioMetodo = false;

  formularioDireccion: SolicitudDireccion = this.direccionVacia();
  formularioMetodo: SolicitudMetodoPago = this.metodoVacio();

  ngOnInit(): void {
    this.idUsuario = this.autenticacion.obtenerUsuarioActual()?.id ?? null;
    if (this.idUsuario === null) {
      this.error = 'Tu sesión no está disponible. Inicia sesión nuevamente.';
      this.cargando = false;
      return;
    }
    this.cargarDatos();
  }

  cargarDatos(): void {
    if (this.idUsuario === null) return;
    this.cargando = true;
    this.error = null;
    forkJoin({
      carrito: this.tienda.obtenerCarrito(this.idUsuario),
      direcciones: this.api.listarDirecciones(this.idUsuario),
      metodos: this.api.listarMetodosPago(this.idUsuario),
    }).subscribe({
      next: (datos) => {
        this.carrito = datos.carrito;
        this.direcciones = datos.direcciones;
        this.metodosPago = datos.metodos;
        this.idDireccionSeleccionada = this.direcciones.find((d) => d.esPrincipal)?.idDireccion ?? this.direcciones[0]?.idDireccion ?? null;
        this.idMetodoSeleccionado = this.metodosPago.find((m) => m.esPrincipal)?.idMetodoPago ?? this.metodosPago[0]?.idMetodoPago ?? null;
        this.cargando = false;
      },
      error: (e: HttpErrorResponse) => {
        this.error = this.mensajeError(e, 'No pudimos cargar el resumen de tu compra.');
        this.cargando = false;
      },
    });
  }

  nuevoFormularioDireccion(): void {
    this.direccionEditandoId = null;
    this.formularioDireccion = this.direccionVacia();
    this.mostrarFormularioDireccion = true;
    this.error = null;
  }

  editarDireccion(direccion: DireccionEnvio): void {
    this.direccionEditandoId = direccion.idDireccion;
    this.formularioDireccion = {
      calle: direccion.calle, numero: direccion.numero, colonia: direccion.colonia,
      ciudad: direccion.ciudad, estado: direccion.estado, codigoPostal: direccion.codigoPostal,
      esPrincipal: direccion.esPrincipal,
    };
    this.mostrarFormularioDireccion = true;
    this.error = null;
  }

  guardarDireccion(): void {
    if (this.idUsuario === null || this.guardando) return;
    const solicitud: SolicitudDireccion = { ...this.formularioDireccion, calle: this.formularioDireccion.calle.trim(), numero: this.formularioDireccion.numero.trim(), colonia: this.formularioDireccion.colonia.trim(), ciudad: this.formularioDireccion.ciudad.trim(), estado: this.formularioDireccion.estado.trim(), codigoPostal: this.formularioDireccion.codigoPostal.trim() };
    this.guardando = true;
    this.error = null;
    const peticion = this.direccionEditandoId === null
      ? this.api.crearDireccion(this.idUsuario, solicitud)
      : this.api.actualizarDireccion(this.idUsuario, this.direccionEditandoId, solicitud);
    peticion.subscribe({
      next: (direccion) => {
        this.guardando = false;
        this.idDireccionSeleccionada = direccion.idDireccion;
        this.mostrarFormularioDireccion = false;
        this.mensaje = 'Dirección guardada.';
        this.recargarDirecciones();
      },
      error: (e: HttpErrorResponse) => { this.guardando = false; this.error = this.mensajeError(e, 'No se pudo guardar la dirección.'); },
    });
  }

  eliminarDireccion(direccion: DireccionEnvio): void {
    if (this.idUsuario === null || this.guardando) return;
    if (!window.confirm('¿Eliminar esta dirección? Si está vinculada a un pedido anterior, se conservará para el historial.')) return;
    this.guardando = true;
    this.error = null;
    this.api.eliminarDireccion(this.idUsuario, direccion.idDireccion).subscribe({
      next: () => { this.guardando = false; this.mensaje = 'Dirección eliminada.'; this.recargarDirecciones(); },
      error: (e: HttpErrorResponse) => { this.guardando = false; this.error = this.mensajeError(e, 'No se pudo eliminar la dirección.'); },
    });
  }

  nuevoFormularioMetodo(): void {
    this.metodoEditandoId = null;
    this.formularioMetodo = this.metodoVacio();
    this.mostrarFormularioMetodo = true;
    this.error = null;
  }

  editarMetodo(metodo: MetodoPagoGuardado): void {
    this.metodoEditandoId = metodo.idMetodoPago;
    this.formularioMetodo = { tipo: metodo.tipo, ultimos4Digitos: metodo.ultimos4Digitos ?? '', esPrincipal: metodo.esPrincipal };
    this.mostrarFormularioMetodo = true;
    this.error = null;
  }

  guardarMetodo(): void {
    if (this.idUsuario === null || this.guardando) return;
    const tarjeta = this.formularioMetodo.tipo.startsWith('Tarjeta');
    const solicitud: SolicitudMetodoPago = {
      ...this.formularioMetodo,
      tipo: this.formularioMetodo.tipo,
      ultimos4Digitos: tarjeta ? (this.formularioMetodo.ultimos4Digitos ?? '').trim() : null,
    };
    this.guardando = true;
    this.error = null;
    const peticion = this.metodoEditandoId === null
      ? this.api.crearMetodoPago(this.idUsuario, solicitud)
      : this.api.actualizarMetodoPago(this.idUsuario, this.metodoEditandoId, solicitud);
    peticion.subscribe({
      next: (metodo) => {
        this.guardando = false;
        this.idMetodoSeleccionado = metodo.idMetodoPago;
        this.mostrarFormularioMetodo = false;
        this.mensaje = 'Método guardado.';
        this.recargarMetodos();
      },
      error: (e: HttpErrorResponse) => { this.guardando = false; this.error = this.mensajeError(e, 'No se pudo guardar el método.'); },
    });
  }

  eliminarMetodo(metodo: MetodoPagoGuardado): void {
    if (this.idUsuario === null || this.guardando) return;
    if (!window.confirm('¿Eliminar este método? Si está vinculado a un pedido anterior, se conservará para el historial.')) return;
    this.guardando = true;
    this.error = null;
    this.api.eliminarMetodoPago(this.idUsuario, metodo.idMetodoPago).subscribe({
      next: () => { this.guardando = false; this.mensaje = 'Método eliminado.'; this.recargarMetodos(); },
      error: (e: HttpErrorResponse) => { this.guardando = false; this.error = this.mensajeError(e, 'No se pudo eliminar el método.'); },
    });
  }

  confirmarPedido(): void {
    if (this.idUsuario === null || this.guardando) return;
    if (!this.carrito?.items.length) { this.error = 'Tu carrito está vacío.'; return; }
    if (this.idDireccionSeleccionada === null || this.idMetodoSeleccionado === null) {
      this.error = 'Agrega y selecciona una dirección y un método de pago para continuar.';
      return;
    }
    if (!window.confirm('¿Confirmar el pedido? RecetON registrará la compra de prueba; no se realizará ningún cobro real.')) return;
    this.guardando = true;
    this.error = null;
    this.api.crearPedido(this.idUsuario, { idDireccion: this.idDireccionSeleccionada, idMetodoPago: this.idMetodoSeleccionado }).subscribe({
      next: (pedido) => {
        this.guardando = false;
        void this.router.navigate(['/mis-pedidos'], { state: { mensajePedido: `Pedido #${pedido.idPedido} registrado correctamente.` } });
      },
      error: (e: HttpErrorResponse) => { this.guardando = false; this.error = this.mensajeError(e, 'No se pudo registrar el pedido.'); },
    });
  }

  formatoPrecio(precio: number): string {
    return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(precio);
  }

  esTarjeta(): boolean { return this.formularioMetodo.tipo.startsWith('Tarjeta'); }

  private recargarDirecciones(): void {
    if (this.idUsuario === null) return;
    this.api.listarDirecciones(this.idUsuario).subscribe({
      next: (datos) => { this.direcciones = datos; if (!datos.some((d) => d.idDireccion === this.idDireccionSeleccionada)) this.idDireccionSeleccionada = datos.find((d) => d.esPrincipal)?.idDireccion ?? datos[0]?.idDireccion ?? null; },
      error: (e: HttpErrorResponse) => this.error = this.mensajeError(e, 'No se pudieron actualizar las direcciones.'),
    });
  }

  private recargarMetodos(): void {
    if (this.idUsuario === null) return;
    this.api.listarMetodosPago(this.idUsuario).subscribe({
      next: (datos) => { this.metodosPago = datos; if (!datos.some((m) => m.idMetodoPago === this.idMetodoSeleccionado)) this.idMetodoSeleccionado = datos.find((m) => m.esPrincipal)?.idMetodoPago ?? datos[0]?.idMetodoPago ?? null; },
      error: (e: HttpErrorResponse) => this.error = this.mensajeError(e, 'No se pudieron actualizar los métodos de pago.'),
    });
  }

  private direccionVacia(): SolicitudDireccion { return { calle: '', numero: '', colonia: '', ciudad: '', estado: '', codigoPostal: '', esPrincipal: true }; }
  private metodoVacio(): SolicitudMetodoPago { return { tipo: 'Efectivo al recibir', ultimos4Digitos: '', esPrincipal: true }; }
  private mensajeError(error: HttpErrorResponse, alternativo: string): string {
    const cuerpo = error.error as { mensaje?: string; message?: string } | null;
    if (cuerpo?.mensaje) return cuerpo.mensaje;
    if (cuerpo?.message) return cuerpo.message;
    if (error.status === 0) return 'No se pudo conectar con Spring Boot.';
    if (error.status === 401) return 'Tu sesión venció. Inicia sesión nuevamente.';
    if (error.status === 403) return 'Tu sesión no tiene permiso para esta operación.';
    return alternativo;
  }
}
