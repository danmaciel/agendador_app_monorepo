package com.danmaciel.agendador_backend.feature.usuario.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class UsuarioRequestTest {

    @Test
    void deveCriarRecord() {
        // Act
        UsuarioRequest request = new UsuarioRequest("joao", "senha123", "João Silva");
        
        // Assert
        assertNotNull(request);
        assertEquals("joao", request.login());
        assertEquals("senha123", request.senha());
        assertEquals("João Silva", request.nome());
    }
}
