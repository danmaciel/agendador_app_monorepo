package com.danmaciel.agendador_backend.feature.auth.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.auth.application.dto.LoginRequest;
import com.danmaciel.agendador_backend.feature.auth.application.dto.TokenResponse;
import com.danmaciel.agendador_backend.feature.auth.domain.entity.RefreshToken;
import com.danmaciel.agendador_backend.feature.auth.domain.repository.RefreshTokenRepository;
import com.danmaciel.agendador_backend.feature.auth.domain.service.JwtService;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;

@Component
public class LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUseCase.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration:10080}")
    private long refreshTokenExpirationMinutes;

    public LoginUseCase(AuthenticationManager authenticationManager, JwtService jwtService,
            UsuarioRepository usuarioRepository, RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public TokenResponse execute(LoginRequest request) {
        log.info("Tentativa de login para usuário: {}", request.login());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.senha()));

        Usuario usuario = usuarioRepository.findByLoginAndAtivoTrue(request.login())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        refreshTokenRepository.deleteByUsuarioId(usuario.getId());

        RefreshToken refreshToken = new RefreshToken(usuario, refreshTokenExpirationMinutes);
        refreshToken = refreshTokenRepository.save(refreshToken);

        String token = jwtService.generateToken(usuario);

        log.info("Login bem-sucedido para usuário: {}", usuario.getLogin());

        return new TokenResponse(token, jwtService.getExpiration(), refreshToken.getToken());
    }
}
