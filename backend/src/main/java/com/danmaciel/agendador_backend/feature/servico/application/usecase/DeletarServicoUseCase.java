package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@Component
public class DeletarServicoUseCase {

    private final ServicoRepository servicoRepository;

    public DeletarServicoUseCase(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public void execute(Long id) {
        Optional<Servico> servico = servicoRepository.findByIdAndAtivoTrue(id);
        if (servico.isEmpty()) {
            throw new RecursoNaoEncontradoException("Serviço não encontrado");
        }
        Servico servicoEntity = servico.get();
        servicoEntity.setAtivo(false);
        servicoRepository.save(servicoEntity);
    }
}
