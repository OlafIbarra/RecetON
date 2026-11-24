import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { HistorialNavegacionServicio } from '../../servicios/historial-navegacion.servicio';

interface SolicitudArcoUsuario {
  idSolicitud: number;
  idUsuario: number;
  nombreUsuario: string | null;
  correo: string | null;
  derecho: string;
  descripcion: string;
  fechaSolicitud: string;
  estado: string;
  respuesta: string | null;
  fechaResolucion: string | null;
  nombreResponsable: string | null;
}

/** Aviso público y formulario ARCO conectado al backend; las solicitudes ya no son borradores locales. */
@Component({
  selector: 'app-pagina-privacidad-arco',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pagina-privacidad-arco.component.html',
  styleUrl: './pagina-privacidad-arco.component.css',
})
export class PaginaPrivacidadArcoComponent implements OnInit {
  private readonly http = inject(HttpClient);
  readonly autenticacion = inject(AutenticacionServicio);
  private readonly historial = inject(HistorialNavegacionServicio);
  readonly api = environment.apiUrl;

  derechoSeleccionado = 'Acceso';
  descripcionSolicitud = '';
  solicitudes: SolicitudArcoUsuario[] = [];
  cargandoHistorial = false;
  enviandoSolicitud = false;
  mensajeEstado = '';
  errorEstado = '';

  regresarPantallaAnterior(): void { void this.historial.regresar(); }

  get sesionActiva(): boolean { return this.autenticacion.estaAutenticado(); }
  get esAdministrador(): boolean { return this.autenticacion.esAdministrador(); }

  ngOnInit(): void {
    if (this.sesionActiva) this.cargarSolicitudes();
  }

  cargarSolicitudes(): void {
    if (!this.sesionActiva) return;
    this.cargandoHistorial = true;
    this.errorEstado = '';
    this.http.get<SolicitudArcoUsuario[]>(`${this.api}/api/solicitudes-arco`).subscribe({
      next: solicitudes => {
        this.solicitudes = solicitudes;
        this.cargandoHistorial = false;
      },
      error: (e: HttpErrorResponse) => {
        this.errorEstado = this.mensajeError(e, 'No fue posible consultar tus solicitudes.');
        this.cargandoHistorial = false;
      },
    });
  }

  enviarSolicitud(): void {
    if (!this.sesionActiva) {
      this.errorEstado = 'Inicia sesión para registrar una solicitud ARCO desde RecetON.';
      return;
    }
    const descripcion = this.descripcionSolicitud.trim();
    if (descripcion.length < 10) {
      this.errorEstado = 'Describe la solicitud con al menos 10 caracteres.';
      return;
    }
    if (descripcion.length > 2000) {
      this.errorEstado = 'La descripción no puede superar los 2000 caracteres.';
      return;
    }

    this.enviandoSolicitud = true;
    this.errorEstado = '';
    this.mensajeEstado = '';
    this.http.post<SolicitudArcoUsuario>(`${this.api}/api/solicitudes-arco`, {
      derecho: this.derechoSeleccionado,
      descripcion,
    }).subscribe({
      next: solicitud => {
        this.descripcionSolicitud = '';
        this.solicitudes = [solicitud, ...this.solicitudes];
        this.enviandoSolicitud = false;
        this.mensajeEstado = `Tu solicitud se registró con el folio #${solicitud.idSolicitud}. Guarda este número para dar seguimiento.`;
      },
      error: (e: HttpErrorResponse) => {
        this.errorEstado = this.mensajeError(e, 'No se pudo registrar la solicitud. Inténtalo nuevamente.');
        this.enviandoSolicitud = false;
      },
    });
  }

  private mensajeError(e: HttpErrorResponse, alternativo: string): string {
    if (e.status === 0) return 'No se pudo conectar con Spring Boot. Comprueba que el backend esté ejecutándose.';
    if (e.status === 401) return 'Tu sesión expiró. Inicia sesión de nuevo para consultar o enviar solicitudes.';
    const cuerpo = e.error as { mensaje?: string } | null;
    return cuerpo?.mensaje ?? alternativo;
  }
}
