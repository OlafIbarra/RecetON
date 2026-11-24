# Auditoría técnica de RecetON

## 1. Alcance revisado

Se revisaron los archivos fuente incluidos en `proyecto_noe(1).zip`: `backend/`, `recetas-angular/`, controladores, servicios, repositorios, DTO, configuración, rutas Angular, servicios HTTP y scripts SQL.

## 2. Arquitectura actual

- **Frontend:** Angular 17, TypeScript con formularios tipados, rutas por dispositivo y servicios HTTP.
- **Backend:** Java 17, Spring Boot 4.1.1, Gradle 9.7.1 Wrapper, Spring MVC y JdbcTemplate.
- **Persistencia:** PostgreSQL, base esperada `form_recetas`.
- **Esquema base:** el archivo `backend/sql/estructura_form_recetas.sql` define 24 tablas de negocio.
- **Migraciones adicionales existentes:** `migracion_solicitudes_arco.sql` crea la tabla de solicitudes ARCO y `migracion_cuentas_eliminadas.sql` crea el archivo de cuentas eliminadas. Son extensiones ya presentes para funcionalidades añadidas; deben revisarse/aplicarse conscientemente, no ejecutar todo a ciegas.

## 3. Funcionalidades que sí aparecen en el ZIP

- Inicio de sesión con verificación de contraseña mediante BCrypt.
- Roles de usuario y administrador en `usuario_rol`/`roles`.
- CRUD de usuarios, categorías, recetas y productos.
- Detalle de receta, ingredientes, pasos, equipo de cocina, información nutricional y valoraciones.
- Imágenes de receta.
- Favoritos.
- Carrito de compras.
- Direcciones y métodos de pago limitados a tipo y últimos cuatro dígitos.
- Creación de pedidos, historial y administración de pedidos.
- Mi cuenta, cambio de contraseña y baja de cuenta.
- Solicitudes de derechos ARCO y gestión administrativa.
- Páginas Angular de inicio, reloj, teléfono, tableta, TV, detalle, favoritos, productos, carrito, checkout, pedidos, cuenta, privacidad y administración.
- Servicio para consultar recetas externas y traducción de recetas.

**Lista de compras:** no encontré en los fuentes de Angular incluidos en este ZIP una página/servicio persistente de lista de compras independiente. Puede que el parche que habíamos preparado no esté integrado en esta copia. No la voy a dar por terminada en esta auditoría.

## 4. Autenticación actual: no es JWT ni OAuth 2.0

### Lo que existe

- `SesionAutenticacionServicio` genera una cadena aleatoria Base64 URL-safe con 32 bytes de aleatoriedad.
- El backend guarda esa cadena en un `ConcurrentHashMap` junto con el ID de usuario y una expiración de ocho horas.
- El token desaparece al cerrar sesión o reiniciar el backend.
- `AutorizacionApiInterceptor` valida el token consultando ese mapa y aplica reglas de rol/propiedad.
- Angular guarda los datos de sesión y el token en `localStorage` y agrega `Authorization: Bearer ...` mediante `token.interceptor.ts`.
- Las contraseñas se comparan con BCrypt.

### Lo que falta para el requisito del profesor

- No aparece `spring-boot-starter-security` en el `build.gradle` actual.
- No hay servidor OAuth 2.0 ni endpoints estándar de autorización/token en el código fuente revisado.
- No hay emisión ni validación de JWT firmado.
- El endpoint `POST /api/autenticacion/iniciar-sesion` es un login propio, no un flujo OAuth 2.0.

### Implementación propuesta

Usar Spring Authorization Server y Spring Security Resource Server integrados en el backend, con OAuth 2.0 Authorization Code + PKCE para Angular (cliente público) y access tokens JWT firmados. Se evitará el antiguo grant de contraseña de OAuth, que no es recomendable para aplicaciones nuevas. El backend también debe comprobar firma, emisor y expiración del JWT, y conservar las reglas de administrador, usuario propietario y baja de cuenta.

La transición requiere cambios coordinados en Spring Boot y Angular. No debe añadirse JWT por encima del interceptor actual sin retirar/reemplazar la dependencia del mapa de sesiones, porque eso crearía dos mecanismos incompatibles.

## 5. Riesgos y correcciones que deben abordarse

1. **README desactualizado:** `backend/README-FORM-RECETAS.md` describe rutas en inglés (`/api/recipes`, `/api/categories`, `/api/users`) aunque el código actual usa nombres en español.
2. **Credenciales de base de datos inconsistentes:** `application.properties` y el README no describen los mismos valores predeterminados. Para la entrega debe usarse configuración por variables de entorno y dejar los secretos fuera de la colección Postman y del ZIP.
3. **CORS limitado a localhost:** `CorsConfig.java` permite `localhost:4200` y `127.0.0.1:4200`; no incluye el futuro dominio local.
4. **Angular apunta a localhost:** ambos archivos `environment*.ts` usan `http://localhost:8080`.
5. **Pruebas limitadas en el repo:** solo encontré una prueba de carga del contexto (`BackendApplicationTests`). Falta un conjunto de pruebas de autorización y regresión para los endpoints que usará el profesor.
6. **Seguridad API personalizada:** las reglas actuales están implementadas en un `HandlerInterceptor`; al introducir Spring Security será importante evitar que queden dos validadores de token intentando interpretar tokens diferentes.
7. **Errores y contratos:** el manual final se generará a partir de las rutas y DTO actuales, verificando cada payload. No se deben inventar columnas ni campos.

## 6. Cinco recursos recomendados para los 20 endpoints

Para respetar el requisito literal de 20 operaciones (4 por controlador: listar/leer, crear, actualizar, eliminar), propongo documentar estas cinco rutas de recurso:

- `CategoriasController` — `/api/categorias`
- `RecetasController` — `/api/recetas`
- `UsuariosController` — `/api/usuarios`
- `ProductosController` — `/api/productos`
- `ImagenesRecetaController` — `/api/recetas/{idReceta}/imagenes`

Se seleccionarán exactamente cuatro operaciones CRUD por recurso para las 20 pruebas de evaluación. Las consultas por ID y las rutas complementarias de recetas pueden documentarse como pruebas adicionales; no se eliminarán del sistema.

## 7. Dominio local

La configuración actual no define dominio local. El siguiente parche agrega una modalidad opcional `receton.local` sin quitar `localhost`, mantiene backend en el puerto 8080 y frontend en 4200, y añade instrucciones para el archivo `hosts` de Windows. El alias solo funcionará en cada equipo donde se configure; no equivale a publicar el sitio en Internet.
