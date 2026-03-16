package com.danmaciel.agendador_backend.feature.servico.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ServicoTest {

    @Test
    void deveCriarServico() {
        // Act
        Servico servico = new Servico("Corte de cabelo", "Corte masculino", new BigDecimal("50.00"));

        // Assert
        assertEquals("Corte de cabelo", servico.getNome());
        assertEquals("Corte masculino", servico.getDescricao());
        assertEquals(new BigDecimal("50.00"), servico.getValor());
    }

    @Test
    void deveAlterarDados() {
        // Arrange
        Servico servico = new Servico("Corte", "Corte", new BigDecimal("40.00"));

        // Act
        servico.setNome("Corte atualizado");
        servico.setDescricao("Descrição atualizada");
        servico.setValor(new BigDecimal("60.00"));

        // Assert
        assertEquals("Corte atualizado", servico.getNome());
        assertEquals("Descrição atualizada", servico.getDescricao());
        assertEquals(new BigDecimal("60.00"), servico.getValor());
    }
}
