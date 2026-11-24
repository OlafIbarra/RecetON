# ZIP 02 — OAuth 2.0 + JWT para RecetON

Este parche adapta el inicio de sesión que usaba un token opaco a OAuth 2.0 Authorization Code + PKCE y access tokens JWT firmados, conservando Angular, Spring Boot, PostgreSQL y las rutas de negocio existentes.

## Antes de aplicar

1. Cierra el backend y Angular.
2. Haz una copia de seguridad completa de `proyecto_noe`.
3. Extrae el contenido de este ZIP en la carpeta que **contiene** a `proyecto_noe` (por ejemplo, el Escritorio). Permite combinar carpetas y reemplazar los archivos del parche.
4. El parche no reemplaza `application.properties`, para no sobrescribir tus credenciales ni la conexión que ya funciona con PostgreSQL. La configuración OAuth usa `http://172.20.10.2:8080` como emisor predeterminado en `SeguridadOAuth2Config.java`. Si cambia la IP, actualiza el valor predeterminado en esa clase o configura la variable de entorno `RECETON_OAUTH_ISSUER`; también actualiza el redirect URI de la IP en `RegisteredClient`.

No ejecutes scripts SQL ni cambies las 24 tablas de PostgreSQL: este parche no requiere migraciones de base de datos. Conserva tu `server.address=0.0.0.0` actual en `application.properties`.

## Arrancar el backend

Abre CMD/PowerShell en `proyecto_noe\backend`:

```powershell
.\gradlew.bat bootRun
```

La primera ejecución puede tener que descargar dependencias desde Maven Central; requiere conexión a Internet. Espera a ver que Spring Boot termina de arrancar. Si falla, guarda el primer error completo para diagnosticarlo; no borres la base de datos ni sus tablas.

## Arrancar Angular

En otra terminal:

```powershell
cd ..\recetas-angular
npm install
npm run start:lan
```

Abre en la laptop servidor `http://172.20.10.2:4200`. En la laptop cliente, abre la misma URL si ambas están conectadas a la misma red local y el firewall permite el puerto 4200.

Si solo estás probando en la misma laptop también puedes abrir `http://localhost:4200`; Angular selecciona la URL de la API según el host que utilizaste para abrir la página.

## Nuevo flujo de inicio de sesión

1. En la página de inicio de sesión de RecetON pulsa **Continuar con inicio de sesión seguro**.
2. El navegador se redirige al formulario de Spring Security en el backend (`/login`).
3. Introduce el nombre de usuario o correo y la contraseña de un usuario existente y activo en `form_recetas`.
4. Si las credenciales son correctas, Spring Authorization Server devuelve un código a `/oauth-callback` en Angular. Angular valida `state` e intercambia el código usando PKCE S256.
5. El servidor emite un JWT de acceso firmado. Angular guarda la sesión y envía el JWT como `Authorization: Bearer <token>` en llamadas a la API.

No se crea un usuario de prueba automáticamente ni se incluye ninguna contraseña real en este ZIP.

## Comprobaciones rápidas

Desde CMD en la laptop servidor:

```cmd
curl.exe -i http://localhost:8080/saludo
curl.exe -i http://172.20.10.2:8080/oauth2/jwks
curl.exe -i http://172.20.10.2:8080/api/categorias
```

Resultados esperados:

- `/saludo`: `200 OK` y el texto de saludo.
- `/oauth2/jwks`: `200 OK` y un JSON con una lista `keys` (clave pública para verificar JWT).
- `/api/categorias` sin token: `401 Unauthorized`, porque la API ahora está protegida.

La prueba final es iniciar sesión desde Angular y comprobar que las páginas y peticiones API sigan funcionando. Si aparece `401` después del inicio de sesión, revisa que la IP configurada en `receton.oauth2.issuer` coincida con el emisor y que el reloj del equipo esté correcto.

## Endpoints OAuth 2.0

- Autorización: `GET /oauth2/authorize`
- Intercambio de código por token: `POST /oauth2/token`
- Claves públicas: `GET /oauth2/jwks`
- Revocación estándar: `POST /oauth2/revoke` (esta configuración de demostración no emite refresh tokens)
- Formulario de acceso: `/login`

Cliente público registrado: `receton-angular`. No hay `client_secret` en Angular. El flujo es Authorization Code con PKCE (`S256`); no se debe volver a enviar la contraseña directamente a `/api/autenticacion/iniciar-sesion`.

Los access tokens duran 20 minutos. Esta configuración de demostración guarda los códigos/autorizaciones en memoria y genera una clave RSA al iniciar; por tanto, los tokens y autorizaciones dejan de servir después de reiniciar el backend. No debe desplegarse directamente en Internet ni tratarse como configuración productiva. Para una entrega real en producción se requeriría HTTPS, claves persistentes protegidas, almacenamiento persistente de autorizaciones y una política operativa de revocación.

## Compatibilidad con las pruebas anteriores

El endpoint antiguo `POST /api/autenticacion/iniciar-sesion` devuelve `410 Gone` intencionadamente para indicar que fue retirado. Por ello, la colección Postman anterior que intenta el login antiguo ya no sirve para autenticar. En el siguiente ZIP se actualizará la colección y el manual para OAuth 2.0 + JWT.

Las rutas de negocio se conservan; las llamadas `/api/**` ahora requieren un JWT con scope `api.read` para lectura y `api.write` para operaciones de escritura. La verificación de roles y cuenta activa sigue usando los datos existentes de `usuarios`, `usuario_rol` y `roles`.

## Si falla el arranque

- Comprueba que Java 17 está instalado: `java -version`.
- Ejecuta los comandos desde el directorio correcto; `gradlew.bat` está dentro de `backend`, no en `C:\Users\...`.
- Si Gradle no puede descargar dependencias, comprueba Internet y proxy/firewall.
- Si hay `401` en `/api/**`, comprueba que la página haya completado `/oauth-callback` y que Angular haya guardado el JWT.
- Si hay `403`, el token puede ser válido pero no tener el scope requerido o el usuario puede carecer del rol correspondiente.
- No compartas por chat contraseñas reales, el JWT activo ni el contenido completo de variables secretas.
