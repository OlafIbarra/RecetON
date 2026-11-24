import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Categoria, CategoriaApi } from '../interfaces/categoria.interface';
import { Receta, RecetaApi } from '../interfaces/receta.interface';
import {
  ActualizacionIngredienteReceta,
  DetalleReceta,
  DetalleRecetaApi,
  EstadoFavorito,
  ImagenReceta,
  SolicitudEquipoReceta,
  SolicitudInformacionNutricional,
  SolicitudIngredienteReceta,
  SolicitudImagenReceta,
  SolicitudPasoReceta,
  SolicitudValoracionReceta,
} from '../interfaces/detalle-receta.interface';
import { DatosPanel } from '../interfaces/panel.interface';
import { CATEGORIAS_RESPALDO, RECETAS_RESPALDO } from '../datos/datos-respaldo';

/**
 * Único punto de la app que sabe que el backend de Spring Boot devuelve
 * los campos de datos en inglés (name, image, price...), aunque las URL están en español. Aquí se traduce a las
 * interfaces en español (Receta, Categoria) que usa el resto de la app,
 * así el resto de los componentes nunca ven una palabra en inglés.
 *
 * Si el backend no responde (por ejemplo, porque todavía solo estás
 * trabajando en el frontend), cada método cae en datos de respaldo
 * (`datos-respaldo.ts`) en vez de romper la pantalla. Esto beneficia a
 * las 4 pantallas por igual, porque todas llaman a estos mismos métodos.
 */
@Injectable({
  providedIn: 'root',
})
export class RecetaApiServicio {
  private readonly http: HttpClient = inject(HttpClient);

  /** true si la última carga tuvo que usar los datos de respaldo. */
  estaEnModoRespaldo: boolean = false;

  obtenerRecetas(): Observable<Receta[]> {
    const url: string = `${environment.apiUrl}/api/recetas`;
    return this.http.get<RecetaApi[]>(url).pipe(
      map((lista: RecetaApi[]) => lista.map(this.traducirReceta)),
      tap(() => (this.estaEnModoRespaldo = false)),
      catchError(() => {
        console.warn('No se pudo conectar con el backend; usando recetas de respaldo.');
        this.estaEnModoRespaldo = true;
        return of(RECETAS_RESPALDO);
      })
    );
  }

  obtenerRecetaPorId(id: number): Observable<Receta | undefined> {
    return this.obtenerRecetas().pipe(
      map((recetas: Receta[]) => recetas.find((receta: Receta) => receta.id === id))
    );
  }

  /** Consulta los datos completos de la receta y sus relaciones en PostgreSQL. */
  obtenerDetalleReceta(id: number): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${id}/detalle`;
    return this.http.get<DetalleRecetaApi>(url).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle)),
      tap(() => (this.estaEnModoRespaldo = false))
    );
  }

  /** Agrega un ingrediente a una receta. El backend lo busca o crea en el catálogo existente. */
  agregarIngredienteReceta(idReceta: number, solicitud: SolicitudIngredienteReceta): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/ingredientes`;
    return this.http.post<DetalleRecetaApi>(url, solicitud).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle))
    );
  }

  /** Actualiza la cantidad o unidad del ingrediente asociado. */
  actualizarIngredienteReceta(idReceta: number, idIngrediente: number, solicitud: ActualizacionIngredienteReceta): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/ingredientes/${idIngrediente}`;
    return this.http.put<DetalleRecetaApi>(url, solicitud).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle))
    );
  }

  /** Quita la relación con la receta sin borrar el ingrediente del catálogo. */
  eliminarIngredienteReceta(idReceta: number, idIngrediente: number): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/ingredientes/${idIngrediente}`;
    return this.http.delete<DetalleRecetaApi>(url).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle))
    );
  }

  /** Agrega un paso de preparación a una receta. */
  agregarPasoReceta(idReceta: number, solicitud: SolicitudPasoReceta): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/pasos`;
    return this.http.post<DetalleRecetaApi>(url, solicitud).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle))
    );
  }

  /** Actualiza un paso que pertenece a la receta indicada. */
  actualizarPasoReceta(idReceta: number, idPaso: number, solicitud: SolicitudPasoReceta): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/pasos/${idPaso}`;
    return this.http.put<DetalleRecetaApi>(url, solicitud).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle))
    );
  }

  /** Elimina un paso perteneciente a esa receta. */
  eliminarPasoReceta(idReceta: number, idPaso: number): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/pasos/${idPaso}`;
    return this.http.delete<DetalleRecetaApi>(url).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle))
    );
  }

  /** Relaciona un equipo de cocina existente o nuevo con la receta. */
  agregarEquipoReceta(idReceta: number, solicitud: SolicitudEquipoReceta): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/equipos`;
    return this.http.post<DetalleRecetaApi>(url, solicitud).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle))
    );
  }

  /** Quita la relación con un equipo, sin eliminar el equipo del catálogo. */
  eliminarEquipoReceta(idReceta: number, idEquipo: number): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/equipos/${idEquipo}`;
    return this.http.delete<DetalleRecetaApi>(url).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle))
    );
  }

  /** Crea o actualiza los datos nutricionales de una receta. */
  guardarInformacionNutricional(idReceta: number, solicitud: SolicitudInformacionNutricional): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/informacion-nutricional`;
    return this.http.put<DetalleRecetaApi>(url, solicitud).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle))
    );
  }

  /** Elimina los datos nutricionales de la receta, no la receta. */
  eliminarInformacionNutricional(idReceta: number): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/informacion-nutricional`;
    return this.http.delete<DetalleRecetaApi>(url).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle))
    );
  }

  /** Registra una valoración relacionada con un usuario y una receta existentes. */
  agregarValoracionReceta(idReceta: number, solicitud: SolicitudValoracionReceta): Observable<DetalleReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/valoraciones`;
    return this.http.post<DetalleRecetaApi>(url, solicitud).pipe(
      map((detalle: DetalleRecetaApi) => this.traducirDetalle(detalle))
    );
  }

  /** Consulta imágenes con ID y estado de portada para administrarlas desde el detalle. */
  obtenerImagenesReceta(idReceta: number): Observable<ImagenReceta[]> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/imagenes`;
    return this.http.get<ImagenReceta[]>(url);
  }

  /** Agrega una URL de imagen a la tabla imagenes_receta existente. */
  agregarImagenReceta(idReceta: number, solicitud: SolicitudImagenReceta): Observable<ImagenReceta> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/imagenes`;
    return this.http.post<ImagenReceta>(url, solicitud);
  }

  /** Marca una de las imágenes registradas como portada. */
  establecerImagenPortada(idReceta: number, idImagen: number): Observable<ImagenReceta[]> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/imagenes/${idImagen}/portada`;
    return this.http.put<ImagenReceta[]>(url, {});
  }

  /** Elimina una imagen específica y conserva la relación de las demás. */
  eliminarImagenReceta(idReceta: number, idImagen: number): Observable<ImagenReceta[]> {
    const url: string = `${environment.apiUrl}/api/recetas/${idReceta}/imagenes/${idImagen}`;
    return this.http.delete<ImagenReceta[]>(url);
  }

  /** Consulta si el usuario marcó una receta como favorita. */
  consultarFavorito(idUsuario: number, idReceta: number): Observable<EstadoFavorito> {
    const url: string = `${environment.apiUrl}/api/usuarios/${idUsuario}/favoritos/${idReceta}`;
    return this.http.get<EstadoFavorito>(url);
  }

  /** Agrega la receta a favoritos mediante la clave compuesta ya existente. */
  agregarFavorito(idUsuario: number, idReceta: number): Observable<EstadoFavorito> {
    const url: string = `${environment.apiUrl}/api/usuarios/${idUsuario}/favoritos/${idReceta}`;
    return this.http.put<EstadoFavorito>(url, {});
  }

  /** Quita la relación de favoritos, sin eliminar la receta ni al usuario. */
  eliminarFavorito(idUsuario: number, idReceta: number): Observable<EstadoFavorito> {
    const url: string = `${environment.apiUrl}/api/usuarios/${idUsuario}/favoritos/${idReceta}`;
    return this.http.delete<EstadoFavorito>(url);
  }

  /** Devuelve las recetas favoritas del usuario usando la tabla favoritos. */
  obtenerFavoritosUsuario(idUsuario: number): Observable<Receta[]> {
    const url: string = `${environment.apiUrl}/api/usuarios/${idUsuario}/favoritos`;
    return this.http.get<RecetaApi[]>(url).pipe(
      map((lista: RecetaApi[]) => lista.map(this.traducirReceta))
    );
  }

  /** Pide categorías y recetas a la vez; lo usan las 4 pantallas. */
  obtenerDatosPanel(): Observable<DatosPanel> {
    return forkJoin({
      categorias: this.obtenerCategorias(),
      recetas: this.obtenerRecetas(),
    });
  }

  /** Filtra recetas por nombre (buscador de las pantallas). */
  filtrarPorNombre(recetas: Receta[], texto: string): Receta[] {
    const textoNormalizado: string = texto.trim().toLowerCase();
    return textoNormalizado
      ? recetas.filter((receta: Receta) => receta.nombre.toLowerCase().includes(textoNormalizado))
      : recetas;
  }

  obtenerCategorias(): Observable<Categoria[]> {
    const url: string = `${environment.apiUrl}/api/categorias`;
    return this.http.get<CategoriaApi[]>(url).pipe(
      map((lista: CategoriaApi[]) => lista.map(this.traducirCategoria)),
      catchError(() => {
        console.warn('No se pudo conectar con el backend; usando categorías de respaldo.');
        this.estaEnModoRespaldo = true;
        return of(CATEGORIAS_RESPALDO);
      })
    );
  }

  private traducirReceta(receta: RecetaApi): Receta {
    return {
      id: receta.id,
      nombre: receta.name,
      imagen: receta.image,
      precio: receta.price,
      calificacion: receta.rating,
      tiempo: receta.time,
      etiqueta: receta.tag ?? undefined,
      colorEtiqueta: receta.tagColor ?? undefined,
      descripcion: receta.description ?? null,
      idUsuario: receta.idUsuario ?? null,
      idPais: receta.idPais ?? null,
      porciones: receta.servings ?? null,
      dificultad: receta.difficulty ?? null,
      activo: receta.active ?? null,
    };
  }

  private traducirDetalle(detalle: DetalleRecetaApi): DetalleReceta {
    return {
      receta: this.traducirReceta(detalle.receta),
      paisOrigen: detalle.paisOrigen,
      categorias: detalle.categorias.map((categoria: CategoriaApi) => categoria.name),
      imagenes: detalle.imagenes ?? [],
      ingredientes: detalle.ingredientes ?? [],
      pasos: detalle.pasos ?? [],
      equipos: detalle.equipos ?? [],
      informacionNutricional: detalle.informacionNutricional,
      valoraciones: detalle.valoraciones ?? [],
    };
  }

  private traducirCategoria(categoria: CategoriaApi): Categoria {
    return {
      id: categoria.id,
      nombre: categoria.name,
      imagen: categoria.image,
    };
  }
}
