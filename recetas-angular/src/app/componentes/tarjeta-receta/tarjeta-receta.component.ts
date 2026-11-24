import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Receta } from '../../interfaces/receta.interface';

/**
 * Tarjeta individual de una receta. Solo recibe un objeto Receta y lo
 * dibuja; al hacer clic navega a /receta/:id usando routerLink.
 */
@Component({
  selector: 'app-tarjeta-receta',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './tarjeta-receta.component.html',
  styleUrl: './tarjeta-receta.component.css',
})
export class TarjetaRecetaComponent {
  @Input({ required: true }) receta!: Receta;
}
