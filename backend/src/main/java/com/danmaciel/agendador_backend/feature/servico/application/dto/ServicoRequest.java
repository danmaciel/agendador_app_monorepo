package com.danmaciel.agendador_backend.feature.servico.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record ServicoRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    String nome,
    
    String descricao,
    
    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    BigDecimal valor,
    
    @NotNull(message = "Tempo de execução é obrigatório")
    @Min(value = 1, message = "Tempo de execução deve ser pelo menos 1 minuto")
    Integer tempoExecucao
) {}
