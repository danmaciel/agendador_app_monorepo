package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.HistoricoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;

@Component
public class BuscarHistoricoUseCase {

    private final AgendamentoRepository agendamentoRepository;

    public BuscarHistoricoUseCase(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public Page<HistoricoResponse> execute(Long usuarioId, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        Page<Agendamento> agendamentos;

        if (dataInicio != null && dataFim != null) {
            agendamentos = agendamentoRepository.findByUsuarioIdAndDataBetweenAndAtivoTrue(usuarioId, dataInicio, dataFim, pageable);
        } else {
            agendamentos = agendamentoRepository.findByUsuarioIdAndAtivoTrue(usuarioId, pageable);
        }

        return agendamentos.map(this::toResponse);
    }

    public Map<LocalDate, List<HistoricoResponse>> buscarAgrupadosPorDia(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        List<Agendamento> agendamentos;

        if (dataInicio != null && dataFim != null) {
            agendamentos = agendamentoRepository.findByUsuarioIdAndDataBetweenAndAtivoTrue(usuarioId, dataInicio, dataFim);
        } else {
            agendamentos = agendamentoRepository.findByUsuarioIdAndAtivoTrue(usuarioId);
        }

        return agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.APROVADO)
                .map(this::toResponse)
                .collect(Collectors.groupingBy(HistoricoResponse::data));
    }

    private HistoricoResponse toResponse(Agendamento agendamento) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime agendamentoDateTime = LocalDateTime.of(agendamento.getData(), agendamento.getHorario());
        boolean isPassado = agendamentoDateTime.isBefore(now);

        BigDecimal valorTotal = agendamento.getServicos().stream()
                .map(Servico::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new HistoricoResponse(
                agendamento.getId(),
                agendamento.getUsuario().getId(),
                agendamento.getUsuario().getNome(),
                agendamento.getServicos().stream()
                        .map(s -> new ServicoResponse(s.getId(), s.getNome(), s.getDescricao(), s.getValor(), s.getTempoExecucao()))
                        .collect(Collectors.toSet()),
                agendamento.getData(),
                agendamento.getHorario(),
                agendamento.getStatus(),
                agendamento.getCreatedAt(),
                valorTotal,
                isPassado,
                isPassado,
                agendamento.getTempoTotal()
        );
    }
}
