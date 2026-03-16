package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.shared.exception.ResourceNotFoundException;

@Component
public class DeletarServicoUseCase {

    private final ServicoRepository servicoRepository;

    public DeletarServicoUseCase(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public void execute(Long id) {
        if (!servicoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Serviço não encontrado");
        }
        servicoRepository.deleteById(id);
    }
}
