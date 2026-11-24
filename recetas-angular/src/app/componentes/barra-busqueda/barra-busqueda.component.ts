import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

/**
 * Campo de búsqueda reutilizable. Emite el texto escrito con
 * @Output() cambioTexto para que el componente padre decida qué hacer.
 */
@Component({
  selector: 'app-barra-busqueda',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './barra-busqueda.component.html',
  styleUrl: './barra-busqueda.component.css',
})
export class BarraBusquedaComponent {
  @Input() marcador: string = 'Buscar…';
  @Output() cambioTexto: EventEmitter<string> = new EventEmitter<string>();

  texto: string = '';

  alEscribir(): void {
    this.cambioTexto.emit(this.texto);
  }
}
