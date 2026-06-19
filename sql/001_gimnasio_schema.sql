CREATE TABLE IF NOT EXISTS rol (
    id_rol SERIAL PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario SERIAL PRIMARY KEY,
    id_rol INTEGER NOT NULL REFERENCES rol(id_rol) ON DELETE RESTRICT,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT chk_usuario_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_usuario_estado'
    ) THEN
        ALTER TABLE usuario
            ADD CONSTRAINT chk_usuario_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'));
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS membresia (
    id_membresia SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    precio NUMERIC(10, 2) NOT NULL CHECK (precio >= 0),
    duracion_dias INTEGER NOT NULL CHECK (duracion_dias > 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT chk_membresia_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

ALTER TABLE membresia
    ADD COLUMN IF NOT EXISTS descripcion TEXT;

ALTER TABLE membresia
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_membresia_estado'
    ) THEN
        ALTER TABLE membresia
            ADD CONSTRAINT chk_membresia_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'));
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS suscripcion (
    id_suscripcion SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    tipo VARCHAR(20) NOT NULL,
    id_referencia INTEGER NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    usuario_creacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion TIMESTAMP,
    CONSTRAINT chk_suscripcion_tipo CHECK (tipo IN ('MEMBRESIA', 'PAQUETE')),
    CONSTRAINT chk_suscripcion_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'VENCIDO')),
    CONSTRAINT chk_fechas_suscripcion CHECK (fecha_fin >= fecha_inicio)
);

ALTER TABLE suscripcion
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(20);

ALTER TABLE suscripcion
    ADD COLUMN IF NOT EXISTS id_referencia INTEGER;

UPDATE suscripcion
SET tipo = 'MEMBRESIA'
WHERE tipo IS NULL
  AND EXISTS (
      SELECT 1
      FROM information_schema.columns
      WHERE table_name = 'suscripcion'
        AND column_name = 'id_membresia'
  );

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'suscripcion'
          AND column_name = 'id_membresia'
    ) THEN
        EXECUTE 'UPDATE suscripcion SET id_referencia = id_membresia WHERE id_referencia IS NULL';
    END IF;
END $$;

UPDATE suscripcion
SET tipo = 'MEMBRESIA'
WHERE tipo IS NULL;

UPDATE suscripcion
SET id_referencia = 1
WHERE id_referencia IS NULL;

ALTER TABLE suscripcion
    ALTER COLUMN tipo SET NOT NULL;

ALTER TABLE suscripcion
    ALTER COLUMN id_referencia SET NOT NULL;

ALTER TABLE suscripcion
    ALTER COLUMN fecha_inicio DROP DEFAULT;

ALTER TABLE suscripcion
    ADD COLUMN IF NOT EXISTS fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE;

ALTER TABLE suscripcion
    ADD COLUMN IF NOT EXISTS usuario_creacion INTEGER;

ALTER TABLE suscripcion
    ADD COLUMN IF NOT EXISTS usuario_modificacion INTEGER;

ALTER TABLE suscripcion
    ADD COLUMN IF NOT EXISTS fecha_modificacion TIMESTAMP;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'suscripcion'
          AND column_name = 'fecha_pago'
    ) THEN
        EXECUTE 'UPDATE suscripcion SET fecha_registro = fecha_pago::date WHERE fecha_registro IS NULL';
    END IF;
END $$;

UPDATE suscripcion
SET estado = 'ACTIVO'
WHERE estado IS NULL
   OR estado NOT IN ('ACTIVO', 'INACTIVO', 'VENCIDO');

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'suscripcion'
          AND column_name = 'id_membresia'
    ) THEN
        EXECUTE 'ALTER TABLE suscripcion ALTER COLUMN id_membresia DROP NOT NULL';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'suscripcion'
          AND column_name = 'fecha_pago'
    ) THEN
        EXECUTE 'ALTER TABLE suscripcion ALTER COLUMN fecha_pago DROP NOT NULL';
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_suscripcion_tipo'
    ) THEN
        ALTER TABLE suscripcion
            ADD CONSTRAINT chk_suscripcion_tipo CHECK (tipo IN ('MEMBRESIA', 'PAQUETE'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_suscripcion_estado'
    ) THEN
        ALTER TABLE suscripcion
            ADD CONSTRAINT chk_suscripcion_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'VENCIDO'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_suscripcion_usuario_creacion'
    ) THEN
        ALTER TABLE suscripcion
            ADD CONSTRAINT fk_suscripcion_usuario_creacion
            FOREIGN KEY (usuario_creacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_suscripcion_usuario_modificacion'
    ) THEN
        ALTER TABLE suscripcion
            ADD CONSTRAINT fk_suscripcion_usuario_modificacion
            FOREIGN KEY (usuario_modificacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'suscripcion'
          AND column_name = 'id_membresia'
    ) THEN
        EXECUTE 'COMMENT ON COLUMN suscripcion.id_membresia IS '
            || quote_literal('Obsoleto desde CU4. Se mantiene temporalmente por compatibilidad; usar tipo + id_referencia.');
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'suscripcion'
          AND column_name = 'fecha_pago'
    ) THEN
        EXECUTE 'COMMENT ON COLUMN suscripcion.fecha_pago IS '
            || quote_literal('Obsoleto desde CU4. Se mantiene temporalmente por compatibilidad; usar fecha_registro y pago.');
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS paquete (
    id_paquete SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio NUMERIC(10, 2) NOT NULL CHECK (precio > 0),
    duracion_dias INTEGER NOT NULL CHECK (duracion_dias > 0),
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    usuario_creacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion TIMESTAMP,
    CONSTRAINT chk_paquete_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

ALTER TABLE paquete
    ADD COLUMN IF NOT EXISTS descripcion TEXT;

ALTER TABLE paquete
    ADD COLUMN IF NOT EXISTS duracion_dias INTEGER;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'paquete'
          AND column_name = 'num_sesiones'
    ) THEN
        UPDATE paquete
        SET duracion_dias = num_sesiones
        WHERE duracion_dias IS NULL;
    END IF;
END $$;

UPDATE paquete
SET duracion_dias = 1
WHERE duracion_dias IS NULL;

ALTER TABLE paquete
    ALTER COLUMN duracion_dias SET NOT NULL;

ALTER TABLE paquete
    ADD COLUMN IF NOT EXISTS fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE;

ALTER TABLE paquete
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';

ALTER TABLE paquete
    ADD COLUMN IF NOT EXISTS usuario_creacion INTEGER;

ALTER TABLE paquete
    ADD COLUMN IF NOT EXISTS usuario_modificacion INTEGER;

ALTER TABLE paquete
    ADD COLUMN IF NOT EXISTS fecha_modificacion TIMESTAMP;

DO $$
DECLARE
    column_type text;
BEGIN
    SELECT data_type
    INTO column_type
    FROM information_schema.columns
    WHERE table_name = 'paquete'
      AND column_name = 'usuario_creacion';

    IF column_type IS NOT NULL AND column_type <> 'integer' THEN
        ALTER TABLE paquete ADD COLUMN usuario_creacion_id_tmp INTEGER;
        UPDATE paquete p
        SET usuario_creacion_id_tmp = u.id_usuario
        FROM usuario u
        WHERE lower(p.usuario_creacion::text) = lower(u.email);
        ALTER TABLE paquete DROP COLUMN usuario_creacion;
        ALTER TABLE paquete RENAME COLUMN usuario_creacion_id_tmp TO usuario_creacion;
    END IF;
END $$;

DO $$
DECLARE
    column_type text;
BEGIN
    SELECT data_type
    INTO column_type
    FROM information_schema.columns
    WHERE table_name = 'paquete'
      AND column_name = 'usuario_modificacion';

    IF column_type IS NOT NULL AND column_type <> 'integer' THEN
        ALTER TABLE paquete ADD COLUMN usuario_modificacion_id_tmp INTEGER;
        UPDATE paquete p
        SET usuario_modificacion_id_tmp = u.id_usuario
        FROM usuario u
        WHERE lower(p.usuario_modificacion::text) = lower(u.email);
        ALTER TABLE paquete DROP COLUMN usuario_modificacion;
        ALTER TABLE paquete RENAME COLUMN usuario_modificacion_id_tmp TO usuario_modificacion;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'paquete'
          AND column_name = 'num_sesiones'
    ) THEN
        ALTER TABLE paquete
            ALTER COLUMN num_sesiones DROP NOT NULL;
    END IF;
END $$;

DO $$
DECLARE
    constraint_name text;
BEGIN
    FOR constraint_name IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'paquete'::regclass
          AND c.contype = 'u'
          AND a.attname = 'nombre'
    LOOP
        EXECUTE format('ALTER TABLE paquete DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_paquete_precio'
    ) THEN
        ALTER TABLE paquete
            ADD CONSTRAINT chk_paquete_precio CHECK (precio > 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_paquete_duracion_dias'
    ) THEN
        ALTER TABLE paquete
            ADD CONSTRAINT chk_paquete_duracion_dias CHECK (duracion_dias > 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_paquete_estado'
    ) THEN
        ALTER TABLE paquete
            ADD CONSTRAINT chk_paquete_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_paquete_usuario_creacion'
    ) THEN
        ALTER TABLE paquete
            ADD CONSTRAINT fk_paquete_usuario_creacion
            FOREIGN KEY (usuario_creacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_paquete_usuario_modificacion'
    ) THEN
        ALTER TABLE paquete
            ADD CONSTRAINT fk_paquete_usuario_modificacion
            FOREIGN KEY (usuario_modificacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS venta_paquete (
    id_venta SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    id_paquete INTEGER NOT NULL REFERENCES paquete(id_paquete) ON DELETE RESTRICT,
    sesiones_restantes INTEGER NOT NULL CHECK (sesiones_restantes >= 0),
    fecha_compra DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS rutina (
    id_rutina SERIAL PRIMARY KEY,
    id_instructor INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    objetivo TEXT,
    nivel VARCHAR(20),
    duracion_dias INTEGER,
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    usuario_creacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion TIMESTAMP
);

ALTER TABLE rutina
    ADD COLUMN IF NOT EXISTS objetivo TEXT;

ALTER TABLE rutina
    ADD COLUMN IF NOT EXISTS nivel VARCHAR(20);

ALTER TABLE rutina
    ADD COLUMN IF NOT EXISTS duracion_dias INTEGER;

ALTER TABLE rutina
    ADD COLUMN IF NOT EXISTS fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE;

ALTER TABLE rutina
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';

ALTER TABLE rutina
    ADD COLUMN IF NOT EXISTS usuario_creacion INTEGER;

ALTER TABLE rutina
    ADD COLUMN IF NOT EXISTS usuario_modificacion INTEGER;

ALTER TABLE rutina
    ADD COLUMN IF NOT EXISTS fecha_modificacion TIMESTAMP;

UPDATE rutina
SET objetivo = 'Sin objetivo registrado'
WHERE objetivo IS NULL;

UPDATE rutina
SET nivel = 'BASICO'
WHERE nivel IS NULL
   OR nivel NOT IN ('BASICO', 'INTERMEDIO', 'AVANZADO');

UPDATE rutina
SET duracion_dias = 1
WHERE duracion_dias IS NULL
   OR duracion_dias <= 0;

UPDATE rutina
SET estado = 'ACTIVO'
WHERE estado IS NULL
   OR estado NOT IN ('ACTIVO', 'INACTIVO', 'FINALIZADO');

ALTER TABLE rutina
    ALTER COLUMN objetivo SET NOT NULL;

ALTER TABLE rutina
    ALTER COLUMN nivel SET NOT NULL;

ALTER TABLE rutina
    ALTER COLUMN duracion_dias SET NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'rutina'
          AND column_name = 'id_instructor'
    ) THEN
        EXECUTE 'ALTER TABLE rutina ALTER COLUMN id_instructor DROP NOT NULL';
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_rutina_nivel'
    ) THEN
        ALTER TABLE rutina
            ADD CONSTRAINT chk_rutina_nivel CHECK (nivel IN ('BASICO', 'INTERMEDIO', 'AVANZADO'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_rutina_estado'
    ) THEN
        ALTER TABLE rutina
            ADD CONSTRAINT chk_rutina_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'FINALIZADO'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_rutina_duracion_dias'
    ) THEN
        ALTER TABLE rutina
            ADD CONSTRAINT chk_rutina_duracion_dias CHECK (duracion_dias > 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'rutina'::regclass
          AND c.contype = 'f'
          AND a.attname = 'usuario_creacion'
    ) THEN
        ALTER TABLE rutina
            ADD CONSTRAINT fk_rutina_usuario_creacion
            FOREIGN KEY (usuario_creacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'rutina'::regclass
          AND c.contype = 'f'
          AND a.attname = 'usuario_modificacion'
    ) THEN
        ALTER TABLE rutina
            ADD CONSTRAINT fk_rutina_usuario_modificacion
            FOREIGN KEY (usuario_modificacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DROP TABLE IF EXISTS asignacion_rutina;

CREATE TABLE IF NOT EXISTS rutina_cliente (
    id_rutina_cliente SERIAL PRIMARY KEY,
    id_rutina INTEGER NOT NULL REFERENCES rutina(id_rutina) ON DELETE RESTRICT,
    id_cliente INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    usuario_creacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion TIMESTAMP,
    CONSTRAINT chk_rutina_cliente_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'FINALIZADO')),
    CONSTRAINT chk_rutina_cliente_fechas CHECK (fecha_fin >= fecha_inicio)
);

ALTER TABLE rutina_cliente
    ADD COLUMN IF NOT EXISTS id_rutina INTEGER;

ALTER TABLE rutina_cliente
    ADD COLUMN IF NOT EXISTS id_cliente INTEGER;

ALTER TABLE rutina_cliente
    ADD COLUMN IF NOT EXISTS fecha_inicio DATE;

ALTER TABLE rutina_cliente
    ADD COLUMN IF NOT EXISTS fecha_fin DATE;

ALTER TABLE rutina_cliente
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';

ALTER TABLE rutina_cliente
    ADD COLUMN IF NOT EXISTS usuario_creacion INTEGER;

ALTER TABLE rutina_cliente
    ADD COLUMN IF NOT EXISTS usuario_modificacion INTEGER;

ALTER TABLE rutina_cliente
    ADD COLUMN IF NOT EXISTS fecha_modificacion TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'rutina_cliente'::regclass
          AND c.contype = 'f'
          AND a.attname = 'id_rutina'
    ) THEN
        ALTER TABLE rutina_cliente
            ADD CONSTRAINT fk_rutina_cliente_rutina
            FOREIGN KEY (id_rutina) REFERENCES rutina(id_rutina) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'rutina_cliente'::regclass
          AND c.contype = 'f'
          AND a.attname = 'id_cliente'
    ) THEN
        ALTER TABLE rutina_cliente
            ADD CONSTRAINT fk_rutina_cliente_usuario
            FOREIGN KEY (id_cliente) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'rutina_cliente'::regclass
          AND c.contype = 'f'
          AND a.attname = 'usuario_creacion'
    ) THEN
        ALTER TABLE rutina_cliente
            ADD CONSTRAINT fk_rutina_cliente_usuario_creacion
            FOREIGN KEY (usuario_creacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'rutina_cliente'::regclass
          AND c.contype = 'f'
          AND a.attname = 'usuario_modificacion'
    ) THEN
        ALTER TABLE rutina_cliente
            ADD CONSTRAINT fk_rutina_cliente_usuario_modificacion
            FOREIGN KEY (usuario_modificacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_rutina_cliente_estado'
    ) THEN
        ALTER TABLE rutina_cliente
            ADD CONSTRAINT chk_rutina_cliente_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'FINALIZADO'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_rutina_cliente_fechas'
    ) THEN
        ALTER TABLE rutina_cliente
            ADD CONSTRAINT chk_rutina_cliente_fechas CHECK (fecha_fin >= fecha_inicio);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS seguimiento (
    id_seguimiento SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    fecha DATE NOT NULL,
    peso NUMERIC(5, 2) CHECK (peso > 0),
    medidas TEXT,
    observaciones TEXT,
    CONSTRAINT uk_seguimiento_cliente_fecha UNIQUE (id_cliente, fecha)
);

ALTER TABLE seguimiento
    ADD COLUMN IF NOT EXISTS id_rutina_cliente INTEGER;

ALTER TABLE seguimiento
    ADD COLUMN IF NOT EXISTS fecha_seguimiento DATE;

ALTER TABLE seguimiento
    ADD COLUMN IF NOT EXISTS observacion TEXT;

ALTER TABLE seguimiento
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'EN_PROGRESO';

ALTER TABLE seguimiento
    ADD COLUMN IF NOT EXISTS estado_logico VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';

ALTER TABLE seguimiento
    ADD COLUMN IF NOT EXISTS usuario_creacion INTEGER;

ALTER TABLE seguimiento
    ADD COLUMN IF NOT EXISTS usuario_modificacion INTEGER;

ALTER TABLE seguimiento
    ADD COLUMN IF NOT EXISTS fecha_modificacion TIMESTAMP;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'seguimiento'
          AND column_name = 'fecha'
    ) THEN
        EXECUTE 'UPDATE seguimiento SET fecha_seguimiento = fecha WHERE fecha_seguimiento IS NULL';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'seguimiento'
          AND column_name = 'observaciones'
    ) THEN
        EXECUTE 'UPDATE seguimiento SET observacion = observaciones WHERE observacion IS NULL';
    END IF;
END $$;

UPDATE seguimiento
SET fecha_seguimiento = CURRENT_DATE
WHERE fecha_seguimiento IS NULL;

UPDATE seguimiento
SET observacion = 'Sin observacion registrada'
WHERE observacion IS NULL
   OR btrim(observacion) = '';

UPDATE seguimiento
SET estado = 'EN_PROGRESO'
WHERE estado IS NULL
   OR estado NOT IN ('EN_PROGRESO', 'COMPLETADO', 'PAUSADO');

UPDATE seguimiento
SET estado_logico = 'ACTIVO'
WHERE estado_logico IS NULL
   OR estado_logico NOT IN ('ACTIVO', 'INACTIVO');

ALTER TABLE seguimiento
    ALTER COLUMN fecha_seguimiento SET NOT NULL;

ALTER TABLE seguimiento
    ALTER COLUMN observacion SET NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'seguimiento'
          AND column_name = 'id_cliente'
    ) THEN
        EXECUTE 'ALTER TABLE seguimiento ALTER COLUMN id_cliente DROP NOT NULL';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'seguimiento'
          AND column_name = 'fecha'
    ) THEN
        EXECUTE 'ALTER TABLE seguimiento ALTER COLUMN fecha DROP NOT NULL';
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'seguimiento'::regclass
          AND c.contype = 'f'
          AND a.attname = 'id_rutina_cliente'
    ) THEN
        ALTER TABLE seguimiento
            ADD CONSTRAINT fk_seguimiento_rutina_cliente
            FOREIGN KEY (id_rutina_cliente) REFERENCES rutina_cliente(id_rutina_cliente) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'seguimiento'::regclass
          AND c.contype = 'f'
          AND a.attname = 'usuario_creacion'
    ) THEN
        ALTER TABLE seguimiento
            ADD CONSTRAINT fk_seguimiento_usuario_creacion
            FOREIGN KEY (usuario_creacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'seguimiento'::regclass
          AND c.contype = 'f'
          AND a.attname = 'usuario_modificacion'
    ) THEN
        ALTER TABLE seguimiento
            ADD CONSTRAINT fk_seguimiento_usuario_modificacion
            FOREIGN KEY (usuario_modificacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_seguimiento_estado'
    ) THEN
        ALTER TABLE seguimiento
            ADD CONSTRAINT chk_seguimiento_estado CHECK (estado IN ('EN_PROGRESO', 'COMPLETADO', 'PAUSADO'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_seguimiento_estado_logico'
    ) THEN
        ALTER TABLE seguimiento
            ADD CONSTRAINT chk_seguimiento_estado_logico CHECK (estado_logico IN ('ACTIVO', 'INACTIVO'));
    END IF;
END $$;

DROP TABLE IF EXISTS pagos;

CREATE TABLE IF NOT EXISTS pago (
    id_pago SERIAL PRIMARY KEY,
    id_suscripcion INTEGER NOT NULL REFERENCES suscripcion(id_suscripcion) ON DELETE RESTRICT,
    tipo_pago VARCHAR(20) NOT NULL,
    monto_total NUMERIC(10, 2) NOT NULL,
    cantidad_cuotas INTEGER NOT NULL DEFAULT 1,
    cuotas_pagadas INTEGER NOT NULL DEFAULT 0,
    metodo VARCHAR(20) NOT NULL,
    fecha_pago DATE,
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    usuario_creacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    usuario_modificacion INTEGER REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    fecha_modificacion TIMESTAMP,
    CONSTRAINT chk_pago_tipo CHECK (tipo_pago IN ('CONTADO', 'CREDITO')),
    CONSTRAINT chk_pago_metodo CHECK (metodo IN ('EFECTIVO', 'TRANSFERENCIA', 'QR')),
    CONSTRAINT chk_pago_estado CHECK (estado IN ('PENDIENTE', 'PAGADO', 'PARCIAL', 'INACTIVO')),
    CONSTRAINT chk_pago_monto_total CHECK (monto_total > 0),
    CONSTRAINT chk_pago_cantidad_cuotas CHECK (cantidad_cuotas >= 1),
    CONSTRAINT chk_pago_cuotas_pagadas CHECK (cuotas_pagadas >= 0 AND cuotas_pagadas <= cantidad_cuotas)
);

CREATE TABLE IF NOT EXISTS cuota (
    id_cuota SERIAL PRIMARY KEY,
    id_pago INTEGER NOT NULL REFERENCES pago(id_pago) ON DELETE RESTRICT,
    numero_cuota INTEGER NOT NULL,
    monto NUMERIC(10, 2) NOT NULL CHECK (monto > 0),
    fecha_pago DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT chk_cuota_estado CHECK (estado IN ('PENDIENTE', 'PAGADA')),
    CONSTRAINT chk_cuota_numero CHECK (numero_cuota > 0),
    CONSTRAINT uk_pago_numero_cuota UNIQUE (id_pago, numero_cuota)
);

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS id_suscripcion INTEGER;

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS tipo_pago VARCHAR(20);

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS monto_total NUMERIC(10, 2);

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS cantidad_cuotas INTEGER NOT NULL DEFAULT 1;

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS cuotas_pagadas INTEGER NOT NULL DEFAULT 0;

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS metodo VARCHAR(20);

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS fecha_pago DATE;

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE;

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE';

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS usuario_creacion INTEGER;

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS usuario_modificacion INTEGER;

ALTER TABLE pago
    ADD COLUMN IF NOT EXISTS fecha_modificacion TIMESTAMP;

ALTER TABLE cuota
    ADD COLUMN IF NOT EXISTS id_pago INTEGER;

ALTER TABLE cuota
    ADD COLUMN IF NOT EXISTS numero_cuota INTEGER;

ALTER TABLE cuota
    ADD COLUMN IF NOT EXISTS monto NUMERIC(10, 2);

ALTER TABLE cuota
    ADD COLUMN IF NOT EXISTS fecha_pago DATE;

ALTER TABLE cuota
    ADD COLUMN IF NOT EXISTS estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'pago'::regclass
          AND c.contype = 'f'
          AND a.attname = 'id_suscripcion'
    ) THEN
        ALTER TABLE pago
            ADD CONSTRAINT fk_pago_suscripcion
            FOREIGN KEY (id_suscripcion) REFERENCES suscripcion(id_suscripcion) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'pago'::regclass
          AND c.contype = 'f'
          AND a.attname = 'usuario_creacion'
    ) THEN
        ALTER TABLE pago
            ADD CONSTRAINT fk_pago_usuario_creacion
            FOREIGN KEY (usuario_creacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'pago'::regclass
          AND c.contype = 'f'
          AND a.attname = 'usuario_modificacion'
    ) THEN
        ALTER TABLE pago
            ADD CONSTRAINT fk_pago_usuario_modificacion
            FOREIGN KEY (usuario_modificacion) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a
          ON a.attrelid = c.conrelid
         AND a.attnum = ANY (c.conkey)
        WHERE c.conrelid = 'cuota'::regclass
          AND c.contype = 'f'
          AND a.attname = 'id_pago'
    ) THEN
        ALTER TABLE cuota
            ADD CONSTRAINT fk_cuota_pago
            FOREIGN KEY (id_pago) REFERENCES pago(id_pago) ON DELETE RESTRICT;
    END IF;
END $$;

INSERT INTO rol (nombre_rol) VALUES
    ('Propietario'),
    ('Secretaria'),
    ('Instructor'),
    ('Cliente')
ON CONFLICT DO NOTHING;
