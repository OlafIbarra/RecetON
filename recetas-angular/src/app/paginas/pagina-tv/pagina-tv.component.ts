import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RecetaApiServicio } from '../../servicios/receta-api.servicio';
import { Categoria } from '../../interfaces/categoria.interface';
import { Receta } from '../../interfaces/receta.interface';
import { DatosPanel } from '../../interfaces/panel.interface';
import { BarraLateralComponent } from '../../componentes/barra-lateral/barra-lateral.component';
import { BarraBusquedaComponent } from '../../componentes/barra-busqueda/barra-busqueda.component';
import { BannerPrincipalComponent } from '../../componentes/banner-principal/banner-principal.component';
import { ListaCategoriasComponent } from '../../componentes/lista-categorias/lista-categorias.component';
import { CuadriculaRecetasComponent } from '../../componentes/cuadricula-recetas/cuadricula-recetas.component';

import { DescubreRecetasComponent } from '../../componentes/descubre-recetas/descubre-recetas.component';

/**
 * PANTALLA DE TV (Figma "Pantalla tv").
 * Componentes: barra-lateral + barra-busqueda + banner-principal (grande) +
 * lista-categorias (círculos grandes) + cuadricula-recetas (4 columnas).
 */
@Component({
  selector: 'app-pagina-tv',
  standalone: true,
  imports: [
    CommonModule,
    DescubreRecetasComponent,
    BarraLateralComponent,
    BarraBusquedaComponent,
    BannerPrincipalComponent,
    ListaCategoriasComponent,
    CuadriculaRecetasComponent,
  ],
  templateUrl: './pagina-tv.component.html',
  styleUrl: './pagina-tv.component.css',
})
export class PaginaTvComponent implements OnInit {
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
