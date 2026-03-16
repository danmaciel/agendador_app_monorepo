package com.danmaciel.agendador_backend.feature.auth.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Role;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "chave-secreta-padrao-para-desenvolvimento-minimo-256-bits");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
    }

    @Test
    void deveGerarToken() {
        // Arrange
        Usuario usuario = new Usuario("admin", "senha", "Administrador");
        usuario.setId(1L);
        usuario.addRole(Role.ROLE_ADMIN);
        usuario.addRole(Role.ROLE_CLIENTE);

        // Act
        String token = jwtService.generateToken(usuario);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void deveExtrairLoginDoToken() {
        // Arrange
        Usuario usuario = new Usuario("admin", "senha", "Administrador");
        usuario.setId(1L);
        usuario.addRole(Role.ROLE_ADMIN);
        String token = jwtService.generateToken(usuario);

        // Act
        String loginExtraido = jwtService.extractLogin(token);

        // Assert
        assertEquals("admin", loginExtraido);
    }

    @Test
    void deveExtrairUserIdDoToken() {
        // Arrange
        Usuario usuario = new Usuario("admin", "senha", "Administrador");
        usuario.setId(42L);
        usuario.addRole(Role.ROLE_ADMIN);
        String token = jwtService.generateToken(usuario);

        // Act
        Long userId = jwtService.extractUserId(token);

        // Assert
        assertEquals(42L, userId);
    }

    @Test
    void deveExtrairNomeDoToken() {
        // Arrange
        Usuario usuario = new Usuario("admin", "senha", "Administrador");
        usuario.setId(1L);
        usuario.addRole(Role.ROLE_ADMIN);
        String token = jwtService.generateToken(usuario);

        // Act
        String nome = jwtService.extractNome(token);

        // Assert
        assertEquals("Administrador", nome);
    }

    @Test
    void deveValidarTokenValido() {
        // Arrange
        Usuario usuario = new Usuario("admin", "senha", "Administrador");
        usuario.setId(1L);
        usuario.addRole(Role.ROLE_ADMIN);
        String token = jwtService.generateToken(usuario);

        // Act
        boolean valido = jwtService.isTokenValido(token);

        // Assert
        assertTrue(valido);
    }

    @Test
    void deveInvalidarTokenInvalido() {
        // Act
        boolean valido = jwtService.isTokenValido("token.invalido");

        // Assert
        assertFalse(valido);
    }

    @Test
    void deveRetornarExpiracao() {
        // Act
        Long expiracao = jwtService.getExpiration();

        // Assert
        assertEquals(3600000L, expiracao);
    }
}
