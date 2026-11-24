import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Receta } from '../../interfaces/receta.interface';
import { UsuarioSesion } from '../../interfaces/usuario.interface';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { RecetaApiServicio } from '../../servicios/receta-api.servicio';
import { HistorialNavegacionServicio } from '../../servicios/historial-navegacion.servicio';

@Component({
  selector: 'app-pagina-favoritos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagina-favoritos.component.html',
  styleUrl: './pagina-favoritos.component.css',
})
export class PaginaFavoritosComponent implements OnInit {
  private readonly autenticacionServicio: AutenticacionServicio = inject(AutenticacionServicio);
  private readonly recetaApiServicio: RecetaApiServicio = inject(RecetaApiServicio);
  private readonly router: Router = inject(Router);
  private readonly historial = inject(HistorialNavegacionServicio);

  recetas: Receta[] = [];
  cargando: boolean = true;
  recetaEnProcesoId: number | null = null;
  mensajeError: string | null = null;
  mensaje: string | null = null;

  ngOnInit(): void {
    this.cargarFavoritos();
  }

  get usuarioActual(): UsuarioSesion | null {
    return this.autenticacionServicio.obtenerUsuarioActual();
  }

  cargarFavoritos(): void {
    const usuario: UsuarioSesion | null = this.usuarioActual;
    if (!usuario) {
      this.cargando = false;
      this.mensajeError = 'Inicia sesión para consultar tus recetas favoritas.';
      return;
    }
    this.cargando = true;
    this.mensajeError = null;
    this.recetaApiServicio.obtenerFavoritosUsuario(usuario.id).subscribe({
      next: (recetas: Receta[]) => {
        this.recetas = recetas;
        this.cargando = false;
      },
      error: (error: HttpErrorResponse) => {
        this.cargando = false;
        this.mensajeError = this.mensajeDeError(error);
      },
    });
  }

  abrirReceta(receta: Receta): void {
    this.router.navigate(['/receta', receta.id]);
  }

  quitarFavorito(receta: Receta): void {
    const usuario: UsuarioSesion | null = this.usuarioActual;
    if (!usuario || !confirm(`¿Quitar "${receta.nombre}" de tus favoritos?`)) return;
    this.recetaEnProcesoId = receta.id;
    this.mensaje = null;
    this.mensajeError = null;
    this.recetaApiServicio.eliminarFavorito(usuario.id, receta.id).subscribe({
      next: () => {
        this.recetas = this.recetas.filter((actual: Receta) => actual.id !== receta.id);
        this.recetaEnProcesoId = null;
        this.mensaje = 'La receta se quitó de tus favoritos.';
      },
      error: (error: HttpErrorResponse) => {
        this.recetaEnProcesoId = null;
        this.mensajeError = this.mensajeDeError(error);
      },
    });
  }

  volverAlInicio(): void {
    void this.historial.regresar();
  }

  private mensajeDeError(error: HttpErrorResponse): string {
    const cuerpo: unknown = error.error;
    if (typeof cuerpo === 'object' && cuerpo !== null && 'mensaje' in cuerpo) {
      const mensaje: unknown = (cuerpo as { mensaje?: unknown }).mensaje;
      if (typeof mensaje === 'string') return mensaje;
    }
    if (error.status === 0) return 'No se pudo conectar con Spring Boot. Comprueba que el backend esté iniciado.';
    if (error.status === 404) return 'No se encontró el usuario o la información solicitada.';
    return `No se pudieron cargar los favoritos (error ${error.status}).`;
  }
}
