import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CategoriaApi } from '../interfaces/categoria.interface';
import { RecetaApi } from '../interfaces/receta.interface';
import {
  SolicitudCategoriaAdministracion,
  SolicitudRecetaAdministracion,
  SolicitudUsuarioAdministracion,
  UsuarioAdministracion,
} from '../interfaces/administracion.interface';

/** Operaciones CRUD reservadas a administradores. La API también valida el rol en el servidor. */
@Injectable({ providedIn: 'root' })
export class AdministracionApiServicio {
  private readonly http: HttpClient = inject(HttpClient);
  private readonly baseUrl: string = environment.apiUrl;

  listarRecetas(): Observable<RecetaApi[]> { return this.http.get<RecetaApi[]>(`${this.baseUrl}/api/recetas`); }
  crearReceta(datos: SolicitudRecetaAdministracion): Observable<RecetaApi> { return this.http.post<RecetaApi>(`${this.baseUrl}/api/recetas`, datos); }
  actualizarReceta(id: number, datos: SolicitudRecetaAdministracion): Observable<RecetaApi> { return this.http.put<RecetaApi>(`${this.baseUrl}/api/recetas/${id}`, datos); }
  eliminarReceta(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/api/recetas/${id}`); }

  listarCategorias(): Observable<CategoriaApi[]> { return this.http.get<CategoriaApi[]>(`${this.baseUrl}/api/categorias`); }
  crearCategoria(datos: SolicitudCategoriaAdministracion): Observable<CategoriaApi> { return this.http.post<CategoriaApi>(`${this.baseUrl}/api/categorias`, datos); }
  actualizarCategoria(id: number, datos: SolicitudCategoriaAdministracion): Observable<CategoriaApi> { return this.http.put<CategoriaApi>(`${this.baseUrl}/api/categorias/${id}`, datos); }
  eliminarCategoria(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/api/categorias/${id}`); }

  listarUsuarios(): Observable<UsuarioAdministracion[]> { return this.http.get<UsuarioAdministracion[]>(`${this.baseUrl}/api/usuarios`); }
  crearUsuario(datos: SolicitudUsuarioAdministracion): Observable<UsuarioAdministracion> { return this.http.post<UsuarioAdministracion>(`${this.baseUrl}/api/usuarios`, datos); }
  actualizarUsuario(id: number, datos: SolicitudUsuarioAdministracion): Observable<UsuarioAdministracion> { return this.http.put<UsuarioAdministracion>(`${this.baseUrl}/api/usuarios/${id}`, datos); }
  eliminarUsuario(id: number): Observable<void> { return this.http.delete<void>(`${this.baseUrl}/api/usuarios/${id}`); }
}
