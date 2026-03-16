-- V6: Adicionar campos de tempo para agendamento e servico
ALTER TABLE servico ADD COLUMN tempo_execucao INTEGER NOT NULL DEFAULT 30;

ALTER TABLE agendamento ADD COLUMN tempo_total INTEGER NOT NULL DEFAULT 30;
