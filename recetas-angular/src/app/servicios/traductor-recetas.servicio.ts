import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, concatMap, forkJoin, from, map, of, toArray } from 'rxjs';
import { IngredienteExterno, RecetaExterna } from '../interfaces/receta-externa.interface';

interface RespuestaTraduccion {
  responseStatus?: number;
  responseData?: { translatedText?: string | null };
}

/** Traduce los textos de recetas al español y guarda las traducciones en el navegador. */
@Injectable({ providedIn: 'root' })
export class TraductorRecetasServicio {
  private readonly http = inject(HttpClient);
  private readonly url = 'https://api.mymemory.translated.net/get';
  private readonly cacheMemoria = new Map<string, string>();
  private readonly maxBytesPorFragmento = 420;

  private readonly ingredientes: Record<string, string> = {
    'all purpose flour': 'harina de trigo', 'plain flour': 'harina de trigo', 'self raising flour': 'harina leudante',
    'almond': 'almendra', 'almonds': 'almendras', 'apple': 'manzana', 'apples': 'manzanas',
    'avocado': 'aguacate', 'avocados': 'aguacates', 'bacon': 'tocino', 'baking powder': 'polvo para hornear',
    'baking soda': 'bicarbonato de sodio', 'basil': 'albahaca', 'bay leaf': 'hoja de laurel', 'bay leaves': 'hojas de laurel',
    'beans': 'frijoles', 'beef': 'carne de res', 'beef stock': 'caldo de res', 'bell pepper': 'pimiento',
    'black pepper': 'pimienta negra', 'breadcrumbs': 'pan molido', 'bread': 'pan', 'broccoli': 'brócoli',
    'brown sugar': 'azúcar mascabado', 'butter': 'mantequilla', 'cabbage': 'col', 'carrot': 'zanahoria', 'carrots': 'zanahorias',
    'cashews': 'nueces de la India', 'cauliflower': 'coliflor', 'celery': 'apio', 'cheddar': 'queso cheddar',
    'cheddar cheese': 'queso cheddar', 'cheese': 'queso', 'cherry tomatoes': 'tomates cherry', 'chicken': 'pollo',
    'chicken breast': 'pechuga de pollo', 'chicken breasts': 'pechugas de pollo', 'chicken stock': 'caldo de pollo',
    'chickpeas': 'garbanzos', 'chilli': 'chile', 'chili': 'chile', 'chili powder': 'chile en polvo',
    'chocolate': 'chocolate', 'cinnamon': 'canela', 'cloves': 'clavos de olor', 'coconut': 'coco',
    'coconut milk': 'leche de coco', 'cod': 'bacalao', 'coriander': 'cilantro', 'corn': 'maíz', 'cornflour': 'fécula de maíz',
    'cornstarch': 'fécula de maíz', 'cream': 'crema', 'cream cheese': 'queso crema', 'cucumber': 'pepino',
    'cumin': 'comino', 'curry powder': 'curry en polvo', 'curry paste': 'pasta de curry', 'dark chocolate': 'chocolate oscuro',
    'dates': 'dátiles', 'dill': 'eneldo', 'double cream': 'crema para batir', 'egg': 'huevo', 'eggs': 'huevos',
    'eggplant': 'berenjena', 'aubergine': 'berenjena', 'feta': 'queso feta', 'fish': 'pescado', 'fish sauce': 'salsa de pescado',
    'flour': 'harina', 'fresh basil': 'albahaca fresca', 'fresh coriander': 'cilantro fresco', 'fresh parsley': 'perejil fresco',
    'garlic': 'ajo', 'garlic clove': 'diente de ajo', 'garlic cloves': 'dientes de ajo', 'ginger': 'jengibre',
    'green beans': 'ejotes', 'green pepper': 'pimiento verde', 'ground beef': 'carne de res molida', 'honey': 'miel',
    'hot sauce': 'salsa picante', 'leek': 'puerro', 'leeks': 'puerros', 'lemon': 'limón', 'lemons': 'limones',
    'lettuce': 'lechuga', 'lime': 'lima', 'limes': 'limas', 'lamb': 'cordero', 'lentils': 'lentejas',
    'maple syrup': 'jarabe de arce', 'mayonnaise': 'mayonesa', 'milk': 'leche', 'mint': 'menta',
    'mixed herbs': 'hierbas mixtas', 'mozzarella': 'queso mozzarella', 'mushroom': 'champiñón', 'mushrooms': 'champiñones',
    'mustard': 'mostaza', 'nutmeg': 'nuez moscada', 'oats': 'avena', 'olive oil': 'aceite de oliva',
    'onion': 'cebolla', 'onions': 'cebollas', 'orange': 'naranja', 'oranges': 'naranjas', 'oregano': 'orégano',
    'paprika': 'pimentón', 'parmesan': 'queso parmesano', 'parmesan cheese': 'queso parmesano', 'parsley': 'perejil',
    'pasta': 'pasta', 'peas': 'chícharos', 'peanut butter': 'crema de cacahuate', 'peanuts': 'cacahuates',
    'pear': 'pera', 'pears': 'peras', 'pepper': 'pimienta', 'pork': 'carne de cerdo',
    'pork chops': 'chuletas de cerdo', 'potato': 'papa', 'potatoes': 'papas', 'prawns': 'langostinos',
    'pumpkin': 'calabaza', 'red onion': 'cebolla morada', 'red pepper': 'pimiento rojo', 'rice': 'arroz',
    'rice vinegar': 'vinagre de arroz', 'rosemary': 'romero', 'salmon': 'salmón', 'salt': 'sal',
    'sausages': 'salchichas', 'sesame oil': 'aceite de sésamo', 'sesame seeds': 'semillas de sésamo',
    'shallot': 'chalota', 'shallots': 'chalotas', 'shrimp': 'camarón', 'shrimps': 'camarones',
    'sour cream': 'crema agria', 'soy sauce': 'salsa de soya', 'spinach': 'espinaca', 'spring onion': 'cebollín',
    'spring onions': 'cebollines', 'squid': 'calamar', 'strawberries': 'fresas', 'sugar': 'azúcar',
    'sweet potato': 'camote', 'sweet potatoes': 'camotes', 'thyme': 'tomillo', 'tomato': 'tomate',
    'tomatoes': 'tomates', 'tomato paste': 'concentrado de tomate', 'tuna': 'atún', 'turkey': 'pavo',
    'vanilla extract': 'extracto de vainilla', 'vegetable oil': 'aceite vegetal', 'vegetable stock': 'caldo de verduras',
    'vinegar': 'vinagre', 'walnuts': 'nueces', 'water': 'agua', 'white chocolate': 'chocolate blanco',
    'white wine': 'vino blanco', 'whole chicken': 'pollo entero', 'yogurt': 'yogur', 'yoghurt': 'yogur',
    'zucchini': 'calabacín', 'courgette': 'calabacín', 'black beans': 'frijoles negros', 'kidney beans': 'frijoles rojos',
    'spaghetti': 'espagueti', 'noodles': 'fideos', 'tortilla': 'tortilla', 'tortillas': 'tortillas',
  };

  traducirReceta(receta: RecetaExterna): Observable<RecetaExterna> {
    const nombre$ = this.traducirTexto(receta.nombre);
    const instrucciones$ = receta.instrucciones.trim()
      ? this.traducirInstrucciones(receta.instrucciones)
      : of(receta.instrucciones);
    const ingredientes$ = from(receta.ingredientes).pipe(
      concatMap((ingrediente) => this.traducirIngrediente(ingrediente).pipe(
        map((traducido) => traducido),
      )),
      toArray(),
    );

    return forkJoin({ nombre: nombre$, instrucciones: instrucciones$, ingredientes: ingredientes$ }).pipe(
      map((traducido) => ({
        ...receta,
        nombre: traducido.nombre,
        instrucciones: traducido.instrucciones,
        ingredientes: traducido.ingredientes,
      })),
    );
  }

  traducirTexto(texto: string): Observable<string> {
    const limpio = texto?.trim() ?? '';
    if (!limpio || this.pareceEspanol(limpio)) return of(limpio);
    return this.traducirPorFragmentos(limpio, 'en|es');
  }

  traducirAlIngles(texto: string): Observable<string> {
    const limpio = texto?.trim() ?? '';
    if (!limpio) return of(limpio);
    return this.traducirPorFragmentos(limpio, 'es|en');
  }

  private traducirIngrediente(ingrediente: IngredienteExterno): Observable<IngredienteExterno> {
    const clave = this.normalizar(ingrediente.nombre);
    const traduccionLocal = this.ingredientes[clave];
    const nombre$ = traduccionLocal ? of(traduccionLocal) : this.traducirTexto(ingrediente.nombre);
    return nombre$.pipe(map((nombre) => ({
      nombre,
      medida: this.traducirMedida(ingrediente.medida),
    })));
  }

  private traducirMedida(medida: string): string {
    let salida = medida.trim();
    const reemplazos: Array<[RegExp, string]> = [
      [/\bto taste\b/gi, 'al gusto'], [/\bas needed\b/gi, 'según se necesite'],
      [/\btablespoons?\b/gi, 'cucharadas'], [/\b(tbsp|tbs)\b/gi, 'cda.'],
      [/\bteaspoons?\b/gi, 'cucharaditas'], [/\btsp\b/gi, 'cdta.'],
      [/\bcups?\b/gi, 'tazas'], [/\bounces?\b/gi, 'onzas'], [/\bpounds?\b/gi, 'libras'],
      [/\bcloves?\b/gi, 'dientes'], [/\bpinch(es)?\b/gi, 'pizca'], [/\bhandfuls?\b/gi, 'puñados'],
      [/\bcans?\b/gi, 'latas'], [/\btins?\b/gi, 'latas'], [/\bpackets?\b/gi, 'paquetes'],
      [/\bpackages?\b/gi, 'paquetes'], [/\bbunches?\b/gi, 'manojos'], [/\bstalks?\b/gi, 'tallos'],
      [/\bslices?\b/gi, 'rebanadas'], [/\bsprigs?\b/gi, 'ramitas'], [/\bleaves\b/gi, 'hojas'],
      [/\bmedium\b/gi, 'mediano'], [/\blarge\b/gi, 'grande'], [/\bsmall\b/gi, 'pequeño'],
      [/\bof\b/gi, 'de'],
    ];
    for (const [patron, reemplazo] of reemplazos) salida = salida.replace(patron, reemplazo);
    return salida;
  }

  private traducirInstrucciones(texto: string): Observable<string> {
    const parrafos = texto.replace(/\r/g, '').split(/\n+/).map((p) => p.trim()).filter(Boolean);
    if (!parrafos.length) return of('');
    return from(parrafos).pipe(
      concatMap((parrafo) => this.traducirParrafo(parrafo)),
      toArray(),
      map((traducidos) => traducidos.join('\n\n')),
    );
  }

  private traducirParrafo(texto: string, idioma: 'en|es' | 'es|en' = 'en|es'): Observable<string> {
    const frases = texto.match(/[^.!?]+[.!?]?/gu)?.map((frase) => frase.trim()).filter(Boolean) ?? [texto];
    const fragmentos: string[] = [];
    let actual = '';
    for (const frase of frases) {
      if (this.bytes(frase) > this.maxBytesPorFragmento) {
        if (actual) fragmentos.push(actual);
        actual = '';
        fragmentos.push(...this.dividirPorPalabras(frase));
        continue;
      }
      const candidato = actual ? `${actual} ${frase}` : frase;
      if (this.bytes(candidato) <= this.maxBytesPorFragmento) actual = candidato;
      else {
        if (actual) fragmentos.push(actual);
        actual = frase;
      }
    }
    if (actual) fragmentos.push(actual);
    return from(fragmentos).pipe(
      concatMap((fragmento) => this.traducirFragmento(fragmento, idioma)),
      toArray(),
      map((traducidos) => traducidos.join(' ')),
    );
  }

  private traducirPorFragmentos(texto: string, idioma: 'en|es' | 'es|en'): Observable<string> {
    const parrafos = texto.replace(/\r/g, '').split(/\n+/).map((p) => p.trim()).filter(Boolean);
    return from(parrafos).pipe(
      concatMap((parrafo) => this.traducirParrafo(parrafo, idioma)),
      toArray(),
      map((traducidos) => traducidos.join('\n\n')),
      catchError(() => of(texto)),
    );
  }

  private traducirFragmento(texto: string, idioma: 'en|es' | 'es|en' = 'en|es'): Observable<string> {
    const clave = this.claveCache(`${idioma}:${texto}`);
    const enMemoria = this.cacheMemoria.get(clave);
    if (enMemoria) return of(enMemoria);
    const persistido = this.leerCache(clave);
    if (persistido) {
      this.cacheMemoria.set(clave, persistido);
      return of(persistido);
    }
    if (idioma === 'en|es' && this.pareceEspanol(texto)) return of(texto);

    const parametros = new HttpParams().set('q', texto).set('langpair', idioma);
    return this.http.get<RespuestaTraduccion>(this.url, { params: parametros }).pipe(
      map((respuesta) => {
        const resultado = this.decodificarEntidades(respuesta.responseData?.translatedText ?? '').trim();
        if (respuesta.responseStatus !== 200 || !resultado) return texto;
        this.cacheMemoria.set(clave, resultado);
        this.guardarCache(clave, resultado);
        return resultado;
      }),
      catchError(() => of(texto)),
    );
  }

  private dividirPorPalabras(texto: string): string[] {
    const trozos: string[] = [];
    let actual = '';
    for (const palabra of texto.split(/\s+/)) {
      const candidato = actual ? `${actual} ${palabra}` : palabra;
      if (this.bytes(candidato) <= this.maxBytesPorFragmento) actual = candidato;
      else {
        if (actual) trozos.push(actual);
        actual = palabra;
      }
    }
    if (actual) trozos.push(actual);
    return trozos;
  }

  private pareceEspanol(texto: string): boolean {
    return /[áéíóúñ¿¡]/i.test(texto)
      || /\b(el|la|los|las|de|del|con|para|una|uno|unos|unas|por|hasta|mezcla|agrega|añade|cocina|hornea|sirve|minutos|segundos|cucharada|cucharadita|taza|pizca|al gusto)\b/i.test(texto);
  }

  private normalizar(texto: string): string {
    return texto.trim().toLocaleLowerCase('en').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/\s+/g, ' ');
  }

  private bytes(texto: string): number {
    return new TextEncoder().encode(texto).length;
  }

  private claveCache(texto: string): string {
    let hash = 2166136261;
    for (const caracter of texto.toLocaleLowerCase()) {
      hash ^= caracter.charCodeAt(0);
      hash = Math.imul(hash, 16777619);
    }
    return `receton-traduccion-es-${(hash >>> 0).toString(16)}`;
  }

  private leerCache(clave: string): string | null {
    try {
      return typeof localStorage === 'undefined' ? null : localStorage.getItem(clave);
    } catch {
      return null;
    }
  }

  private guardarCache(clave: string, valor: string): void {
    try {
      if (typeof localStorage !== 'undefined') localStorage.setItem(clave, valor);
    } catch {
      // La traducción sigue funcionando aunque el navegador no permita guardar caché.
    }
  }

  private decodificarEntidades(texto: string): string {
    return texto
      .replace(/&amp;/gi, '&')
      .replace(/&quot;/gi, '"')
      .replace(/&#39;|&apos;/gi, "'")
      .replace(/&lt;/gi, '<')
      .replace(/&gt;/gi, '>');
  }
}
