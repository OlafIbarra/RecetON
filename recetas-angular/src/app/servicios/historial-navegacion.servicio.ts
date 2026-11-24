import { Injectable, inject } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

/** Mantiene el historial interno de RecetON para regresar desde secciones secundarias. */
@Injectable({ providedIn: 'root' })
export class HistorialNavegacionServicio {
  private readonly router = inject(Router);
  private readonly claveHistorial = 'receton:historial-rutas';
  private historial: string[] = [];
  private ignorarSiguienteNavegacion = false;

  constructor() {
    this.historial = this.leerHistorial();

    this.router.events
      .pipe(filter((evento): evento is NavigationEnd => evento instanceof NavigationEnd))
      .subscribe((evento: NavigationEnd) => {
        const nuevaRuta = this.normalizar(evento.urlAfterRedirects);
        if (!this.esRutaInterna(nuevaRuta)) return;

        if (this.ignorarSiguienteNavegacion) {
          // regresar() ya retiró la ruta actual de la pila antes de navegar.
          this.ignorarSiguienteNavegacion = false;
        } else if (this.historial.length > 1 && this.historial[this.historial.length - 2] === nuevaRuta) {
          // También reconoce los enlaces de regreso ya existentes (por ejemplo,
          // finalizar compra -> carrito o una subsección de administración -> panel).
          this.historial.pop();
        } else if (this.historial[this.historial.length - 1] !== nuevaRuta) {
          this.historial.push(nuevaRuta);
        }

        // Limita el tamaño para evitar que la sesión acumule rutas indefinidamente.
        this.historial = this.historial.slice(-30);
        this.guardarHistorial();
      });
  }

  /** Regresa una sola pantalla en el historial; si no existe, vuelve al selector principal. */
  regresar(): Promise<boolean> {
    const actual = this.normalizar(this.router.url);
    if (this.historial[this.historial.length - 1] !== actual && this.esRutaInterna(actual)) {
      this.historial.push(actual);
    }

    if (this.historial.length > 1) {
      this.historial.pop();
    } else {
      this.historial = ['/inicio'];
    }

    const destino = this.historial[this.historial.length - 1] ?? '/inicio';
    this.ignorarSiguienteNavegacion = true;
    this.guardarHistorial();

    return this.router.navigateByUrl(destino).then((exito) => {
      if (!exito) this.ignorarSiguienteNavegacion = false;
      return exito;
    });
  }

  /** Lleva siempre al selector principal de dispositivos. Se conserva para acciones explícitas de inicio. */
  volverAlInicio(): Promise<boolean> {
    return this.router.navigateByUrl('/inicio');
  }

  private normalizar(url: string): string {
    const ruta = url.trim() || '/';
    return ruta.startsWith('/') && !ruta.startsWith('//') ? ruta : `/${ruta}`;
  }

  private esRutaInterna(url: string): boolean {
    const ruta = url.split(/[?#]/, 1)[0];
    return ruta.startsWith('/')
      && ruta !== '/'
      && ruta !== '/inicio-sesion'
      && ruta !== '/oauth-callback';
  }

  private leerHistorial(): string[] {
    try {
      if (typeof sessionStorage === 'undefined') return [];
      const contenido = sessionStorage.getItem(this.claveHistorial);
      if (!contenido) return [];
      const datos: unknown = JSON.parse(contenido);
      return Array.isArray(datos)
        ? datos.filter((valor): valor is string => typeof valor === 'string' && this.esRutaInterna(valor)).slice(-30)
        : [];
    } catch {
      return [];
    }
  }

  private guardarHistorial(): void {
    try {
      if (typeof sessionStorage !== 'undefined') {
        sessionStorage.setItem(this.claveHistorial, JSON.stringify(this.historial));
      }
    } catch {
      // La navegación sigue funcionando aunque el navegador bloquee el almacenamiento de sesión.
    }
  }
}
