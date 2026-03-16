package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

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

@ExtendWith(MockitoExtension.class)
class CriarServicoUseCaseTest {

    @Mock
    private ServicoRepository servicoRepository;

    private CriarServicoUseCase criarServicoUseCase;

    @BeforeEach
    void setUp() {
        criarServicoUseCase = new CriarServicoUseCase(servicoRepository);
    }

    @Test
    void deveCriarServicoQuandoDadosValidos() {
        // Arrange
        ServicoRequest request = new ServicoRequest("Corte de cabelo", "Corte masculino", new BigDecimal("50.00"), 30);
        Servico servico = new Servico("Corte de cabelo", "Corte masculino", new BigDecimal("50.00"), 30);
        servico.setId(1L);

        when(servicoRepository.existsByNome("Corte de cabelo")).thenReturn(false);
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        // Act
        ServicoResponse result = criarServicoUseCase.execute(request);

        // Assert
        assertNotNull(result);
        assertEquals("Corte de cabelo", result.nome());
        assertEquals(new BigDecimal("50.00"), result.valor());
        assertEquals(30, result.tempoExecucao());
    }

    @Test
    void deveLancarExcecaoQuandoNomeJaExistir() {
        // Arrange
        ServicoRequest request = new ServicoRequest("Corte de cabelo", "Corte masculino", new BigDecimal("50.00"), 30);

        when(servicoRepository.existsByNome("Corte de cabelo")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> criarServicoUseCase.execute(request));
    }
}
