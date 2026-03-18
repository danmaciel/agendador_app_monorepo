package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoRequest;
import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class AtualizarServicoUseCaseTest {

    @Mock
    private ServicoRepository servicoRepository;

    private AtualizarServicoUseCase atualizarServicoUseCase;

    @BeforeEach
    void setUp() {
        atualizarServicoUseCase = new AtualizarServicoUseCase(servicoRepository);
    }

    @Test
    void deveAtualizarServicoQuandoExistir() {
        // Arrange
        Long id = 1L;
        Servico servico = new Servico("Corte velho", "Corte antigo", new BigDecimal("40.00"), 30);
        servico.setId(id);
        servico.setAtivo(true);

        ServicoRequest request = new ServicoRequest("Corte novo", "Corte atualizado", new BigDecimal("60.00"), 45);

        when(servicoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(servico));
        when(servicoRepository.existsByNomeAndAtivoTrue("Corte novo")).thenReturn(false);
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        // Act
        ServicoResponse result = atualizarServicoUseCase.execute(id, request);

        // Assert
        assertNotNull(result);
    }

    @Test
    void deveLancarExcecaoQuandoNaoExistir() {
        // Arrange
        Long id = 999L;
        ServicoRequest request = new ServicoRequest("Corte novo", "Corte atualizado", new BigDecimal("60.00"), 45);

        when(servicoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> atualizarServicoUseCase.execute(id, request));
    }

    @Test
    void deveLancarExcecaoQuandoNomeJaExistir() {
        // Arrange
        Long id = 1L;
        Servico servico = new Servico("Corte_velho", "Corte antigo", new BigDecimal("40.00"), 30);
        servico.setId(1L);
        servico.setAtivo(true);

        ServicoRequest request = new ServicoRequest("Corte_novo", "Corte atualizado", new BigDecimal("60.00"), 45);

        when(servicoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(servico));
        when(servicoRepository.existsByNomeAndAtivoTrue("Corte_novo")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> atualizarServicoUseCase.execute(id, request));
    }
}
