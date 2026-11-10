package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import com.danmaciel.agendador_backend.feature.agendamento.infrastructure.lock.AgendaWriteLock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AprovarAgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;

@ExtendWith(MockitoExtension.class)
class AprovarAgendamentoUseCaseTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendaWriteLock agendaWriteLock;

    private AprovarAgendamentoUseCase aprobarAgendamentoUseCase;

    @BeforeEach
    void setUp() {
        aprobarAgendamentoUseCase = new AprovarAgendamentoUseCase(agendamentoRepository, agendaWriteLock);
    }

    @Test
    void deveAprovarAgendamento() {
        Long id = 1L;
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(1L);

        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));
        servico.setId(1L);
        servico.setAtivo(true);

        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamento.setId(id);
        agendamento.setServicos(Set.of(servico));
        agendamento.setAtivo(true);

        AprovarAgendamentoRequest request = new AprovarAgendamentoRequest(Set.of(1L));

        when(agendamentoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        AgendamentoResponse result = aprobarAgendamentoUseCase.execute(id, request);

        assertNotNull(result);
    }
    @Test
    void naoDeveAprovarAgendamentoJaRejeitado() {
        Usuario usuario = new Usuario("rejeitado", "senha", "Rejeitado");
        usuario.setId(2L);
        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamento.setId(2L);
        agendamento.setStatus(StatusAgendamento.REJEITADO);
        when(agendamentoRepository.findByIdAndAtivoTrue(2L)).thenReturn(Optional.of(agendamento));

        assertThrows(BusinessException.class,
                () -> aprobarAgendamentoUseCase.execute(2L, new AprovarAgendamentoRequest(Set.of(1L))));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void naoDeveAprovarAgendamentoJaAprovado() {
        Usuario usuario = new Usuario("aprovado", "senha", "Aprovado");
        usuario.setId(3L);
        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamento.setId(3L);
        agendamento.setStatus(StatusAgendamento.APROVADO);
        when(agendamentoRepository.findByIdAndAtivoTrue(3L)).thenReturn(Optional.of(agendamento));

        assertThrows(BusinessException.class,
                () -> aprobarAgendamentoUseCase.execute(3L, new AprovarAgendamentoRequest(Set.of(1L))));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

}
