import { CategoriaApi } from './categoria.interface';
import { Receta, RecetaApi } from './receta.interface';

/** Estructura JSON que entrega GET /api/recetas/{id}/detalle. */
export interface DetalleRecetaApi {
  receta: RecetaApi;
  paisOrigen: string | null;
  categorias: CategoriaApi[];
  imagenes: string[];
  ingredientes: IngredienteRecetaApi[];
  pasos: PasoRecetaApi[];
  equipos: EquipoCocinaRecetaApi[];
  informacionNutricional: InformacionNutricionalApi | null;
  valoraciones: ValoracionRecetaApi[];
}

export interface IngredienteRecetaApi {
  idIngrediente: number;
  nombre: string;
  cantidad: number | null;
  unidad: string | null;
  abreviatura: string | null;
}

export interface PasoRecetaApi {
  idPaso: number;
  numeroPaso: number | null;
  descripcion: string;
  tiempoEstimadoMin: number | null;
}

export interface EquipoCocinaReceta {
  idEquipo: number;
  nombre: string;
}

export interface EquipoCocinaRecetaApi {
  idEquipo: number;
  nombre: string;
}

export interface InformacionNutricionalApi {
  calorias: number | null;
  proteinasG: number | null;
  carbohidratosG: number | null;
  grasasG: number | null;
  azucaresG: number | null;
  sodioMg: number | null;
}

export interface ValoracionRecetaApi {
  idValoracion: number;
  usuario: string;
  puntuacion: number | null;
  comentario: string | null;
  fecha: string | null;
}

/** Cuerpo enviado al crear o modificar la relación de un ingrediente. */
export interface SolicitudIngredienteReceta {
  nombreIngrediente: string;
  cantidad: number | null;
  nombreUnidad: string | null;
  abreviaturaUnidad: string | null;
}

/** Cuerpo para modificar la cantidad o unidad de un ingrediente ya relacionado. */
export interface ActualizacionIngredienteReceta {
  cantidad: number | null;
  nombreUnidad: string | null;
  abreviaturaUnidad: string | null;
}

/** Cuerpo enviado para crear o modificar un paso de preparación. */
export interface SolicitudPasoReceta {
  numeroPaso: number | null;
  descripcion: string;
  tiempoEstimadoMin: number | null;
}

export interface SolicitudEquipoReceta {
  nombreEquipo: string;
}

export interface SolicitudInformacionNutricional {
  calorias: number | null;
  proteinasG: number | null;
  carbohidratosG: number | null;
  grasasG: number | null;
  azucaresG: number | null;
  sodioMg: number | null;
}

export interface SolicitudValoracionReceta {
  idUsuario: number;
  puntuacion: number;
  comentario: string | null;
}

/** Modelo con nombres en español que consumen los componentes Angular. */
export interface DetalleReceta {
  receta: Receta;
  paisOrigen: string | null;
  categorias: string[];
  imagenes: string[];
  ingredientes: IngredienteReceta[];
  pasos: PasoReceta[];
  equipos: EquipoCocinaReceta[];
  informacionNutricional: InformacionNutricional | null;
  valoraciones: ValoracionReceta[];
}

export interface IngredienteReceta {
  idIngrediente: number;
  nombre: string;
  cantidad: number | null;
  unidad: string | null;
  abreviatura: string | null;
}

export interface PasoReceta {
  idPaso: number;
  numeroPaso: number | null;
  descripcion: string;
  tiempoEstimadoMin: number | null;
}

export interface InformacionNutricional {
  calorias: number | null;
  proteinasG: number | null;
  carbohidratosG: number | null;
  grasasG: number | null;
  azucaresG: number | null;
  sodioMg: number | null;
}

export interface ValoracionReceta {
  idValoracion: number;
  usuario: string;
  puntuacion: number | null;
  comentario: string | null;
  fecha: string | null;
}


/** Imagen individual de una receta, con su identificador y estado de portada. */
export interface ImagenReceta {
  idImagen: number;
  idReceta: number;
  urlImagen: string;
  esPortada: boolean;
}

export interface SolicitudImagenReceta {
  urlImagen: string;
  esPortada: boolean;
}

export interface EstadoFavorito {
  favorito: boolean;
  fechaAgregado: string | null;
}
