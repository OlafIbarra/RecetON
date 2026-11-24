/**
 * Configuración de desarrollo de RecetON mediante dominio LAN estable.
 * Abrir la aplicación siempre con http://receton.test:4200.
 */
export const environment = {
  produccion: false,
  apiUrl: 'http://api.receton.test:8080',
  oauthClientId: 'receton-angular',
  oauthRedirectUri: 'http://receton.test:4200/oauth-callback',
};
