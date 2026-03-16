package com.danmaciel.agendador_backend.feature.usuario.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
    @NotBlank(message = "Login é obrigatório")
    @Size(min = 3, max = 255, message = "Login deve ter entre 3 e 255 caracteres")
    String login,
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    String senha,
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    String nome
) {}
