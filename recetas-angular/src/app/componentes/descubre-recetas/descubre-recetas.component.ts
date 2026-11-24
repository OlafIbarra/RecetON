import { CommonModule } from '@angular/common';
import { Component, Input, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, forkJoin, map } from 'rxjs';
import { OpcionOrigenExterna, RecetaExterna, TipoBusquedaExterna } from '../../interfaces/receta-externa.interface';
import { RecetaExternaServicio } from '../../servicios/receta-externa.servicio';

type DispositivoRecetas = 'reloj' | 'telefono' | 'tableta' | 'tv';

@Component({
  selector: 'app-descubre-recetas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './descubre-recetas.component.html',
  styleUrl: './descubre-recetas.component.css',
})
export class DescubreRecetasComponent implements OnInit, OnDestroy {
  @Input() dispositivo: DispositivoRecetas = 'telefono';

  private readonly servicio = inject(RecetaExternaServicio);
  categorias: string[] = [];
  origenes: OpcionOrigenExterna[] = [];
  textoBusqueda = '';
  tipoBusqueda: TipoBusquedaExterna = 'nombre';
  categoriaSeleccionada = '';
  origenSeleccionado = '';
  resultados: RecetaExterna[] = [];
  recetaSeleccionada: RecetaExterna | null = null;
  cargando = false;
  cargandoDetalle = false;
  error: string | null = null;
  avisoCatalogos: string | null = null;
  limiteResultados = 8;
  readonly pasoResultados = 8;

  /** Herramientas del modo cocina: checklist de ingredientes y preparación guiada. */
  modoCocina = false;
  modoCocinaCompletado = false;
  pasoActual = 0;
  ingredientesMarcados = new Set<number>();

  /** Herramientas opcionales para cocinar con las manos ocupadas. */
  minutosTemporizador = 5;
  segundosTemporizador = 0;
  segundosRestantes = 300;
  temporizadorActivo = false;
  temporizadorPausado = false;
  avisoTemporizador = '';
  lecturaEnVozAlta = false;
  avisoLecturaVoz = '';
  private intervaloTemporizador: ReturnType<typeof setInterval> | null = null;

  get tiempoRestanteTexto(): string {
    const minutos = Math.floor(this.segundosRestantes / 60).toString().padStart(2, '0');
    const segundos = (this.segundosRestantes % 60).toString().padStart(2, '0');
    return `${minutos}:${segundos}`;
  }

  get vozDisponible(): boolean {
    return typeof window !== 'undefined' && 'speechSynthesis' in window && typeof SpeechSynthesisUtterance !== 'undefined';
  }

  ngOnDestroy(): void {
    this.detenerIntervaloTemporizador();
    this.detenerLectura();
  }

  get resultadosVisibles(): RecetaExterna[] {
    return this.resultados.slice(0, this.limiteResultados);
  }

  get hayMasResultados(): boolean {
    return this.limiteResultados < this.resultados.length;
  }

  /** Divide la preparación en pasos utilizando saltos de línea, numeración o frases como alternativa. */
  get pasosPreparacion(): string[] {
    const texto = this.recetaSeleccionada?.instrucciones?.replace(/\r/g, '').trim() ?? '';
    if (!texto) return [];

    const parrafos = texto.split(/\n+/).map((paso) => paso.trim()).filter(Boolean);
    if (parrafos.length > 1) return parrafos.map((paso) => paso.replace(/^\s*(?:paso\s*)?\d+[.)-]\s*/i, ''));

    const numerados = texto.split(/(?=\b\d+[.)]\s+)/).map((paso) => paso.trim()).filter(Boolean);
    if (numerados.length > 1) return numerados.map((paso) => paso.replace(/^\d+[.)]\s*/, ''));

    const frases = texto.match(/[^.!?]+[.!?]+|[^.!?]+$/gu)?.map((paso) => paso.trim()).filter(Boolean) ?? [texto];
    return frases.length > 1 ? frases : [texto];
  }

  get progresoCocina(): number {
    const total = this.pasosPreparacion.length;
    if (!total) return 0;
    return this.modoCocinaCompletado ? 100 : Math.round(((this.pasoActual + 1) / total) * 100);
  }

  get criteriosActivos(): string[] {
    const criterios: string[] = [];
    if (this.textoBusqueda.trim()) criterios.push(this.tipoBusqueda === 'nombre' ? `Nombre: ${this.textoBusqueda.trim()}` : `Ingrediente: ${this.textoBusqueda.trim()}`);
    if (this.categoriaSeleccionada) criterios.push(this.servicio.traducirCategoria(this.categoriaSeleccionada));
    if (this.origenSeleccionado) criterios.push(this.origenes.find((o) => o.valor === this.origenSeleccionado)?.etiqueta ?? this.origenSeleccionado);
    return criterios;
  }

  ngOnInit(): void {
    this.servicio.obtenerCategorias().subscribe({
      next: (categorias) => this.categorias = categorias,
      error: () => this.avisoCatalogos = 'No se pudieron cargar las categorías. Puedes intentarlo de nuevo más tarde.',
    });
    this.servicio.obtenerOrigenes().subscribe({
      next: (origenes) => this.origenes = origenes,
      error: () => this.avisoCatalogos = 'No se pudieron cargar todos los países y cocinas disponibles.',
    });
    this.sorprender();
  }

  buscarPorTexto(): void {
    this.aplicarFiltros();
  }

  /** Ejecuta todos los criterios juntos; no sustituye el filtro anterior por el último seleccionado. */
  aplicarFiltros(): void {
    const texto = this.textoBusqueda.trim();
    if (!texto && !this.categoriaSeleccionada && !this.origenSeleccionado) {
      this.error = 'Escribe un platillo o ingrediente, o elige una categoría o país.';
      return;
    }
    this.ejecutarBusqueda(this.servicio.buscarConFiltros(
      texto, this.tipoBusqueda, this.categoriaSeleccionada, this.origenSeleccionado,
    ));
  }

  sorprender(): void {
    this.textoBusqueda = '';
    this.tipoBusqueda = 'nombre';
    this.categoriaSeleccionada = '';
    this.origenSeleccionado = '';
    this.cargando = true;
    this.cargandoDetalle = false;
    this.error = null;
    this.servicio.obtenerRecetaAleatoria().subscribe({
      next: (receta) => {
        this.reiniciarModoCocina();
        this.resultados = [receta];
        this.limiteResultados = this.pasoResultados;
        this.recetaSeleccionada = receta;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        this.error = 'No pudimos cargar una receta. Revisa tu conexión e inténtalo de nuevo.';
      },
    });
  }

  seleccionarReceta(receta: RecetaExterna): void {
    if (!receta.id) return;
    this.reiniciarModoCocina();
    this.recetaSeleccionada = receta;
    this.cargandoDetalle = true;
    this.error = null;
    this.servicio.obtenerDetalle(receta.id).subscribe({
      next: (detalle) => {
        this.recetaSeleccionada = detalle;
        this.resultados = this.resultados.map((item) => item.id === detalle.id ? { ...item, nombre: detalle.nombre, categoria: detalle.categoria, origen: detalle.origen } : item);
        this.cargandoDetalle = false;
      },
      error: () => {
        this.cargandoDetalle = false;
        this.error = 'No se pudo cargar el detalle de esa receta. Selecciónala otra vez para reintentar.';
      },
    });
  }

  limpiarFiltros(): void {
    this.reiniciarModoCocina();
    this.textoBusqueda = '';
    this.categoriaSeleccionada = '';
    this.origenSeleccionado = '';
    this.tipoBusqueda = 'nombre';
    this.error = null;
    this.resultados = [];
    this.recetaSeleccionada = null;
    this.limiteResultados = this.pasoResultados;
  }

  mostrarMasResultados(): void {
    const inicio = this.limiteResultados;
    this.limiteResultados = Math.min(this.resultados.length, this.limiteResultados + this.pasoResultados);
    this.traducirTitulosVisibles(inicio, this.limiteResultados);
  }

  mostrarMenosResultados(): void {
    this.limiteResultados = this.pasoResultados;
  }

  etiquetaCategoria(categoria: string): string {
    return this.servicio.traducirCategoria(categoria);
  }

  abrirModoCocina(): void {
    if (!this.pasosPreparacion.length) return;
    this.modoCocina = true;
    this.modoCocinaCompletado = false;
    this.pasoActual = 0;
  }

  cerrarModoCocina(): void {
    this.modoCocina = false;
    this.modoCocinaCompletado = false;
    this.pasoActual = 0;
    this.detenerLectura();
    this.reiniciarTemporizador();
  }

  avanzarPaso(): void {
    this.detenerLectura();
    if (this.pasoActual < this.pasosPreparacion.length - 1) {
      this.pasoActual += 1;
      return;
    }
    this.modoCocinaCompletado = true;
  }

  retrocederPaso(): void {
    this.detenerLectura();
    if (this.modoCocinaCompletado) {
      this.modoCocinaCompletado = false;
      this.pasoActual = Math.max(0, this.pasosPreparacion.length - 1);
      return;
    }
    this.pasoActual = Math.max(0, this.pasoActual - 1);
  }

  iniciarTemporizador(): void {
    if (this.temporizadorActivo) return;

    if (!this.temporizadorPausado) {
      const minutos = Math.max(0, Math.min(999, Math.floor(Number(this.minutosTemporizador) || 0)));
      const segundos = Math.max(0, Math.min(59, Math.floor(Number(this.segundosTemporizador) || 0)));
      this.segundosRestantes = minutos * 60 + segundos;
    }

    if (this.segundosRestantes <= 0) {
      this.avisoTemporizador = 'Elige un tiempo mayor que cero.';
      return;
    }

    this.avisoTemporizador = '';
    this.temporizadorActivo = true;
    this.temporizadorPausado = false;
    this.intervaloTemporizador = setInterval(() => {
      if (this.segundosRestantes > 0) this.segundosRestantes -= 1;
      if (this.segundosRestantes <= 0) {
        this.segundosRestantes = 0;
        this.detenerIntervaloTemporizador();
        this.temporizadorActivo = false;
        this.temporizadorPausado = false;
        this.avisoTemporizador = '¡Tiempo terminado! Revisa tu preparación.';
      }
    }, 1000);
  }

  pausarTemporizador(): void {
    if (!this.temporizadorActivo) return;
    this.detenerIntervaloTemporizador();
    this.temporizadorActivo = false;
    this.temporizadorPausado = true;
    this.avisoTemporizador = 'Temporizador pausado.';
  }

  reiniciarTemporizador(): void {
    this.detenerIntervaloTemporizador();
    this.temporizadorActivo = false;
    this.temporizadorPausado = false;
    const minutos = Math.max(0, Math.min(999, Math.floor(Number(this.minutosTemporizador) || 0)));
    const segundos = Math.max(0, Math.min(59, Math.floor(Number(this.segundosTemporizador) || 0)));
    this.segundosRestantes = minutos * 60 + segundos;
    this.avisoTemporizador = '';
  }

  actualizarDuracionTemporizador(): void {
    if (this.temporizadorActivo) return;
    this.temporizadorPausado = false;
    const minutos = Math.max(0, Math.min(999, Math.floor(Number(this.minutosTemporizador) || 0)));
    const segundos = Math.max(0, Math.min(59, Math.floor(Number(this.segundosTemporizador) || 0)));
    this.minutosTemporizador = minutos;
    this.segundosTemporizador = segundos;
    this.segundosRestantes = minutos * 60 + segundos;
    this.avisoTemporizador = '';
  }

  leerPasoActual(): void {
    if (!this.vozDisponible) {
      this.avisoLecturaVoz = 'La lectura en voz alta no está disponible en este navegador.';
      return;
    }
    const paso = this.pasosPreparacion[this.pasoActual];
    if (!paso || this.modoCocinaCompletado) return;

    window.speechSynthesis.cancel();
    const mensaje = new SpeechSynthesisUtterance(paso);
    mensaje.lang = 'es-MX';
    mensaje.rate = 0.92;
    mensaje.pitch = 1;
    this.lecturaEnVozAlta = true;
    this.avisoLecturaVoz = 'Leyendo el paso en voz alta.';
    mensaje.onend = () => {
      this.lecturaEnVozAlta = false;
      this.avisoLecturaVoz = 'Terminó la lectura del paso.';
    };
    mensaje.onerror = () => {
      this.lecturaEnVozAlta = false;
      this.avisoLecturaVoz = 'No se pudo leer el paso. Puedes leerlo en pantalla.';
    };
    window.speechSynthesis.speak(mensaje);
  }

  detenerLectura(): void {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) window.speechSynthesis.cancel();
    this.lecturaEnVozAlta = false;
  }

  private detenerIntervaloTemporizador(): void {
    if (this.intervaloTemporizador !== null) {
      clearInterval(this.intervaloTemporizador);
      this.intervaloTemporizador = null;
    }
  }

  actualizarIngrediente(indice: number, marcado: boolean): void {
    const nuevos = new Set(this.ingredientesMarcados);
    if (marcado) nuevos.add(indice);
    else nuevos.delete(indice);
    this.ingredientesMarcados = nuevos;
  }

  private reiniciarModoCocina(): void {
    this.detenerIntervaloTemporizador();
    this.detenerLectura();
    this.temporizadorActivo = false;
    this.temporizadorPausado = false;
    this.minutosTemporizador = 5;
    this.segundosTemporizador = 0;
    this.segundosRestantes = 300;
    this.avisoTemporizador = '';
    this.avisoLecturaVoz = '';
    this.modoCocina = false;
    this.modoCocinaCompletado = false;
    this.pasoActual = 0;
    this.ingredientesMarcados = new Set<number>();
  }

  private traducirTitulosVisibles(inicio: number, fin: number): void {
    const lote = this.resultados.slice(inicio, fin);
    if (!lote.length) return;

    forkJoin(lote.map((receta) => this.servicio.traducirTextoEspanol(receta.nombre).pipe(
      map((nombre) => ({ id: receta.id, nombre })),
    ))).subscribe({
      next: (traducidos) => {
        const nombresPorId = new Map(traducidos.map((item) => [item.id, item.nombre]));
        this.resultados = this.resultados.map((receta) => {
          const nombre = nombresPorId.get(receta.id);
          return nombre ? { ...receta, nombre } : receta;
        });
      },
    });
  }

  private ejecutarBusqueda(peticion: Observable<RecetaExterna[]>): void {
    this.cargando = true;
    this.error = null;
    this.recetaSeleccionada = null;
    this.cargandoDetalle = false;
    this.limiteResultados = this.pasoResultados;
    peticion.subscribe({
      next: (resultados) => {
        this.resultados = resultados;
        this.cargando = false;
        this.traducirTitulosVisibles(0, Math.min(this.resultados.length, this.limiteResultados));
        if (!resultados.length) {
          this.error = 'No encontramos recetas con esa combinación. Prueba cambiar o quitar uno de los filtros.';
        } else if (resultados.length === 1) {
          this.seleccionarReceta(resultados[0]);
        }
      },
      error: () => {
        this.cargando = false;
        this.error = 'La búsqueda no está disponible en este momento. Inténtalo de nuevo.';
      },
    });
  }
}
