package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.shared.exception.ResourceNotFoundException;

import java.util.stream.Collectors;

@Component
public class AprovarAgendamentoUseCase {

    private final AgendamentoRepository agendamentoRepository;

    public AprovarAgendamentoUseCase(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional
    public AgendamentoResponse execute(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        agendamento.setStatus(StatusAgendamento.APROVADO);
        agendamento = agendamentoRepository.save(agendamento);
        return toResponse(agendamento);
    }

    private AgendamentoResponse toResponse(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getUsuario().getId(),
                agendamento.getUsuario().getNome(),
                agendamento.getServicos().stream()
                        .map(s -> new ServicoResponse(s.getId(), s.getNome(), s.getDescricao(), s.getValor(), s.getTempoExecucao()))
                        .collect(Collectors.toSet()),
                agendamento.getData(),
                agendamento.getHorario(),
                agendamento.getStatus(),
                agendamento.getTempoTotal()
        );
    }
}
