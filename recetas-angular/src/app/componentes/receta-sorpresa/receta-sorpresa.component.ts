import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RecetaExterna } from '../../interfaces/receta-externa.interface';

/**
 * Tarjeta que muestra una receta traída de una API externa (TheMealDB).
 * No sabe cómo se pide la receta: solo la recibe por @Input() y avisa
 * con @Output() cuando el usuario quiere otra.
 */
@Component({
  selector: 'app-receta-sorpresa',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './receta-sorpresa.component.html',
  styleUrl: './receta-sorpresa.component.css',
})
export class RecetaSorpresaComponent {
  @Input() receta: RecetaExterna | null = null;
  @Input() cargando: boolean = true;
  @Input() mensajeError: string | null = null;
  @Output() otraReceta: EventEmitter<void> = new EventEmitter<void>();
}
