CREATE TABLE IF NOT EXISTS roles (
    nombre      VARCHAR(20)  PRIMARY KEY,
    descripcion VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS usuarios (
    id             BIGSERIAL    PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    email          VARCHAR(120) NOT NULL UNIQUE,
    password       VARCHAR(100) NOT NULL,
    estado         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    rol            VARCHAR(20)  NOT NULL DEFAULT 'LECTOR' REFERENCES roles (nombre),
    fecha_registro TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_usuarios_estado CHECK (estado IN ('ACTIVO', 'SANCIONADO'))
);

CREATE TABLE IF NOT EXISTS libros (
    id               BIGSERIAL    PRIMARY KEY,
    isbn             VARCHAR(20)  NOT NULL UNIQUE,
    titulo           VARCHAR(200) NOT NULL,
    autor            VARCHAR(150) NOT NULL,
    categoria        VARCHAR(80)  NOT NULL,
    stock_total      INTEGER      NOT NULL,
    stock_disponible INTEGER      NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_libros_stock CHECK (stock_total >= 0 AND stock_disponible >= 0 AND stock_disponible <= stock_total)
);

CREATE TABLE IF NOT EXISTS prestamos (
    id                        BIGSERIAL   PRIMARY KEY,
    usuario_id                BIGINT      NOT NULL REFERENCES usuarios (id),
    libro_id                  BIGINT      NOT NULL REFERENCES libros (id),
    fecha_prestamo            DATE        NOT NULL,
    fecha_devolucion_esperada DATE        NOT NULL,
    fecha_devolucion_real     DATE,
    estado                    VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT ck_prestamos_estado CHECK (estado IN ('ACTIVO', 'DEVUELTO', 'ATRASADO'))
);

CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON usuarios (rol);
CREATE INDEX IF NOT EXISTS idx_usuarios_estado ON usuarios (estado);
CREATE INDEX IF NOT EXISTS idx_libros_categoria ON libros (categoria);
CREATE INDEX IF NOT EXISTS idx_libros_activo ON libros (activo);
CREATE INDEX IF NOT EXISTS idx_prestamos_usuario_estado ON prestamos (usuario_id, estado);
CREATE INDEX IF NOT EXISTS idx_prestamos_libro ON prestamos (libro_id);
CREATE INDEX IF NOT EXISTS idx_prestamos_estado_fecha ON prestamos (estado, fecha_devolucion_esperada);