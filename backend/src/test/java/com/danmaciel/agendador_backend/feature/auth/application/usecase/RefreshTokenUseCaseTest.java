package com.danmaciel.agendador_backend.feature.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
import com.danmaciel.agendador_backend.feature.auth.domain.service.RefreshTokenFingerprint;
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
        String refreshTokenString = RefreshTokenFingerprint.issueSecret();
        Usuario usuario = new Usuario("admin", "senha", "Admin");
        usuario.setId(1L);
        usuario.addRole(Role.ROLE_ADMIN);

        RefreshToken refreshToken = new RefreshToken(usuario, 10080);
        refreshToken.setId(1L);

        RefreshToken newRefreshToken = new RefreshToken(usuario, 10080);
        newRefreshToken.setId(2L);

        when(refreshTokenRepository.findByToken(RefreshTokenFingerprint.fingerprint(refreshTokenString)))
                .thenReturn(Optional.of(refreshToken));
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
    void naoDeveRenovarTokenDeUsuarioDesativado() {
        Usuario usuario = new Usuario("conta-desativada", "hash-teste", "Conta teste");
        usuario.setId(42L);
        usuario.setAtivo(false);
        RefreshToken refreshToken = new RefreshToken(usuario, 10080);
        when(refreshTokenRepository.findByToken(
                RefreshTokenFingerprint.fingerprint("refresh-de-conta-desativada")))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(BusinessException.class,
                () -> refreshTokenUseCase.execute("refresh-de-conta-desativada"));

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }

    @Test
    void permiteRenovacaoTransitoriaDeUuidLegadoEEmiteNovoSegredoProtegido() {
        String legacy = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
        Usuario usuario = new Usuario("cliente-legado", "hash-teste", "Cliente legado");
        usuario.setId(33L);
        RefreshToken previous = new RefreshToken(usuario, 90);
        RefreshToken issued = new RefreshToken(usuario, 90);

        when(refreshTokenRepository.findByToken(RefreshTokenFingerprint.fingerprint(legacy)))
                .thenReturn(Optional.empty());
        when(refreshTokenRepository.findByToken(legacy)).thenReturn(Optional.of(previous));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(issued);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("access-jwt");
        when(jwtService.getExpiration()).thenReturn(3600000L);

        TokenResponse result = refreshTokenUseCase.execute(legacy);

        assertEquals(issued.getIssuedSecret(), result.refreshToken());
        assertEquals(RefreshTokenFingerprint.fingerprint(result.refreshToken()), issued.getToken());
    }

    @Test
    void naoAceitaFingerprintDoBancoComoCredencialBearer() {
        String leakedFingerprint = RefreshTokenFingerprint.fingerprint("secret-for-test");
        when(refreshTokenRepository.findByToken(
                RefreshTokenFingerprint.fingerprint(leakedFingerprint))).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> refreshTokenUseCase.execute(leakedFingerprint));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verify(jwtService, never()).generateToken(any(Usuario.class));
        verify(refreshTokenRepository, never()).findByToken(leakedFingerprint);
    }

    @Test
    void deveLancarExcecaoQuandoRefreshTokenInvalido() {
        // Arrange
        String refreshTokenString = "invalid-token";

        when(refreshTokenRepository.findByToken(RefreshTokenFingerprint.fingerprint(refreshTokenString)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () -> refreshTokenUseCase.execute(refreshTokenString));
    }

    @Test
    void deveLancarExcecaoQuandoRefreshTokenExpirado() {
        // Arrange
        String refreshTokenString = RefreshTokenFingerprint.issueSecret();
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
