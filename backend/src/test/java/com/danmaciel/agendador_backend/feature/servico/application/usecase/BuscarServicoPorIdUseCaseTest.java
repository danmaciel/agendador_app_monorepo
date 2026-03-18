package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class BuscarServicoPorIdUseCaseTest {

    @Mock
    private ServicoRepository servicoRepository;

    private BuscarServicoPorIdUseCase buscarServicoPorIdUseCase;

    @BeforeEach
    void setUp() {
        buscarServicoPorIdUseCase = new BuscarServicoPorIdUseCase(servicoRepository);
    }

    @Test
    void deveRetornarServicoQuandoEncontrado() {
        // Arrange
        Long id = 1L;
        Servico servico = new Servico("Corte de cabelo", "Corte masculino", new BigDecimal("50.00"));
        servico.setId(id);
        servico.setAtivo(true);

        when(servicoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(servico));

        // Act
        ServicoResponse result = buscarServicoPorIdUseCase.execute(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("Corte de cabelo", result.nome());
    }

    @Test
    void deveLancarExcecaoQuandoNaoEncontrado() {
        // Arrange
        Long id = 999L;

        when(servicoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> buscarServicoPorIdUseCase.execute(id));
    }
}
