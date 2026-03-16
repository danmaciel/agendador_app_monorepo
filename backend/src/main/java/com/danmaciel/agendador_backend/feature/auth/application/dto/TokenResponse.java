package com.danmaciel.agendador_backend.feature.auth.application.dto;

public record TokenResponse(
        String token,
        String tipo,
        Long expiracao,
        String refreshToken) {
    public TokenResponse(String token) {
        this(token, "Bearer", null, null);
    }

    public TokenResponse(String token, Long expiracao) {
        this(token, "Bearer", expiracao, null);
    }

    public TokenResponse(String token, Long expiracao, String refreshToken) {
        this(token, "Bearer", expiracao, refreshToken);
    }
}
