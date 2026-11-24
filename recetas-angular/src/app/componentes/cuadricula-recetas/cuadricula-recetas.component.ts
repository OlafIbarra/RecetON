import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { Receta } from '../../interfaces/receta.interface';
import { TarjetaRecetaComponent } from '../tarjeta-receta/tarjeta-receta.component';

/** Cuadrícula de recetas. Cada pantalla decide cuántas columnas usar (@Input() columnas). */
@Component({
  selector: 'app-cuadricula-recetas',
  standalone: true,
  imports: [CommonModule, TarjetaRecetaComponent],
  templateUrl: './cuadricula-recetas.component.html',
  styleUrl: './cuadricula-recetas.component.css',
})
export class CuadriculaRecetasComponent {
  @Input() recetas: Receta[] = [];
  @Input() columnas: number = 2;
}
