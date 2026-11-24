# RecetON — PARCHE 04D: respaldo y recuperación de PostgreSQL

## Objetivo

Agregar procedimientos repetibles para respaldar la base PostgreSQL `form_recetas`, comprobar que el archivo de respaldo sea legible por las herramientas de PostgreSQL y restaurarlo de forma explícita en una base de datos destino.

Este paquete es operativo: **no modifica Angular, Spring Boot, las rutas HTTP, los DTO, los contratos JSON ni el esquema de las 24 tablas**. Tampoco contiene migraciones SQL.

## Archivos incluidos

- `backend/scripts/RecetonPostgreSql.psm1`: utilidades compartidas para localizar herramientas PostgreSQL, resolver la conexión y solicitar la contraseña sin mostrarla.
- `backend/scripts/respaldar-form-recetas.ps1`: crea un respaldo completo en formato custom (`.backup`), genera el catálogo legible por `pg_restore` y calcula SHA-256.
- `backend/scripts/verificar-respaldo-form-recetas.ps1`: comprueba que `pg_restore` pueda leer el catálogo y, si existe el archivo `.sha256`, valida que el hash coincida.
- `backend/scripts/restaurar-form-recetas.ps1`: restaura en una base destino existente. No elimina la base de datos; limpiar objetos requiere el parámetro explícito `-LimpiarDestino` y una confirmación escrita exacta.

## Requisitos

- Windows PowerShell 5.1 o superior.
- Herramientas de cliente de PostgreSQL: `pg_dump.exe` y `pg_restore.exe`. Si no están en `PATH`, define `PG_BIN` con la ruta de la carpeta `bin` de PostgreSQL. Ejemplo:

```powershell
$env:PG_BIN = 'C:\Program Files\PostgreSQL\17\bin'
```

Utiliza herramientas de cliente compatibles con la versión del servidor PostgreSQL.

Los scripts toman la conexión de `DB_URL` y `DB_USERNAME` cuando están presentes. Si no, usan `localhost:5432`, base `form_recetas` y usuario `postgres`. También permiten indicar los datos por parámetros.

## Instalación

1. Detén el backend si está ejecutándose.
2. Extrae el ZIP en la raíz de `proyecto_noe`, conservando las carpetas `backend/` y `documentacion/`.
3. No reemplaces ni regeneres `backend/secrets/receton-jwt.p12`; este paquete no incluye ni necesita el keystore.
4. No hace falta ejecutar migraciones ni modificar PostgreSQL.

## 1. Crear un respaldo

Desde PowerShell, en la carpeta `proyecto_noe\backend`:

```powershell
.\scripts\respaldar-form-recetas.ps1
```

El script detecta `DB_URL` y `DB_USERNAME` si están definidos. Si no hay una contraseña disponible en `PGPASSWORD` o `DB_PASSWORD`, la solicita de forma oculta.

Por defecto, guarda los archivos fuera del proyecto, en:

```text
%LOCALAPPDATA%\RecetON\Respaldos
```

Cada respaldo produce tres archivos que deben conservarse juntos:

- `form_recetas_FECHA_HORA.backup`: archivo de respaldo PostgreSQL.
- `form_recetas_FECHA_HORA.backup.list.txt`: catálogo de objetos contenido en el respaldo.
- `form_recetas_FECHA_HORA.backup.sha256`: huella SHA-256 para detectar cambios posteriores.

También se puede elegir otro destino:

```powershell
.\scripts\respaldar-form-recetas.ps1 -Destino 'D:\Respaldos\RecetON'
```

Y se pueden especificar los datos de conexión:

```powershell
.\scripts\respaldar-form-recetas.ps1 -HostName 'localhost' -Port 5432 -BaseDatos 'form_recetas' -Usuario 'postgres'
```

## 2. Verificar un respaldo

Copia la ruta del archivo `.backup` generado y ejecuta:

```powershell
.\scripts\verificar-respaldo-form-recetas.ps1 -Archivo 'C:\RUTA\form_recetas_FECHA_HORA.backup'
```

La comprobación valida que `pg_restore` pueda leer el catálogo del archivo y compara el SHA-256 cuando está presente el archivo `.sha256` correspondiente.

**Alcance:** esto comprueba legibilidad e integridad frente al hash guardado. No demuestra por sí solo que todos los objetos puedan restaurarse correctamente; una restauración de prueba en una base aislada es la comprobación más completa.

## 3. Restaurar en una base de prueba

Crea primero una base de datos vacía de prueba desde pgAdmin, por ejemplo `form_recetas_restauracion`. Luego, desde `proyecto_noe\backend`, ejecuta:

```powershell
.\scripts\restaurar-form-recetas.ps1 `
  -Archivo 'C:\RUTA\form_recetas_FECHA_HORA.backup' `
  -BaseDatosDestino 'form_recetas_restauracion'
```

El script solicita que escribas exactamente `RESTAURAR form_recetas_restauracion`. Si la confirmación no coincide, cancela sin conectarse para restaurar.

La restauración normal no limpia los objetos existentes. Por eso, utiliza preferentemente una base destino recién creada y vacía.

### Reemplazar objetos en una base destino existente

Solo cuando sepas que deseas sustituir los objetos de esa base, añade `-LimpiarDestino`:

```powershell
.\scripts\restaurar-form-recetas.ps1 `
  -Archivo 'C:\RUTA\form_recetas_FECHA_HORA.backup' `
  -BaseDatosDestino 'form_recetas_restauracion' `
  -LimpiarDestino
```

En este modo se solicitará escribir exactamente `LIMPIAR form_recetas_restauracion`. La opción usa `pg_restore --clean --if-exists`: puede eliminar tablas, vistas, secuencias y otros objetos dentro de la base destino. **No la uses contra `form_recetas` de trabajo salvo que realmente quieras reemplazar sus objetos y tengas un respaldo verificado.** El script no ejecuta `DROP DATABASE`.

## Qué incluye y qué no incluye el respaldo

`pg_dump` realiza el respaldo de la base `form_recetas`: estructura, datos y objetos propios de esa base que admite el formato de volcado de PostgreSQL.

Los roles y usuarios a nivel de clúster PostgreSQL no forman parte de un `pg_dump` individual. Si se migra a otro servidor, deben crearse por separado. La restauración del paquete usa `--no-owner --no-acl`, por lo que los objetos restaurados quedarán bajo el usuario que ejecute la restauración y no intentará recrear ACL u ownership del servidor original.

El respaldo de PostgreSQL no incluye el keystore JWT `backend/secrets/receton-jwt.p12`, archivos de Angular ni otros documentos del proyecto. Conserva el keystore por separado y protegido; no lo regeneres para restaurar una base de datos.

## Recomendación de uso para la entrega escolar

Antes de una demostración, cambio importante o entrega final:

1. Crea un respaldo con `respaldar-form-recetas.ps1`.
2. Ejecuta `verificar-respaldo-form-recetas.ps1` sobre el archivo recién creado.
3. Conserva juntos el `.backup`, `.list.txt` y `.sha256` en una ubicación segura distinta de la carpeta del proyecto.
4. Al menos una vez, restaura en una base de prueba separada para comprobar el procedimiento completo.

Este paquete no configura respaldos automáticos ni los sube a servicios externos; deja los procedimientos manuales disponibles para ejecutarlos cuando los necesites.
