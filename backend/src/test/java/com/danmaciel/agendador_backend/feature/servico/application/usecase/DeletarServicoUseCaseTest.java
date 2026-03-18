package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

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
        Servico servico = new Servico("Servico Teste", "Descricao", new java.math.BigDecimal("100.00"));
        servico.setId(id);
        servico.setAtivo(true);

        when(servicoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(servico)).thenReturn(servico);

        // Act
        deletarServicoUseCase.execute(id);

        // Assert
        verify(servicoRepository).save(servico);
    }

    @Test
    void deveLancarExcecaoQuandoServicoNaoExistir() {
        // Arrange
        Long id = 999L;

        when(servicoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> deletarServicoUseCase.execute(id));
    }
}
