import { NombreIcono } from './icono.interface';

/** Una pestaña de navegación que lleva a una función existente de la app. */
export interface Pestana {
  etiqueta: string;
  icono: NombreIcono;
  ruta: string;
}
