# Backend Form Recetas — primera entrega CRUD

Backend basado en Spring Boot, Gradle, Java 17 y PostgreSQL. Conserva el endpoint de prueba `/saludo` y agrega tres controladores con operaciones CRUD.

## 1. Preparar PostgreSQL

1. En pgAdmin, crea una base de datos llamada `form_recetas` si todavía no existe.
2. Selecciona `form_recetas` y abre Query Tool.
3. Si las tablas aún no existen, ejecuta `sql/estructura_form_recetas.sql`. Si ya tienes las tablas creadas, no ejecutes otra vez el script.
4. Comprueba que estén creadas las 24 tablas usadas por el proyecto.

## 2. Configurar usuario y contraseña de PostgreSQL

Abre `src/main/resources/application.properties`. La configuración apunta por defecto a:

- Base de datos: `form_recetas`
- Servidor: `localhost:5432`
- Usuario predeterminado: `postgres`
- Contraseña: vacía

Si tu usuario de PostgreSQL usa una contraseña, cambia esta línea:

```properties
spring.datasource.password=${DB_PASSWORD:}
```

Por ejemplo, para una práctica local podrías poner tu contraseña directamente:

```properties
spring.datasource.password=TU_CONTRASENA_DE_POSTGRES
```

No compartas ese archivo con una contraseña real. También puedes configurar la variable de entorno `DB_PASSWORD`.

## 3. Ejecutar el backend

Abre una terminal en la carpeta `backend` y ejecuta:

**Windows:**

```powershell
.\gradlew.bat bootRun
```

También puedes abrir el proyecto desde tu IDE y ejecutar `BackendApplication.java`. La primera ejecución del wrapper puede necesitar internet para descargar Gradle y las dependencias.

Al iniciar, el backend crea cuatro secuencias de PostgreSQL para asignar IDs de usuarios, recetas, categorías e imágenes, ya que el DDL original usa `INT PRIMARY KEY` sin autoincremento. Las secuencias se sincronizan con los IDs existentes.

## 4. Controladores y operaciones

| Controlador | Ruta | Operaciones |
|---|---|---|
| `RecetasController` | `/api/recipes` | GET lista, GET por ID, POST, PUT por ID, DELETE por ID |
| `CategoriasController` | `/api/categories` | GET lista, GET por ID, POST, PUT por ID, DELETE por ID |
| `UsuariosController` | `/api/users` | GET lista, GET por ID, POST, PUT por ID, DELETE por ID |

El login que espera Angular también está disponible en `POST /api/auth/login`.

URLs locales completas:

- `http://localhost:8080/api/recipes`
- `http://localhost:8080/api/categories`
- `http://localhost:8080/api/users`
- `http://localhost:8080/api/auth/login`
- `http://localhost:8080/saludo` (prueba original)

Los controladores usan CORS para aceptar solicitudes desde `http://localhost:4200` y `http://127.0.0.1:4200`.

## 5. Probar en Postman

### Crear una categoría

`POST http://localhost:8080/api/categories`

```json
{
  "name": "Postres",
  "image": "https://ejemplo.com/postres.jpg"
}
```

### Crear un usuario

`POST http://localhost:8080/api/users`

```json
{
  "username": "admin",
  "email": "admin@formrecetas.local",
  "password": "admin123",
  "fullName": "Administrador Form Recetas",
  "active": true
}
```

La contraseña debe tener al menos ocho caracteres. Se guarda como hash BCrypt y no se devuelve en consultas.

### Crear una receta

`POST http://localhost:8080/api/recipes`

```json
{
  "name": "Ensalada de ejemplo",
  "image": "https://ejemplo.com/ensalada.jpg",
  "price": "120.00",
  "time": "25 min",
  "tag": "Postres",
  "description": "Una receta para probar el CRUD.",
  "idUsuario": 1,
  "idPais": null,
  "servings": 2,
  "difficulty": "Fácil",
  "active": true
}
```

`idUsuario` debe existir si se envía. `idPais` puede ser `null` o un ID existente en `paises`. `tag` se vincula a una categoría si ya existe una categoría con ese nombre; no se crea automáticamente. En el ejemplo de usuario, usa en `idUsuario` el ID que devolvió el POST del usuario (puede no ser 1).

### Actualizar una categoría

`PUT http://localhost:8080/api/categories/1`

```json
{
  "name": "Postres caseros",
  "image": "https://ejemplo.com/postres-caseros.jpg"
}
```

### Eliminar una categoría

`DELETE http://localhost:8080/api/categories/1`

Respuestas `204` significan que se eliminó correctamente. Las respuestas `404` indican que el ID no existe. Una respuesta `409` indica que una restricción de datos impide la operación o que el registro está conectado a un pedido/carrito.

## 6. Forma de los datos que recibe Angular

`GET /api/recipes` devuelve `id`, `name`, `image`, `price`, `rating`, `time`, `tag` y `tagColor`, además de algunos campos de detalle. La imagen se obtiene de `imagenes_receta`, la categoría de `receta_categoria` / `categorias`, y la calificación del promedio de `valoraciones`.

`GET /api/categories` devuelve `id`, `name` e `image`, como espera `receta-api.servicio.ts`.

El CRUD de usuarios nunca expone `contrasena_hash`.

## 7. Consideraciones

- Primero debe existir la base y sus tablas antes de iniciar Spring Boot.
- Recetas con productos ligados a carritos o pedidos no se eliminan para conservar el historial.
- Usuarios con recetas publicadas o pedidos tampoco se eliminan; se devuelve HTTP `409` para proteger los datos relacionados.
- El endpoint CRUD todavía no usa autorización por roles. Para la práctica, se centra en el CRUD y la conexión a PostgreSQL; no debe publicarse como servicio real sin controles de acceso adicionales.
