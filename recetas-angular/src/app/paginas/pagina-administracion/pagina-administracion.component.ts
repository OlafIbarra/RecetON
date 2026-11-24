import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AutenticacionServicio } from '../../servicios/autenticacion.servicio';
import { AdministracionApiServicio } from '../../servicios/administracion-api.servicio';
import { HistorialNavegacionServicio } from '../../servicios/historial-navegacion.servicio';
import {
  CategoriaAdministracion,
  RecetaAdministracion,
  SolicitudCategoriaAdministracion,
  SolicitudRecetaAdministracion,
  SolicitudUsuarioAdministracion,
  UsuarioAdministracion,
  VistaAdministracion,
} from '../../interfaces/administracion.interface';

@Component({
  selector: 'app-pagina-administracion',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pagina-administracion.component.html',
  styleUrl: './pagina-administracion.component.css',
})
export class PaginaAdministracionComponent implements OnInit {
  private readonly api = inject(AdministracionApiServicio);
  private readonly autenticacion = inject(AutenticacionServicio);
  private readonly router = inject(Router);
  private readonly historial = inject(HistorialNavegacionServicio);

  vista: VistaAdministracion = 'recetas';
  recetas: RecetaAdministracion[] = [];
  categorias: CategoriaAdministracion[] = [];
  usuarios: UsuarioAdministracion[] = [];
  cargando: boolean = true;
  guardando: boolean = false;
  error: string | null = null;
  mensaje: string | null = null;
  recetaEditandoId: number | null = null;
  categoriaEditandoId: number | null = null;
  usuarioEditandoId: number | null = null;

  formularioReceta: SolicitudRecetaAdministracion = this.recetaVacia();
  formularioCategoria: SolicitudCategoriaAdministracion = this.categoriaVacia();
  formularioUsuario: SolicitudUsuarioAdministracion = this.usuarioVacio();

  get idUsuarioActual(): number | null { return this.autenticacion.obtenerUsuarioActual()?.id ?? null; }

  ngOnInit(): void {
    if (!this.autenticacion.esAdministrador()) {
      void this.router.navigate(['/inicio']);
      return;
    }
    this.cargarDatos();
  }

  regresarPantallaAnterior(): void { void this.historial.regresar(); }

  cargarDatos(): void {
    this.cargando = true;
    this.error = null;
    forkJoin({ recetas: this.api.listarRecetas(), categorias: this.api.listarCategorias(), usuarios: this.api.listarUsuarios() }).subscribe({
      next: (datos) => {
        this.recetas = datos.recetas;
        this.categorias = datos.categorias;
        this.usuarios = datos.usuarios;
        this.cargando = false;
      },
      error: (error: HttpErrorResponse) => {
        this.error = this.mensajeError(error, 'No se pudieron cargar los datos de administración.');
        this.cargando = false;
      },
    });
  }

  seleccionarVista(vista: VistaAdministracion): void {
    this.vista = vista;
    this.cancelarTodos();
    this.error = null;
    this.mensaje = null;
  }

  guardarReceta(): void {
    if (this.guardando) return;
    const datos: SolicitudRecetaAdministracion = {
      ...this.formularioReceta,
      name: this.formularioReceta.name.trim(),
      image: this.formularioReceta.image.trim(),
      price: String(this.formularioReceta.price || '0'),
      time: this.formularioReceta.time.trim(),
      tag: this.formularioReceta.tag.trim(),
      description: this.formularioReceta.description.trim(),
      idUsuario: this.formularioReceta.idUsuario || this.idUsuarioActual || 1,
      idPais: this.formularioReceta.idPais && this.formularioReceta.idPais > 0 ? this.formularioReceta.idPais : null,
    };
    if (!datos.name) { this.error = 'El título de la receta es obligatorio.'; return; }
    this.ejecutarGuardado(this.recetaEditandoId === null
      ? this.api.crearReceta(datos)
      : this.api.actualizarReceta(this.recetaEditandoId, datos), () => {
        this.mensaje = this.recetaEditandoId === null ? 'Receta creada correctamente.' : 'Receta actualizada correctamente.';
        this.recetaEditandoId = null;
        this.formularioReceta = this.recetaVacia();
        this.api.listarRecetas().subscribe({ next: (filas) => this.recetas = filas });
      });
  }

  editarReceta(receta: RecetaAdministracion): void {
    this.recetaEditandoId = receta.id;
    this.formularioReceta = {
      name: receta.name, image: receta.image ?? '', price: String(receta.price ?? '0'), rating: receta.rating ?? 0,
      time: receta.time ?? '', tag: receta.tag ?? '', tagColor: receta.tagColor ?? 'sage',
      description: receta.description ?? '', idUsuario: receta.idUsuario ?? this.idUsuarioActual ?? 1,
      idPais: receta.idPais ?? null, servings: receta.servings ?? 1, difficulty: receta.difficulty ?? 'Fácil', active: receta.active ?? true,
    };
    this.vista = 'recetas';
  }

  eliminarReceta(receta: RecetaAdministracion): void {
    if (!confirm(`¿Eliminar la receta «${receta.name}»? Si tiene registros relacionados, la base de datos puede impedirlo para proteger sus relaciones.`)) return;
    this.api.eliminarReceta(receta.id).subscribe({
      next: () => { this.recetas = this.recetas.filter((actual) => actual.id !== receta.id); this.mensaje = 'Receta eliminada.'; this.error = null; },
      error: (error: HttpErrorResponse) => this.error = this.mensajeError(error, 'No se pudo eliminar la receta. Revisa sus relaciones.'),
    });
  }

  guardarCategoria(): void {
    if (this.guardando) return;
    const datos: SolicitudCategoriaAdministracion = { name: this.formularioCategoria.name.trim(), image: this.formularioCategoria.image.trim() };
    if (!datos.name) { this.error = 'El nombre de la categoría es obligatorio.'; return; }
    this.ejecutarGuardado(this.categoriaEditandoId === null
      ? this.api.crearCategoria(datos)
      : this.api.actualizarCategoria(this.categoriaEditandoId, datos), () => {
        this.mensaje = this.categoriaEditandoId === null ? 'Categoría creada correctamente.' : 'Categoría actualizada correctamente.';
        this.categoriaEditandoId = null;
        this.formularioCategoria = this.categoriaVacia();
        this.api.listarCategorias().subscribe({ next: (filas) => this.categorias = filas });
      });
  }

  editarCategoria(categoria: CategoriaAdministracion): void {
    this.categoriaEditandoId = categoria.id;
    this.formularioCategoria = { name: categoria.name, image: categoria.image ?? '' };
    this.vista = 'categorias';
  }

  eliminarCategoria(categoria: CategoriaAdministracion): void {
    if (!confirm(`¿Eliminar la categoría «${categoria.name}»? Si está relacionada con recetas, primero revisa esas relaciones.`)) return;
    this.api.eliminarCategoria(categoria.id).subscribe({
      next: () => { this.categorias = this.categorias.filter((actual) => actual.id !== categoria.id); this.mensaje = 'Categoría eliminada.'; this.error = null; },
      error: (error: HttpErrorResponse) => this.error = this.mensajeError(error, 'No se pudo eliminar la categoría porque puede estar relacionada con recetas.'),
    });
  }

  guardarUsuario(): void {
    if (this.guardando) return;
    const datos: SolicitudUsuarioAdministracion = { ...this.formularioUsuario, username: this.formularioUsuario.username.trim(), email: this.formularioUsuario.email.trim(), fullName: this.formularioUsuario.fullName.trim() };
    if (!datos.username || !datos.fullName) { this.error = 'El nombre de usuario y el nombre completo son obligatorios.'; return; }
    if (this.usuarioEditandoId === null && datos.password.trim().length < 8) { this.error = 'La contraseña inicial debe tener al menos 8 caracteres.'; return; }
    this.ejecutarGuardado(this.usuarioEditandoId === null
      ? this.api.crearUsuario(datos)
      : this.api.actualizarUsuario(this.usuarioEditandoId, { ...datos, password: datos.password.trim() }), () => {
        this.mensaje = this.usuarioEditandoId === null ? 'Usuario creado con rol común.' : 'Usuario actualizado correctamente.';
        this.usuarioEditandoId = null;
        this.formularioUsuario = this.usuarioVacio();
        this.api.listarUsuarios().subscribe({ next: (filas) => this.usuarios = filas });
      });
  }

  editarUsuario(usuario: UsuarioAdministracion): void {
    this.usuarioEditandoId = usuario.id;
    this.formularioUsuario = { username: usuario.username, email: usuario.email ?? '', password: '', fullName: usuario.fullName, active: usuario.active ?? true };
    this.vista = 'usuarios';
  }

  eliminarUsuario(usuario: UsuarioAdministracion): void {
    if (usuario.id === this.idUsuarioActual) { this.error = 'No puedes eliminar la cuenta con la que tienes la sesión iniciada.'; return; }
    if (!confirm(`¿Eliminar al usuario «${usuario.username}»? Si tiene recetas o pedidos, la base de datos protegerá las relaciones.`)) return;
    this.api.eliminarUsuario(usuario.id).subscribe({
      next: () => { this.usuarios = this.usuarios.filter((actual) => actual.id !== usuario.id); this.mensaje = 'Usuario eliminado.'; this.error = null; },
      error: (error: HttpErrorResponse) => this.error = this.mensajeError(error, 'No se pudo eliminar el usuario. Revisa sus relaciones.'),
    });
  }

  cancelarTodos(): void {
    this.recetaEditandoId = null; this.categoriaEditandoId = null; this.usuarioEditandoId = null;
    this.formularioReceta = this.recetaVacia(); this.formularioCategoria = this.categoriaVacia(); this.formularioUsuario = this.usuarioVacio();
  }

  private ejecutarGuardado(peticion: import('rxjs').Observable<unknown>, alTerminar: () => void): void {
    this.guardando = true; this.error = null; this.mensaje = null;
    peticion.subscribe({
      next: () => { this.guardando = false; alTerminar(); },
      error: (error: HttpErrorResponse) => { this.guardando = false; this.error = this.mensajeError(error, 'No se pudo guardar el cambio.'); },
    });
  }

  private recetaVacia(): SolicitudRecetaAdministracion {
    return { name: '', image: '', price: '0.00', rating: 0, time: '', tag: '', tagColor: 'sage', description: '', idUsuario: this.idUsuarioActual ?? 1, idPais: null, servings: 1, difficulty: 'Fácil', active: true };
  }
  private categoriaVacia(): SolicitudCategoriaAdministracion { return { name: '', image: '' }; }
  private usuarioVacio(): SolicitudUsuarioAdministracion { return { username: '', email: '', password: '', fullName: '', active: true }; }

  private mensajeError(error: HttpErrorResponse, alternativo: string): string {
    const cuerpo = error.error as { mensaje?: string; message?: string } | null;
    if (cuerpo?.mensaje) return cuerpo.mensaje;
    if (cuerpo?.message) return cuerpo.message;
    if (error.status === 0) return 'No se pudo conectar con Spring Boot.';
    if (error.status === 403) return 'Tu sesión no tiene permiso para esta operación.';
    return alternativo;
  }
}
