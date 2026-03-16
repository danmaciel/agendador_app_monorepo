package com.danmaciel.agendador_backend.shared.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class AgendamentoConflitoExceptionTest {

    @Test
    void deveCriarExcecaoComMensagemECDataSugerida() {
        // Arrange
        LocalDate dataSugerida = LocalDate.of(2026, 3, 20);
        
        // Act
        AgendamentoConflitoException exception = new AgendamentoConflitoException(dataSugerida);
        
        // Assert
        assertEquals("Você já possui agendamento nesta semana. Data sugerida: 2026-03-20", exception.getMessage());
        assertEquals(dataSugerida, exception.getDataSugerida());
    }
}
