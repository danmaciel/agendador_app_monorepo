package com.danmaciel.agendador_backend.feature.auth.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class LoginRequestTest {

    @Test
    void deveCriarRecord() {
        // Act
        LoginRequest request = new LoginRequest("admin", "senha123");
        
        // Assert
        assertNotNull(request);
        assertEquals("admin", request.login());
        assertEquals("senha123", request.senha());
    }
}
