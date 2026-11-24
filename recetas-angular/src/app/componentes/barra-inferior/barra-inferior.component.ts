import { CommonModule } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { EnlaceMenu } from '../../interfaces/enlace-menu.interface';
import { IconoComponent } from '../icono/icono.component';

@Component({
  selector: 'app-barra-inferior',
  standalone: true,
  imports: [CommonModule, RouterLink, IconoComponent],
  templateUrl: './barra-inferior.component.html',
  styleUrl: './barra-inferior.component.css',
})
export class BarraInferiorComponent {
  @Input() indiceActivo: number = -1;
  @Input() mostrarEtiquetas: boolean = true;
  @Input() rutaExplorar: string = '/telefono';

  private readonly autenticacionServicio: AutenticacionServicio = inject(AutenticacionServicio);
  private readonly router: Router = inject(Router);
  menuPerfilAbierto: boolean = false;

  readonly elementos: EnlaceMenu[] = [
    { etiqueta: 'Inicio', icono: 'inicio', ruta: '/inicio' },
    { etiqueta: 'Favoritos', icono: 'corazon', ruta: '/favoritos' },
    { etiqueta: 'Tienda', icono: 'tienda', ruta: '/productos' },
    { etiqueta: 'Carrito', icono: 'carrito', ruta: '/carrito' },
    { etiqueta: 'Perfil', icono: 'perfil' },
  ];

  get nombreUsuario(): string { return this.autenticacionServicio.obtenerUsuarioActual()?.nombreCompleto ?? 'Invitado'; }
  get etiquetaRol(): string { return this.autenticacionServicio.obtenerEtiquetaRol(); }
  get esAdministrador(): boolean { return this.autenticacionServicio.esAdministrador(); }

  alternarMenuPerfil(): void { this.menuPerfilAbierto = !this.menuPerfilAbierto; }

  esElementoActivo(elemento: EnlaceMenu): boolean {
    if (elemento.ruta) return this.router.url.split('?')[0] === elemento.ruta;
    return elemento.icono === 'perfil' && this.menuPerfilAbierto;
  }

  ejecutarAccion(elemento: EnlaceMenu): void {
    if (elemento.icono === 'perfil') { this.alternarMenuPerfil(); return; }
    if (elemento.ruta) void this.router.navigateByUrl(elemento.ruta);
  }

  irAExplorar(): void { this.menuPerfilAbierto = false; void this.router.navigateByUrl(this.rutaExplorar); }

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
