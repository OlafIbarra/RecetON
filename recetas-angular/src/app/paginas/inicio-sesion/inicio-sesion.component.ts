import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';

/**
 * /inicio-sesion ya no presenta una pantalla intermedia con un botón.
 * Si no hay una sesión válida, inicia OAuth 2.0 + PKCE automáticamente y
 * el usuario ve directamente el formulario de acceso diseñado en Spring Security.
 */
@Component({
  selector: 'app-inicio-sesion',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inicio-sesion.component.html',
  styleUrl: './inicio-sesion.component.css',
})
export class InicioSesionComponent implements OnInit {
  private readonly autenticacionServicio = inject(AutenticacionServicio);
  private readonly router = inject(Router);

  mensajeError: string | null = null;

  ngOnInit(): void {
    if (this.autenticacionServicio.estaAutenticado()) {
      void this.router.navigate(['/inicio']);
      return;
    }

    try {
      this.autenticacionServicio.iniciarSesion();
    } catch {
      this.mensajeError = 'No fue posible abrir el acceso seguro. Comprueba que el servidor de RecetON esté encendido e inténtalo de nuevo.';
    }
  }

  reintentar(): void {
    this.mensajeError = null;
    try {
      this.autenticacionServicio.iniciarSesion();
    } catch {
      this.mensajeError = 'No fue posible abrir el acceso seguro. Comprueba que el servidor de RecetON esté encendido e inténtalo de nuevo.';
    }
  }
}
