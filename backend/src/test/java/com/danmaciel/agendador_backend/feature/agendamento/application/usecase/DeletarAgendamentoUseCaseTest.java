package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class DeletarAgendamentoUseCaseTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    private DeletarAgendamentoUseCase deletarAgendamentoUseCase;

    @BeforeEach
    void setUp() {
        deletarAgendamentoUseCase = new DeletarAgendamentoUseCase(agendamentoRepository);
    }

    @Test
    void deveDeletarAgendamentoQuandoExistir() {
        // Arrange
        Long id = 1L;
        Usuario usuario = new Usuario("joao", "senha", "João");
        usuario.setId(1L);
        
        Agendamento agendamento = new Agendamento(usuario, LocalDate.now().plusDays(5), LocalTime.of(10, 0));
        agendamento.setId(id);
        agendamento.setAtivo(true);

        when(agendamentoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        // Act
        deletarAgendamentoUseCase.execute(id);

        // Assert
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    void deveLancarExcecaoQuandoNaoExistir() {
        // Arrange
        Long id = 999L;

        when(agendamentoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> deletarAgendamentoUseCase.execute(id));
    }
}
