import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { NombreIcono } from '../../interfaces/icono.interface';

/**
 * Icono reutilizable. El tamaño lo decide quien lo usa con la variable
 * CSS --tam-icono (por defecto 18px).
 */
@Component({
  selector: 'app-icono',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './icono.component.html',
  styleUrl: './icono.component.css',
})
export class IconoComponent {
  @Input({ required: true }) nombre!: NombreIcono;
}
