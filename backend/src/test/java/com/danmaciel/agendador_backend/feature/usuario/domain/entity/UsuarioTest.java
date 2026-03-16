package com.danmaciel.agendador_backend.feature.usuario.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void deveCriarUsuarioComRoleCliente() {
        // Arrange
        Usuario usuario = new Usuario("joao", "senha123", "João Silva");

        // Assert
        assertEquals("joao", usuario.getLogin());
        assertEquals("senha123", usuario.getSenha());
        assertEquals("João Silva", usuario.getNome());
        assertTrue(usuario.hasRole(Role.ROLE_CLIENTE));
    }

    @Test
    void deveAdicionarRole() {
        // Arrange
        Usuario usuario = new Usuario("joao", "senha123", "João Silva");

        // Act
        usuario.addRole(Role.ROLE_ADMIN);

        // Assert
        assertTrue(usuario.hasRole(Role.ROLE_ADMIN));
        assertEquals(2, usuario.getRoles().size());
    }

    @Test
    void deveRemoverRole() {
        // Arrange
        Usuario usuario = new Usuario("joao", "senha123", "João Silva");
        usuario.addRole(Role.ROLE_ADMIN);

        // Act
        usuario.removeRole(Role.ROLE_ADMIN);

        // Assert
        assertFalse(usuario.hasRole(Role.ROLE_ADMIN));
    }

    @Test
    void deveVerificarRole() {
        // Arrange
        Usuario usuario = new Usuario("joao", "senha123", "João Silva");

        // Assert
        assertTrue(usuario.hasRole(Role.ROLE_CLIENTE));
        assertFalse(usuario.hasRole(Role.ROLE_ADMIN));
    }
}
