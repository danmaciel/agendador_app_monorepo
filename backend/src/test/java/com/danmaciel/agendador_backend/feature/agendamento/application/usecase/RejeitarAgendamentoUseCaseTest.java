package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;

@ExtendWith(MockitoExtension.class)
class RejeitarAgendamentoUseCaseTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    private RejeitarAgendamentoUseCase rejeitarAgendamentoUseCase;

    @BeforeEach
    void setUp() {
        rejeitarAgendamentoUseCase = new RejeitarAgendamentoUseCase(agendamentoRepository);
    }

    @Test
    void deveRejeitarAgendamento() {
        Long id = 1L;
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(1L);

        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));

        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamento.setId(id);
        agendamento.setServicos(Set.of(servico));

        when(agendamentoRepository.findById(id)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        AgendamentoResponse result = rejeitarAgendamentoUseCase.execute(id);

        assertNotNull(result);
    }
}
