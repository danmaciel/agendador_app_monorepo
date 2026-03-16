package com.danmaciel.agendador_backend.shared.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LoginInvalidoExceptionTest {

    @Test
    void deveCriarExcecaoComMensagemCorreta() {
        // Act
        LoginInvalidoException exception = new LoginInvalidoException();
        
        // Assert
        assertEquals("Login ou senha inválidos", exception.getMessage());
    }
}
