import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { RecetaApiServicio } from '../../servicios/receta-api.servicio';
import { Receta } from '../../interfaces/receta.interface';
import { TarjetaRelojComponent } from '../../componentes/tarjeta-reloj/tarjeta-reloj.component';
import { IconoComponent } from '../../componentes/icono/icono.component';
import { DescubreRecetasComponent } from '../../componentes/descubre-recetas/descubre-recetas.component';

/**
 * PANTALLA DE RELOJ (Figma "Reloj").
 * Una receta del día por página (app-tarjeta-reloj), se desliza de lado,
 * con puntos indicadores y botones mínimos para volver al índice o salir.
 * Si no hay backend, usa datos de respaldo (ver RecetaApiServicio) y
 * avisa con "modoRespaldo" sin bloquear la pantalla.
 */
@Component({
  selector: 'app-pagina-reloj',
  standalone: true,
  imports: [CommonModule, RouterLink, TarjetaRelojComponent, IconoComponent, DescubreRecetasComponent],
  templateUrl: './pagina-reloj.component.html',
  styleUrl: './pagina-reloj.component.css',
})
export class PaginaRelojComponent implements OnInit {
  private readonly recetaApiServicio: RecetaApiServicio = inject(RecetaApiServicio);
  private readonly autenticacionServicio: AutenticacionServicio = inject(AutenticacionServicio);
  private readonly router: Router = inject(Router);

  recetasDelDia: Receta[] = [];
  indiceActual: number = 0;
  cargando: boolean = true;
  modoRespaldo: boolean = false;

  ngOnInit(): void {
    this.cargarRecetas();
  }

  cargarRecetas(): void {
    this.cargando = true;

    this.recetaApiServicio.obtenerRecetas().subscribe({
      next: (recetas: Receta[]) => {
        this.recetasDelDia = recetas.slice(0, 5);
        this.modoRespaldo = this.recetaApiServicio.estaEnModoRespaldo;
        this.cargando = false;
      },
    });
  }

  alDesplazar(evento: Event): void {
    const pista: HTMLElement = evento.target as HTMLElement;
    const anchoPagina: number = pista.clientWidth || 1;
    this.indiceActual = Math.round(pista.scrollLeft / anchoPagina);
  }

  irAReceta(indice: number): void {
    const pista = document.querySelector('.reloj-pista') as HTMLElement | null;
    if (!pista) return;
    pista.scrollTo({ left: indice * (pista.clientWidth || 1), behavior: 'smooth' });
    this.indiceActual = indice;
  }

  async cerrarSesion(): Promise<void> {
    try {
      await this.autenticacionServicio.cerrarSesion();
      await this.router.navigate(['/inicio-sesion']);
    } catch (error) {
      console.error('No se pudo cerrar la sesión de RecetON.', error);
      window.alert(error instanceof Error ? error.message : 'No se pudo cerrar la sesión. Inténtalo nuevamente.');
    }
  }
}
