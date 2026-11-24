/** Colores de etiqueta disponibles para una tarjeta de receta. */
export type ColorEtiqueta = 'squash' | 'sage' | 'plum';

/** Receta tal como la usa toda la aplicación Angular (nombres en español). */
export interface Receta {
  id: number;
  nombre: string;
  imagen: string;
  precio: string;
  calificacion: number;
  tiempo: string;
  etiqueta?: string;
  colorEtiqueta?: ColorEtiqueta;
  descripcion?: string | null;
  idUsuario?: number | null;
  idPais?: number | null;
  porciones?: number | null;
  dificultad?: string | null;
  activo?: boolean | null;
}

/** Forma cruda que entrega la API de Spring Boot (campos en inglés). */
export interface RecetaApi {
  id: number;
  name: string;
  image: string;
  price: string;
  rating: number;
  time: string;
  tag: string | null;
  tagColor: ColorEtiqueta | null;
  description?: string | null;
  idUsuario?: number | null;
  idPais?: number | null;
  servings?: number | null;
  difficulty?: string | null;
  active?: boolean | null;
}
