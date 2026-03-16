package com.danmaciel.agendador_backend.feature.servico.application.dto;

import java.math.BigDecimal;

public record ServicoResponse(
    Long id,
    String nome,
    String descricao,
    BigDecimal valor,
    Integer tempoExecucao
) {}
