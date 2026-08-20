-- V2__Crear_tabla_refresh_token.sql
-- Crear tabla para almacenar refresh tokens

CREATE TABLE refresh_token (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    token TEXT NOT NULL UNIQUE,
    expiracion TIMESTAMP NOT NULL,
    revocado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Crear índices para optimizar búsquedas
CREATE INDEX idx_refresh_token_usuario_id ON refresh_token(usuario_id);
CREATE INDEX idx_refresh_token_token ON refresh_token(token);
CREATE INDEX idx_refresh_token_expiracion ON refresh_token(expiracion);
