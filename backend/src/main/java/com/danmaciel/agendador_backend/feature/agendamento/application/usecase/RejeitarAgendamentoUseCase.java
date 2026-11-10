package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;
import com.danmaciel.agendador_backend.feature.agendamento.infrastructure.lock.AgendaWriteLock;

import java.util.stream.Collectors;

@Component
public class RejeitarAgendamentoUseCase {

    private final AgendamentoRepository agendamentoRepository;
    private final AgendaWriteLock agendaWriteLock;

    public RejeitarAgendamentoUseCase(AgendamentoRepository agendamentoRepository,
            AgendaWriteLock agendaWriteLock) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendaWriteLock = agendaWriteLock;
    }

    @Transactional
    public AgendamentoResponse execute(Long id) {
        agendaWriteLock.acquire();
        Agendamento agendamento = agendamentoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado"));

        if (agendamento.getStatus() != StatusAgendamento.PENDENTE) {
            throw new BusinessException("Somente agendamentos pendentes podem ser rejeitados");
        }

        agendamento.setStatus(StatusAgendamento.REJEITADO);
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
