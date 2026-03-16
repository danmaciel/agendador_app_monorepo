package com.danmaciel.agendador_backend.feature.usuario.application.dto;

import java.util.Set;

import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Role;

public record UsuarioResponse(
    Long id,
    String login,
    String nome,
    Set<Role> roles
) {}
