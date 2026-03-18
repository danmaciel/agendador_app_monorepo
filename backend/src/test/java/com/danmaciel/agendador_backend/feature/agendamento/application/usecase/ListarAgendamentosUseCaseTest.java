package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;

@ExtendWith(MockitoExtension.class)
class ListarAgendamentosUseCaseTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    private ListarAgendamentosUseCase listarAgendamentosUseCase;

    @BeforeEach
    void setUp() {
        listarAgendamentosUseCase = new ListarAgendamentosUseCase(agendamentoRepository);
    }

    @Test
    void deveListarAgendamentosPorUsuario() {
        // Arrange
        Long usuarioId = 1L;
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);
        
        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));
        
        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamento.setId(1L);
        agendamento.setServicos(Set.of(servico));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> page = new PageImpl<>(List.of(agendamento), pageable, 1);

        when(agendamentoRepository.findByUsuarioIdAndAtivoTrue(usuarioId, pageable)).thenReturn(page);

        // Act
        Page<AgendamentoResponse> result = listarAgendamentosUseCase.executePorUsuario(usuarioId, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deveListarAgendamentosAdmin() {
        // Arrange
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(1L);
        
        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));
        
        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamento.setId(1L);
        agendamento.setServicos(Set.of(servico));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> page = new PageImpl<>(List.of(agendamento), pageable, 1);

        when(agendamentoRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<AgendamentoResponse> result = listarAgendamentosUseCase.executeAdmin(null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deveListarAgendamentosComFiltroDeData() {
        // Arrange
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

        // Act
        Page<AgendamentoResponse> result = listarAgendamentosUseCase.executePorUsuario(usuarioId, dataInicio, dataFim, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deveListarAgendamentosPendentes() {
        // Arrange
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(1L);
        
        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));
        
        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamento.setId(1L);
        agendamento.setServicos(Set.of(servico));
        agendamento.setStatus(StatusAgendamento.PENDENTE);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> page = new PageImpl<>(List.of(agendamento), pageable, 1);

        when(agendamentoRepository.findByStatusAndAtivoTrue(StatusAgendamento.PENDENTE, pageable)).thenReturn(page);

        // Act
        Page<AgendamentoResponse> result = listarAgendamentosUseCase.executePendentes(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(StatusAgendamento.PENDENTE, result.getContent().get(0).status());
    }
}
