-- V5: Criar tabela refresh_token
CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_usuario ON refresh_token(usuario_id);
CREATE INDEX idx_refresh_token_token ON refresh_token(token);
