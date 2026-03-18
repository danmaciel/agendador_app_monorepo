package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.shared.exception.AgendamentoConflitoException;
import com.danmaciel.agendador_backend.shared.exception.AlteracaoForaDoPrazoException;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@Component
public class AtualizarAgendamentoUseCase {

    private final AgendamentoRepository agendamentoRepository;
    private final ServicoRepository servicoRepository;

    public AtualizarAgendamentoUseCase(AgendamentoRepository agendamentoRepository,
            ServicoRepository servicoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public AgendamentoResponse execute(Long id, AgendamentoRequest request) {
        Agendamento agendamento = agendamentoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado"));

        if (agendamento.getData().minusDays(2).isBefore(LocalDate.now())) {
            throw new AlteracaoForaDoPrazoException();
        }

        Set<Servico> servicos = new java.util.HashSet<>(servicoRepository.findAllByIdAndAtivoTrue(new java.util.HashSet<>(request.servicoIds())));
        if (servicos.size() != request.servicoIds().size()) {
            throw new RecursoNaoEncontradoException("Um ou mais serviços não encontrados");
        }

        int tempoTotal = servicos.stream()
                .mapToInt(Servico::getTempoExecucao)
                .sum();

        List<Agendamento> agendamentosDia = agendamentoRepository.findByDataAndAtivoTrue(request.data());
        Long idAgendamentoAtual = agendamento.getId();
        List<Agendamento> agendamentosFiltrados = agendamentosDia.stream()
                .filter(ag -> ag.getId() == null || !ag.getId().equals(idAgendamentoAtual))
                .collect(Collectors.toList());
        validarConflitoHorario(agendamentosFiltrados, request.horario(), tempoTotal);

        agendamento.setData(request.data());
        agendamento.setHorario(request.horario());
        agendamento.setServicos(servicos);
        agendamento.setTempoTotal(tempoTotal);

        agendamento = agendamentoRepository.save(agendamento);
        return toResponse(agendamento);
    }

    private void validarConflitoHorario(List<Agendamento> agendamentosDia, LocalTime horarioInicio, int duracao) {
        LocalTime horarioFim = horarioInicio.plusMinutes(duracao);

        boolean haConflito = agendamentosDia.stream()
                .anyMatch(ag -> existeConflito(horarioInicio, horarioFim, ag));

        if (haConflito) {
            throw new AgendamentoConflitoException(null);
        }
    }

    private boolean existeConflito(LocalTime novoInicio, LocalTime novoFim, Agendamento existente) {
        LocalTime existenteInicio = existente.getHorario();
        LocalTime existenteFim = existenteInicio.plusMinutes(existente.getTempoTotal());
        
        boolean sobrepoe = novoInicio.isBefore(existenteFim) && novoFim.isAfter(existenteInicio);
        
        boolean novoCabeEntre = (novoInicio.isAfter(existenteInicio) || novoInicio.equals(existenteInicio))
                             && novoFim.isBefore(existenteFim);
        
        boolean novoEngloba = novoInicio.isBefore(existenteInicio) && novoFim.isAfter(existenteFim);
        
        return sobrepoe || novoCabeEntre || novoEngloba;
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
                agendamento.getTempoTotal());
    }
}
