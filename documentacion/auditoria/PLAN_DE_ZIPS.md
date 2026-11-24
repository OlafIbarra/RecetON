# Plan incremental de entrega para RecetON

## ZIP 00 — Auditoría (este paquete)

Inventario del código existente, estado de OAuth/JWT, rutas actuales, selección de los 20 endpoints y riesgos conocidos. No altera el código funcional.

## ZIP 01 — Dominio local de Windows

- alias de frontend y API con archivo `hosts`;
- modo opcional de Angular sin eliminar `localhost`;
- ajuste CORS para el nuevo origen;
- guía de activación y reversión.

## ZIP 02 — OAuth 2.0 + JWT

- Spring Security y Spring Authorization Server;
- Authorization Code + PKCE para Angular y Postman;
- JWT firmado y validación como OAuth2 Resource Server;
- carga de usuario/roles desde PostgreSQL y mantenimiento de las reglas de propiedad y administrador;
- adaptación coordinada del login Angular;
- pruebas de token ausente, expirado, inválido y permisos.

## ZIP 03 — CRUD de evaluación

- confirmar los 5 controladores seleccionados;
- completar y estabilizar 20 operaciones de evaluación;
- respuestas HTTP, validación de DTO y errores predecibles;
- pruebas de regresión.

## ZIP 04 — Colección y manual Postman/CMD

- colección importable;
- entorno importable y variables base;
- OAuth 2.0 Authorization Code + PKCE para solicitar el token;
- 20 solicitudes numeradas con cuerpos y pruebas;
- comandos `curl` equivalentes para CMD;
- diagnóstico de errores comunes.

## ZIP 05 — Entrega consolidada

- manual de instalación desde cero en la computadora del profesor;
- instrucciones PostgreSQL y variables de entorno;
- arranque de backend, frontend y dominio local;
- colección y entorno Postman;
- evidencias de pruebas tras ejecutarlas en el equipo disponible.

## Regla de trabajo

Cada ZIP se aplicará sobre la versión previa y tendrá lista de archivos modificados. No se regenerará `estructura_form_recetas.sql`, no se cambiarán columnas existentes y no se tocarán contratos Angular/Java sin comprobar sus dependencias.
