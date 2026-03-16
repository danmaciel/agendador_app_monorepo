package com.danmaciel.agendador_backend.feature.auth.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class TokenResponseTest {

    @Test
    void deveCriarRecordComToken() {
        // Act
        TokenResponse token = new TokenResponse("token-jwt");

        // Assert
        assertNotNull(token);
        assertEquals("token-jwt", token.token());
        assertEquals("Bearer", token.tipo());
    }

    @Test
    void deveCriarRecordCompleto() {
        // Act
        TokenResponse token = new TokenResponse("token-jwt", "Bearer", 3600000L, "refresh-token");

        // Assert
        assertNotNull(token);
        assertEquals("token-jwt", token.token());
        assertEquals("Bearer", token.tipo());
        assertEquals(3600000L, token.expiracao());
        assertEquals("refresh-token", token.refreshToken());
    }
}
