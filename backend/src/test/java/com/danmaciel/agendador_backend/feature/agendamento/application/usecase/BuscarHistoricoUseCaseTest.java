package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.HistoricoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;

@ExtendWith(MockitoExtension.class)
class BuscarHistoricoUseCaseTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    private BuscarHistoricoUseCase buscarHistoricoUseCase;

    @BeforeEach
    void setUp() {
        buscarHistoricoUseCase = new BuscarHistoricoUseCase(agendamentoRepository);
    }

    @Test
    void deveRetornarHistoricoComValorTotal() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        Servico servico1 = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));
        Servico servico2 = new Servico("Barba", "Barba modelada", new BigDecimal("30.00"));

        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamento.setId(1L);
        agendamento.setServicos(Set.of(servico1, servico2));
        agendamento.setStatus(StatusAgendamento.APROVADO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> page = new PageImpl<>(List.of(agendamento), pageable, 1);

        when(agendamentoRepository.findByUsuarioIdAndAtivoTrue(usuarioId, pageable)).thenReturn(page);

        Page<HistoricoResponse> result = buscarHistoricoUseCase.execute(usuarioId, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(new BigDecimal("80.00"), result.getContent().get(0).valorTotal());
    }

    @Test
    void deveIdentificarAgendamentoPassado() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));

        Agendamento agendamentoPassado = new Agendamento(usuario, LocalDate.now().minusDays(5), LocalTime.of(10, 0));
        agendamentoPassado.setId(1L);
        agendamentoPassado.setServicos(Set.of(servico));
        agendamentoPassado.setStatus(StatusAgendamento.APROVADO);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> page = new PageImpl<>(List.of(agendamentoPassado), pageable, 1);

        when(agendamentoRepository.findByUsuarioIdAndAtivoTrue(usuarioId, pageable)).thenReturn(page);

        Page<HistoricoResponse> result = buscarHistoricoUseCase.execute(usuarioId, null, null, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().get(0).isPassado());
    }

    @Test
    void deveIdentificarAgendamentoFuturo() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));

        Agendamento agendamentoFuturo = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamentoFuturo.setId(1L);
        agendamentoFuturo.setServicos(Set.of(servico));
        agendamentoFuturo.setStatus(StatusAgendamento.PENDENTE);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> page = new PageImpl<>(List.of(agendamentoFuturo), pageable, 1);

        when(agendamentoRepository.findByUsuarioIdAndAtivoTrue(usuarioId, pageable)).thenReturn(page);

        Page<HistoricoResponse> result = buscarHistoricoUseCase.execute(usuarioId, null, null, pageable);

        assertNotNull(result);
        assertFalse(result.getContent().get(0).isPassado());
    }

    @Test
    void deveFiltrarPorPeriodo() {
        Long usuarioId = 1L;
        LocalDate dataInicio = LocalDate.now();
        LocalDate dataFim = LocalDate.now().plusDays(7);

        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));

        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamento.setId(1L);
        agendamento.setServicos(Set.of(servico));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> page = new PageImpl<>(List.of(agendamento), pageable, 1);

        when(agendamentoRepository.findByUsuarioIdAndDataBetweenAndAtivoTrue(usuarioId, dataInicio, dataFim, pageable)).thenReturn(page);

        Page<HistoricoResponse> result = buscarHistoricoUseCase.execute(usuarioId, dataInicio, dataFim, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deveRetornarListaVazia() {
        Long usuarioId = 1L;

        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> page = new PageImpl<>(List.of(), pageable, 0);

        when(agendamentoRepository.findByUsuarioIdAndAtivoTrue(usuarioId, pageable)).thenReturn(page);

        Page<HistoricoResponse> result = buscarHistoricoUseCase.execute(usuarioId, null, null, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void deveCalcularValorTotalComMultiplosServicos() {
        Long usuarioId = 1L;
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        Servico servico1 = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));
        Servico servico2 = new Servico("Barba", "Barba modelada", new BigDecimal("30.00"));
        Servico servico3 = new Servico("Sobrancelha", "Design de sobrancelha", new BigDecimal("20.00"));

        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamento.setId(1L);
        agendamento.setServicos(Set.of(servico1, servico2, servico3));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> page = new PageImpl<>(List.of(agendamento), pageable, 1);

        when(agendamentoRepository.findByUsuarioIdAndAtivoTrue(usuarioId, pageable)).thenReturn(page);

        Page<HistoricoResponse> result = buscarHistoricoUseCase.execute(usuarioId, null, null, pageable);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getContent().get(0).valorTotal());
    }
}
