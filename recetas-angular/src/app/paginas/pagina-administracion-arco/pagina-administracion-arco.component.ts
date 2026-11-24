import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';

interface SolicitudArcoApi {
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

interface SolicitudArcoEdicion extends SolicitudArcoApi {
  estadoEdicion: string;
  respuestaEdicion: string;
}

@Component({
  selector: 'app-pagina-administracion-arco',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pagina-administracion-arco.component.html',
  styleUrl: './pagina-administracion-arco.component.css',
})
export class PaginaAdministracionArcoComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AutenticacionServicio);
  private readonly router = inject(Router);
  private readonly api = environment.apiUrl;

  solicitudes: SolicitudArcoEdicion[] = [];
  cargando = true;
  guardandoId: number | null = null;
  error = '';
  mensaje = '';
  busqueda = '';
  filtroEstado = 'Todas';
  readonly filtros = ['Todas', 'Por revisar', 'En curso', 'Atendidas', 'No procedentes'];
  readonly estados = ['Recibida', 'En revisión', 'Pendiente de información', 'Atendida', 'No procedente'];

  get solicitudesFiltradas(): SolicitudArcoEdicion[] {
    const q = this.busqueda.trim().toLocaleLowerCase('es-MX');
    return this.solicitudes.filter(s => {
      const estado = s.estado;
      const coincideFiltro = this.filtroEstado === 'Todas'
        || (this.filtroEstado === 'Por revisar' && estado === 'Recibida')
        || (this.filtroEstado === 'En curso' && ['En revisión', 'Pendiente de información'].includes(estado))
        || (this.filtroEstado === 'Atendidas' && estado === 'Atendida')
        || (this.filtroEstado === 'No procedentes' && estado === 'No procedente');
      const texto = `${s.idSolicitud} ${s.idUsuario} ${s.nombreUsuario ?? ''} ${s.correo ?? ''} ${s.derecho} ${s.descripcion} ${s.estado} ${s.estadoEdicion}`.toLocaleLowerCase('es-MX');
      return coincideFiltro && (!q || texto.includes(q));
    });
  }

  get solicitudesAbiertas(): number {
    return this.solicitudes.filter(s => ['Recibida', 'En revisión', 'Pendiente de información'].includes(s.estado)).length;
  }

  get solicitudesCerradas(): number {
    return this.solicitudes.filter(s => ['Atendida', 'No procedente'].includes(s.estado)).length;
  }

  ngOnInit(): void {
    if (!this.auth.esAdministrador()) {
      void this.router.navigate(['/inicio']);
      return;
    }
    this.cargarSolicitudes();
  }

  cargarSolicitudes(): void {
    this.cargando = true;
    this.error = '';
    this.http.get<SolicitudArcoApi[]>(`${this.api}/api/administracion/solicitudes-arco`).subscribe({
      next: filas => {
        this.solicitudes = filas.map(s => ({ ...s, estadoEdicion: s.estado, respuestaEdicion: s.respuesta ?? '' }));
        this.cargando = false;
      },
      error: e => {
        this.error = this.mensajeError(e, 'No se pudieron cargar las solicitudes ARCO.');
        this.cargando = false;
      },
    });
  }

  guardar(solicitud: SolicitudArcoEdicion): void {
    if (this.guardandoId !== null) return;
    if ((solicitud.estadoEdicion === 'Atendida' || solicitud.estadoEdicion === 'No procedente') && !solicitud.respuestaEdicion.trim()) {
      this.error = 'Escribe una respuesta antes de cerrar la solicitud.';
      this.mensaje = '';
      return;
    }
    this.guardandoId = solicitud.idSolicitud;
    this.error = '';
    this.mensaje = '';
    this.http.put<SolicitudArcoApi>(`${this.api}/api/administracion/solicitudes-arco/${solicitud.idSolicitud}`, {
      estado: solicitud.estadoEdicion,
      respuesta: solicitud.respuestaEdicion,
    }).subscribe({
      next: actualizada => {
        this.solicitudes = this.solicitudes.map(s => s.idSolicitud === actualizada.idSolicitud
          ? { ...actualizada, estadoEdicion: actualizada.estado, respuestaEdicion: actualizada.respuesta ?? '' }
          : s);
        this.guardandoId = null;
        this.mensaje = `La solicitud #${actualizada.idSolicitud} se actualizó correctamente.`;
      },
      error: (e: HttpErrorResponse) => {
        this.error = this.mensajeError(e, 'No se pudo actualizar la solicitud.');
        this.guardandoId = null;
      },
    });
  }

  private mensajeError(e: HttpErrorResponse, alternativo: string): string {
    if (e.status === 0) return 'No se pudo conectar con Spring Boot. Comprueba que el backend esté ejecutándose.';
    const cuerpo = e.error as { mensaje?: string } | null;
    return cuerpo?.mensaje ?? alternativo;
  }
}
