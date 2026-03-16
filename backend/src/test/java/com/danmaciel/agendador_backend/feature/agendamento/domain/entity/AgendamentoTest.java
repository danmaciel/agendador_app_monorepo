package com.danmaciel.agendador_backend.feature.agendamento.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;

class AgendamentoTest {

    @Test
    void deveCriarAgendamento() {
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        LocalDate data = LocalDate.now().plusDays(5);
        LocalTime horario = LocalTime.of(10, 0);

        Agendamento agendamento = new Agendamento(usuario, data, horario);

        assertEquals(usuario, agendamento.getUsuario());
        assertEquals(data, agendamento.getData());
        assertEquals(horario, agendamento.getHorario());
        assertEquals(StatusAgendamento.PENDENTE, agendamento.getStatus());
    }

    @Test
    void deveAdicionarServico() {
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));

        Servico servico = new Servico("Corte", "Corte", new BigDecimal("50.00"));

        agendamento.addServico(servico);

        assertEquals(1, agendamento.getServicos().size());
    }

    @Test
    void deveAlterarStatus() {
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));

        agendamento.setStatus(StatusAgendamento.APROVADO);

        assertEquals(StatusAgendamento.APROVADO, agendamento.getStatus());
    }
}
