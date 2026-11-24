import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Categoria } from '../../interfaces/categoria.interface';

/** Fila de categorías (círculo + nombre). @Input() grande agranda los círculos (TV). */
@Component({
  selector: 'app-lista-categorias',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './lista-categorias.component.html',
  styleUrl: './lista-categorias.component.css',
})
export class ListaCategoriasComponent {
  @Input() categorias: Categoria[] = [];
  @Input() grande: boolean = false;
  @Input() categoriaActivaId: number | null = null;
  @Output() seleccionarCategoria: EventEmitter<Categoria> = new EventEmitter<Categoria>();
}
