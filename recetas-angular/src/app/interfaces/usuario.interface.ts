/** Usuario usado en datos de desarrollo; nunca se guarda su contraseña en la sesión. */
export interface Usuario {
  id: number;
  nombreUsuario: string;
  contrasena: string;
  nombreCompleto: string;
}

/** Credenciales enviadas al backend. */
export interface CredencialesLogin {
  nombreUsuario: string;
  contrasena: string;
}

/** Datos de sesión OAuth2; el servidor valida criptográficamente el access token JWT. */
export interface UsuarioSesion {
  id: number;
  nombreUsuario: string;
  nombreCompleto: string;
  roles: string[];
  token: string;
  refreshToken?: string;
  expiraEn?: number;
}

/** Respuesta heredada del endpoint de inicio de sesión anterior (retirado al migrar a OAuth2). */
export interface RespuestaLogin {
  exito: boolean;
  mensaje: string;
  usuario?: Omit<UsuarioSesion, 'token'>;
  token?: string;
}
