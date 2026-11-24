/** Configuración de ejecución pública para RecetON. */
export interface RecetonRuntimeConfig {
  apiUrl?: string;
  oauthClientId?: string;
}

declare global {
  interface Window {
    __RECETON_CONFIG__?: RecetonRuntimeConfig;
  }
}

const API_LOCAL_PREDETERMINADA = 'http://api.receton.test:8080';
const CLIENTE_OAUTH_PREDETERMINADO = 'receton-angular';

function obtenerApiUrl(): string {
  const valor = typeof window !== 'undefined'
    ? window.__RECETON_CONFIG__?.apiUrl
    : undefined;
  return (typeof valor === 'string' && valor.trim() ? valor : API_LOCAL_PREDETERMINADA)
    .replace(/\/+$/, '');
}

function obtenerClienteOAuth(): string {
  const valor = typeof window !== 'undefined'
    ? window.__RECETON_CONFIG__?.oauthClientId
    : undefined;
  return typeof valor === 'string' && valor.trim()
    ? valor.trim()
    : CLIENTE_OAUTH_PREDETERMINADO;
}

function obtenerRedirectUri(): string {
  if (typeof window === 'undefined') {
    return 'http://receton.test:4200/oauth-callback';
  }
  // OAuth exige coincidencia exacta. Cada entorno utiliza el origen real
  // (dominio local, LAN o HTTPS público) desde el que abrió RecetON.
  return `${window.location.origin}/oauth-callback`;
}

export const environment = {
  produccion: false,
  get apiUrl(): string {
    return obtenerApiUrl();
  },
  get oauthClientId(): string {
    return obtenerClienteOAuth();
  },
  get oauthRedirectUri(): string {
    return obtenerRedirectUri();
  },
};
