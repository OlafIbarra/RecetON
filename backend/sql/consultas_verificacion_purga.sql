-- Consultas de verificación para la política de conservación de cuentas eliminadas.
-- Ejecutar en la base form_recetas.

-- 1. Ver cuántos registros ya vencieron (solo lectura).
SELECT COUNT(*) AS registros_vencidos
FROM cuentas_eliminadas
WHERE fecha_purga <= CURRENT_TIMESTAMP;

-- 2. Ver la fecha de eliminación y vencimiento sin exponer nombres/correos.
SELECT id_registro, id_usuario_original, fecha_eliminacion, fecha_purga
FROM cuentas_eliminadas
ORDER BY fecha_purga ASC;

-- 3. Crear un registro de prueba para comprobar la tarea programada.
-- Ejecutar SOLO cuando se vaya a probar la purga; la fila se borrará en la siguiente ejecución.
-- No uses datos reales. El siguiente INSERT no modifica usuarios, pedidos ni recetas.
--
-- INSERT INTO cuentas_eliminadas
--     (id_usuario_original, nombre_usuario, nombre_completo, correo,
--      fecha_eliminacion, fecha_purga, motivo)
-- VALUES
--     (-999999, 'PRUEBA_DEPURACION', 'REGISTRO DE PRUEBA', NULL,
--      CURRENT_TIMESTAMP - INTERVAL '3 years', CURRENT_TIMESTAMP - INTERVAL '1 day', 'Prueba técnica');
--
-- SELECT id_registro, id_usuario_original, fecha_purga
-- FROM cuentas_eliminadas WHERE motivo = 'Prueba técnica';
--
-- Tras la siguiente ejecución programada, confirma que la fila de prueba ya no existe:
-- SELECT COUNT(*) FROM cuentas_eliminadas WHERE motivo = 'Prueba técnica';
