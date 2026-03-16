package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;

@Component
public class GerarGraficoSemanalUseCase {

    private final AgendamentoRepository agendamentoRepository;

    public GerarGraficoSemanalUseCase(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    public Map<String, Long> execute(LocalDate dataInicio, LocalDate dataFim) {
        List<Agendamento> agendamentos = agendamentoRepository.findByDataBetween(dataInicio, dataFim);
        
        Map<String, Long> grafico = new HashMap<>();
        grafico.put("aprovados", agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.APROVADO).count());
        grafico.put("rejeitados", agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.REJEITADO).count());
        grafico.put("pendentes", agendamentos.stream()
                .filter(a -> a.getStatus() == StatusAgendamento.PENDENTE).count());
        
        return grafico;
    }
}
