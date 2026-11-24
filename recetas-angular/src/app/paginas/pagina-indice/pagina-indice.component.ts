import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { OpcionPantalla } from '../../interfaces/opcion-pantalla.interface';
import { TarjetaPantallaComponent } from '../../componentes/tarjeta-pantalla/tarjeta-pantalla.component';
import { IconoComponent } from '../../componentes/icono/icono.component';

/** Índice: aquí eliges a cuál de las 4 pantallas quieres entrar. */
@Component({
  selector: 'app-pagina-indice',
  standalone: true,
  imports: [CommonModule, TarjetaPantallaComponent, IconoComponent],
  templateUrl: './pagina-indice.component.html',
  styleUrl: './pagina-indice.component.css',
})
export class PaginaIndiceComponent {
  private readonly autenticacionServicio: AutenticacionServicio = inject(AutenticacionServicio);
  private readonly router: Router = inject(Router);


  readonly opciones: OpcionPantalla[] = [
    {
      titulo: 'Reloj',
      descripcion: 'Receta del día, una por página, en pantalla mínima.',
      ruta: '/reloj',
      icono: 'reloj',
      anchoReferencia: 'Hasta 300 px',
    },
    {
      titulo: 'Teléfono',
      descripcion: 'Buscador, pestañas, categorías y grilla de 2 columnas.',
      ruta: '/telefono',
      icono: 'telefono',
      anchoReferencia: 'Alrededor de 390 px',
    },
    {
      titulo: 'Tableta',
      descripcion: 'Título con buscador, categorías y grilla de 3 columnas.',
      ruta: '/tableta',
      icono: 'tableta',
      anchoReferencia: 'Alrededor de 900 px',
    },
    {
      titulo: 'Pantalla TV',
      descripcion: 'Barra lateral, banner del chef y categorías grandes.',
      ruta: '/tv',
      icono: 'tv',
      anchoReferencia: '1200 px o más',
    },
  ];

  get nombreUsuario(): string { return this.autenticacionServicio.obtenerUsuarioActual()?.nombreCompleto ?? 'Invitado'; }
  get etiquetaRol(): string { return this.autenticacionServicio.obtenerEtiquetaRol(); }
  get esAdministrador(): boolean { return this.autenticacionServicio.esAdministrador(); }

  verFavoritos(): void {
    this.router.navigate(['/favoritos']);
  }

  verTienda(): void {
    this.router.navigate(['/productos']);
  }

  verCarrito(): void {
    this.router.navigate(['/carrito']);
  }

  verPedidos(): void {
    this.router.navigate(['/mis-pedidos']);
  }

  verAdministracion(): void {
    this.router.navigate(['/administracion']);
  }

  async cerrarSesion(): Promise<void> {
    try {
      await this.autenticacionServicio.cerrarSesion();
      await this.router.navigate(['/inicio-sesion']);
    } catch (error) {
      console.error('No se pudo cerrar la sesión de RecetON.', error);
      window.alert(error instanceof Error ? error.message : 'No se pudo cerrar la sesión. Inténtalo nuevamente.');
    }
  }
}
