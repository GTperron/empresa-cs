-- V5__Crear_tablas_productos_y_stock.sql
-- Módulo 3 (parte 1): productos, stock y movimientos de stock.

-- Tabla de productos
CREATE TABLE producto (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    tipo VARCHAR(20) NOT NULL,
    unidad_medida VARCHAR(20) NOT NULL,
    precio_venta NUMERIC(14, 2),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_producto_tipo CHECK (tipo IN ('ENTRADA', 'VENTA')),
    CONSTRAINT chk_producto_precio_venta CHECK (precio_venta IS NULL OR precio_venta >= 0)
);

CREATE INDEX idx_producto_codigo ON producto(codigo);
CREATE INDEX idx_producto_tipo ON producto(tipo);
CREATE INDEX idx_producto_activo ON producto(activo);

-- Tabla de stock: cantidad de un producto en una estantería concreta.
CREATE TABLE stock (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    estanteria_id BIGINT NOT NULL,
    cantidad NUMERIC(14, 3) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_producto FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT fk_stock_estanteria FOREIGN KEY (estanteria_id) REFERENCES estanteria(id),
    -- Una sola fila por producto+estantería (si existe, se actualiza la cantidad).
    CONSTRAINT uq_stock_producto_estanteria UNIQUE (producto_id, estanteria_id),
    -- Red de seguridad a nivel BD: el stock nunca puede quedar negativo.
    CONSTRAINT chk_stock_cantidad_no_negativa CHECK (cantidad >= 0)
);

CREATE INDEX idx_stock_producto_id ON stock(producto_id);
CREATE INDEX idx_stock_estanteria_id ON stock(estanteria_id);

-- Tabla de movimientos de stock (histórico inmutable).
CREATE TABLE movimiento_stock (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    estanteria_id BIGINT NOT NULL,
    estanteria_destino_id BIGINT,
    tipo VARCHAR(20) NOT NULL,
    cantidad NUMERIC(14, 3) NOT NULL,
    usuario_id BIGINT NOT NULL,
    motivo VARCHAR(500),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_producto FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT fk_mov_estanteria FOREIGN KEY (estanteria_id) REFERENCES estanteria(id),
    CONSTRAINT fk_mov_estanteria_destino FOREIGN KEY (estanteria_destino_id) REFERENCES estanteria(id),
    CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_mov_tipo CHECK (tipo IN ('ENTRADA', 'TRASLADO', 'AJUSTE')),
    -- El destino solo existe (y debe diferir del origen) en un TRASLADO.
    CONSTRAINT chk_mov_traslado_destino CHECK (
        (tipo = 'TRASLADO' AND estanteria_destino_id IS NOT NULL AND estanteria_destino_id <> estanteria_id)
        OR (tipo <> 'TRASLADO' AND estanteria_destino_id IS NULL)
    )
);

CREATE INDEX idx_mov_producto_id ON movimiento_stock(producto_id);
CREATE INDEX idx_mov_estanteria_id ON movimiento_stock(estanteria_id);
CREATE INDEX idx_mov_tipo ON movimiento_stock(tipo);
CREATE INDEX idx_mov_fecha ON movimiento_stock(fecha);
CREATE INDEX idx_mov_usuario_id ON movimiento_stock(usuario_id);
