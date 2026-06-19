-- ============================================================
-- ESQUEMA CORREGIDO - Gimnasio DB v3
-- Correcciones aplicadas:
--   1. FK polimórfico resuelto con sub-tablas (suscripcion_membresia, suscripcion_paquete)
--   2. venta_paquete eliminada (absorbida por suscripcion_paquete.sesiones_restantes)
--   3. seguimiento limpiado: columnas residuales removidas, UNIQUE corregido
--   4. rutina.id_instructor restaurado a NOT NULL
--   5. ON DELETE CASCADE en venta_paquete reemplazado por RESTRICT
--   6. Columnas QR extraídas de pago hacia pago_qr (cardinalidad 1:0..1)
--   7. UNIQUE añadido a rutina_cliente(id_rutina, id_cliente)
--   8. Auditoría añadida a membresia, cuota
-- ============================================================

-- ============================================================
-- SECCION 1: CATÁLOGOS Y ENTIDADES BASE
-- ============================================================

CREATE TABLE IF NOT EXISTS rol (
    id_rol   SERIAL PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario    SERIAL PRIMARY KEY,
    id_rol        INTEGER NOT NULL REFERENCES rol(id_rol) ON DELETE RESTRICT,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    contrasena    VARCHAR(255) NOT NULL,
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    estado        VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT chk_usuario_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- ============================================================
-- SECCION 2: PRODUCTOS (membresia, paquete)
-- ============================================================

CREATE TABLE IF NOT EXISTS membresia (
    id_membresia    SERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL UNIQUE,
    descripcion     TEXT,
    precio          NUMERIC(10, 2) NOT NULL CHECK (precio >= 0),
    duracion_dias   INTEGER NOT NULL CHECK (duracion_dias > 0),
    estado          VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    -- Auditoría añadida (corrección #8)
    usuario_creacion     INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion   TIMESTAMP,
    CONSTRAINT chk_membresia_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE IF NOT EXISTS paquete (
    id_paquete      SERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    descripcion     TEXT,
    precio          NUMERIC(10, 2) NOT NULL CHECK (precio > 0),
    duracion_dias   INTEGER NOT NULL CHECK (duracion_dias > 0),
    fecha_registro  DATE NOT NULL DEFAULT CURRENT_DATE,
    estado          VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    usuario_creacion     INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion   TIMESTAMP,
    CONSTRAINT chk_paquete_estado  CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT chk_paquete_precio  CHECK (precio > 0),
    CONSTRAINT chk_paquete_duracion CHECK (duracion_dias > 0)
);

-- ============================================================
-- SECCION 3: SUSCRIPCION + SUB-TABLAS (corrección #1)
-- Reemplaza el patrón polimórfico tipo + id_referencia
-- ============================================================

CREATE TABLE IF NOT EXISTS suscripcion (
    id_suscripcion  SERIAL PRIMARY KEY,
    id_cliente      INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_inicio    DATE NOT NULL,
    fecha_fin       DATE NOT NULL,
    fecha_registro  DATE NOT NULL DEFAULT CURRENT_DATE,
    estado          VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    usuario_creacion     INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion   TIMESTAMP,
    CONSTRAINT chk_suscripcion_estado  CHECK (estado IN ('ACTIVO', 'INACTIVO', 'VENCIDO')),
    CONSTRAINT chk_fechas_suscripcion  CHECK (fecha_fin >= fecha_inicio)
);

-- Sub-tabla especialización: suscripción a membresía (cardinalidad 1:0..1)
CREATE TABLE IF NOT EXISTS suscripcion_membresia (
    id_suscripcion INTEGER PRIMARY KEY
        REFERENCES suscripcion(id_suscripcion) ON DELETE RESTRICT,
    id_membresia   INTEGER NOT NULL
        REFERENCES membresia(id_membresia) ON DELETE RESTRICT
);

-- Sub-tabla especialización: suscripción a paquete (cardinalidad 1:0..1)
-- Absorbe venta_paquete.sesiones_restantes (corrección #2)
CREATE TABLE IF NOT EXISTS suscripcion_paquete (
    id_suscripcion      INTEGER PRIMARY KEY
        REFERENCES suscripcion(id_suscripcion) ON DELETE RESTRICT,
    id_paquete          INTEGER NOT NULL
        REFERENCES paquete(id_paquete) ON DELETE RESTRICT,
    sesiones_restantes  INTEGER NOT NULL DEFAULT 0
        CHECK (sesiones_restantes >= 0)
);

-- venta_paquete fue eliminada (corrección #2): sus datos se migran a suscripcion_paquete

-- ============================================================
-- SECCION 4: RUTINAS Y ASIGNACIONES
-- ============================================================

-- id_instructor restaurado a NOT NULL (corrección #4)
CREATE TABLE IF NOT EXISTS rutina (
    id_rutina     SERIAL PRIMARY KEY,
    id_instructor INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    nombre        VARCHAR(100) NOT NULL,
    descripcion   TEXT,
    objetivo      TEXT NOT NULL,
    nivel         VARCHAR(20) NOT NULL,
    duracion_dias INTEGER NOT NULL CHECK (duracion_dias > 0),
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    estado        VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    usuario_creacion     INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion   TIMESTAMP,
    CONSTRAINT chk_rutina_nivel    CHECK (nivel IN ('BASICO', 'INTERMEDIO', 'AVANZADO')),
    CONSTRAINT chk_rutina_estado   CHECK (estado IN ('ACTIVO', 'INACTIVO', 'FINALIZADO')),
    CONSTRAINT chk_rutina_duracion CHECK (duracion_dias > 0)
);

-- UNIQUE añadido en (id_rutina, id_cliente) para evitar asignaciones duplicadas (corrección #7)
CREATE TABLE IF NOT EXISTS rutina_cliente (
    id_rutina_cliente SERIAL PRIMARY KEY,
    id_rutina         INTEGER NOT NULL REFERENCES rutina(id_rutina) ON DELETE RESTRICT,
    id_cliente        INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_inicio      DATE NOT NULL,
    fecha_fin         DATE NOT NULL,
    estado            VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    usuario_creacion     INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion   TIMESTAMP,
    CONSTRAINT chk_rutina_cliente_estado  CHECK (estado IN ('ACTIVO', 'INACTIVO', 'FINALIZADO')),
    CONSTRAINT chk_rutina_cliente_fechas  CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT uk_rutina_cliente_activa   UNIQUE (id_rutina, id_cliente)
);

-- ============================================================
-- SECCION 5: SEGUIMIENTO
-- Columnas residuales removidas: id_cliente, fecha, observaciones
-- UNIQUE corregido sobre columnas NOT NULL (corrección #3)
-- ============================================================

CREATE TABLE IF NOT EXISTS seguimiento (
    id_seguimiento    SERIAL PRIMARY KEY,
    id_rutina_cliente INTEGER NOT NULL
        REFERENCES rutina_cliente(id_rutina_cliente) ON DELETE RESTRICT,
    fecha_seguimiento DATE NOT NULL,
    peso              NUMERIC(5, 2) CHECK (peso > 0),
    medidas           TEXT,
    observacion       TEXT NOT NULL,
    estado            VARCHAR(20) NOT NULL DEFAULT 'EN_PROGRESO',
    estado_logico     VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    usuario_creacion     INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion   TIMESTAMP,
    -- UNIQUE corregido: apunta a columnas NOT NULL (corrección #3)
    CONSTRAINT uk_seguimiento_rutina_fecha    UNIQUE (id_rutina_cliente, fecha_seguimiento),
    CONSTRAINT chk_seguimiento_estado        CHECK (estado IN ('EN_PROGRESO', 'COMPLETADO', 'PAUSADO')),
    CONSTRAINT chk_seguimiento_estado_logico CHECK (estado_logico IN ('ACTIVO', 'INACTIVO'))
);

-- ============================================================
-- SECCION 6: PAGOS + SUB-TABLA QR
-- ============================================================

-- pago sin columnas QR (corrección #6)
CREATE TABLE IF NOT EXISTS pago (
    id_pago         SERIAL PRIMARY KEY,
    id_suscripcion  INTEGER NOT NULL REFERENCES suscripcion(id_suscripcion) ON DELETE RESTRICT,
    tipo_pago       VARCHAR(20) NOT NULL,
    monto_total     NUMERIC(10, 2) NOT NULL CHECK (monto_total > 0),
    cantidad_cuotas INTEGER NOT NULL DEFAULT 1 CHECK (cantidad_cuotas >= 1),
    cuotas_pagadas  INTEGER NOT NULL DEFAULT 0,
    metodo          VARCHAR(20) NOT NULL,
    fecha_pago      DATE,
    fecha_registro  DATE NOT NULL DEFAULT CURRENT_DATE,
    estado          VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    usuario_creacion     INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion   TIMESTAMP,
    CONSTRAINT chk_pago_tipo    CHECK (tipo_pago IN ('CONTADO', 'CREDITO')),
    CONSTRAINT chk_pago_metodo  CHECK (metodo IN ('EFECTIVO', 'TRANSFERENCIA', 'QR')),
    CONSTRAINT chk_pago_estado  CHECK (estado IN ('PENDIENTE', 'PAGADO', 'PARCIAL', 'INACTIVO')),
    CONSTRAINT chk_pago_cuotas  CHECK (cuotas_pagadas >= 0 AND cuotas_pagadas <= cantidad_cuotas)
);

-- Sub-tabla especialización: datos QR opcionales (cardinalidad 1:0..1) (corrección #6)
CREATE TABLE IF NOT EXISTS pago_qr (
    id_pago                  INTEGER PRIMARY KEY
        REFERENCES pago(id_pago) ON DELETE RESTRICT,
    payment_number           VARCHAR(120),
    pagofacil_transaction_id BIGINT,
    qr_contenido             TEXT,
    qr_imagen_base64         TEXT,
    callback_url             TEXT,
    estado_externo           VARCHAR(50),
    respuesta_externa        TEXT
);

-- cuota con auditoría añadida (corrección #8)
CREATE TABLE IF NOT EXISTS cuota (
    id_cuota     SERIAL PRIMARY KEY,
    id_pago      INTEGER NOT NULL REFERENCES pago(id_pago) ON DELETE RESTRICT,
    numero_cuota INTEGER NOT NULL CHECK (numero_cuota > 0),
    monto        NUMERIC(10, 2) NOT NULL CHECK (monto > 0),
    fecha_pago   DATE,
    estado       VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    usuario_creacion     INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion   TIMESTAMP,
    CONSTRAINT chk_cuota_estado  CHECK (estado IN ('PENDIENTE', 'PAGADA')),
    CONSTRAINT uk_pago_cuota     UNIQUE (id_pago, numero_cuota)
);

-- ============================================================
-- SECCION 7: DATOS INICIALES
-- ============================================================

INSERT INTO rol (nombre_rol) VALUES
    ('Propietario'),
    ('Secretaria'),
    ('Instructor'),
    ('Cliente')
ON CONFLICT DO NOTHING;
