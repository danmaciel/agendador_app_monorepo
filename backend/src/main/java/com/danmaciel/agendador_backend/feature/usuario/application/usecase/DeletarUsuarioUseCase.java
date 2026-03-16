package com.danmaciel.agendador_backend.feature.usuario.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.ResourceNotFoundException;

@Component
public class DeletarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public DeletarUsuarioUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void execute(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}
