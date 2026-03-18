package com.danmaciel.agendador_backend.feature.usuario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

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
        Usuario usuario = new Usuario("login", "senha", "Nome");
        usuario.setId(id);
        usuario.setAtivo(true);

        when(usuarioRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        // Act
        deletarUsuarioUseCase.execute(id);

        // Assert
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistir() {
        // Arrange
        Long id = 999L;

        when(usuarioRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> deletarUsuarioUseCase.execute(id));
    }
}
