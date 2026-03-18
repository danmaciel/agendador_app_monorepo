package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@Component
public class DeletarAgendamentoUseCase {

    private final AgendamentoRepository agendamentoRepository;

    public DeletarAgendamentoUseCase(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional
    public void execute(Long id) {
        Optional<Agendamento> agendamento = agendamentoRepository.findByIdAndAtivoTrue(id);
        if (agendamento.isEmpty()) {
            throw new RecursoNaoEncontradoException("Agendamento não encontrado");
        }
        Agendamento agendamentoEntity = agendamento.get();
        agendamentoEntity.setAtivo(false);
        agendamentoRepository.save(agendamentoEntity);
    }
}
