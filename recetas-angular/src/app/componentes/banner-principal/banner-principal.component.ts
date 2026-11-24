import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

/** Banner con imagen de fondo. Cada pantalla elige su tamaño con @Input() tamano. */
@Component({
  selector: 'app-banner-principal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './banner-principal.component.html',
  styleUrl: './banner-principal.component.css',
})
export class BannerPrincipalComponent {
  @Input() imagen: string = '';
  @Input() insignia: string = '';
  @Input() titulo: string = '';
  @Input() subtitulo: string = '';
  @Input() tamano: 'pequeno' | 'mediano' | 'grande' = 'mediano';
}
