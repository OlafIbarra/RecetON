import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HistorialNavegacionServicio } from '../../servicios/historial-navegacion.servicio';
import { Observable } from 'rxjs';
import {
  ActualizacionIngredienteReceta,
  DetalleReceta,
  EquipoCocinaReceta,
  IngredienteReceta,
  ImagenReceta,
  EstadoFavorito,
  PasoReceta,
  SolicitudEquipoReceta,
  SolicitudInformacionNutricional,
  SolicitudImagenReceta,
  SolicitudIngredienteReceta,
  SolicitudPasoReceta,
  SolicitudValoracionReceta,
} from '../../interfaces/detalle-receta.interface';
import { RecetaApiServicio } from '../../servicios/receta-api.servicio';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { UsuarioSesion } from '../../interfaces/usuario.interface';

@Component({
  selector: 'app-detalle-receta',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './detalle-receta.component.html',
  styleUrl: './detalle-receta.component.css',
})
export class DetalleRecetaComponent implements OnInit {
  private readonly ruta: ActivatedRoute = inject(ActivatedRoute);
  private readonly recetaApiServicio: RecetaApiServicio = inject(RecetaApiServicio);
  private readonly autenticacionServicio: AutenticacionServicio = inject(AutenticacionServicio);
  private readonly historialNavegacion = inject(HistorialNavegacionServicio);

  detalle: DetalleReceta | undefined;
  cargando: boolean = true;
  mensajeError: string | null = null;
  mensajeAccion: string | null = null;
  errorAccion: string | null = null;
  guardandoContenido: boolean = false;
  imagenesAdministrables: ImagenReceta[] = [];
  mostrarFormularioImagen: boolean = false;
  guardandoImagen: boolean = false;
  nuevaImagen: SolicitudImagenReceta = { urlImagen: '', esPortada: false };
  esFavorita: boolean = false;
  cargandoFavorito: boolean = false;
  guardandoFavorito: boolean = false;

  mostrarFormularioIngrediente: boolean = false;
  mostrarFormularioPaso: boolean = false;
  mostrarFormularioEquipo: boolean = false;
  mostrarFormularioNutricion: boolean = false;
  ingredienteEditandoId: number | null = null;
  pasoEditandoId: number | null = null;

  nuevoIngrediente = {
    nombreIngrediente: '',
    cantidad: null as number | null,
    nombreUnidad: '',
    abreviaturaUnidad: '',
  };

  ingredienteEnEdicion = {
    cantidad: null as number | null,
    nombreUnidad: '',
    abreviaturaUnidad: '',
  };

  nuevoPaso = {
    numeroPaso: null as number | null,
    descripcion: '',
    tiempoEstimadoMin: null as number | null,
  };

  pasoEnEdicion = {
    numeroPaso: null as number | null,
    descripcion: '',
    tiempoEstimadoMin: null as number | null,
  };

  nuevoEquipo: string = '';

  nutricionEnEdicion: SolicitudInformacionNutricional = {
    calorias: null,
    proteinasG: null,
    carbohidratosG: null,
    grasasG: null,
    azucaresG: null,
    sodioMg: null,
  };

  nuevaValoracion = {
    puntuacion: 5,
    comentario: '',
  };

  ngOnInit(): void {
    const id: number = Number(this.ruta.snapshot.paramMap.get('id'));

    if (!Number.isInteger(id) || id <= 0) {
      this.cargando = false;
      this.mensajeError = 'El identificador de la receta no es válido.';
      return;
    }

    this.recetaApiServicio.obtenerDetalleReceta(id).subscribe({
      next: (detalle: DetalleReceta) => {
        this.detalle = detalle;
        this.cargando = false;
        this.cargarImagenes(id);
        this.cargarEstadoFavorito(id);
      },
      error: (error: HttpErrorResponse) => {
        this.cargando = false;
        if (error.status === 404) {
          this.mensajeError = 'No se encontró esa receta en la base de datos.';
        } else if (error.status === 0) {
          this.mensajeError = 'No se pudo conectar con Spring Boot. Comprueba que el backend esté iniciado.';
        } else {
          this.mensajeError = `No se pudo cargar el detalle de la receta (error ${error.status}).`;
        }
      },
    });
  }

  private cargarImagenes(idReceta: number): void {
    this.recetaApiServicio.obtenerImagenesReceta(idReceta).subscribe({
      next: (imagenes: ImagenReceta[]) => { this.imagenesAdministrables = imagenes; },
      error: (error: HttpErrorResponse) => {
        this.errorAccion = this.mensajeDeError(error);
      },
    });
  }

  private cargarEstadoFavorito(idReceta: number): void {
    const usuario: UsuarioSesion | null = this.usuarioActual;
    if (!usuario) {
      this.esFavorita = false;
      return;
    }
    this.cargandoFavorito = true;
    this.recetaApiServicio.consultarFavorito(usuario.id, idReceta).subscribe({
      next: (estado: EstadoFavorito) => {
        this.esFavorita = estado.favorito;
        this.cargandoFavorito = false;
      },
      error: (error: HttpErrorResponse) => {
        this.cargandoFavorito = false;
        this.errorAccion = this.mensajeDeError(error);
      },
    });
  }

  alternarFavorito(): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    const usuario: UsuarioSesion | null = this.usuarioActual;
    if (!detalle || !usuario) {
      this.errorAccion = 'Inicia sesión para guardar recetas favoritas.';
      return;
    }
    this.guardandoFavorito = true;
    this.errorAccion = null;
    this.mensajeAccion = null;
    const peticion = this.esFavorita
      ? this.recetaApiServicio.eliminarFavorito(usuario.id, detalle.receta.id)
      : this.recetaApiServicio.agregarFavorito(usuario.id, detalle.receta.id);
    peticion.subscribe({
      next: (estado: EstadoFavorito) => {
        this.esFavorita = estado.favorito;
        this.guardandoFavorito = false;
        this.mensajeAccion = estado.favorito
          ? 'La receta se agregó a tus favoritos.'
          : 'La receta se quitó de tus favoritos.';
      },
      error: (error: HttpErrorResponse) => {
        this.guardandoFavorito = false;
        this.errorAccion = this.mensajeDeError(error);
      },
    });
  }

  mostrarAgregarImagen(): void {
    this.mostrarFormularioImagen = !this.mostrarFormularioImagen;
    this.mensajeAccion = null;
    this.errorAccion = null;
  }

  guardarImagen(): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    const urlImagen: string = this.nuevaImagen.urlImagen.trim();
    if (!detalle || !urlImagen) {
      this.errorAccion = 'Escribe el enlace directo de la foto.';
      return;
    }
    if (urlImagen.length > 255 || !(urlImagen.startsWith('https://') || urlImagen.startsWith('http://'))) {
      this.errorAccion = 'La URL debe comenzar con http:// o https:// y no superar 255 caracteres.';
      return;
    }
    const solicitud: SolicitudImagenReceta = { urlImagen, esPortada: this.nuevaImagen.esPortada };
    this.guardandoImagen = true;
    this.errorAccion = null;
    this.mensajeAccion = null;
    this.recetaApiServicio.agregarImagenReceta(detalle.receta.id, solicitud).subscribe({
      next: () => {
        this.guardandoImagen = false;
        this.mostrarFormularioImagen = false;
        this.nuevaImagen = { urlImagen: '', esPortada: false };
        this.mensajeAccion = 'La imagen se agregó correctamente.';
        this.recargarDetalleEImagenes(detalle.receta.id);
      },
      error: (error: HttpErrorResponse) => {
        this.guardandoImagen = false;
        this.errorAccion = this.mensajeDeError(error);
      },
    });
  }

  establecerComoPortada(imagen: ImagenReceta): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    if (!detalle || imagen.esPortada) return;
    this.guardandoImagen = true;
    this.errorAccion = null;
    this.recetaApiServicio.establecerImagenPortada(detalle.receta.id, imagen.idImagen).subscribe({
      next: (imagenes: ImagenReceta[]) => {
        this.imagenesAdministrables = imagenes;
        this.guardandoImagen = false;
        this.mensajeAccion = 'La foto principal se actualizó.';
        this.recargarDetalle(detalle.receta.id);
      },
      error: (error: HttpErrorResponse) => {
        this.guardandoImagen = false;
        this.errorAccion = this.mensajeDeError(error);
      },
    });
  }

  eliminarImagen(imagen: ImagenReceta): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    if (!detalle || !confirm('¿Eliminar esta imagen de la receta?')) return;
    this.guardandoImagen = true;
    this.errorAccion = null;
    this.recetaApiServicio.eliminarImagenReceta(detalle.receta.id, imagen.idImagen).subscribe({
      next: (imagenes: ImagenReceta[]) => {
        this.imagenesAdministrables = imagenes;
        this.guardandoImagen = false;
        this.mensajeAccion = 'La imagen se eliminó correctamente.';
        this.recargarDetalle(detalle.receta.id);
      },
      error: (error: HttpErrorResponse) => {
        this.guardandoImagen = false;
        this.errorAccion = this.mensajeDeError(error);
      },
    });
  }

  private recargarDetalleEImagenes(idReceta: number): void {
    this.cargarImagenes(idReceta);
    this.recargarDetalle(idReceta);
  }

  private recargarDetalle(idReceta: number): void {
    this.recetaApiServicio.obtenerDetalleReceta(idReceta).subscribe({
      next: (detalle: DetalleReceta) => { this.detalle = detalle; },
      error: (error: HttpErrorResponse) => { this.errorAccion = this.mensajeDeError(error); },
    });
  }

  get imagenPrincipal(): string {
    const imagenReceta: string = this.detalle?.receta.imagen ?? '';
    return imagenReceta || this.detalle?.imagenes[0] || '';
  }

  get imagenesAdicionales(): string[] {
    const imagenPrincipal: string = this.imagenPrincipal;
    return (this.detalle?.imagenes ?? []).filter(
      (imagen: string) => imagen.length > 0 && imagen !== imagenPrincipal
    );
  }

  get siguienteNumeroPaso(): number {
    const pasos: number[] = (this.detalle?.pasos ?? [])
      .map((paso: PasoReceta) => paso.numeroPaso ?? 0);
    return pasos.length ? Math.max(...pasos) + 1 : 1;
  }

  mostrarAgregarIngrediente(): void {
    this.mostrarFormularioIngrediente = !this.mostrarFormularioIngrediente;
    this.mensajeAccion = null;
    this.errorAccion = null;
  }

  mostrarAgregarPaso(): void {
    this.mostrarFormularioPaso = !this.mostrarFormularioPaso;
    this.mensajeAccion = null;
    this.errorAccion = null;
  }

  get usuarioActual(): UsuarioSesion | null {
    return this.autenticacionServicio.obtenerUsuarioActual();
  }

  get esAdministrador(): boolean {
    return this.autenticacionServicio.esAdministrador();
  }

  mostrarAgregarEquipo(): void {
    this.mostrarFormularioEquipo = !this.mostrarFormularioEquipo;
    this.mensajeAccion = null;
    this.errorAccion = null;
  }

  guardarEquipo(): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    const nombreEquipo: string = this.nuevoEquipo.trim();
    if (!detalle || !nombreEquipo) {
      this.errorAccion = 'Escribe el nombre del equipo de cocina.';
      return;
    }
    const solicitud: SolicitudEquipoReceta = { nombreEquipo };
    this.procesarCambio(
      this.recetaApiServicio.agregarEquipoReceta(detalle.receta.id, solicitud),
      'Equipo relacionado con la receta correctamente.',
      () => { this.nuevoEquipo = ''; this.mostrarFormularioEquipo = false; }
    );
  }

  eliminarEquipo(equipo: EquipoCocinaReceta): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    if (!detalle || !confirm(`¿Quitar "${equipo.nombre}" de esta receta? Se conservará en el catálogo.`)) return;
    this.procesarCambio(
      this.recetaApiServicio.eliminarEquipoReceta(detalle.receta.id, equipo.idEquipo),
      'Equipo retirado de la receta.'
    );
  }

  iniciarEdicionNutricion(): void {
    const nutricion = this.detalle?.informacionNutricional;
    this.nutricionEnEdicion = {
      calorias: nutricion?.calorias ?? null,
      proteinasG: nutricion?.proteinasG ?? null,
      carbohidratosG: nutricion?.carbohidratosG ?? null,
      grasasG: nutricion?.grasasG ?? null,
      azucaresG: nutricion?.azucaresG ?? null,
      sodioMg: nutricion?.sodioMg ?? null,
    };
    this.mostrarFormularioNutricion = true;
    this.mensajeAccion = null;
    this.errorAccion = null;
  }

  cancelarEdicionNutricion(): void {
    this.mostrarFormularioNutricion = false;
  }

  guardarNutricion(): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    if (!detalle) return;
    const solicitud: SolicitudInformacionNutricional = { ...this.nutricionEnEdicion };
    const hayDato: boolean = Object.values(solicitud).some((valor: number | null) => valor !== null && valor !== undefined);
    if (!hayDato) {
      this.errorAccion = 'Ingresa al menos un dato nutricional.';
      return;
    }
    this.procesarCambio(
      this.recetaApiServicio.guardarInformacionNutricional(detalle.receta.id, solicitud),
      'Información nutricional guardada correctamente.',
      () => { this.mostrarFormularioNutricion = false; }
    );
  }

  eliminarNutricion(): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    if (!detalle?.informacionNutricional || !confirm('¿Eliminar la información nutricional de esta receta?')) return;
    this.procesarCambio(
      this.recetaApiServicio.eliminarInformacionNutricional(detalle.receta.id),
      'Información nutricional eliminada.'
    );
  }

  guardarValoracion(): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    const usuario: UsuarioSesion | null = this.usuarioActual;
    if (!detalle) return;
    if (!usuario) {
      this.errorAccion = 'Inicia sesión para valorar la receta.';
      return;
    }
    const puntuacion: number = Number(this.nuevaValoracion.puntuacion);
    if (!Number.isInteger(puntuacion) || puntuacion < 1 || puntuacion > 5) {
      this.errorAccion = 'Selecciona una puntuación entre 1 y 5.';
      return;
    }
    const solicitud: SolicitudValoracionReceta = {
      idUsuario: usuario.id,
      puntuacion,
      comentario: this.nuevaValoracion.comentario.trim() || null,
    };
    this.procesarCambio(
      this.recetaApiServicio.agregarValoracionReceta(detalle.receta.id, solicitud),
      'Tu valoración se guardó correctamente.',
      () => { this.nuevaValoracion = { puntuacion: 5, comentario: '' }; }
    );
  }

  guardarIngrediente(): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    const nombreIngrediente: string = this.nuevoIngrediente.nombreIngrediente.trim();
    if (!detalle || !nombreIngrediente) {
      this.errorAccion = 'Escribe el nombre del ingrediente.';
      return;
    }

    const solicitud: SolicitudIngredienteReceta = {
      nombreIngrediente,
      cantidad: this.nuevoIngrediente.cantidad,
      nombreUnidad: this.nuevoIngrediente.nombreUnidad.trim() || null,
      abreviaturaUnidad: this.nuevoIngrediente.abreviaturaUnidad.trim() || null,
    };

    this.procesarCambio(
      this.recetaApiServicio.agregarIngredienteReceta(detalle.receta.id, solicitud),
      'Ingrediente guardado correctamente.',
      () => {
        this.nuevoIngrediente = {
          nombreIngrediente: '', cantidad: null, nombreUnidad: '', abreviaturaUnidad: '',
        };
        this.mostrarFormularioIngrediente = false;
      }
    );
  }

  iniciarEdicionIngrediente(ingrediente: IngredienteReceta): void {
    this.ingredienteEditandoId = ingrediente.idIngrediente;
    this.ingredienteEnEdicion = {
      cantidad: ingrediente.cantidad,
      nombreUnidad: ingrediente.unidad ?? '',
      abreviaturaUnidad: ingrediente.abreviatura ?? '',
    };
    this.mensajeAccion = null;
    this.errorAccion = null;
  }

  cancelarEdicionIngrediente(): void {
    this.ingredienteEditandoId = null;
  }

  guardarEdicionIngrediente(ingrediente: IngredienteReceta): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    if (!detalle) return;

    const solicitud: ActualizacionIngredienteReceta = {
      cantidad: this.ingredienteEnEdicion.cantidad,
      nombreUnidad: this.ingredienteEnEdicion.nombreUnidad.trim() || null,
      abreviaturaUnidad: this.ingredienteEnEdicion.abreviaturaUnidad.trim() || null,
    };

    this.procesarCambio(
      this.recetaApiServicio.actualizarIngredienteReceta(detalle.receta.id, ingrediente.idIngrediente, solicitud),
      'Ingrediente actualizado correctamente.',
      () => { this.ingredienteEditandoId = null; }
    );
  }

  eliminarIngrediente(ingrediente: IngredienteReceta): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    if (!detalle || !confirm(`¿Quitar "${ingrediente.nombre}" de esta receta? El ingrediente seguirá en el catálogo.`)) return;

    this.procesarCambio(
      this.recetaApiServicio.eliminarIngredienteReceta(detalle.receta.id, ingrediente.idIngrediente),
      'Ingrediente retirado de la receta.'
    );
  }

  guardarPaso(): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    const descripcion: string = this.nuevoPaso.descripcion.trim();
    if (!detalle || !descripcion) {
      this.errorAccion = 'Escribe la descripción del paso.';
      return;
    }

    const solicitud: SolicitudPasoReceta = {
      numeroPaso: this.nuevoPaso.numeroPaso,
      descripcion,
      tiempoEstimadoMin: this.nuevoPaso.tiempoEstimadoMin,
    };

    this.procesarCambio(
      this.recetaApiServicio.agregarPasoReceta(detalle.receta.id, solicitud),
      'Paso de preparación guardado correctamente.',
      () => {
        this.nuevoPaso = { numeroPaso: null, descripcion: '', tiempoEstimadoMin: null };
        this.mostrarFormularioPaso = false;
      }
    );
  }

  iniciarEdicionPaso(paso: PasoReceta, indice: number): void {
    this.pasoEditandoId = paso.idPaso;
    this.pasoEnEdicion = {
      numeroPaso: paso.numeroPaso ?? indice + 1,
      descripcion: paso.descripcion,
      tiempoEstimadoMin: paso.tiempoEstimadoMin,
    };
    this.mensajeAccion = null;
    this.errorAccion = null;
  }

  cancelarEdicionPaso(): void {
    this.pasoEditandoId = null;
  }

  guardarEdicionPaso(paso: PasoReceta): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    if (!detalle) return;
    const descripcion: string = this.pasoEnEdicion.descripcion.trim();
    if (!descripcion) {
      this.errorAccion = 'La descripción del paso es obligatoria.';
      return;
    }

    const solicitud: SolicitudPasoReceta = {
      numeroPaso: this.pasoEnEdicion.numeroPaso,
      descripcion,
      tiempoEstimadoMin: this.pasoEnEdicion.tiempoEstimadoMin,
    };
    this.procesarCambio(
      this.recetaApiServicio.actualizarPasoReceta(detalle.receta.id, paso.idPaso, solicitud),
      'Paso actualizado correctamente.',
      () => { this.pasoEditandoId = null; }
    );
  }

  eliminarPaso(paso: PasoReceta): void {
    const detalle: DetalleReceta | undefined = this.detalle;
    if (!detalle || !confirm(`¿Eliminar el paso ${paso.numeroPaso ?? ''} de esta receta?`)) return;

    this.procesarCambio(
      this.recetaApiServicio.eliminarPasoReceta(detalle.receta.id, paso.idPaso),
      'Paso eliminado correctamente.'
    );
  }

  /** Ejecuta mutaciones y reemplaza el detalle por la respuesta de PostgreSQL. */
  private procesarCambio(
    peticion: Observable<DetalleReceta>,
    mensajeExito: string,
    alCompletar: () => void = () => undefined
  ): void {
    this.guardandoContenido = true;
    this.mensajeAccion = null;
    this.errorAccion = null;
    peticion.subscribe({
      next: (detalle: DetalleReceta) => {
        this.detalle = detalle;
        this.guardandoContenido = false;
        this.mensajeAccion = mensajeExito;
        alCompletar();
      },
      error: (error: HttpErrorResponse) => {
        this.guardandoContenido = false;
        this.errorAccion = this.mensajeDeError(error);
      },
    });
  }

  private mensajeDeError(error: HttpErrorResponse): string {
    const cuerpo: unknown = error.error;
    if (typeof cuerpo === 'object' && cuerpo !== null && 'mensaje' in cuerpo) {
      const mensaje: unknown = (cuerpo as { mensaje?: unknown }).mensaje;
      if (typeof mensaje === 'string') return mensaje;
    }
    if (error.status === 0) return 'No se pudo conectar con Spring Boot. Comprueba que el backend esté iniciado.';
    if (error.status === 400) return 'Los datos enviados no son válidos. Revisa los campos.';
    if (error.status === 404) return 'No se encontró la receta o el elemento que intentas modificar.';
    if (error.status === 409) return 'La operación entra en conflicto con datos relacionados de la base de datos.';
    return `No se pudo guardar el cambio (error ${error.status}).`;
  }

  /** Regresa a la pantalla interna desde la que se abrió la receta. */
  volver(): void {
    void this.historialNavegacion.regresar();
  }
}
