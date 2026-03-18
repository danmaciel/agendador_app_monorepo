package com.danmaciel.agendador_backend.feature.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.danmaciel.agendador_backend.feature.auth.application.dto.LoginRequest;
import com.danmaciel.agendador_backend.feature.auth.application.dto.TokenResponse;
import com.danmaciel.agendador_backend.feature.auth.domain.entity.RefreshToken;
import com.danmaciel.agendador_backend.feature.auth.domain.repository.RefreshTokenRepository;
import com.danmaciel.agendador_backend.feature.auth.domain.service.JwtService;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Role;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private Authentication authentication;

    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(authenticationManager, jwtService, usuarioRepository, refreshTokenRepository);
    }

    @Test
    void deveRetornarTokenQuandoCredenciaisValidas() {
        // Arrange
        LoginRequest request = new LoginRequest("admin", "admin123");
        Usuario usuario = new Usuario("admin", "senhaEncoded", "Admin");
        usuario.setId(1L);
        usuario.addRole(Role.ROLE_CLIENTE);

        RefreshToken refreshToken = new RefreshToken(usuario, 10080);
        refreshToken.setId(1L);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(usuarioRepository.findByLoginAndAtivoTrue("admin")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("token-jwt");
        when(jwtService.getExpiration()).thenReturn(3600000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // Act
        TokenResponse result = loginUseCase.execute(request);

        // Assert
        assertNotNull(result);
        assertEquals("token-jwt", result.token());
        assertEquals(3600000L, result.expiracao());
        assertNotNull(result.refreshToken());
    }
}
