package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.shared.exception.ResourceNotFoundException;

@Component
public class DeletarAgendamentoUseCase {

    private final AgendamentoRepository agendamentoRepository;

    public DeletarAgendamentoUseCase(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional
    public void execute(Long id) {
        if (!agendamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agendamento não encontrado");
        }
        agendamentoRepository.deleteById(id);
    }
}
