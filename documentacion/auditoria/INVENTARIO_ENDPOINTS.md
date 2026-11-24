# Inventario de API de RecetON (rutas presentes en el código fuente revisado)

Todos los métodos se entienden bajo el backend base `http://localhost:8080` o el dominio local que se configure posteriormente.

## Las 20 pruebas CRUD propuestas para el manual

Los cuatro métodos de cada grupo son la lista de evaluación (4 × 5 = 20). Las lecturas por ID que ya existen se añadirán como pruebas complementarias, sin contarlas dentro de las 20.

| Nº | Controlador/recurso | Método | Ruta | Qué comprueba |
|---:|---|---|---|---|
| 1 | Categorías | GET | `/api/categorias` | Listar categorías |
| 2 | Categorías | POST | `/api/categorias` | Crear categoría |
| 3 | Categorías | PUT | `/api/categorias/{id}` | Actualizar categoría |
| 4 | Categorías | DELETE | `/api/categorias/{id}` | Eliminar categoría |
| 5 | Recetas | GET | `/api/recetas` | Listar recetas |
| 6 | Recetas | POST | `/api/recetas` | Crear receta |
| 7 | Recetas | PUT | `/api/recetas/{id}` | Actualizar receta |
| 8 | Recetas | DELETE | `/api/recetas/{id}` | Eliminar receta |
| 9  | Usuarios | GET | `/api/usuarios` | Listar usuarios (requiere administrador) |
| 10 | Usuarios | POST | `/api/usuarios` | Crear usuario (requiere administrador) |
| 11 | Usuarios | PUT | `/api/usuarios/{id}` | Actualizar usuario (requiere administrador) |
| 12 | Usuarios | DELETE | `/api/usuarios/{id}` | Eliminar usuario (requiere administrador) |
| 13 | Productos | GET | `/api/productos` | Listar productos |
| 14 | Productos | POST | `/api/productos` | Crear producto |
| 15 | Productos | PUT | `/api/productos/{id}` | Actualizar producto |
| 16 | Productos | DELETE | `/api/productos/{id}` | Eliminar producto |
| 17 | Imágenes de receta | GET | `/api/recetas/{idReceta}/imagenes` | Listar imágenes de una receta |
| 18 | Imágenes de receta | POST | `/api/recetas/{idReceta}/imagenes` | Agregar imagen |
| 19 | Imágenes de receta | PUT | `/api/recetas/{idReceta}/imagenes/{idImagen}/portada` | Elegir imagen de portada |
| 20 | Imágenes de receta | DELETE | `/api/recetas/{idReceta}/imagenes/{idImagen}` | Eliminar imagen |

**Nota sobre el alcance:** en el código actual los CRUD de usuarios y operaciones de escritura generales están sujetos a reglas de administrador. Las solicitudes definitivas de Postman deben autenticarse con una cuenta administradora de prueba que exista en la base de datos. No se deben incluir contraseñas reales ni crear un administrador con permisos automáticamente sin decisión explícita.

## Rutas adicionales ya existentes

### Saludo
- `GET /saludo`
- `GET /adios`

### Autenticación actual (se sustituirá al introducir OAuth 2.0)
- `POST /api/autenticacion/iniciar-sesion`
- `DELETE /api/autenticacion/cerrar-sesion`

### Categorías
- `GET /api/categorias/{id}`

### Recetas y relaciones
- `GET /api/recetas/{id}`
- `GET /api/recetas/{id}/detalle`
- `POST /api/recetas/{id}/ingredientes`
- `PUT /api/recetas/{id}/ingredientes/{idIngrediente}`
- `DELETE /api/recetas/{id}/ingredientes/{idIngrediente}`
- `POST /api/recetas/{id}/pasos`
- `PUT /api/recetas/{id}/pasos/{idPaso}`
- `DELETE /api/recetas/{id}/pasos/{idPaso}`
- `POST /api/recetas/{id}/equipos`
- `DELETE /api/recetas/{id}/equipos/{idEquipo}`
- `PUT /api/recetas/{id}/informacion-nutricional`
- `DELETE /api/recetas/{id}/informacion-nutricional`
- `POST /api/recetas/{id}/valoraciones`

### Favoritos y carrito
- `GET /api/usuarios/{idUsuario}/favoritos`
- `GET /api/usuarios/{idUsuario}/favoritos/{idReceta}`
- `PUT /api/usuarios/{idUsuario}/favoritos/{idReceta}`
- `DELETE /api/usuarios/{idUsuario}/favoritos/{idReceta}`
- `GET /api/usuarios/{idUsuario}/carrito`
- `POST /api/usuarios/{idUsuario}/carrito/items`
- `PUT /api/usuarios/{idUsuario}/carrito/items/{idProducto}`
- `DELETE /api/usuarios/{idUsuario}/carrito/items/{idProducto}`
- `DELETE /api/usuarios/{idUsuario}/carrito`

### Direcciones, métodos de pago e historial
- `GET|POST /api/usuarios/{idUsuario}/direcciones`
- `PUT|DELETE /api/usuarios/{idUsuario}/direcciones/{idDireccion}`
- `GET|POST /api/usuarios/{idUsuario}/metodos-pago`
- `PUT|DELETE /api/usuarios/{idUsuario}/metodos-pago/{idMetodoPago}`
- `GET /api/usuarios/{idUsuario}/pedidos`
- `GET /api/usuarios/{idUsuario}/pedidos/{idPedido}`
- `POST /api/usuarios/{idUsuario}/pedidos`

### Mi cuenta y ARCO
- `GET|PUT|DELETE /api/mi-cuenta`
- `PUT /api/mi-cuenta/contrasena`
- `GET|POST /api/solicitudes-arco`
- `GET /api/administracion/solicitudes-arco`
- `PUT /api/administracion/solicitudes-arco/{idSolicitud}`

### Administración de pedidos
- `GET /api/administracion/pedidos`
- `GET /api/administracion/pedidos/{idPedido}`
- `PUT /api/administracion/pedidos/{idPedido}/estado`

## Payloads confirmados por DTO

- Categoría: `{"name":"...","image":"https://..."}`
- Receta: `{"name":"...","image":"https://...","price":"45.00","rating":4.5,"time":"25 min","tag":"...","tagColor":"...","description":"...","idUsuario":1,"idPais":null,"servings":2,"difficulty":"Fácil","active":true}`
- Producto: `{"idReceta":1,"nombreProducto":"...","descripcion":"...","precio":25.50,"stock":10}`
- Imagen de receta: `{"urlImagen":"https://...","esPortada":false}`
- Usuario: `{"username":"...","email":"...","password":"...","fullName":"...","active":true}`

Los ejemplos anteriores son la forma de los DTO, no garantizan que esos IDs existan en la base de datos del profesor. El manual final incluirá instrucciones para tomar los IDs devueltos por los POST anteriores y usarlos en las siguientes solicitudes.
