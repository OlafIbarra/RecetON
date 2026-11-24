import { NombreIcono } from './icono.interface';

/** Una de las 4 pantallas que se puede elegir en el índice. */
export interface OpcionPantalla {
  titulo: string;
  descripcion: string;
  ruta: string;
  icono: NombreIcono;
  anchoReferencia: string;
}
