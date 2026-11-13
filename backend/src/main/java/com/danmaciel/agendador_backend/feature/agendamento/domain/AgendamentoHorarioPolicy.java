package com.danmaciel.agendador_backend.feature.agendamento.domain;

import java.time.LocalTime;

import com.danmaciel.agendador_backend.shared.exception.BusinessException;

/**
 * This single-day scheduling example does not model an appointment end date.
 * Reject intervals crossing midnight so LocalTime arithmetic cannot wrap
 * around and silently bypass same-day overlap checks.
 */
public final class AgendamentoHorarioPolicy {

    private static final int MINUTOS_POR_DIA = 24 * 60;

    private AgendamentoHorarioPolicy() {
    }

    public static void validarIntervaloNoMesmoDia(LocalTime inicio, int duracaoMinutos) {
        if (inicio == null || duracaoMinutos <= 0
                || inicio.toSecondOfDay() + (long) duracaoMinutos * 60 > (long) MINUTOS_POR_DIA * 60) {
            throw new BusinessException("O agendamento precisa terminar até meia-noite do mesmo dia");
        }
    }
}
