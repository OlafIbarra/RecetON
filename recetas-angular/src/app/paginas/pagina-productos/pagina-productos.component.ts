import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { TiendaApiServicio } from '../../servicios/tienda-api.servicio';
import { HistorialNavegacionServicio } from '../../servicios/historial-navegacion.servicio';
import { Producto, SolicitudProducto } from '../../interfaces/tienda.interface';

@Component({
  selector: 'app-pagina-productos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pagina-productos.component.html',
  styleUrl: './pagina-productos.component.css',
})
export class PaginaProductosComponent implements OnInit {
  private readonly tienda: TiendaApiServicio = inject(TiendaApiServicio);
  private readonly autenticacion: AutenticacionServicio = inject(AutenticacionServicio);
  private readonly router: Router = inject(Router);
  private readonly historial = inject(HistorialNavegacionServicio);

  productos: Producto[] = [];
  cargando: boolean = true;
  error: string | null = null;
  mensaje: string | null = null;
  cantidades: Record<number, number> = {};
  mostrarFormulario: boolean = false;
  guardando: boolean = false;
  productoEditandoId: number | null = null;

  nuevoProducto: SolicitudProducto = this.formularioVacio();

  ngOnInit(): void {
    this.cargarProductos();
  }

  private get idUsuario(): number | null {
    return this.autenticacion.obtenerUsuarioActual()?.id ?? null;
  }

  get esAdministrador(): boolean {
    return this.autenticacion.esAdministrador();
  }

  cargarProductos(): void {
    this.cargando = true;
    this.error = null;
    this.tienda.obtenerProductos().subscribe({
      next: (productos: Producto[]) => {
        this.productos = productos;
        this.cargando = false;
      },
      error: (error: HttpErrorResponse) => {
        this.error = this.mensajeError(error, 'No se pudo cargar el catálogo de productos.');
        this.cargando = false;
      },
    });
  }

  agregarAlCarrito(producto: Producto): void {
    const idUsuario = this.idUsuario;
    const cantidad = this.cantidades[producto.idProducto] ?? 1;
    if (idUsuario === null) {
      this.error = 'No se encontró la sesión del usuario. Inicia sesión nuevamente.';
      return;
    }
    this.error = null;
    this.mensaje = null;
    this.tienda.agregarAlCarrito(idUsuario, { idProducto: producto.idProducto, cantidad }).subscribe({
      next: () => {
        this.mensaje = `Se agregó ${producto.nombreProducto} al carrito.`;
      },
      error: (error: HttpErrorResponse) => {
        this.error = this.mensajeError(error, 'No se pudo agregar el producto al carrito.');
      },
    });
  }

  alternarFormulario(): void {
    this.mostrarFormulario = !this.mostrarFormulario;
    this.error = null;
    this.mensaje = null;
    if (!this.mostrarFormulario) {
      this.nuevoProducto = this.formularioVacio();
      this.productoEditandoId = null;
    }
  }

  editarProducto(producto: Producto): void {
    if (!this.esAdministrador) return;
    this.productoEditandoId = producto.idProducto;
    this.nuevoProducto = {
      idReceta: producto.idReceta,
      nombreProducto: producto.nombreProducto,
      descripcion: producto.descripcion ?? '',
      precio: producto.precio,
      stock: producto.stock,
    };
    this.mostrarFormulario = true;
    this.error = null;
    this.mensaje = null;
  }

  eliminarProducto(producto: Producto): void {
    if (!this.esAdministrador || !confirm(`¿Eliminar el producto «${producto.nombreProducto}»? Si está relacionado con un carrito o pedido, la base de datos puede impedirlo.`)) return;
    this.error = null;
    this.mensaje = null;
    this.tienda.eliminarProducto(producto.idProducto).subscribe({
      next: () => {
        this.productos = this.productos.filter((actual) => actual.idProducto !== producto.idProducto);
        this.mensaje = 'Producto eliminado correctamente.';
      },
      error: (error: HttpErrorResponse) => this.error = this.mensajeError(error, 'No se pudo eliminar el producto. Comprueba si está relacionado con un carrito o pedido.'),
    });
  }

  guardarProducto(): void {
    if (this.guardando) return;
    if (!this.nuevoProducto.nombreProducto.trim()) {
      this.error = 'Escribe el nombre del producto.';
      return;
    }
    if (!Number.isFinite(this.nuevoProducto.precio) || this.nuevoProducto.precio < 0) {
      this.error = 'El precio debe ser cero o mayor.';
      return;
    }
    if (!Number.isInteger(this.nuevoProducto.stock) || this.nuevoProducto.stock < 0) {
      this.error = 'El stock debe ser un entero cero o mayor.';
      return;
    }

    const solicitud: SolicitudProducto = {
      ...this.nuevoProducto,
      idReceta: this.nuevoProducto.idReceta && this.nuevoProducto.idReceta > 0 ? this.nuevoProducto.idReceta : null,
      nombreProducto: this.nuevoProducto.nombreProducto.trim(),
      descripcion: this.nuevoProducto.descripcion.trim(),
    };

    this.guardando = true;
    this.error = null;
    const idEditado = this.productoEditandoId;
    const peticion = idEditado === null
      ? this.tienda.crearProducto(solicitud)
      : this.tienda.actualizarProducto(idEditado, solicitud);
    peticion.subscribe({
      next: (producto: Producto) => {
        this.guardando = false;
        this.mensaje = idEditado === null
          ? `Se registró el producto ${producto.nombreProducto}.`
          : `Se actualizó el producto ${producto.nombreProducto}.`;
        this.mostrarFormulario = false;
        this.productoEditandoId = null;
        this.nuevoProducto = this.formularioVacio();
        this.cargarProductos();
      },
      error: (error: HttpErrorResponse) => {
        this.guardando = false;
        this.error = this.mensajeError(error, idEditado === null ? 'No se pudo registrar el producto.' : 'No se pudo actualizar el producto.');
      },
    });
  }

  irAlCarrito(): void {
    this.router.navigate(['/carrito']);
  }

  regresarPantallaAnterior(): void {
    void this.historial.regresar();
  }

  formatoPrecio(precio: number): string {
    return new Intl.NumberFormat('es-MX', { style: 'currency', currency: 'MXN' }).format(precio);
  }

  private formularioVacio(): SolicitudProducto {
    return { idReceta: null, nombreProducto: '', descripcion: '', precio: 0, stock: 1 };
  }

  private mensajeError(error: HttpErrorResponse, alternativo: string): string {
    const cuerpo = error.error as { mensaje?: string } | null;
    return cuerpo?.mensaje ?? (error.status === 0 ? 'No se pudo conectar con Spring Boot.' : alternativo);
  }
}
