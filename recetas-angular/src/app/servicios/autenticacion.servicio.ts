import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import { UsuarioSesion } from '../interfaces/usuario.interface';
import { crearParametrosPkce } from './oauth2-pkce';

const CLAVE_ALMACENAMIENTO = 'recetas_sesion_usuario';
const CLAVE_OAUTH_STATE = 'receton_oauth_state';
const CLAVE_OAUTH_VERIFIER = 'receton_oauth_verifier';

interface RespuestaTokenOAuth2 {
  access_token: string;
  token_type: string;
  expires_in: number;
  refresh_token?: string;
  scope?: string;
}

interface ClaimsUsuarioJwt {
  sub?: string;
  id_usuario?: number;
  nombre_usuario?: string;
  nombre_completo?: string;
  roles?: string[];
  exp?: number;
}

/** Autenticación OAuth 2.0 Authorization Code + PKCE. El backend emite JWT firmados. */
@Injectable({ providedIn: 'root' })
export class AutenticacionServicio {
  private readonly http = inject(HttpClient);

  /** Inicia el flujo OAuth2 y redirige al servidor de autorización para capturar la contraseña allí. */
  iniciarSesion(): void {
    this.limpiarSesionLocal();
    const pkce = crearParametrosPkce();
    sessionStorage.setItem(CLAVE_OAUTH_STATE, pkce.state);
    sessionStorage.setItem(CLAVE_OAUTH_VERIFIER, pkce.verifier);

    const redirectUri = this.obtenerRedirectUri();
    const autorizacion = new URL(`${environment.apiUrl}/oauth2/authorize`);
    autorizacion.searchParams.set('response_type', 'code');
    autorizacion.searchParams.set('client_id', environment.oauthClientId);
    autorizacion.searchParams.set('redirect_uri', redirectUri);
    autorizacion.searchParams.set('scope', 'api.read api.write');
    autorizacion.searchParams.set('state', pkce.state);
    autorizacion.searchParams.set('code_challenge', pkce.challenge);
    autorizacion.searchParams.set('code_challenge_method', 'S256');
    window.location.assign(autorizacion.toString());
  }

  /** Valida state y cambia el código de autorización por un access token JWT. */
  async completarInicioSesion(codigo: string, stateRecibido: string): Promise<void> {
    const stateEsperado = sessionStorage.getItem(CLAVE_OAUTH_STATE);
    const verifier = sessionStorage.getItem(CLAVE_OAUTH_VERIFIER);
    if (!codigo || !stateEsperado || !verifier || stateRecibido !== stateEsperado) {
      this.limpiarDatosTemporalesOAuth();
      throw new Error('La respuesta OAuth 2.0 no pasó la validación de state. Vuelve a iniciar sesión.');
    }

    const cuerpo = new URLSearchParams();
    cuerpo.set('grant_type', 'authorization_code');
    cuerpo.set('client_id', environment.oauthClientId);
    cuerpo.set('redirect_uri', this.obtenerRedirectUri());
    cuerpo.set('code', codigo);
    cuerpo.set('code_verifier', verifier);

    try {
      const respuesta = await firstValueFrom(this.http.post<RespuestaTokenOAuth2>(
        `${environment.apiUrl}/oauth2/token`,
        cuerpo.toString(),
        { headers: new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' }) },
      ));

      const claims = this.decodificarClaims(respuesta.access_token);
      if (typeof claims.id_usuario !== 'number' || !claims.nombre_usuario || !Array.isArray(claims.roles)) {
        throw new Error('El JWT no contiene los datos de usuario esperados.');
      }

      const sesion: UsuarioSesion = {
        id: claims.id_usuario,
        nombreUsuario: claims.nombre_usuario,
        nombreCompleto: claims.nombre_completo ?? claims.nombre_usuario,
        roles: claims.roles.filter((rol): rol is string => typeof rol === 'string'),
        token: respuesta.access_token,
        refreshToken: respuesta.refresh_token,
        expiraEn: Date.now() + Math.max(0, respuesta.expires_in - 10) * 1000,
      };
      localStorage.setItem(CLAVE_ALMACENAMIENTO, JSON.stringify(sesion));
    } finally {
      this.limpiarDatosTemporalesOAuth();
    }
  }

  private obtenerRedirectUri(): string {
    // Debe coincidir con una URI registrada en el backend. Se deriva del
    // origen real (local o público) desde el que se abrió RecetON.
    return environment.oauthRedirectUri;
  }

  private decodificarClaims(token: string): ClaimsUsuarioJwt {
    const segmentos = token.split('.');
    if (segmentos.length !== 3) throw new Error('El servidor no devolvió un JWT válido.');
    const base64 = segmentos[1].replace(/-/g, '+').replace(/_/g, '/');
    const binario = atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '='));
    const bytes = Uint8Array.from(binario, caracter => caracter.charCodeAt(0));
    return JSON.parse(new TextDecoder().decode(bytes)) as ClaimsUsuarioJwt;
  }

  /** Actualiza los datos visibles después de editar el perfil. */
  actualizarDatosPerfil(nombreUsuario: string, nombreCompleto: string): void {
    const sesion = this.obtenerUsuarioActual();
    if (!sesion) return;
    localStorage.setItem(CLAVE_ALMACENAMIENTO, JSON.stringify({ ...sesion, nombreUsuario, nombreCompleto }));
  }

  /**
   * Cierra la sesión local y, antes de navegar al login, invalida también la
   * sesión HTTP del servidor OAuth. withCredentials envía la cookie de sesión
   * al origen de la API configurado; el Bearer JWT lo agrega tokenInterceptor.
   */
  async cerrarSesion(): Promise<void> {
    const sesion = this.leerSesionGuardada();

    if (!sesion?.token) {
      this.limpiarSesionLocal();
      return;
    }

    try {
      await firstValueFrom(this.http.delete<void>(
        `${environment.apiUrl}/api/autenticacion/cerrar-sesion`,
        { withCredentials: true },
      ));
      this.limpiarSesionLocal();
    } catch (error) {
      console.error('No se pudo invalidar la sesión de RecetON en el servidor.', error);
      // No borrar el JWT ni redirigir al flujo OAuth si el servidor no confirmó
      // el cierre: hacerlo provocaría que el navegador reutilizara JSESSIONID.
      throw new Error('No se pudo cerrar la sesión en el servidor. Comprueba la conexión e inténtalo de nuevo.');
    }
  }

  limpiarSesionLocal(): void {
    localStorage.removeItem(CLAVE_ALMACENAMIENTO);
    this.limpiarDatosTemporalesOAuth();
  }

  private limpiarDatosTemporalesOAuth(): void {
    sessionStorage.removeItem(CLAVE_OAUTH_STATE);
    sessionStorage.removeItem(CLAVE_OAUTH_VERIFIER);
  }

  private leerSesionGuardada(): UsuarioSesion | null {
    const datos = localStorage.getItem(CLAVE_ALMACENAMIENTO);
    if (!datos) return null;
    try {
      const sesion = JSON.parse(datos) as Partial<UsuarioSesion>;
      if (typeof sesion.id !== 'number' || typeof sesion.token !== 'string' || sesion.token.split('.').length !== 3) {
        return null;
      }
      return {
        id: sesion.id,
        nombreUsuario: sesion.nombreUsuario ?? '',
        nombreCompleto: sesion.nombreCompleto ?? '',
        roles: Array.isArray(sesion.roles) ? sesion.roles.filter((rol): rol is string => typeof rol === 'string') : [],
        token: sesion.token,
        refreshToken: typeof sesion.refreshToken === 'string' ? sesion.refreshToken : undefined,
        expiraEn: typeof sesion.expiraEn === 'number' ? sesion.expiraEn : undefined,
      };
    } catch {
      return null;
    }
  }

  obtenerUsuarioActual(): UsuarioSesion | null {
    const sesion = this.leerSesionGuardada();
    if (!sesion) return null;
    if (typeof sesion.expiraEn === 'number' && sesion.expiraEn <= Date.now()) {
      localStorage.removeItem(CLAVE_ALMACENAMIENTO);
      return null;
    }
    return sesion;
  }

  obtenerToken(): string | null {
    return this.obtenerUsuarioActual()?.token ?? null;
  }

  esAdministrador(): boolean {
    const roles = this.obtenerUsuarioActual()?.roles ?? [];
    return roles.some((rol: string) => {
      const normalizado = rol.normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim().toLowerCase();
      return ['administrador', 'admin', 'administrator'].includes(normalizado);
    });
  }

  obtenerEtiquetaRol(): string {
    return this.esAdministrador() ? 'Administrador' : 'Usuario';
  }

  estaAutenticado(): boolean {
    return this.obtenerToken() !== null;
  }
}
