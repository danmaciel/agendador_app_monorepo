package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;

@Component
public class GerarGraficoSemanalUseCase {

    private final AgendamentoRepository agendamentoRepository;

    public GerarGraficoSemanalUseCase(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public Map<String, Object> execute(LocalDate dataInicio, LocalDate dataFim) {
        List<Agendamento> agendamentos = agendamentoRepository.findByDataBetweenAndAtivoTrue(dataInicio, dataFim);
        
        List<Agendamento> aprovados = agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.APROVADO)
                .collect(Collectors.toList());
        
        LocalDate hoje = LocalDate.now();
        
        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("aprovados", (long) aprovados.size());
        relatorio.put("rejeitados", agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.REJEITADO).count());
        relatorio.put("pendentes", agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.PENDENTE).count());
        
        Map<Integer, Long> agendamentosPorDia = new HashMap<>();
        for (Agendamento agendamento : agendamentos) {
            if (agendamento.getStatus() == StatusAgendamento.APROVADO) {
                int diaMes = agendamento.getData().getDayOfMonth();
                agendamentosPorDia.merge(diaMes, 1L, Long::sum);
            }
        }
        relatorio.put("agendamentosPorDia", agendamentosPorDia);

        Map<LocalDate, List<AgendamentoResponse>> listaAprovadosPorDia = aprovados.stream()
                .map(this::toResponse)
                .collect(Collectors.groupingBy(AgendamentoResponse::data));

        List<AgendamentoResponse> agendamentosHoje = aprovados.stream()
                .filter(a -> a.getData().equals(hoje))
                .map(this::toResponse)
                .collect(Collectors.toList());
        relatorio.put("agendamentosHoje", agendamentosHoje);
        
        return relatorio;
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
