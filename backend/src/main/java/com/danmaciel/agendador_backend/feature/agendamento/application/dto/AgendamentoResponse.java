package com.danmaciel.agendador_backend.feature.agendamento.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

public record AgendamentoResponse(

    Long id,
    Long usuarioId,
    String nomeUsuario,
    Set<ServicoResponse> servicos,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate data,
    @Schema(example = "14:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime horario,
    StatusAgendamento status,
    Integer tempoTotal
) {}
