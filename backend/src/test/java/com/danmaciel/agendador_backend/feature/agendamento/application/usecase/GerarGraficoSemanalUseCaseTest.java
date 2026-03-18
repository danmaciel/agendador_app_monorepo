package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GerarGraficoSemanalUseCaseTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    private GerarGraficoSemanalUseCase gerarGraficoSemanalUseCase;

    @BeforeEach
    void setUp() {
        gerarGraficoSemanalUseCase = new GerarGraficoSemanalUseCase(agendamentoRepository);
    }

    @Test
    void deveGerarGraficoComDados() {
        LocalDate dataInicio = LocalDate.now();
        LocalDate dataFim = LocalDate.now().plusDays(7);
        
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(1L);
        
        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));
        
        LocalDate dataAgendamento1 = LocalDate.now().plusDays(5);
        Agendamento agendamento1 = new Agendamento(usuario, dataAgendamento1, LocalTime.of(10, 0));
        agendamento1.setId(1L);
        agendamento1.setStatus(StatusAgendamento.APROVADO);
        agendamento1.setServicos(Set.of(servico));
        agendamento1.setAtivo(true);
        
        LocalDate dataAgendamento2 = LocalDate.now().plusDays(6);
        Agendamento agendamento2 = new Agendamento(usuario, dataAgendamento2, LocalTime.of(14, 0));
        agendamento2.setId(2L);
        agendamento2.setStatus(StatusAgendamento.REJEITADO);
        agendamento2.setServicos(Set.of(servico));
        agendamento2.setAtivo(true);

        when(agendamentoRepository.findByDataBetweenAndAtivoTrue(dataInicio, dataFim)).thenReturn(List.of(agendamento1, agendamento2));

        Map<String, Object> result = gerarGraficoSemanalUseCase.execute(dataInicio, dataFim);

        assertEquals(1L, result.get("aprovados"));
        assertEquals(1L, result.get("rejeitados"));
        assertEquals(0L, result.get("pendentes"));
        
        @SuppressWarnings("unchecked")
        Map<Integer, Long> agendamentosPorDia = (Map<Integer, Long>) result.get("agendamentosPorDia");
        assertNotNull(agendamentosPorDia);
        assertEquals(1L, agendamentosPorDia.get(dataAgendamento1.getDayOfMonth()));
        assertNull(agendamentosPorDia.get(dataAgendamento2.getDayOfMonth()));
    }

    @Test
    void deveGerarGraficoVazio() {
        LocalDate dataInicio = LocalDate.now();
        LocalDate dataFim = LocalDate.now().plusDays(7);

        when(agendamentoRepository.findByDataBetweenAndAtivoTrue(dataInicio, dataFim)).thenReturn(List.of());

        Map<String, Object> result = gerarGraficoSemanalUseCase.execute(dataInicio, dataFim);

        assertEquals(0L, result.get("aprovados"));
        assertEquals(0L, result.get("rejeitados"));
        
        @SuppressWarnings("unchecked")
        Map<Integer, Long> agendamentosPorDia = (Map<Integer, Long>) result.get("agendamentosPorDia");
        assertNotNull(agendamentosPorDia);
        assertTrue(agendamentosPorDia.isEmpty());
    }
}
