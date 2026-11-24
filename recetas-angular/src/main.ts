import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import type { RecetonRuntimeConfig } from './environments/environment';

declare global {
  interface Window {
    __RECETON_CONFIG__?: RecetonRuntimeConfig;
  }
}

/**
 * Carga la configuración pública de despliegue antes de iniciar Angular.
 * El JSON solo contiene direcciones públicas: nunca guardar claves, contraseñas ni tokens.
 * Si no está disponible, se conserva la configuración local predeterminada.
 */
async function cargarConfiguracionPublica(): Promise<void> {
  try {
    const respuesta = await fetch('/assets/receton-config.json', { cache: 'no-store' });
    if (!respuesta.ok) {
      console.warn('No se encontró receton-config.json; se utilizarán los valores locales.');
      return;
    }

    const configuracion = await respuesta.json() as Partial<RecetonRuntimeConfig>;
    if (typeof configuracion.apiUrl !== 'string' || configuracion.apiUrl.trim().length === 0) {
      console.warn('La configuración pública no incluye una apiUrl válida; se utilizará la configuración local.');
      return;
    }

    const api = new URL(configuracion.apiUrl);
    if (api.protocol !== 'http:' && api.protocol !== 'https:') {
      console.warn('La apiUrl debe utilizar HTTP o HTTPS; se utilizará la configuración local.');
      return;
    }

    window.__RECETON_CONFIG__ = {
      apiUrl: api.toString().replace(/\/$/, ''),
      oauthClientId: typeof configuracion.oauthClientId === 'string'
        ? configuracion.oauthClientId
        : 'receton-angular',
    };
  } catch (error: unknown) {
    console.warn('No fue posible leer la configuración pública de RecetON; se utilizarán los valores locales.', error);
  }
}

async function iniciarAplicacion(): Promise<void> {
  await cargarConfiguracionPublica();
  await bootstrapApplication(AppComponent, appConfig)
    .catch((error: unknown) => console.error('No fue posible iniciar RecetON.', error));
}

void iniciarAplicacion();
