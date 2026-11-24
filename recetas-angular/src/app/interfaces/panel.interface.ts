import { Categoria } from './categoria.interface';
import { Receta } from './receta.interface';

/** Datos que necesitan las 4 pantallas para dibujarse. */
export interface DatosPanel {
  categorias: Categoria[];
  recetas: Receta[];
}
