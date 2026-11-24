-- RecetON: registro de solicitudes ARCO.
-- Ejecutar una sola vez en la base form_recetas. No modifica las 24 tablas existentes.
-- Esta tabla es independiente del archivo de cuentas eliminadas y no impone una retención automática de dos años.

CREATE TABLE IF NOT EXISTS solicitudes_arco (
    id_solicitud BIGSERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE RESTRICT,
    derecho VARCHAR(20) NOT NULL CHECK (derecho IN ('Acceso', 'Rectificación', 'Cancelación', 'Oposición')),
    descripcion TEXT NOT NULL,
    fecha_solicitud TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(40) NOT NULL DEFAULT 'Recibida'
        CHECK (estado IN ('Recibida', 'En revisión', 'Pendiente de información', 'Atendida', 'No procedente')),
    respuesta TEXT,
    fecha_resolucion TIMESTAMP,
    id_responsable INTEGER REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_solicitudes_arco_usuario_fecha
    ON solicitudes_arco (id_usuario, fecha_solicitud DESC);

CREATE INDEX IF NOT EXISTS idx_solicitudes_arco_estado_fecha
    ON solicitudes_arco (estado, fecha_solicitud DESC);

COMMENT ON TABLE solicitudes_arco IS
'Registro de solicitudes ARCO de RecetON. El registro no equivale a la resolución de la petición. La política de conservación debe documentarse por separado.';
