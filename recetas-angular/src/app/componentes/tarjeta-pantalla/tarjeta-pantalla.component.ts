import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { OpcionPantalla } from '../../interfaces/opcion-pantalla.interface';
import { IconoComponent } from '../icono/icono.component';

/** Botón grande del índice que lleva a una de las 4 pantallas. */
@Component({
  selector: 'app-tarjeta-pantalla',
  standalone: true,
  imports: [RouterLink, IconoComponent],
  templateUrl: './tarjeta-pantalla.component.html',
  styleUrl: './tarjeta-pantalla.component.css',
})
export class TarjetaPantallaComponent {
  @Input({ required: true }) opcion!: OpcionPantalla;
}
