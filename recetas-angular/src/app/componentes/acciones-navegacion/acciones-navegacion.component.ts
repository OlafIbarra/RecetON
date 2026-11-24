import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HistorialNavegacionServicio } from '../../servicios/historial-navegacion.servicio';

@Component({
  selector: 'app-acciones-navegacion',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './acciones-navegacion.component.html',
  styleUrl: './acciones-navegacion.component.css',
})
export class AccionesNavegacionComponent {
  private readonly router = inject(Router);
  private readonly historial = inject(HistorialNavegacionServicio);

  get visible(): boolean {
    const ruta = this.router.url.split(/[?#]/, 1)[0];
    return ruta !== '/'
      && ruta !== '/inicio'
      && ruta !== '/inicio-sesion'
      && ruta !== '/oauth-callback';
  }

  get sobreBarraInferior(): boolean {
    const ruta = this.router.url.split(/[?#]/, 1)[0];
    return ruta === '/telefono' || ruta === '/tableta' || ruta === '/reloj';
  }

  regresar(): void {
    void this.historial.regresar();
  }

  volverAlInicio(): void {
    void this.historial.volverAlInicio();
  }
}
