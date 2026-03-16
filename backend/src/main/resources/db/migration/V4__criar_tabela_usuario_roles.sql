-- V4: Criar tabela de roles do usuario
CREATE TABLE usuario_roles (
    usuario_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (usuario_id, role),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Criar índices para melhor performance
CREATE INDEX idx_agendamento_usuario ON agendamento(usuario_id);
CREATE INDEX idx_agendamento_data ON agendamento(data);
CREATE INDEX idx_agendamento_servico_agendamento ON agendamento_servico(agendamento_id);
CREATE INDEX idx_agendamento_servico_servico ON agendamento_servico(servico_id);
