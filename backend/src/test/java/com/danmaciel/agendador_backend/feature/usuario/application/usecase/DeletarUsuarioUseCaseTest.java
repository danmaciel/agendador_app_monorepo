package com.danmaciel.agendador_backend.feature.usuario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class DeletarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private DeletarUsuarioUseCase deletarUsuarioUseCase;

    @BeforeEach
    void setUp() {
        deletarUsuarioUseCase = new DeletarUsuarioUseCase(usuarioRepository);
    }

    @Test
    void deveDeletarUsuarioQuandoExistir() {
        // Arrange
        Long id = 1L;

        when(usuarioRepository.existsById(id)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(id);

        // Act
        deletarUsuarioUseCase.execute(id);

        // Assert
        verify(usuarioRepository).deleteById(id);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistir() {
        // Arrange
        Long id = 999L;

        when(usuarioRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> deletarUsuarioUseCase.execute(id));
    }
}
