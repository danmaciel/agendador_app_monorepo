package com.danmaciel.agendador_backend.feature.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.auth.application.dto.TokenResponse;
import com.danmaciel.agendador_backend.feature.auth.domain.entity.RefreshToken;
import com.danmaciel.agendador_backend.feature.auth.domain.repository.RefreshTokenRepository;
import com.danmaciel.agendador_backend.feature.auth.domain.service.JwtService;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Role;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    private RefreshTokenUseCase refreshTokenUseCase;

    @BeforeEach
    void setUp() {
        refreshTokenUseCase = new RefreshTokenUseCase(refreshTokenRepository, jwtService);
    }

    @Test
    void deveGerarNovoTokenQuandoRefreshTokenValido() {
        // Arrange
        String refreshTokenString = "valid-refresh-token";
        Usuario usuario = new Usuario("admin", "senha", "Admin");
        usuario.setId(1L);
        usuario.addRole(Role.ROLE_ADMIN);

        RefreshToken refreshToken = new RefreshToken(usuario, 10080);
        refreshToken.setId(1L);

        RefreshToken newRefreshToken = new RefreshToken(usuario, 10080);
        newRefreshToken.setId(2L);

        when(refreshTokenRepository.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("new-jwt-token");
        when(jwtService.getExpiration()).thenReturn(3600000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newRefreshToken);

        // Act
        TokenResponse result = refreshTokenUseCase.execute(refreshTokenString);

        // Assert
        assertNotNull(result);
        assertEquals("new-jwt-token", result.token());
        assertEquals(3600000L, result.expiracao());
        assertNotNull(result.refreshToken());
    }

    @Test
    void deveLancarExcecaoQuandoRefreshTokenInvalido() {
        // Arrange
        String refreshTokenString = "invalid-token";

        when(refreshTokenRepository.findByToken(refreshTokenString)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> refreshTokenUseCase.execute(refreshTokenString));
    }

    @Test
    void deveLancarExcecaoQuandoRefreshTokenExpirado() {
        // Arrange
        String refreshTokenString = "expired-token";
        Usuario usuario = new Usuario("admin", "senha", "Admin");
        usuario.setId(1L);

        RefreshToken refreshToken = new RefreshToken(usuario, -1);
        refreshToken.setId(1L);
        refreshToken.setExpirationDate(LocalDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));

        // Act & Assert
        assertThrows(BusinessException.class, () -> refreshTokenUseCase.execute(refreshTokenString));
    }
}
