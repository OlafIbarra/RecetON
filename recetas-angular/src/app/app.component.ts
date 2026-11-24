import { Component, HostListener, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HistorialNavegacionServicio } from './servicios/historial-navegacion.servicio';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  constructor() {
    // Mantiene el historial global para que las pantallas secundarias puedan regresar
    // a la interfaz (TV, tableta, teléfono o reloj) desde la que se abrieron.
    inject(HistorialNavegacionServicio);
  }

  hayConexion = typeof navigator === 'undefined' ? true : navigator.onLine;

  @HostListener('window:online')
  alRecuperarConexion(): void {
    this.hayConexion = true;
  }

  @HostListener('window:offline')
  alPerderConexion(): void {
    this.hayConexion = false;
  }
}
