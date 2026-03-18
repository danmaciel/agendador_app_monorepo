package com.danmaciel.agendador_backend.feature.servico.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.danmaciel.agendador_backend.feature.servico.application.dto.ServicoResponse;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;

@ExtendWith(MockitoExtension.class)
class ListarServicosUseCaseTest {

    @Mock
    private ServicoRepository servicoRepository;

    private ListarServicosUseCase listarServicosUseCase;

    @BeforeEach
    void setUp() {
        listarServicosUseCase = new ListarServicosUseCase(servicoRepository);
    }

    @Test
    void deveRetornarListaDeServicos() {
        // Arrange
        Servico servico1 = new Servico("Corte de cabelo", "Corte masculino", new BigDecimal("50.00"));
        servico1.setId(1L);

        Servico servico2 = new Servico("Barba", "Barba modelada", new BigDecimal("30.00"));
        servico2.setId(2L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Servico> page = new PageImpl<>(List.of(servico1, servico2), pageable, 2);

        when(servicoRepository.findAllAtivos(pageable)).thenReturn(page);

        // Act
        Page<ServicoResponse> result = listarServicosUseCase.execute(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistirServicos() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Servico> page = new PageImpl<>(List.of(), pageable, 0);

        when(servicoRepository.findAllAtivos(pageable)).thenReturn(page);

        // Act
        Page<ServicoResponse> result = listarServicosUseCase.execute(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }
}
