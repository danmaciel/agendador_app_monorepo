package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AprovarAgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.shared.exception.AgendaConflitoException;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

import java.time.LocalTime;
import java.util.List;

@Component
public class AprovarAgendamentoUseCase {

    private final AgendamentoRepository agendamentoRepository;

    public AprovarAgendamentoUseCase(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional
    public AgendamentoResponse execute(Long id, AprovarAgendamentoRequest request) {
        Agendamento agendamento = agendamentoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado"));

        for (Servico servico : agendamento.getServicos()) {
            if (!servico.getAtivo()) {
                throw new RecursoNaoEncontradoException("Serviço '" + servico.getNome() + "' não está mais disponível");
            }
        }

        Set<Servico> servicosAprovados = agendamento.getServicos().stream()
                .filter(s -> request.servicosAprovados().contains(s.getId()))
                .collect(Collectors.toSet());

        agendamento.setServicos(servicosAprovados);
        
        int tempoTotal = servicosAprovados.stream()
                .mapToInt(Servico::getTempoExecucao)
                .sum();
        agendamento.setTempoTotal(tempoTotal);

        verificarConflitoHorario(agendamento);

        agendamento.setStatus(StatusAgendamento.APROVADO);
        agendamento = agendamentoRepository.save(agendamento);
        return toResponse(agendamento);
    }

    private void verificarConflitoHorario(Agendamento agendamento) {
        List<Agendamento> agendamentosDia = agendamentoRepository.findByDataAndAtivoTrue(agendamento.getData());
        
        for (Agendamento existente : agendamentosDia) {
            if (existente.getId().equals(agendamento.getId())) continue;
            if (existente.getStatus() != StatusAgendamento.APROVADO) continue;
            
            LocalTime novoInicio = agendamento.getHorario();
            LocalTime novoFim = novoInicio.plusMinutes(agendamento.getTempoTotal());
            LocalTime existenteInicio = existente.getHorario();
            LocalTime existenteFim = existenteInicio.plusMinutes(existente.getTempoTotal());
            
            boolean sobrepoe = novoInicio.isBefore(existenteFim) && novoFim.isAfter(existenteInicio);
            
            if (sobrepoe) {
                throw new AgendaConflitoException("Conflito de horário com agendamento ID " + existente.getId() + 
                       " do cliente " + existente.getUsuario().getNome() + " neste mesmo dia");
            }
        }
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
