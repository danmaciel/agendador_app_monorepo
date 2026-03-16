package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class DeletarServicoUseCaseTest {

    @Mock
    private ServicoRepository servicoRepository;

    private DeletarServicoUseCase deletarServicoUseCase;

    @BeforeEach
    void setUp() {
        deletarServicoUseCase = new DeletarServicoUseCase(servicoRepository);
    }

    @Test
    void deveDeletarServicoQuandoExistir() {
        // Arrange
        Long id = 1L;

        when(servicoRepository.existsById(id)).thenReturn(true);
        doNothing().when(servicoRepository).deleteById(id);

        // Act
        deletarServicoUseCase.execute(id);

        // Assert
        verify(servicoRepository).deleteById(id);
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoExistir() {
        // Arrange
        Long id = 999L;

        when(servicoRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> deletarServicoUseCase.execute(id));
    }
}
