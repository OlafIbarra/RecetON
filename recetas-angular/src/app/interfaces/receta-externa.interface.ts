/** Contratos de TheMealDB. Estos datos externos no se guardan en form_recetas. */
export interface RecetaExternaApi {
  meals: MealApi[] | null;
}

export interface MealApi {
  idMeal: string;
  strMeal: string;
  strMealThumb: string;
  strArea?: string | null;
  strCountry?: string | null;
  strCategory?: string | null;
  strInstructions?: string | null;
  strSource?: string | null;
  strYoutube?: string | null;
  [campo: string]: string | null | undefined;
}

export interface CategoriasTheMealDBApi {
  categories: Array<{ strCategory: string }> | null;
}

export interface ListaTheMealDBApi {
  meals: Array<{
    strArea?: string | null;
    strCountry?: string | null;
    strCategory?: string | null;
    strIngredient?: string | null;
  }> | null;
}

export interface OpcionOrigenExterna {
  /** Valor original que TheMealDB espera en filter.php?a=. */
  valor: string;
  /** País o región traducido para que el usuario no tenga que conocer el gentilicio inglés. */
  etiqueta: string;
}

export interface IngredienteExterno {
  nombre: string;
  medida: string;
}

/** Modelo listo para mostrar en las pantallas de RecetON. */
export interface RecetaExterna {
  id: string;
  nombre: string;
  imagen: string;
  categoria: string;
  origen: string;
  instrucciones: string;
  ingredientes: IngredienteExterno[];
  fuente: string | null;
  video: string | null;
}

export type TipoBusquedaExterna = 'nombre' | 'ingrediente';
