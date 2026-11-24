import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AutenticacionServicio } from './autenticacion.servicio';

/** Envía el token a Spring Boot y devuelve al login si la sesión caducó. */
export const tokenInterceptor: HttpInterceptorFn = (solicitud, siguiente) => {
  const autenticacion = inject(AutenticacionServicio);
  const router = inject(Router);
  const esLoginAnterior = solicitud.url.includes('/api/autenticacion/iniciar-sesion');
  const esEndpointOAuth = solicitud.url.includes('/oauth2/token') || solicitud.url.includes('/oauth2/revoke');
  const esApiPropia = solicitud.url.startsWith(environment.apiUrl);
  const token = autenticacion.obtenerToken();

  if (!esApiPropia || esLoginAnterior || esEndpointOAuth || !token) return siguiente(solicitud);

  const solicitudAutenticada = solicitud.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });

  return siguiente(solicitudAutenticada).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        autenticacion.limpiarSesionLocal();
        void router.navigate(['/inicio-sesion'], { queryParams: { sesion: 'expirada' } });
      }
      return throwError(() => error);
    })
  );
};
