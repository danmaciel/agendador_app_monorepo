package com.danmaciel.agendador_backend.feature.agendamento.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.danmaciel.agendador_backend.shared.exception.BusinessException;

class AgendamentoHorarioPolicyTest {

    @Test
    void devePermitirIntervalosQueTerminamAntesDaMeiaNoite() {
        assertDoesNotThrow(() -> AgendamentoHorarioPolicy.validarIntervaloNoMesmoDia(
                LocalTime.of(23, 29), 30));
        assertDoesNotThrow(() -> AgendamentoHorarioPolicy.validarIntervaloNoMesmoDia(
                LocalTime.MIDNIGHT, 1));
    }

    @Test
    void deveRejeitarTerminoExatamenteNaMeiaNoiteOuNoDiaSeguinte() {
        assertThrows(BusinessException.class,
                () -> AgendamentoHorarioPolicy.validarIntervaloNoMesmoDia(LocalTime.of(23, 30), 30));
        assertThrows(BusinessException.class,
                () -> AgendamentoHorarioPolicy.validarIntervaloNoMesmoDia(LocalTime.of(23, 45), 30));
    }

    @Test
    void deveRejeitarDuracaoNaoPositivaOuExcessiva() {
        assertThrows(BusinessException.class,
                () -> AgendamentoHorarioPolicy.validarIntervaloNoMesmoDia(LocalTime.NOON, 0));
        assertThrows(BusinessException.class,
                () -> AgendamentoHorarioPolicy.validarIntervaloNoMesmoDia(LocalTime.NOON, -5));
        assertThrows(BusinessException.class,
                () -> AgendamentoHorarioPolicy.validarIntervaloNoMesmoDia(LocalTime.NOON, Integer.MAX_VALUE));
        assertThrows(BusinessException.class,
                () -> AgendamentoHorarioPolicy.validarIntervaloNoMesmoDia(null, 10));
    }
}
