package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import java.time.LocalDate;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;

@Component
public class ListarAgendamentosUseCase {

    private final AgendamentoRepository agendamentoRepository;

    public ListarAgendamentosUseCase(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public Page<AgendamentoResponse> executePorUsuario(Long usuarioId, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        Page<Agendamento> agendamentos;
        
        if (dataInicio != null && dataFim != null) {
            agendamentos = agendamentoRepository.findByUsuarioIdAndDataBetweenAndAtivoTrue(usuarioId, dataInicio, dataFim, pageable);
        } else {
            agendamentos = agendamentoRepository.findByUsuarioIdAndAtivoTrue(usuarioId, pageable);
        }
        
        return agendamentos.map(this::toResponse);
    }

    public Page<AgendamentoResponse> executeAdmin(LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        Page<Agendamento> agendamentos;
        
        if (dataInicio != null && dataFim != null) {
            agendamentos = agendamentoRepository.findByDataBetweenAndAtivoTrue(dataInicio, dataFim, pageable);
        } else {
            agendamentos = agendamentoRepository.findAll(pageable);
        }
        
        return agendamentos.map(this::toResponse);
    }

    public Page<AgendamentoResponse> executePendentes(Pageable pageable) {
        Page<Agendamento> agendamentos = agendamentoRepository.findByStatusAndAtivoTrue(StatusAgendamento.PENDENTE, pageable);
        return agendamentos.map(this::toResponse);
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
