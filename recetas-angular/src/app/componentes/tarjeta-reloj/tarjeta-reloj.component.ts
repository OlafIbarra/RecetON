import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Receta } from '../../interfaces/receta.interface';

/** Una "página" de la pantalla del reloj: receta del día con foto redonda. */
@Component({
  selector: 'app-tarjeta-reloj',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './tarjeta-reloj.component.html',
  styleUrl: './tarjeta-reloj.component.css',
})
export class TarjetaRelojComponent {
  @Input({ required: true }) receta!: Receta;
}
