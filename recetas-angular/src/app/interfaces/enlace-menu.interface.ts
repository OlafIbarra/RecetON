import { NombreIcono } from './icono.interface';

/** Un elemento de la barra lateral (TV) o de la barra inferior (teléfono/tableta). */
export interface EnlaceMenu {
  etiqueta: string;
  icono: NombreIcono;
  ruta?: string;
}
