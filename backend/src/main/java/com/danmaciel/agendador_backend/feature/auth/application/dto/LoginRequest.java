package com.danmaciel.agendador_backend.feature.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Login é obrigatório")
    String login,
    
    @NotBlank(message = "Senha é obrigatória")
    String senha
) {}
