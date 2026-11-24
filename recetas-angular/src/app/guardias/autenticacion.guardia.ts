import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AutenticacionServicio } from '../servicios/autenticacion.servicio';

/**
 * Protege las rutas que requieren sesión iniciada.
 * Si no hay sesión, redirige a /inicio-sesion en vez de dejar pasar.
 */
export const autenticacionGuardia: CanActivateFn = () => {
  const autenticacionServicio: AutenticacionServicio = inject(AutenticacionServicio);
  const router: Router = inject(Router);

  if (autenticacionServicio.estaAutenticado()) {
    return true;
  }

  router.navigate(['/inicio-sesion']);
  return false;
};
