package com.danmaciel.agendador_backend.feature.usuario.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Role;

class UsuarioResponseTest {

    @Test
    void deveCriarRecord() {
        // Arrange
        Set<Role> roles = Set.of(Role.ROLE_CLIENTE);
        
        // Act
        UsuarioResponse response = new UsuarioResponse(1L, "joao", "João Silva", roles);
        
        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("joao", response.login());
        assertEquals("João Silva", response.nome());
        assertEquals(1, response.roles().size());
    }
}
