package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import com.danmaciel.agendador_backend.feature.agendamento.infrastructure.lock.AgendaWriteLock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.AgendamentoConflitoException;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class CriarAgendamentoUseCaseTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendaWriteLock agendaWriteLock;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicoRepository servicoRepository;

    private CriarAgendamentoUseCase criarAgendamentoUseCase;

    @BeforeEach
    void setUp() {
        criarAgendamentoUseCase = new CriarAgendamentoUseCase(agendamentoRepository, usuarioRepository,
                servicoRepository, agendaWriteLock);
    }

    @Test
    void deveCriarAgendamentoQuandoDadosValidos() {
        // Arrange
        Long usuarioId = 1L;
        Long servicoId = 1L;
        LocalDate data = LocalDate.now().plusDays(5);
        LocalTime horario = LocalTime.of(10, 0);

        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"), 30);
        servico.setId(servicoId);

        AgendamentoRequest request = new AgendamentoRequest(usuarioId, data, horario, Set.of(servicoId));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(servicoId)))).thenReturn(Set.of(servico));
        when(agendamentoRepository.findByDataAndAtivoTrue(data)).thenReturn(List.of());
        when(agendamentoRepository.findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(eq(usuarioId), any(), any(), eq(StatusAgendamento.PENDENTE)))
                .thenReturn(List.of());
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(i -> {
            Agendamento a = i.getArgument(0);
            a.setId(1L);
            return a;
        });

        // Act
        AgendamentoResponse result = criarAgendamentoUseCase.execute(request);

        // Assert
        assertNotNull(result);
        assertEquals(usuarioId, result.usuarioId());
        assertEquals(data, result.data());
        assertEquals(30, result.tempoTotal());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // Arrange
        Long usuarioId = 999L;
        AgendamentoRequest request = new AgendamentoRequest(usuarioId, LocalDate.now().plusDays(5), LocalTime.of(10, 0),
                Set.of(1L));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> criarAgendamentoUseCase.execute(request));
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoEncontrado() {
        // Arrange
        Long usuarioId = 1L;
        Long servicoId = 999L;

        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        AgendamentoRequest request = new AgendamentoRequest(usuarioId, LocalDate.now().plusDays(5), LocalTime.of(10, 0),
                Set.of(servicoId));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(servicoId)))).thenReturn(Set.of());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> criarAgendamentoUseCase.execute(request));
    }

    @Test
    void deveLancarExcecaoQuandoHorarioJaOcupadoPorOutroCliente() {
        // Arrange
        Long usuarioId = 1L;
        Long servicoId = 1L;
        LocalDate data = LocalDate.now().plusDays(5);
        LocalTime horario = LocalTime.of(10, 0);

        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"), 30);
        servico.setId(servicoId);

        Usuario usuario2 = new Usuario("maria", "senha", "Maria");
        usuario2.setId(2L);
        
        Agendamento agendamentoExistente = new Agendamento(usuario2, data, horario);
        agendamentoExistente.setId(1L);
        agendamentoExistente.setTempoTotal(30);

        AgendamentoRequest request = new AgendamentoRequest(usuarioId, data, horario, Set.of(servicoId));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(servicoId)))).thenReturn(Set.of(servico));
        when(agendamentoRepository.findByDataAndAtivoTrue(data)).thenReturn(List.of(agendamentoExistente));

        // Act & Assert
        AgendamentoConflitoException ex = assertThrows(AgendamentoConflitoException.class,
                () -> criarAgendamentoUseCase.execute(request));
        assertEquals("Já existe agendamento para este horário", ex.getMessage());
    }

    @Test
    void deveRejeitarSobreposicaoParcialDeIntervalos() {
        Long usuarioId = 1L;
        Long servicoId = 1L;
        LocalDate data = LocalDate.now().plusDays(5);
        Usuario solicitante = new Usuario("solicitante", "senha", "Solicitante");
        solicitante.setId(usuarioId);
        Usuario outro = new Usuario("outro", "senha", "Outro");
        outro.setId(2L);

        Servico servico = new Servico("Corte", "Corte", new BigDecimal("50.00"), 30);
        servico.setId(servicoId);
        Agendamento existente = new Agendamento(outro, data, LocalTime.of(10, 0));
        existente.setId(77L);
        existente.setTempoTotal(30);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(solicitante));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(servicoId))))
                .thenReturn(Set.of(servico));
        when(agendamentoRepository.findByDataAndAtivoTrue(data))
                .thenReturn(List.of(existente));

        // 10:15–10:45 conflicts with the existing 10:00–10:30 interval.
        AgendamentoRequest request = new AgendamentoRequest(
                usuarioId, data, LocalTime.of(10, 15), Set.of(servicoId));
        assertThrows(AgendamentoConflitoException.class,
                () -> criarAgendamentoUseCase.execute(request));
    }

    @Test
    void devePermitirHorarioAdjacenteSemSobreposicao() {
        Long usuarioId = 1L;
        Long servicoId = 1L;
        LocalDate data = LocalDate.now().plusDays(5);
        Usuario solicitante = new Usuario("solicitante", "senha", "Solicitante");
        solicitante.setId(usuarioId);
        Usuario outro = new Usuario("outro", "senha", "Outro");
        outro.setId(2L);

        Servico servico = new Servico("Corte", "Corte", new BigDecimal("50.00"), 30);
        servico.setId(servicoId);
        Agendamento existente = new Agendamento(outro, data, LocalTime.of(10, 0));
        existente.setId(77L);
        existente.setTempoTotal(30);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(solicitante));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(servicoId))))
                .thenReturn(Set.of(servico));
        when(agendamentoRepository.findByDataAndAtivoTrue(data))
                .thenReturn(List.of(existente));
        when(agendamentoRepository.findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(
                eq(usuarioId), any(), any(), eq(StatusAgendamento.PENDENTE)))
                .thenReturn(List.of());
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> {
            Agendamento salvo = invocation.getArgument(0);
            salvo.setId(78L);
            return salvo;
        });

        // 10:30–11:00 begins exactly when the existing interval ends.
        AgendamentoRequest request = new AgendamentoRequest(
                usuarioId, data, LocalTime.of(10, 30), Set.of(servicoId));
        AgendamentoResponse result = criarAgendamentoUseCase.execute(request);
        assertEquals(LocalTime.of(10, 30), result.horario());
    }

    @Test
    void deveLancarExcecaoQuandoJaPossuiAgendamentoNaSemana() {
        // Arrange
        Long usuarioId = 1L;
        Long servicoId = 1L;
        LocalDate data = LocalDate.now().plusDays(5);
        LocalTime horario = LocalTime.of(10, 0);

        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"), 30);
        servico.setId(servicoId);

        Agendamento agendamentoExistente = new Agendamento(usuario, data, horario);
        agendamentoExistente.setId(1L);
        agendamentoExistente.setTempoTotal(30);
        agendamentoExistente.setStatus(StatusAgendamento.PENDENTE);

        AgendamentoRequest request = new AgendamentoRequest(usuarioId, data, horario, Set.of(servicoId));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(servicoId)))).thenReturn(Set.of(servico));
        when(agendamentoRepository.findByDataAndAtivoTrue(data)).thenReturn(List.of());
        when(agendamentoRepository.findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(eq(usuarioId), any(), any(), eq(StatusAgendamento.PENDENTE)))
                .thenReturn(List.of(agendamentoExistente));

        // Act & Assert
        AgendamentoConflitoException ex = assertThrows(AgendamentoConflitoException.class,
                () -> criarAgendamentoUseCase.execute(request));
        assertEquals("Você já possui agendamento nesta semana. Data sugerida: " + data, ex.getMessage());
        assertEquals(data, ex.getDataSugerida());
    }
}
