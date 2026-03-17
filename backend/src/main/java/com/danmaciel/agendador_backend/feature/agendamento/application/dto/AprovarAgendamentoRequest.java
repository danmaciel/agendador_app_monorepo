package com.danmaciel.agendador_backend.feature.agendamento.application.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record AprovarAgendamentoRequest(
    
    @NotEmpty(message = "Lista de serviços não pode estar vazia")
    @Schema(description = "IDs dos serviços que serão aprovados")
    Set<Long> servicosAprovados
) {}
