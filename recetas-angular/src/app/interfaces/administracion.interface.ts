import { ColorEtiqueta, RecetaApi } from './receta.interface';
import { CategoriaApi } from './categoria.interface';

export type VistaAdministracion = 'recetas' | 'categorias' | 'usuarios';

export interface SolicitudRecetaAdministracion {
  name: string;
  image: string;
  price: string;
  rating: number;
  time: string;
  tag: string;
  tagColor: ColorEtiqueta;
  description: string;
  idUsuario: number;
  idPais: number | null;
  servings: number;
  difficulty: string;
  active: boolean;
}

export interface SolicitudCategoriaAdministracion {
  name: string;
  image: string;
}

export interface UsuarioAdministracion {
  id: number;
  username: string;
  email: string | null;
  fullName: string;
  dateRegistered: string | null;
  active: boolean | null;
}

export interface SolicitudUsuarioAdministracion {
  username: string;
  email: string;
  password: string;
  fullName: string;
  active: boolean;
}

export type RecetaAdministracion = RecetaApi;
export type CategoriaAdministracion = CategoriaApi;
