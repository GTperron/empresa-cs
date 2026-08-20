-- V4__Crear_tablas_estructura_almacenamiento.sql
-- Módulo 2: estructura física de almacenamiento (Almacén -> Zona -> Estantería)

-- Tabla de almacenes
CREATE TABLE almacen (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    direccion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_almacen_codigo ON almacen(codigo);
CREATE INDEX idx_almacen_activo ON almacen(activo);

-- Tabla de zonas (pertenecen a un almacén)
CREATE TABLE zona (
    id BIGSERIAL PRIMARY KEY,
    almacen_id BIGINT NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_zona_almacen FOREIGN KEY (almacen_id) REFERENCES almacen(id),
    -- El código de zona es único DENTRO del mismo almacén, no globalmente
    CONSTRAINT uq_zona_codigo_por_almacen UNIQUE (almacen_id, codigo)
);

CREATE INDEX idx_zona_almacen_id ON zona(almacen_id);
CREATE INDEX idx_zona_activo ON zona(activo);

-- Tabla de estanterías (pertenecen a una zona)
CREATE TABLE estanteria (
    id BIGSERIAL PRIMARY KEY,
    zona_id BIGINT NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    capacidad_maxima INTEGER,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_estanteria_zona FOREIGN KEY (zona_id) REFERENCES zona(id),
    -- El código de estantería es único DENTRO de la misma zona
    CONSTRAINT uq_estanteria_codigo_por_zona UNIQUE (zona_id, codigo)
);

CREATE INDEX idx_estanteria_zona_id ON estanteria(zona_id);
CREATE INDEX idx_estanteria_activo ON estanteria(activo);
