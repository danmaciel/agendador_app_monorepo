package com.danmaciel.agendador_backend.shared.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AlteracaoForaDoPrazoExceptionTest {

    @Test
    void deveCriarExcecaoComMensagemCorreta() {
        // Act
        AlteracaoForaDoPrazoException exception = new AlteracaoForaDoPrazoException();
        
        // Assert
        assertEquals("Esta alteração só poder ser feita via telefone! Data vencimento menor que 2 dias!", exception.getMessage());
    }
}
