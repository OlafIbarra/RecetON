import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin, map, of, switchMap } from 'rxjs';
import { TraductorRecetasServicio } from './traductor-recetas.servicio';
import {
  CategoriasTheMealDBApi,
  IngredienteExterno,
  ListaTheMealDBApi,
  MealApi,
  OpcionOrigenExterna,
  RecetaExterna,
  RecetaExternaApi,
  TipoBusquedaExterna,
} from '../interfaces/receta-externa.interface';

const CATEGORIAS_ES: Record<string, string> = {
  Beef: 'Carne de res', Breakfast: 'Desayunos', Chicken: 'Pollo', Dessert: 'Postres',
  Goat: 'Cabra', Lamb: 'Cordero', Miscellaneous: 'Variados', Pasta: 'Pasta',
  Pork: 'Cerdo', Seafood: 'Mariscos', Side: 'Acompañamientos', Starter: 'Entradas',
  Vegan: 'Veganas', Vegetarian: 'Vegetarianas',
};

const INGREDIENTES_BUSQUEDA_EN: Record<string, string> = {
  pollo: 'chicken', 'pechuga de pollo': 'chicken breast', res: 'beef', 'carne de res': 'beef',
  'carne molida': 'ground beef', cerdo: 'pork', cordero: 'lamb', pescado: 'fish',
  mariscos: 'seafood', camaron: 'shrimp', camarones: 'shrimp', langostinos: 'prawns',
  salmon: 'salmon', atun: 'tuna', bacalao: 'cod', huevo: 'egg', huevos: 'egg',
  leche: 'milk', queso: 'cheese', mantequilla: 'butter', crema: 'cream', yogur: 'yogurt',
  arroz: 'rice', pasta: 'pasta', espagueti: 'spaghetti', fideos: 'noodles', pan: 'bread',
  papa: 'potato', papas: 'potato', patata: 'potato', patatas: 'potato', camote: 'sweet potato',
  tomate: 'tomato', tomates: 'tomato', jitomate: 'tomato', cebolla: 'onion', ajo: 'garlic',
  zanahoria: 'carrot', zanahorias: 'carrot', lechuga: 'lettuce', espinaca: 'spinach',
  brocoli: 'broccoli', coliflor: 'cauliflower', champinones: 'mushroom', champinon: 'mushroom',
  aguacate: 'avocado', limon: 'lemon', lima: 'lime', chile: 'chilli', chiles: 'chilli',
  pimiento: 'bell pepper', frijoles: 'beans', frijol: 'beans', garbanzos: 'chickpeas',
  lentejas: 'lentils', maiz: 'corn', elote: 'corn', hongos: 'mushroom', pepino: 'cucumber',
  harina: 'flour', azucar: 'sugar', sal: 'salt', pimienta: 'pepper', cilantro: 'coriander',
  perejil: 'parsley', albahaca: 'basil', canela: 'cinnamon', comino: 'cumin', miel: 'honey',
  aceite: 'olive oil', 'aceite de oliva': 'olive oil', calabaza: 'pumpkin', fresa: 'strawberry',
  fresas: 'strawberry', naranja: 'orange', mango: 'mango', pina: 'pineapple',
  'salsa de soya': 'soy sauce', mostaza: 'mustard', oregano: 'oregano',
};

const PAISES_ES: Record<string, string> = {
  Afghanistan: 'Afganistán', Albania: 'Albania', Algeria: 'Argelia', Andorra: 'Andorra',
  Angola: 'Angola', Argentina: 'Argentina', Armenia: 'Armenia', Australia: 'Australia',
  Austria: 'Austria', Azerbaijan: 'Azerbaiyán', Bahamas: 'Bahamas', Bahrain: 'Baréin',
  Bangladesh: 'Bangladés', Barbados: 'Barbados', Belarus: 'Bielorrusia', Belgium: 'Bélgica',
  Belize: 'Belice', Benin: 'Benín', Bhutan: 'Bután', Bolivia: 'Bolivia',
  'Bosnia and Herzegovina': 'Bosnia y Herzegovina', Botswana: 'Botsuana', Brazil: 'Brasil',
  Brunei: 'Brunéi', Bulgaria: 'Bulgaria', Cambodia: 'Camboya', Cameroon: 'Camerún',
  Canada: 'Canadá', 'Cape Verde': 'Cabo Verde', 'Cayman Islands': 'Islas Caimán',
  Chile: 'Chile', China: 'China', Colombia: 'Colombia', 'Costa Rica': 'Costa Rica',
  Croatia: 'Croacia', Cuba: 'Cuba', Cyprus: 'Chipre', Czechia: 'Chequia', Denmark: 'Dinamarca',
  Dominica: 'Dominica', 'Dominican Republic': 'República Dominicana', Ecuador: 'Ecuador',
  Egypt: 'Egipto', 'El Salvador': 'El Salvador', Estonia: 'Estonia', Ethiopia: 'Etiopía',
  Finland: 'Finlandia', France: 'Francia', Gabon: 'Gabón', Georgia: 'Georgia', Germany: 'Alemania',
  Ghana: 'Ghana', Greece: 'Grecia', Greenland: 'Groenlandia', Guatemala: 'Guatemala',
  Haiti: 'Haití', Honduras: 'Honduras', 'Hong Kong': 'Hong Kong', Hungary: 'Hungría',
  Iceland: 'Islandia', India: 'India', Indonesia: 'Indonesia', Iran: 'Irán', Iraq: 'Irak',
  Ireland: 'Irlanda', Israel: 'Israel', Italy: 'Italia', 'Ivory Coast': 'Costa de Marfil',
  Jamaica: 'Jamaica', Japan: 'Japón', Jordan: 'Jordania', Kazakhstan: 'Kazajistán',
  Kenya: 'Kenia', Kuwait: 'Kuwait', Laos: 'Laos', Latvia: 'Letonia', Lebanon: 'Líbano',
  Libya: 'Libia', Lithuania: 'Lituania', Luxembourg: 'Luxemburgo', Madagascar: 'Madagascar',
  Malawi: 'Malaui', Malaysia: 'Malasia', Maldives: 'Maldivas', Mali: 'Malí', Malta: 'Malta',
  Mauritius: 'Mauricio', Mexico: 'México', Moldova: 'Moldavia', Mongolia: 'Mongolia',
  Montenegro: 'Montenegro', Morocco: 'Marruecos', Mozambique: 'Mozambique', Myanmar: 'Myanmar',
  Namibia: 'Namibia', Nepal: 'Nepal', Netherlands: 'Países Bajos', 'New Zealand': 'Nueva Zelanda',
  Nicaragua: 'Nicaragua', Niger: 'Níger', Nigeria: 'Nigeria', 'North Korea': 'Corea del Norte',
  'North Macedonia': 'Macedonia del Norte', Norway: 'Noruega', Oman: 'Omán', Pakistan: 'Pakistán',
  Palestine: 'Palestina', Panama: 'Panamá', Paraguay: 'Paraguay', Peru: 'Perú',
  Philippines: 'Filipinas', Poland: 'Polonia', Portugal: 'Portugal', 'Puerto Rico': 'Puerto Rico',
  Qatar: 'Catar', Romania: 'Rumania', Russia: 'Rusia', Rwanda: 'Ruanda',
  'Saudi Arabia': 'Arabia Saudita', Senegal: 'Senegal', Serbia: 'Serbia', Singapore: 'Singapur',
  Slovakia: 'Eslovaquia', Slovenia: 'Eslovenia', 'South Africa': 'Sudáfrica',
  'South Korea': 'Corea del Sur', Spain: 'España', 'Sri Lanka': 'Sri Lanka', Sudan: 'Sudán',
  Sweden: 'Suecia', Switzerland: 'Suiza', Syria: 'Siria', Taiwan: 'Taiwán', Tanzania: 'Tanzania',
  Thailand: 'Tailandia', Tunisia: 'Túnez', Turkey: 'Turquía', Ukraine: 'Ucrania',
  'United Arab Emirates': 'Emiratos Árabes Unidos', 'United Kingdom': 'Reino Unido',
  'United States': 'Estados Unidos', Uruguay: 'Uruguay', Uzbekistan: 'Uzbekistán',
  Venezuela: 'Venezuela', Vietnam: 'Vietnam', Yemen: 'Yemen', Zambia: 'Zambia', Zimbabwe: 'Zimbabue',
  'Central African Republic': 'República Centroafricana', 'Equatorial Guinea': 'Guinea Ecuatorial',
  'Faroe Islands': 'Islas Feroe', 'Papua New Guinea': 'Papúa Nueva Guinea',
  'Republic of the Congo': 'República del Congo', 'DR Congo': 'República Democrática del Congo',
  'Solomon Islands': 'Islas Salomón', 'South Sudan': 'Sudán del Sur',
};

const COCINAS_A_PAIS: Record<string, string> = {
  Afghan: 'Afganistán', Albanian: 'Albania', Algerian: 'Argelia', American: 'Estados Unidos',
  Argentine: 'Argentina', Australian: 'Australia', Azerbaijani: 'Azerbaiyán', Brazilian: 'Brasil',
  British: 'Reino Unido', Canadian: 'Canadá', Cambodian: 'Camboya', Chinese: 'China',
  Colombian: 'Colombia', Croatian: 'Croacia', Dutch: 'Países Bajos', Egyptian: 'Egipto',
  Filipino: 'Filipinas', French: 'Francia', Greek: 'Grecia', Indian: 'India', Irish: 'Irlanda',
  Italian: 'Italia', Jamaican: 'Jamaica', Japanese: 'Japón', Kenyan: 'Kenia', Malaysian: 'Malasia',
  Mexican: 'México', Moroccan: 'Marruecos', Polish: 'Polonia', Portuguese: 'Portugal', Russian: 'Rusia',
  Spanish: 'España', Thai: 'Tailandia', Tunisian: 'Túnez', Turkish: 'Turquía', Ukrainian: 'Ucrania',
  Vietnamese: 'Vietnam', 'United States': 'Estados Unidos', 'United Kingdom': 'Reino Unido',
};

/**
 * Cliente de TheMealDB. Solo lee el catálogo externo: nunca inserta recetas en PostgreSQL.
 * La API V1 gratuita filtra por un criterio cada vez; cuando se combinan filtros,
 * intersectamos las listas mediante idMeal para que se cumplan TODOS los criterios.
 */
@Injectable({ providedIn: 'root' })
export class RecetaExternaServicio {
  private readonly http = inject(HttpClient);
  private readonly traductor = inject(TraductorRecetasServicio);
  private readonly baseUrl = 'https://www.themealdb.com/api/json/v1/1';

  obtenerRecetaAleatoria(): Observable<RecetaExterna> {
    return this.http.get<RecetaExternaApi>(`${this.baseUrl}/random.php`).pipe(
      map((respuesta) => {
        const comida = respuesta.meals?.[0];
        if (!comida) throw new Error('No encontramos una receta para mostrar.');
        return this.traducir(comida);
      }),
      switchMap((receta) => this.traductor.traducirReceta(receta)),
    );
  }

  buscar(texto: string, tipo: TipoBusquedaExterna): Observable<RecetaExterna[]> {
    const valor = texto.trim();
    if (!valor) return of([]);
    if (tipo === 'ingrediente') {
      const clave = this.normalizarBusqueda(valor);
      const conocido = INGREDIENTES_BUSQUEDA_EN[clave];
      if (conocido) return this.buscarIngredienteApi(conocido);
      if (this.pareceConsultaEnEspanol(valor)) {
        return this.traductor.traducirAlIngles(valor).pipe(
          switchMap((terminoApi) => this.buscarIngredienteApi(terminoApi.toLocaleLowerCase().replace(/\s+/g, '_'))),
        );
      }
      return this.buscarIngredienteApi(valor.toLocaleLowerCase().replace(/\s+/g, '_'));
    }

    return this.buscarNombreApi(valor).pipe(
      switchMap((resultados) => {
        if (resultados.length || !this.pareceConsultaEnEspanol(valor)) return of(resultados);
        return this.traductor.traducirAlIngles(valor).pipe(
          switchMap((terminoTraducido) => terminoTraducido && terminoTraducido.toLocaleLowerCase() !== valor.toLocaleLowerCase()
            ? this.buscarNombreApi(terminoTraducido)
            : of(resultados)),
        );
      }),
    );
  }

  private buscarNombreApi(valor: string): Observable<RecetaExterna[]> {
    return this.http.get<RecetaExternaApi>(`${this.baseUrl}/search.php`, {
      params: new HttpParams().set('s', valor),
    }).pipe(map((respuesta) => (respuesta.meals ?? []).map((comida) => this.traducir(comida))));
  }

  private buscarIngredienteApi(valor: string): Observable<RecetaExterna[]> {
    return this.http.get<RecetaExternaApi>(`${this.baseUrl}/filter.php`, {
      params: new HttpParams().set('i', valor),
    }).pipe(map((respuesta) => (respuesta.meals ?? []).map((comida) => this.traducir(comida))));
  }

  private normalizarBusqueda(valor: string): string {
    return valor.trim().toLocaleLowerCase('es').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/\s+/g, ' ');
  }

  private pareceConsultaEnEspanol(valor: string): boolean {
    return /[áéíóúñ¿¡]/i.test(valor)
      || /\b(de|del|con|pollo|carne|res|pescado|mariscos|camarón|atun|atún|huevo|leche|arroz|cebolla|ajo|tomate|papa|papas|ensalada|sopa|tacos|taco|pastel|frijoles|queso|postre)\b/i.test(valor);
  }

  /** Aplica texto, categoría y origen en conjunto. Si se eligen varios, solo devuelve la intersección. */
  buscarConFiltros(
    texto: string,
    tipo: TipoBusquedaExterna,
    categoria: string,
    origen: string,
  ): Observable<RecetaExterna[]> {
    const termino = texto.trim();
    const buscarTexto$ = termino ? this.buscar(termino, tipo) : of(null);
    const buscarCategoria$ = categoria ? this.filtrar('c', categoria) : of(null);
    const buscarOrigen$ = origen ? this.filtrar('a', origen) : of(null);

    return forkJoin({ texto: buscarTexto$, categoria: buscarCategoria$, origen: buscarOrigen$ }).pipe(
      map((respuestas) => {
        const base = respuestas.texto ?? respuestas.categoria ?? respuestas.origen ?? [];
        const listasRequeridas = [respuestas.categoria, respuestas.origen]
          .filter((lista): lista is RecetaExterna[] => lista !== null);
        const coincidencias = base.filter((receta) =>
          listasRequeridas.every((lista) => lista.some((otra) => otra.id === receta.id)),
        );

        return coincidencias.map((receta) => ({
          ...receta,
          categoria: receta.categoria === 'Sin categoría' && categoria
            ? this.traducirCategoria(categoria) : receta.categoria,
          origen: receta.origen === 'Origen no especificado' && origen
            ? this.traducirArea(origen) : receta.origen,
        }));
      }),
    );
  }

  filtrarPorCategoria(categoria: string): Observable<RecetaExterna[]> {
    return this.filtrar('c', categoria);
  }

  filtrarPorOrigen(origen: string): Observable<RecetaExterna[]> {
    return this.filtrar('a', origen);
  }

  obtenerDetalle(id: string): Observable<RecetaExterna> {
    return this.http.get<RecetaExternaApi>(`${this.baseUrl}/lookup.php`, {
      params: new HttpParams().set('i', id),
    }).pipe(
      map((respuesta) => {
        const comida = respuesta.meals?.[0];
        if (!comida) throw new Error('No encontramos los detalles de esa receta.');
        return this.traducir(comida);
      }),
      switchMap((receta) => this.traductor.traducirReceta(receta)),
    );
  }

  obtenerCategorias(): Observable<string[]> {
    return this.http.get<CategoriasTheMealDBApi>(`${this.baseUrl}/categories.php`).pipe(
      map((respuesta) => (respuesta.categories ?? [])
        .map((categoria) => categoria.strCategory).filter(Boolean)),
    );
  }

  /** Muestra el nombre del país, pero conserva strArea como valor enviado a TheMealDB. */
  obtenerOrigenes(): Observable<OpcionOrigenExterna[]> {
    return this.http.get<ListaTheMealDBApi>(`${this.baseUrl}/list.php`, {
      params: new HttpParams().set('a', 'list'),
    }).pipe(map((respuesta) => {
      const agrupados = new Map<string, Set<string>>();
      for (const elemento of respuesta.meals ?? []) {
        const valor = elemento.strArea?.trim();
        if (!valor) continue;
        if (!agrupados.has(valor)) agrupados.set(valor, new Set<string>());
        const pais = elemento.strCountry?.trim();
        if (pais) agrupados.get(valor)!.add(this.traducirPais(pais));
      }
      return Array.from(agrupados.entries())
        .map(([valor, paises]) => ({
          valor,
          etiqueta: paises.size ? Array.from(paises).sort((a, b) => a.localeCompare(b, 'es')).join(' / ')
            : this.traducirArea(valor),
        }))
        .sort((a, b) => a.etiqueta.localeCompare(b.etiqueta, 'es'));
    }));
  }

  traducirCategoria(categoria: string): string {
    return CATEGORIAS_ES[categoria] ?? categoria;
  }

  /** Traduce nombres visibles de las tarjetas, reutilizando la caché local. */
  traducirTextoEspanol(texto: string): Observable<string> {
    return this.traductor.traducirTexto(texto);
  }

  private filtrar(tipo: 'c' | 'a' | 'i', valor: string): Observable<RecetaExterna[]> {
    const normalizado = tipo === 'i' ? valor.trim().toLocaleLowerCase().replace(/\s+/g, '_') : valor;
    return this.http.get<RecetaExternaApi>(`${this.baseUrl}/filter.php`, {
      params: new HttpParams().set(tipo, normalizado),
    }).pipe(map((respuesta) => (respuesta.meals ?? []).map((comida) => this.traducir(comida))));
  }

  private traducir(comida: MealApi): RecetaExterna {
    const ingredientes: IngredienteExterno[] = [];
    for (let i = 1; i <= 20; i += 1) {
      const nombre = String(comida[`strIngredient${i}`] ?? '').trim();
      const medida = String(comida[`strMeasure${i}`] ?? '').trim();
      if (nombre) ingredientes.push({ nombre, medida });
    }
    const pais = comida.strCountry?.trim();
    const area = comida.strArea?.trim();
    return {
      id: String(comida.idMeal ?? ''),
      nombre: String(comida.strMeal ?? 'Receta sin nombre'),
      imagen: String(comida.strMealThumb ?? ''),
      categoria: comida.strCategory ? this.traducirCategoria(comida.strCategory) : 'Sin categoría',
      origen: pais ? this.traducirPais(pais) : area ? this.traducirArea(area) : 'Origen no especificado',
      instrucciones: String(comida.strInstructions ?? '').trim(),
      ingredientes,
      fuente: comida.strSource?.trim() || null,
      video: comida.strYoutube?.trim() || null,
    };
  }

  private traducirPais(pais: string): string {
    return PAISES_ES[pais] ?? pais;
  }

  private traducirArea(area: string): string {
    return COCINAS_A_PAIS[area] ?? area;
  }
}
