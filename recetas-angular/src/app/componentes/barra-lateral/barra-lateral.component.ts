import { CommonModule } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { EnlaceMenu } from '../../interfaces/enlace-menu.interface';
import { IconoComponent } from '../icono/icono.component';

@Component({
  selector: 'app-barra-lateral',
  standalone: true,
  imports: [CommonModule, RouterLink, IconoComponent],
  templateUrl: './barra-lateral.component.html',
  styleUrl: './barra-lateral.component.css',
})
export class BarraLateralComponent {
  @Input() indiceActivo: number = 0;
  private readonly autenticacionServicio = inject(AutenticacionServicio);
  private readonly router: Router = inject(Router);

  readonly enlaces: EnlaceMenu[] = [
    { etiqueta: 'Explorar', icono: 'brujula', ruta: '/tv' },
    { etiqueta: 'Favoritos', icono: 'corazon', ruta: '/favoritos' },
    { etiqueta: 'Tienda', icono: 'tienda', ruta: '/productos' },
    { etiqueta: 'Carrito', icono: 'carrito', ruta: '/carrito' },
    { etiqueta: 'Mis pedidos', icono: 'lista', ruta: '/mis-pedidos' },
    { etiqueta: 'Inicio', icono: 'inicio', ruta: '/inicio' },
  ];

  get nombreUsuario(): string { return this.autenticacionServicio.obtenerUsuarioActual()?.nombreCompleto ?? 'Invitado'; }
  get etiquetaRol(): string { return this.autenticacionServicio.obtenerEtiquetaRol(); }
  get esAdministrador(): boolean { return this.autenticacionServicio.esAdministrador(); }

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
