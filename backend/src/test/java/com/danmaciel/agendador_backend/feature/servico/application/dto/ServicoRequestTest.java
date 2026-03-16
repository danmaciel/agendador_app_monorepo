package com.danmaciel.agendador_backend.feature.servico.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ServicoRequestTest {

    @Test
    void deveCriarRecord() {
        // Act
        ServicoRequest request = new ServicoRequest("Corte", "Corte masculino", new BigDecimal("50.00"), 30);
        
        // Assert
        assertNotNull(request);
        assertEquals("Corte", request.nome());
        assertEquals("Corte masculino", request.descricao());
        assertEquals(new BigDecimal("50.00"), request.valor());
        assertEquals(30, request.tempoExecucao());
    }
}
