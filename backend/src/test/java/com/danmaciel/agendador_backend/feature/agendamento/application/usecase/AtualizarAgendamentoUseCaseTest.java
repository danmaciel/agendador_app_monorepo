package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.shared.exception.AlteracaoForaDoPrazoException;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class AtualizarAgendamentoUseCaseTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    private AtualizarAgendamentoUseCase atualizarAgendamentoUseCase;

    @BeforeEach
    void setUp() {
        atualizarAgendamentoUseCase = new AtualizarAgendamentoUseCase(agendamentoRepository, servicoRepository);
    }

    @Test
    void deveAtualizarAgendamentoQuandoDatosValidos() {
        // Arrange
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

        AgendamentoRequest request = new AgendamentoRequest(1L, LocalDate.now().plusDays(10), LocalTime.of(14, 0),
                Set.of(1L));

        when(agendamentoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(agendamento));
        when(servicoRepository.findAllByIdAndAtivoTrue(new java.util.HashSet<>(Set.of(1L)))).thenReturn(Set.of(servico));
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        // Act
        AgendamentoResponse result = atualizarAgendamentoUseCase.execute(id, request);

        // Assert
        assertNotNull(result);
    }

    @Test
    void deveLancarExcecaoQuandoAgendamentoNaoExistir() {
        // Arrange
        Long id = 999L;
        AgendamentoRequest request = new AgendamentoRequest(1L, LocalDate.now().plusDays(10), LocalTime.of(14, 0),
                Set.of(1L));

        when(agendamentoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> atualizarAgendamentoUseCase.execute(id, request));
    }

    @Test
    void deveLancarExcecaoQuandoDataVencida() {
        // Arrange
        Long id = 1L;
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(1L);

        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));

        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(1), LocalTime.of(10, 0));
        agendamento.setId(id);
        agendamento.setServicos(Set.of(servico));
        agendamento.setAtivo(true);

        AgendamentoRequest request = new AgendamentoRequest(1L, LocalDate.now().plusDays(10), LocalTime.of(14, 0),
                Set.of(1L));

        when(agendamentoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(agendamento));

        // Act & Assert
        assertThrows(AlteracaoForaDoPrazoException.class, () -> atualizarAgendamentoUseCase.execute(id, request));
    }
}
