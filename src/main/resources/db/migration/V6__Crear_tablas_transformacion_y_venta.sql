-- V6__Crear_tablas_transformacion_y_venta.sql
-- Módulo 3 (parte 2): transformación de productos y salida por venta.

-- Ampliar el check de tipo de movimiento para incluir TRANSFORMACION y SALIDA_VENTA.
ALTER TABLE movimiento_stock DROP CONSTRAINT chk_mov_tipo;
ALTER TABLE movimiento_stock ADD CONSTRAINT chk_mov_tipo
    CHECK (tipo IN ('ENTRADA', 'TRASLADO', 'AJUSTE', 'TRANSFORMACION', 'SALIDA_VENTA'));

-- Cabecera de transformación
CREATE TABLE transformacion (
    id BIGSERIAL PRIMARY KEY,
    producto_entrada_id BIGINT NOT NULL,
    estanteria_origen_id BIGINT NOT NULL,
    cantidad_consumida NUMERIC(14, 3) NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transf_producto_entrada FOREIGN KEY (producto_entrada_id) REFERENCES producto(id),
    CONSTRAINT fk_transf_estanteria_origen FOREIGN KEY (estanteria_origen_id) REFERENCES estanteria(id),
    CONSTRAINT fk_transf_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_transf_cantidad_consumida CHECK (cantidad_consumida > 0)
);

CREATE INDEX idx_transf_producto_entrada_id ON transformacion(producto_entrada_id);
CREATE INDEX idx_transf_usuario_id ON transformacion(usuario_id);
CREATE INDEX idx_transf_fecha ON transformacion(fecha);

-- Detalle de transformación (productos de venta generados)
CREATE TABLE transformacion_detalle (
    id BIGSERIAL PRIMARY KEY,
    transformacion_id BIGINT NOT NULL,
    producto_venta_id BIGINT NOT NULL,
    estanteria_destino_id BIGINT NOT NULL,
    cantidad_generada NUMERIC(14, 3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfd_transformacion FOREIGN KEY (transformacion_id) REFERENCES transformacion(id),
    CONSTRAINT fk_transfd_producto_venta FOREIGN KEY (producto_venta_id) REFERENCES producto(id),
    CONSTRAINT fk_transfd_estanteria_destino FOREIGN KEY (estanteria_destino_id) REFERENCES estanteria(id),
    CONSTRAINT chk_transfd_cantidad_generada CHECK (cantidad_generada > 0)
);

CREATE INDEX idx_transfd_transformacion_id ON transformacion_detalle(transformacion_id);
CREATE INDEX idx_transfd_producto_venta_id ON transformacion_detalle(producto_venta_id);

-- Cabecera de venta
CREATE TABLE venta (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total NUMERIC(14, 2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_venta_estado CHECK (estado IN ('COMPLETADA', 'ANULADA')),
    CONSTRAINT chk_venta_total CHECK (total >= 0)
);

CREATE INDEX idx_venta_usuario_id ON venta(usuario_id);
CREATE INDEX idx_venta_fecha ON venta(fecha);
CREATE INDEX idx_venta_estado ON venta(estado);

-- Detalle de venta
CREATE TABLE venta_detalle (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    estanteria_id BIGINT NOT NULL,
    cantidad NUMERIC(14, 3) NOT NULL,
    precio_unitario NUMERIC(14, 2) NOT NULL,
    subtotal NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ventad_venta FOREIGN KEY (venta_id) REFERENCES venta(id),
    CONSTRAINT fk_ventad_producto FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT fk_ventad_estanteria FOREIGN KEY (estanteria_id) REFERENCES estanteria(id),
    CONSTRAINT chk_ventad_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_ventad_precio_unitario CHECK (precio_unitario >= 0),
    CONSTRAINT chk_ventad_subtotal CHECK (subtotal >= 0)
);

CREATE INDEX idx_ventad_venta_id ON venta_detalle(venta_id);
CREATE INDEX idx_ventad_producto_id ON venta_detalle(producto_id);
