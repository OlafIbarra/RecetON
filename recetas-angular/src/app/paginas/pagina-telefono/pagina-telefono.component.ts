import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RecetaApiServicio } from '../../servicios/receta-api.servicio';
import { Categoria } from '../../interfaces/categoria.interface';
import { Receta } from '../../interfaces/receta.interface';
import { DatosPanel } from '../../interfaces/panel.interface';
import { BarraBusquedaComponent } from '../../componentes/barra-busqueda/barra-busqueda.component';
import { PestanasNavegacionComponent } from '../../componentes/pestanas-navegacion/pestanas-navegacion.component';
import { BannerPrincipalComponent } from '../../componentes/banner-principal/banner-principal.component';
import { ListaCategoriasComponent } from '../../componentes/lista-categorias/lista-categorias.component';
import { CuadriculaRecetasComponent } from '../../componentes/cuadricula-recetas/cuadricula-recetas.component';
import { BarraInferiorComponent } from '../../componentes/barra-inferior/barra-inferior.component';

import { DescubreRecetasComponent } from '../../componentes/descubre-recetas/descubre-recetas.component';

/**
 * PANTALLA DE TELÉFONO (Figma "Teléfono").
 * Componentes: barra-busqueda + pestanas-navegacion + banner-principal +
 * lista-categorias + cuadricula-recetas (2 columnas) + barra-inferior (solo iconos).
 */
@Component({
  selector: 'app-pagina-telefono',
  standalone: true,
  imports: [
    CommonModule,
    DescubreRecetasComponent,
    BarraBusquedaComponent,
    PestanasNavegacionComponent,
    BannerPrincipalComponent,
    ListaCategoriasComponent,
    CuadriculaRecetasComponent,
    BarraInferiorComponent,
  ],
  templateUrl: './pagina-telefono.component.html',
  styleUrl: './pagina-telefono.component.css',
})
export class PaginaTelefonoComponent implements OnInit {
  private readonly recetaApiServicio: RecetaApiServicio = inject(RecetaApiServicio);

  categorias: Categoria[] = [];
  recetas: Receta[] = [];
  recetasFiltradas: Receta[] = [];
  cargando: boolean = true;
  modoRespaldo: boolean = false;
  categoriaActiva: Categoria | null = null;
  textoBusqueda: string = '';

  ngOnInit(): void {
    this.recetaApiServicio.obtenerDatosPanel().subscribe({
      next: (datos: DatosPanel) => {
        this.categorias = datos.categorias;
        this.recetas = datos.recetas;
        this.recetasFiltradas = datos.recetas;
        this.modoRespaldo = this.recetaApiServicio.estaEnModoRespaldo;
        this.cargando = false;
      },
    });
  }

  alBuscar(texto: string): void {
    this.textoBusqueda = texto;
    this.actualizarFiltros();
  }

  alSeleccionarCategoria(categoria: Categoria): void {
    this.categoriaActiva = this.categoriaActiva?.id === categoria.id ? null : categoria;
    this.actualizarFiltros();
  }

  limpiarFiltros(): void {
    this.categoriaActiva = null;
    this.textoBusqueda = '';
    this.recetasFiltradas = [...this.recetas];
  }

  private actualizarFiltros(): void {
    let resultados = this.recetaApiServicio.filtrarPorNombre(this.recetas, this.textoBusqueda);
    if (this.categoriaActiva) {
      const nombre = this.categoriaActiva.nombre.trim().toLocaleLowerCase();
      resultados = resultados.filter((receta: Receta) => (receta.etiqueta ?? '').trim().toLocaleLowerCase() === nombre);
    }
    this.recetasFiltradas = resultados;
  }
}
