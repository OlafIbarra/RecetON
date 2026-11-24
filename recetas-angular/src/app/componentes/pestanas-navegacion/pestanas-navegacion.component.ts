import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Pestana } from '../../interfaces/pestana.interface';
import { IconoComponent } from '../icono/icono.component';

const PESTANAS_POR_DEFECTO: Pestana[] = [
  { etiqueta: 'Favoritos', icono: 'corazon', ruta: '/favoritos' },
  { etiqueta: 'Tienda', icono: 'tienda', ruta: '/productos' },
  { etiqueta: 'Carrito', icono: 'carrito', ruta: '/carrito' },
  { etiqueta: 'Inicio', icono: 'inicio', ruta: '/inicio' },
];

@Component({
  selector: 'app-pestanas-navegacion',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, IconoComponent],
  templateUrl: './pestanas-navegacion.component.html',
  styleUrl: './pestanas-navegacion.component.css',
})
export class PestanasNavegacionComponent {
  @Input() pestanas: Pestana[] = PESTANAS_POR_DEFECTO;
  @Input() indiceActivo: number = -1;
}
