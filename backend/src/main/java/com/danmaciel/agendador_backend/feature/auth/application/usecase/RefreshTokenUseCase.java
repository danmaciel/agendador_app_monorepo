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
import com.danmaciel.agendador_backend.shared.exception.BusinessException;

@Component
public class RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCase.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh-token-expiration:10080}")
    private long refreshTokenExpirationMinutes;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse execute(String refreshToken) {
        log.debug("Tentativa de refresh token");

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh token inválido");
                    return new BusinessException("Refresh token inválido");
                });

        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            log.warn("Refresh token expirado para usuário: {}", token.getUsuario().getLogin());
            throw new BusinessException("Refresh token expirado");
        }

        refreshTokenRepository.deleteByUsuarioId(token.getUsuario().getId());

        RefreshToken newRefreshToken = new RefreshToken(token.getUsuario(), refreshTokenExpirationMinutes);
        newRefreshToken = refreshTokenRepository.save(newRefreshToken);

        String newToken = jwtService.generateToken(token.getUsuario());

        log.info("Refresh token bem-sucedido para usuário: {}", token.getUsuario().getLogin());

        return new TokenResponse(newToken, jwtService.getExpiration(), newRefreshToken.getToken());
    }
}
