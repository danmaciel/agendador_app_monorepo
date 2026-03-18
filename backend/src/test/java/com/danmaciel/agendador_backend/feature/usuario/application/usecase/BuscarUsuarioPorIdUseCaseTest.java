package com.danmaciel.agendador_backend.feature.usuario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.danmaciel.agendador_backend.feature.usuario.application.dto.UsuarioResponse;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Role;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class BuscarUsuarioPorIdUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private BuscarUsuarioPorIdUseCase buscarUsuarioPorIdUseCase;

    @BeforeEach
    void setUp() {
        buscarUsuarioPorIdUseCase = new BuscarUsuarioPorIdUseCase(usuarioRepository);
    }

    @Test
    void deveRetornarUsuarioQuandoEncontrado() {
        // Arrange
        Long id = 1L;
        Usuario usuario = new Usuario("joao", "senha", "João Silva");
        usuario.setId(id);
        usuario.addRole(Role.ROLE_CLIENTE);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        // Act
        UsuarioResponse result = buscarUsuarioPorIdUseCase.execute(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("joao", result.login());
    }

    @Test
    void deveLancarExcecaoQuandoNaoEncontrado() {
        // Arrange
        Long id = 999L;

        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNaoEncontradoException.class, () -> buscarUsuarioPorIdUseCase.execute(id));
    }
}
