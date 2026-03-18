package com.danmaciel.agendador_backend.feature.usuario.application.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.usuario.application.dto.UsuarioRequest;
import com.danmaciel.agendador_backend.feature.usuario.application.dto.UsuarioResponse;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;

@Component
public class CriarUsuarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(CriarUsuarioUseCase.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CriarUsuarioUseCase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse execute(UsuarioRequest request) {
        log.info("Tentativa de criar usuário com login: {}", request.login());

        if (usuarioRepository.existsByLoginAndAtivoTrue(request.login())) {
            log.warn("Tentativa de criar usuário com login já existente: {}", request.login());
            throw new BusinessException("Login já está em uso");
        }

        Usuario usuario = new Usuario(
                request.login(),
                passwordEncoder.encode(request.senha()),
                request.nome());
        usuario.setCreatedAt(LocalDateTime.now());
        usuario.setUpdatedAt(LocalDateTime.now());

        usuario = usuarioRepository.save(usuario);

        log.info("Usuário criado com sucesso: {}", usuario.getLogin());

        return toResponse(usuario);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getLogin(),
                usuario.getNome(),
                usuario.getRoles());
    }
}
