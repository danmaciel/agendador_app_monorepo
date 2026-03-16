-- V3: Criar tabela agendamento
CREATE TABLE agendamento (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    data DATE NOT NULL,
    horario TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- Tabela de junção para relacionamento ManyToMany entre agendamento e servico
CREATE TABLE agendamento_servico (
    agendamento_id BIGINT NOT NULL,
    servico_id BIGINT NOT NULL,
    status_servico VARCHAR(20) DEFAULT 'PENDENTE',
    PRIMARY KEY (agendamento_id, servico_id),
    FOREIGN KEY (agendamento_id) REFERENCES agendamento(id) ON DELETE CASCADE,
    FOREIGN KEY (servico_id) REFERENCES servico(id)
);
