package com.danmaciel.agendador_backend.feature.auth.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.danmaciel.agendador_backend.feature.auth.application.dto.LoginRequest;
import com.danmaciel.agendador_backend.feature.auth.application.dto.RefreshTokenRequest;
import com.danmaciel.agendador_backend.feature.auth.application.dto.TokenResponse;
import com.danmaciel.agendador_backend.feature.auth.application.usecase.LoginUseCase;
import com.danmaciel.agendador_backend.feature.auth.application.usecase.RefreshTokenUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(LoginUseCase loginUseCase, RefreshTokenUseCase refreshTokenUseCase) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica o usuário e retorna o token de acesso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUseCase.execute(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Atualizar token", description = "Atualiza o token de acesso usando o refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    })
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenUseCase.execute(request.refreshToken()));
    }
}
