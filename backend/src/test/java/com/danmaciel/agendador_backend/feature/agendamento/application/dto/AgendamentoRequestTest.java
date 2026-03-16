package com.danmaciel.agendador_backend.feature.agendamento.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

class AgendamentoRequestTest {

    @Test
    void deveCriarRecord() {
        // Arrange
        Set<Long> servicos = Set.of(1L, 2L);
        LocalDate data = LocalDate.now().plusDays(5);
        LocalTime horario = LocalTime.of(10, 0);

        // Act
        AgendamentoRequest request = new AgendamentoRequest(1L, data, horario, servicos);

        // Assert
        assertNotNull(request);
        assertEquals(1L, request.usuarioId());
        assertEquals(data, request.data());
        assertEquals(horario, request.horario());
        assertEquals(2, request.servicoIds().size());
    }
}
