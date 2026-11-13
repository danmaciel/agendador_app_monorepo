package com.danmaciel.agendador_backend.feature.agendamento.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

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
    @Test
    void deveRejeitarListaDeServicosVaziaOuNula() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            LocalDate data = LocalDate.now().plusDays(5);
            LocalTime horario = LocalTime.of(10, 0);

            AgendamentoRequest vazio = new AgendamentoRequest(1L, data, horario, Set.of());
            AgendamentoRequest nulo = new AgendamentoRequest(1L, data, horario, null);

            assertTrue(validator.validate(vazio).stream()
                    .anyMatch(violacao -> "servicoIds".equals(violacao.getPropertyPath().toString())),
                    "Lista vazia precisa ser rejeitada");
            assertTrue(validator.validate(nulo).stream()
                    .anyMatch(violacao -> "servicoIds".equals(violacao.getPropertyPath().toString())),
                    "Lista nula precisa ser rejeitada");
        }
    }

    @Test
    void deveAceitarSolicitacaoComServico() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            AgendamentoRequest request = new AgendamentoRequest(
                    1L, LocalDate.now().plusDays(5), LocalTime.of(10, 0), Set.of(1L));
            assertTrue(validator.validate(request).isEmpty());
        }
    }

}
