/** Categoría de recetas (Ensaladas, Postres, etc.) dentro de la aplicación Angular. */
export interface Categoria {
  id: number;
  nombre: string;
  imagen: string;
}

/**
 * Forma cruda que entrega la API de Spring Boot (con nombres de campo en
 * inglés, tal como se definió el backend). RecetaApiServicio la traduce
 * a Categoria antes de que el resto de la app la use.
 */
export interface CategoriaApi {
  id: number;
  name: string;
  image: string;
}
