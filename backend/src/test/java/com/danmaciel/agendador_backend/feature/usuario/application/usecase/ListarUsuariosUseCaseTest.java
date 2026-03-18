package com.danmaciel.agendador_backend.feature.usuario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

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

import com.danmaciel.agendador_backend.feature.usuario.application.dto.UsuarioResponse;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Role;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ListarUsuariosUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private ListarUsuariosUseCase listarUsuariosUseCase;

    @BeforeEach
    void setUp() {
        listarUsuariosUseCase = new ListarUsuariosUseCase(usuarioRepository);
    }

    @Test
    void deveRetornarListaDeUsuarios() {
        // Arrange
        Usuario usuario1 = new Usuario("joao", "senha", "João");
        usuario1.setId(1L);
        usuario1.addRole(Role.ROLE_CLIENTE);

        Usuario usuario2 = new Usuario("maria", "senha", "Maria");
        usuario2.setId(2L);
        usuario2.addRole(Role.ROLE_CLIENTE);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> page = new PageImpl<>(List.of(usuario1, usuario2), pageable, 2);

        when(usuarioRepository.findAllAtivos(pageable)).thenReturn(page);

        // Act
        Page<UsuarioResponse> result = listarUsuariosUseCase.execute(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistirUsuarios() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> page = new PageImpl<>(List.of(), pageable, 0);

        when(usuarioRepository.findAllAtivos(pageable)).thenReturn(page);

        // Act
        Page<UsuarioResponse> result = listarUsuariosUseCase.execute(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }
}
