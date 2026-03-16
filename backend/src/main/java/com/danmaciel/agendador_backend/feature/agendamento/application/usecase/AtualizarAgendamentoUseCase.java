package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import java.time.LocalDate;
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
import com.danmaciel.agendador_backend.shared.exception.AlteracaoForaDoPrazoException;
import com.danmaciel.agendador_backend.shared.exception.ResourceNotFoundException;

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
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));

        if (agendamento.getData().minusDays(2).isBefore(LocalDate.now())) {
            throw new AlteracaoForaDoPrazoException();
        }

        Set<Servico> servicos = new java.util.HashSet<>(servicoRepository.findAllById(request.servicoIds()));
        if (servicos.size() != request.servicoIds().size()) {
            throw new ResourceNotFoundException("Um ou mais serviços não encontrados");
        }

        int tempoTotal = servicos.stream()
                .mapToInt(Servico::getTempoExecucao)
                .sum();

        agendamento.setData(request.data());
        agendamento.setHorario(request.horario());
        agendamento.setServicos(servicos);
        agendamento.setTempoTotal(tempoTotal);

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
                agendamento.getTempoTotal());
    }
}
