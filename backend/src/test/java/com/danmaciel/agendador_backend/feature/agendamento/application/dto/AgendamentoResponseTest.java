package com.danmaciel.agendador_backend.feature.agendamento.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;

class AgendamentoResponseTest {

    @Test
    void deveCriarRecord() {
        Set<ServicoResponse> servicos = Set.of(
            new ServicoResponse(1L, "Corte", "Corte masculino", new BigDecimal("50.00"), 30)
        );
        LocalDate data = LocalDate.now().plusDays(5);
        LocalTime horario = LocalTime.of(10, 0);
        
        AgendamentoResponse response = new AgendamentoResponse(1L, 1L, "João Silva", servicos, data, horario, StatusAgendamento.PENDENTE, 30);
        
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(1L, response.usuarioId());
        assertEquals("João Silva", response.nomeUsuario());
        assertEquals(1, response.servicos().size());
        assertEquals(data, response.data());
        assertEquals(horario, response.horario());
        assertEquals(StatusAgendamento.PENDENTE, response.status());
        assertEquals(30, response.tempoTotal());
    }
}
