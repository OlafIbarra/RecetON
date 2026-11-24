-- Consulta de solo lectura. No modifica la base de datos.
-- Debe ejecutarse en form_recetas para comprobar la estructura real de public.solicitudes_arco.
SELECT column_name, data_type, is_nullable, character_maximum_length
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'solicitudes_arco'
ORDER BY ordinal_position;
