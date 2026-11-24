import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { HistorialNavegacionServicio } from '../../servicios/historial-navegacion.servicio';

interface PerfilMiCuenta {
  id: number;
  username: string;
  email: string | null;
  fullName: string;
  dateRegistered: string | null;
  active: boolean;
}

@Component({
  selector: 'app-pagina-mi-cuenta',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pagina-mi-cuenta.component.html',
  styleUrl: './pagina-mi-cuenta.component.css',
})
export class PaginaMiCuentaComponent implements OnInit {
  private readonly http = inject(HttpClient);
  readonly auth = inject(AutenticacionServicio);
  private readonly router = inject(Router);
  private readonly historial = inject(HistorialNavegacionServicio);
  readonly api = environment.apiUrl;

  perfil: PerfilMiCuenta | null = null;
  formularioPerfil = { username: '', fullName: '', email: '' };
  formularioContrasena = { actual: '', nueva: '', confirmar: '' };
  cargando = true;
  guardandoPerfil = false;
  cambiandoContrasena = false;
  eliminando = false;
  error = '';
  mensaje = '';
  editarPerfil = false;
  mostrarConfirmacion = false;

  ngOnInit(): void { this.cargarPerfil(); }

  cargarPerfil(): void {
    this.cargando = true;
    this.error = '';
    this.http.get<PerfilMiCuenta>(`${this.api}/api/mi-cuenta`).subscribe({
      next: perfil => {
        this.perfil = perfil;
        this.formularioPerfil = {
          username: perfil.username ?? '',
          fullName: perfil.fullName ?? '',
          email: perfil.email ?? '',
        };
        this.cargando = false;
      },
      error: (e: HttpErrorResponse) => {
        this.error = this.mensajeError(e, 'No fue posible consultar los datos de tu cuenta.');
        this.cargando = false;
      },
    });
  }

  iniciarEdicion(): void {
    if (!this.perfil) return;
    this.formularioPerfil = {
      username: this.perfil.username ?? '',
      fullName: this.perfil.fullName ?? '',
      email: this.perfil.email ?? '',
    };
    this.editarPerfil = true;
    this.error = '';
    this.mensaje = '';
  }

  cancelarEdicion(): void {
    this.editarPerfil = false;
    if (this.perfil) {
      this.formularioPerfil = {
        username: this.perfil.username ?? '',
        fullName: this.perfil.fullName ?? '',
        email: this.perfil.email ?? '',
      };
    }
    this.error = '';
  }

  guardarPerfil(): void {
    if (this.guardandoPerfil) return;
    const datos = {
      username: this.formularioPerfil.username.trim(),
      fullName: this.formularioPerfil.fullName.trim(),
      email: this.formularioPerfil.email.trim(),
    };
    if (!datos.username || !datos.fullName) {
      this.error = 'El nombre de usuario y el nombre completo son obligatorios.';
      return;
    }
    if (datos.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(datos.email)) {
      this.error = 'Escribe un correo electrónico válido o deja el campo vacío.';
      return;
    }

    this.guardandoPerfil = true;
    this.error = '';
    this.mensaje = '';
    this.http.put<PerfilMiCuenta>(`${this.api}/api/mi-cuenta`, datos).subscribe({
      next: perfil => {
        this.perfil = perfil;
        this.formularioPerfil = { username: perfil.username, fullName: perfil.fullName, email: perfil.email ?? '' };
        this.auth.actualizarDatosPerfil(perfil.username, perfil.fullName);
        this.editarPerfil = false;
        this.guardandoPerfil = false;
        this.mensaje = 'Tu perfil se actualizó correctamente.';
      },
      error: (e: HttpErrorResponse) => {
        this.error = this.mensajeError(e, 'No se pudieron guardar los cambios del perfil.');
        this.guardandoPerfil = false;
      },
    });
  }

  cambiarContrasena(): void {
    if (this.cambiandoContrasena) return;
    const { actual, nueva, confirmar } = this.formularioContrasena;
    if (!actual || !nueva || !confirmar) {
      this.error = 'Completa los tres campos de contraseña.';
      return;
    }
    if (nueva.length < 8) {
      this.error = 'La contraseña nueva debe tener al menos 8 caracteres.';
      return;
    }
    if (nueva !== confirmar) {
      this.error = 'La confirmación no coincide con la contraseña nueva.';
      return;
    }
    if (nueva === actual) {
      this.error = 'La contraseña nueva debe ser diferente de la actual.';
      return;
    }

    this.cambiandoContrasena = true;
    this.error = '';
    this.mensaje = '';
    this.http.put<void>(`${this.api}/api/mi-cuenta/contrasena`, {
      contrasenaActual: actual,
      contrasenaNueva: nueva,
    }).subscribe({
      next: () => {
        this.formularioContrasena = { actual: '', nueva: '', confirmar: '' };
        this.cambiandoContrasena = false;
        // El backend invalida todas las sesiones al guardar la contraseña. Se limpia la sesión local.
        this.auth.limpiarSesionLocal();
        void this.router.navigate(['/inicio-sesion'], { queryParams: { contrasena: 'actualizada' } });
      },
      error: (e: HttpErrorResponse) => {
        this.error = this.mensajeError(e, 'No se pudo cambiar la contraseña.');
        this.cambiandoContrasena = false;
      },
    });
  }

  solicitarBaja(): void {
    this.mostrarConfirmacion = true;
    this.mensaje = '';
    this.error = '';
  }

  cancelarBaja(): void { if (!this.eliminando) this.mostrarConfirmacion = false; }

  confirmarBaja(): void {
    if (!this.perfil || this.eliminando) return;
    if (this.auth.esAdministrador()) {
      this.error = 'La baja desde Mi cuenta está disponible únicamente para usuarios normales.';
      this.mostrarConfirmacion = false;
      return;
    }
    this.eliminando = true;
    this.error = '';
    this.http.delete<void>(`${this.api}/api/mi-cuenta`).subscribe({
      next: () => {
        this.auth.limpiarSesionLocal();
        void this.router.navigate(['/inicio-sesion'], { queryParams: { cuenta: 'eliminada' } });
      },
      error: (e: HttpErrorResponse) => {
        this.error = this.mensajeError(e, 'No se pudo completar la baja. No se cerró tu sesión; vuelve a intentarlo.');
        this.eliminando = false;
        this.mostrarConfirmacion = false;
      },
    });
  }

  volver(): void { void this.historial.regresar(); }

  private mensajeError(e: HttpErrorResponse, alternativo: string): string {
    if (e.status === 0) return 'No se pudo conectar con Spring Boot. Comprueba que el backend esté ejecutándose.';
    const cuerpo = e.error as { mensaje?: string } | null;
    return cuerpo?.mensaje ?? alternativo;
  }
}
