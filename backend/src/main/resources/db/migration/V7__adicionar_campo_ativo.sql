-- V7__adicionar_campo_ativo.sql
-- Adiciona campo ativo para suporte a soft delete

-- Tabela usuario
ALTER TABLE usuario ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

-- Tabela servico
ALTER TABLE servico ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

-- Tabela agendamento
ALTER TABLE agendamento ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
