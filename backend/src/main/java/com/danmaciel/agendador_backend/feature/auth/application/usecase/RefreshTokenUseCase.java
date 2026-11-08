package com.danmaciel.agendador_backend.feature.auth.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.auth.application.dto.TokenResponse;
import com.danmaciel.agendador_backend.feature.auth.domain.entity.RefreshToken;
import com.danmaciel.agendador_backend.feature.auth.domain.repository.RefreshTokenRepository;
import com.danmaciel.agendador_backend.feature.auth.domain.service.JwtService;
import com.danmaciel.agendador_backend.feature.auth.domain.service.RefreshTokenFingerprint;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import java.util.Optional;

@Component
public class RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCase.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Value("${jwt.refresh-token-expiration:10080}")
    private long refreshTokenExpirationMinutes;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository, JwtService jwtService,
            UsuarioRepository usuarioRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public TokenResponse execute(String refreshToken) {
        log.debug("Tentativa de refresh token");

        // Resolve the candidate without locking the token row first. Acquiring
        // a token lock and then a user lock would deadlock against a concurrent
        // login, which holds the user lock before deleting refresh rows.
        String fingerprint = RefreshTokenFingerprint.fingerprint(refreshToken);
        Optional<RefreshToken> candidate = refreshTokenRepository.findByToken(fingerprint);
        if (candidate.isEmpty() && RefreshTokenFingerprint.isLegacyUuid(refreshToken)) {
            candidate = refreshTokenRepository.findByToken(refreshToken);
        }
        Long userId = candidate.map(value -> value.getUsuario().getId())
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        // Login and refresh both acquire this user row lock first. Re-read the
        // refresh row under its own lock: the initial lookup can become stale
        // while waiting for another session operation to commit.
        Usuario usuario = usuarioRepository.findActiveByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        RefreshToken token = refreshTokenRepository.findByTokenForUpdate(fingerprint)
                .or(() -> RefreshTokenFingerprint.isLegacyUuid(refreshToken)
                        ? refreshTokenRepository.findByTokenForUpdate(refreshToken)
                        : Optional.empty())
                .filter(value -> userId.equals(value.getUsuario().getId()))
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            log.warn("Refresh token expirado para usuário: {}", token.getUsuario().getLogin());
            throw new BusinessException("Refresh token expirado");
        }

        refreshTokenRepository.deleteByUsuarioId(usuario.getId());

        RefreshToken newRefreshToken = new RefreshToken(usuario, refreshTokenExpirationMinutes);
        newRefreshToken = refreshTokenRepository.save(newRefreshToken);

        String newToken = jwtService.generateToken(usuario);

        log.info("Refresh token bem-sucedido para usuário: {}", usuario.getLogin());

        return new TokenResponse(newToken, jwtService.getExpiration(), newRefreshToken.getIssuedSecret());
    }
}
