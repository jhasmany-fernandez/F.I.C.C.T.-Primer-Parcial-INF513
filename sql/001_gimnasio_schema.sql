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
    id_cliente INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    id_membresia INTEGER NOT NULL REFERENCES membresia(id_membresia) ON DELETE RESTRICT,
    fecha_inicio DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_pago TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_fechas_suscripcion CHECK (fecha_fin >= fecha_inicio)
);

CREATE TABLE IF NOT EXISTS paquete (
    id_paquete SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    precio NUMERIC(10, 2) NOT NULL CHECK (precio >= 0),
    num_sesiones INTEGER NOT NULL CHECK (num_sesiones > 0)
);

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
    descripcion TEXT
);

CREATE TABLE IF NOT EXISTS asignacion_rutina (
    id_asignacion SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    id_rutina INTEGER NOT NULL REFERENCES rutina(id_rutina) ON DELETE RESTRICT,
    fecha_asignacion DATE NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT uk_cliente_rutina_fecha UNIQUE (id_cliente, id_rutina, fecha_asignacion)
);

CREATE TABLE IF NOT EXISTS seguimiento (
    id_seguimiento SERIAL PRIMARY KEY,
    id_cliente INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    fecha DATE NOT NULL,
    peso NUMERIC(5, 2) CHECK (peso > 0),
    medidas TEXT,
    observaciones TEXT,
    CONSTRAINT uk_seguimiento_cliente_fecha UNIQUE (id_cliente, fecha)
);

CREATE TABLE IF NOT EXISTS pagos (
    id_pago SERIAL PRIMARY KEY,
    id_suscripcion INTEGER NOT NULL REFERENCES suscripcion(id_suscripcion) ON DELETE RESTRICT,
    id_cliente INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    monto NUMERIC(10, 2) NOT NULL CHECK (monto >= 0),
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_proximo_pago DATE
);

CREATE TABLE IF NOT EXISTS help (
    id SERIAL PRIMARY KEY,
    cu VARCHAR(100) NOT NULL,
    accion VARCHAR(100) NOT NULL,
    parametros VARCHAR(200) NOT NULL,
    ejemplo TEXT
);

INSERT INTO rol (nombre_rol) VALUES
    ('Propietario'),
    ('Secretaria'),
    ('Instructor'),
    ('Cliente')
ON CONFLICT (nombre_rol) DO NOTHING;

INSERT INTO help (cu, accion, parametros, ejemplo) VALUES
    ('Usuarios', 'Registrar Usuario', '[nombre; email; contrasena; nombre_rol]', 'usuario agregar [Juan Perez; juan@mail.com; secreto123; Cliente]'),
    ('Usuarios', 'Editar Usuario', '[id_usuario; nombre; email; contrasena; nombre_rol]', 'usuario modificar [5; Ana Lopez; ana@mail.com; nuevoPass; Instructor]'),
    ('Usuarios', 'Eliminar Usuario', '[id_usuario]', 'usuario eliminar [5]'),
    ('Usuarios', 'Ver Usuario', '[id_usuario]', 'usuario ver [10]'),
    ('Usuarios', 'Mostrar Usuarios', 'sin parametros', 'usuario mostrar'),
    ('Usuarios', 'Ayuda Comandos', 'sin parametros', 'usuario ayuda')
ON CONFLICT DO NOTHING;
