import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AutenticacionServicio } from '../servicios/autenticacion.servicio';

/** Restringe las pantallas de administración al rol asignado en PostgreSQL. */
export const administradorGuardia: CanActivateFn = () => {
  const autenticacion = inject(AutenticacionServicio);
  const router = inject(Router);
  if (autenticacion.estaAutenticado() && autenticacion.esAdministrador()) return true;
  void router.navigate(['/inicio']);
  return false;
};
