package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.shared.exception.ResourceNotFoundException;

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

        when(agendamentoRepository.existsById(id)).thenReturn(true);
        doNothing().when(agendamentoRepository).deleteById(id);

        // Act
        deletarAgendamentoUseCase.execute(id);

        // Assert
        verify(agendamentoRepository).deleteById(id);
    }

    @Test
    void deveLancarExcecaoQuandoNaoExistir() {
        // Arrange
        Long id = 999L;

        when(agendamentoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> deletarAgendamentoUseCase.execute(id));
    }
}
