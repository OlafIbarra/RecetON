-- RecetON: registro mínimo de bajas con retención académica de 2 años.
-- Ejecutar una sola vez en form_recetas. No modifica las 24 tablas existentes.
CREATE TABLE IF NOT EXISTS cuentas_eliminadas (
    id_registro BIGSERIAL PRIMARY KEY,
    id_usuario_original INTEGER NOT NULL,
    nombre_usuario VARCHAR(255),
    nombre_completo VARCHAR(255),
    correo VARCHAR(255),
    fecha_registro_original TIMESTAMP,
    fecha_eliminacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_purga TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '2 years'),
    motivo VARCHAR(80) NOT NULL DEFAULT 'Solicitud del titular'
);

CREATE INDEX IF NOT EXISTS idx_cuentas_eliminadas_fecha_purga
    ON cuentas_eliminadas (fecha_purga);

COMMENT ON TABLE cuentas_eliminadas IS
'Archivo de bajas de cuenta del proyecto RecetON. Conservación académica de dos años; revisar obligaciones aplicables antes de purgar.';
