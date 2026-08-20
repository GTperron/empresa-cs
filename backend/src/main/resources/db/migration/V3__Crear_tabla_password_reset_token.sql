-- V3__Crear_tabla_password_reset_token.sql
-- Crear tabla para tokens de recuperación de contraseña

CREATE TABLE password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    token TEXT NOT NULL UNIQUE,
    expiracion TIMESTAMP NOT NULL,
    utilizado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Crear índices para optimizar búsquedas
CREATE INDEX idx_password_reset_token_usuario_id ON password_reset_token(usuario_id);
CREATE INDEX idx_password_reset_token_token ON password_reset_token(token);
CREATE INDEX idx_password_reset_token_expiracion ON password_reset_token(expiracion);
