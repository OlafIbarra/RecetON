-- Form Recetas: configuración inicial de roles sin cambiar el esquema.
-- Ejecutar en pgAdmin con la base form_recetas seleccionada.
-- Crea los roles Administrador y Usuario si no existen, asigna Administrador
-- a la cuenta cuyo nombre de usuario sea 'admin' y Usuario a cuentas que aún
-- no tengan ninguna asignación en usuario_rol. No elimina ni reemplaza roles ya asignados.

DO $$
DECLARE
    id_rol_administrador INTEGER;
    id_rol_usuario INTEGER;
    siguiente_id INTEGER;
BEGIN
    SELECT id_rol
      INTO id_rol_administrador
      FROM roles
     WHERE LOWER(BTRIM(nombre_rol)) IN ('administrador', 'admin', 'administrator')
     ORDER BY CASE WHEN LOWER(BTRIM(nombre_rol)) = 'administrador' THEN 0 ELSE 1 END, id_rol
     LIMIT 1;

    IF id_rol_administrador IS NULL THEN
        SELECT COALESCE(MAX(id_rol), 0) + 1 INTO siguiente_id FROM roles;
        INSERT INTO roles (id_rol, nombre_rol)
        VALUES (siguiente_id, 'Administrador')
        RETURNING id_rol INTO id_rol_administrador;
    END IF;

    SELECT id_rol
      INTO id_rol_usuario
      FROM roles
     WHERE LOWER(BTRIM(nombre_rol)) IN ('usuario', 'user', 'cliente', 'usuario comun', 'usuario común')
     ORDER BY CASE WHEN LOWER(BTRIM(nombre_rol)) = 'usuario' THEN 0 ELSE 1 END, id_rol
     LIMIT 1;

    IF id_rol_usuario IS NULL THEN
        SELECT COALESCE(MAX(id_rol), 0) + 1 INTO siguiente_id FROM roles;
        INSERT INTO roles (id_rol, nombre_rol)
        VALUES (siguiente_id, 'Usuario')
        RETURNING id_rol INTO id_rol_usuario;
    END IF;

    -- Asigna el rol administrativo a la cuenta de administración ya creada.
    INSERT INTO usuario_rol (id_usuario, id_rol)
    SELECT id_usuario, id_rol_administrador
      FROM usuarios
     WHERE LOWER(BTRIM(nombre_usuario)) = 'admin'
    ON CONFLICT DO NOTHING;

    -- Cada cuenta sin rol recibe el rol común. Se respetan asignaciones previas.
    INSERT INTO usuario_rol (id_usuario, id_rol)
    SELECT u.id_usuario, id_rol_usuario
      FROM usuarios u
     WHERE NOT EXISTS (
         SELECT 1 FROM usuario_rol ur WHERE ur.id_usuario = u.id_usuario
     )
    ON CONFLICT DO NOTHING;

    IF NOT EXISTS (
        SELECT 1 FROM usuarios WHERE LOWER(BTRIM(nombre_usuario)) = 'admin'
    ) THEN
        RAISE NOTICE 'No se encontró un usuario llamado admin. Si tu administrador tiene otro nombre, modifica el filtro del script y ejecútalo de nuevo.';
    END IF;
END $$;

-- Comprobación: muestra cada usuario y sus roles asignados.
SELECT u.id_usuario, u.nombre_usuario, STRING_AGG(r.nombre_rol, ', ' ORDER BY r.id_rol) AS roles
  FROM usuarios u
  LEFT JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario
  LEFT JOIN roles r ON r.id_rol = ur.id_rol
 GROUP BY u.id_usuario, u.nombre_usuario
 ORDER BY u.id_usuario;
