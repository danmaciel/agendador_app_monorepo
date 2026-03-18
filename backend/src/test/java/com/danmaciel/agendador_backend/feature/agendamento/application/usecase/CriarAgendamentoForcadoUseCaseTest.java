package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoResponse;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class CriarAgendamentoForcadoUseCaseTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicoRepository servicoRepository;

    private CriarAgendamentoForcadoUseCase criarAgendamentoForcadoUseCase;

    @BeforeEach
    void setUp() {
        criarAgendamentoForcadoUseCase = new CriarAgendamentoForcadoUseCase(agendamentoRepository,
                usuarioRepository, servicoRepository);
    }

    @Test
    void deveCriarAgendamentoForcadoQuandoDadosValidos() {
        // Arrange
        Long usuarioId = 1L;
        Long servicoId = 1L;
        LocalDate data = LocalDate.now().plusDays(5);
        LocalTime horario = LocalTime.of(10, 0);

        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        Servico servico = new Servico("Corte", "Corte masculino", new BigDecimal("50.00"));
        servico.setId(servicoId);

        AgendamentoRequest request = new AgendamentoRequest(usuarioId, data, horario, Set.of(servicoId));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(servicoId)))).thenReturn(Set.of(servico));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(i -> {
            Agendamento a = i.getArgument(0);
            a.setId(1L);
            return a;
        });

        // Act
        AgendamentoResponse result = criarAgendamentoForcadoUseCase.execute(request);

        // Assert
        assertNotNull(result);
        assertEquals(usuarioId, result.usuarioId());
        assertEquals(data, result.data());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // Arrange
        Long usuarioId = 999L;
        AgendamentoRequest request = new AgendamentoRequest(usuarioId, LocalDate.now().plusDays(5),
                LocalTime.of(10, 0), Set.of(1L));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> criarAgendamentoForcadoUseCase.execute(request));
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoEncontrado() {
        // Arrange
        Long usuarioId = 1L;
        Long servicoId = 999L;

        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(usuarioId);

        AgendamentoRequest request = new AgendamentoRequest(usuarioId, LocalDate.now().plusDays(5),
                LocalTime.of(10, 0), Set.of(servicoId));

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(servicoRepository.findAllByIdAndAtivoTrue(new HashSet<>(Set.of(servicoId)))).thenReturn(Set.of());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> criarAgendamentoForcadoUseCase.execute(request));
    }
}
