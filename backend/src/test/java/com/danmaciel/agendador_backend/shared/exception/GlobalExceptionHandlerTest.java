package com.danmaciel.agendador_backend.shared.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

class GlobalExceptionHandlerTest {

    @Test
    void deveTratarResourceNotFound() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RecursoNaoEncontradoException ex = new RecursoNaoEncontradoException("Recurso não encontrado");
        
        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(ex);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recurso não encontrado", response.getBody().get("message"));
    }

    @Test
    void deveTratarBusinessException() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BusinessException ex = new BusinessException("Erro de negócio");
        
        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleBusinessException(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Erro de negócio", response.getBody().get("message"));
    }

    @Test
    void deveTratarBadCredentials() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        org.springframework.security.authentication.BadCredentialsException ex = 
            new org.springframework.security.authentication.BadCredentialsException("Credenciais inválidas");
        
        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleBadCredentials(ex);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Login ou senha inválidos", response.getBody().get("message"));
    }

    @Test
    void deveTratarExceptionGenerica() {
        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception ex = new RuntimeException("Erro interno");
        
        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erro interno do servidor", response.getBody().get("message"));
    }
}
