# Manual de usuario — Form Recetas

Usa este archivo como plantilla para el manual que pide el profesor.
Ve llenando cada sección con capturas de pantalla de tu propia app corriendo.

## 1. Introducción
Qué es Form Recetas y para qué sirve (2-3 líneas).

## 2. Requisitos para usarla
- Tener el backend de Spring Boot corriendo en `http://localhost:8080`.
- Tener el frontend de Angular corriendo en `http://localhost:4200`.

## 3. Inicio de sesión
1. Abre `http://localhost:4200`.
2. Ingresa tu usuario y contraseña.
   - Cuenta de prueba: `admin` / `admin123`.
3. Da clic en **Ingresar**.
   (agrega aquí una captura de pantalla de la pantalla de login)

## 4. Panel principal
Explica qué se ve en el panel: categorías, buscador, recomendaciones.
(captura de pantalla)

## 5. Ver el detalle de una receta
Da clic en cualquier tarjeta de receta para ver su información completa.
(captura de pantalla)

## 6. Cerrar sesión
Da clic en el botón **Cerrar sesión** en la barra superior.

## 7. Preguntas frecuentes
- "No cargan las recetas" → revisa que el backend de Spring Boot esté
  corriendo (`http://localhost:8080/api/recipes` debe responder JSON).
