# RecetON — Auditoría inicial del proyecto

Esta carpeta es documentación de revisión. No sobrescribe ni altera el código de Angular, Spring Boot ni la base de datos.

## Resultado principal

El proyecto subido ya tiene una cantidad importante de funcionalidad. Lo que todavía **no** tiene implementado es OAuth 2.0 con un flujo real y JWT firmado: actualmente devuelve un token opaco aleatorio almacenado en memoria.

## Documentos

- `AUDITORIA_TECNICA.md`: componentes existentes, fortalezas, brechas y riesgos.
- `INVENTARIO_ENDPOINTS.md`: inventario de controladores y propuesta de los 20 endpoints del manual.
- `PLAN_DE_ZIPS.md`: orden de trabajo por parches para proteger las funcionalidades actuales.

## Cómo aplicar

Copia la carpeta `documentacion/auditoria` al directorio raíz `proyecto_noe` de tu proyecto. No es necesario cambiar nada más para esta etapa.

## Nota de verificación

Intenté ejecutar `./gradlew test` en el entorno de análisis, pero el wrapper necesitó descargar Gradle 9.7.1 y la red de ese entorno no permitió resolver `services.gradle.org`. Esto no demuestra que el código falle; significa que no pude ejecutar la prueba de compilación aquí. Las pruebas definitivas se harán al integrar cada parche en tu equipo.
