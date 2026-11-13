package com.danmaciel.agendador_backend.feature.agendamento.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

public record AgendamentoRequest(
    @NotNull(message = "ID do usuário é obrigatório")
    Long usuarioId,
    
    @NotNull(message = "Data é obrigatória")
    @Future(message = "Data deve ser futura")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate data,
    
    @NotNull(message = "Horário é obrigatório")
    @Schema(example = "14:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime horario,
    
    @NotEmpty(message = "É necessário informar pelo menos um serviço")
    Set<Long> servicoIds
) {}
