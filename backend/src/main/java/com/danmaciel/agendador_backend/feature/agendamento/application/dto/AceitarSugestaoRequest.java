package com.danmaciel.agendador_backend.feature.agendamento.application.dto;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

/**
 * Serviços a acrescentar a um agendamento identificado na URL.
 * A data, o horário e o proprietário são obtidos do agendamento persistido.
 */
public record AceitarSugestaoRequest(
        @NotEmpty(message = "É necessário informar pelo menos um serviço")
        Set<Long> servicoIds
) {}
