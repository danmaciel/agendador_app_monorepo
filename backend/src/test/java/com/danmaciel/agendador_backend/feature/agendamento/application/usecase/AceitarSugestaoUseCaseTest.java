package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

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
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.agendamento.infrastructure.lock.AgendaWriteLock;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;
import com.danmaciel.agendador_backend.shared.exception.AgendamentoConflitoException;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class AceitarDataSugestaoUseCaseTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendaWriteLock agendaWriteLock;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicoRepository servicoRepository;

    private AceitarDataSugestaoUseCase aceitarSugestaoUseCase;

    @BeforeEach
    void setUp() {
        aceitarSugestaoUseCase = new AceitarDataSugestaoUseCase(agendamentoRepository, usuarioRepository,
                servicoRepository, agendaWriteLock);
    }

    @Test
    void deveAdicionarServicosAoAgendamentoExistente() {
        // Arrange
        Long usuarioId = 1L;
        Long servicoId1 = 1L;
        Long servicoId2 = 2L;
        LocalDate data = LocalDate.now().plusDays(5);

        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        Servico servico1 = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));
        servico1.setId(servicoId1);

        Servico servico2 = new Servico("Barba", "Barba modelada", new BigDecimal("30.00"));
        servico2.setId(servicoId2);

        Agendamento agendamentoExistente = new Agendamento(usuario, data, LocalTime.of(10, 0));
        agendamentoExistente.setId(1L);
        agendamentoExistente.setServicos(new HashSet<>(Set.of(servico1)));
        agendamentoExistente.setStatus(StatusAgendamento.PENDENTE);

        AgendamentoRequest request = new AgendamentoRequest(usuarioId, data, LocalTime.of(14, 0), Set.of(servicoId2));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(servicoId2)))).thenReturn(Set.of(servico2));
        when(agendamentoRepository.findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(eq(usuarioId), any(), any(), eq(StatusAgendamento.PENDENTE)))
                .thenReturn(List.of(agendamentoExistente));
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamentoExistente);

        // Act
        AgendamentoResponse result = aceitarSugestaoUseCase.execute(request);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.servicos().size());
    }

    @Test
    void naoDeveEstenderAgendamentoQuandoNovoTempoCausarSobreposicao() {
        Long usuarioId = 1L;
        LocalDate data = LocalDate.now().plusDays(5);
        Usuario usuario = new Usuario("joao", "senha", "Joao");
        usuario.setId(usuarioId);
        Usuario outroUsuario = new Usuario("maria", "senha", "Maria");
        outroUsuario.setId(2L);
        Servico atual = new Servico("Corte", "Corte", new BigDecimal("30.00"), 30);
        atual.setId(1L);
        Servico novo = new Servico("Barba", "Barba", new BigDecimal("20.00"), 30);
        novo.setId(2L);
        Agendamento existente = new Agendamento(usuario, data, LocalTime.of(10, 0));
        existente.setId(10L);
        existente.setServicos(new HashSet<>(Set.of(atual)));
        existente.setTempoTotal(30);
        Agendamento outro = new Agendamento(outroUsuario, data, LocalTime.of(10, 45));
        outro.setId(11L);
        outro.setTempoTotal(30);
        AgendamentoRequest request = new AgendamentoRequest(
                usuarioId, data, LocalTime.of(14, 0), Set.of(2L));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(2L))))
                .thenReturn(Set.of(novo));
        when(agendamentoRepository.findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(
                eq(usuarioId), any(), any(), eq(StatusAgendamento.PENDENTE)))
                .thenReturn(List.of(existente));
        when(agendamentoRepository.findByDataAndAtivoTrue(data))
                .thenReturn(List.of(existente, outro));

        assertThrows(AgendamentoConflitoException.class, () -> aceitarSugestaoUseCase.execute(request));
        assertEquals(30, existente.getTempoTotal());
        assertEquals(1, existente.getServicos().size());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void permiteEstenderAgendamentoAteInicioDeOutroHorario() {
        Long usuarioId = 1L;
        LocalDate data = LocalDate.now().plusDays(5);
        Usuario usuario = new Usuario("joao", "senha", "Joao");
        usuario.setId(usuarioId);
        Usuario outroUsuario = new Usuario("maria", "senha", "Maria");
        outroUsuario.setId(2L);
        Servico atual = new Servico("Corte", "Corte", new BigDecimal("30.00"), 30);
        atual.setId(1L);
        Servico novo = new Servico("Barba", "Barba", new BigDecimal("20.00"), 30);
        novo.setId(2L);
        Agendamento existente = new Agendamento(usuario, data, LocalTime.of(10, 0));
        existente.setId(10L);
        existente.setServicos(new HashSet<>(Set.of(atual)));
        existente.setTempoTotal(30);
        Agendamento outro = new Agendamento(outroUsuario, data, LocalTime.of(11, 0));
        outro.setId(11L);
        outro.setTempoTotal(30);
        AgendamentoRequest request = new AgendamentoRequest(
                usuarioId, data, LocalTime.of(14, 0), Set.of(2L));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(2L))))
                .thenReturn(Set.of(novo));
        when(agendamentoRepository.findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(
                eq(usuarioId), any(), any(), eq(StatusAgendamento.PENDENTE)))
                .thenReturn(List.of(existente));
        when(agendamentoRepository.findByDataAndAtivoTrue(data))
                .thenReturn(List.of(existente, outro));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoResponse response = aceitarSugestaoUseCase.execute(request);
        assertEquals(60, response.tempoTotal());
        assertEquals(2, response.servicos().size());
    }

    @Test
    void deveLancarExcecaoQuandoNenhumAgendamentoExistir() {
        // Arrange
        Long usuarioId = 1L;
        Long servicoId = 1L;
        LocalDate data = LocalDate.now().plusDays(5);

        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));
        servico.setId(servicoId);

        AgendamentoRequest request = new AgendamentoRequest(usuarioId, data, LocalTime.of(14, 0), Set.of(servicoId));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(servicoId)))).thenReturn(Set.of(servico));
        when(agendamentoRepository.findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(eq(usuarioId), any(), any(), eq(StatusAgendamento.PENDENTE)))
                .thenReturn(List.of());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> aceitarSugestaoUseCase.execute(request));
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // Arrange
        Long usuarioId = 999L;
        AgendamentoRequest request = new AgendamentoRequest(usuarioId, LocalDate.now().plusDays(5),
                LocalTime.of(14, 0), Set.of(1L));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> aceitarSugestaoUseCase.execute(request));
    }
    @Test
    void naoDeveEscolherArbitrariamenteEntreMultiplosPendentes() {
        Long usuarioId = 1L;
        LocalDate data = LocalDate.now().plusWeeks(2);
        Usuario usuario = new Usuario("joao", "senha", "Joao");
        usuario.setId(usuarioId);

        Servico novo = new Servico("Barba", "Barba", new BigDecimal("20.00"), 30);
        novo.setId(2L);
        Agendamento primeiro = new Agendamento(usuario, data, LocalTime.of(9, 0));
        primeiro.setId(10L);
        Agendamento segundo = new Agendamento(usuario, data.plusDays(1), LocalTime.of(10, 0));
        segundo.setId(11L);

        AgendamentoRequest request = new AgendamentoRequest(
                usuarioId, data, LocalTime.of(14, 0), Set.of(2L));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(Set.of(2L))).thenReturn(Set.of(novo));
        when(agendamentoRepository.findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(
                eq(usuarioId), any(), any(), eq(StatusAgendamento.PENDENTE)))
                .thenReturn(List.of(primeiro, segundo));

        BusinessException erro = assertThrows(
                BusinessException.class, () -> aceitarSugestaoUseCase.execute(request));

        assertEquals("Há mais de um agendamento pendente nesta semana; não é possível identificar qual deve ser atualizado",
                erro.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
        assertEquals(Set.of(), primeiro.getServicos());
        assertEquals(Set.of(), segundo.getServicos());
    }

}
