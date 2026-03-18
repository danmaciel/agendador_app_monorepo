package com.danmaciel.agendador_backend.feature.usuario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.danmaciel.agendador_backend.feature.usuario.application.dto.UsuarioRequest;
import com.danmaciel.agendador_backend.feature.usuario.application.dto.UsuarioResponse;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class CriarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CriarUsuarioUseCase criarUsuarioUseCase;

    @BeforeEach
    void setUp() {
        criarUsuarioUseCase = new CriarUsuarioUseCase(usuarioRepository, passwordEncoder);
    }

    @Test
    void deveCriarUsuarioQuandoDadosValidos() {
        // Arrange
        UsuarioRequest request = new UsuarioRequest("joao", "senha123", "João Silva");
        Usuario usuario = new Usuario("joao", "senhaEncoded", "João Silva");
        usuario.setId(1L);

        when(usuarioRepository.existsByLoginAndAtivoTrue("joao")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senhaEncoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        UsuarioResponse result = criarUsuarioUseCase.execute(request);

        // Assert
        assertNotNull(result);
        assertEquals("joao", result.login());
        assertEquals("João Silva", result.nome());
    }

    @Test
    void deveLancarExcecaoQuandoLoginJaExistir() {
        // Arrange
        UsuarioRequest request = new UsuarioRequest("admin", "senha123", "Admin");

        when(usuarioRepository.existsByLoginAndAtivoTrue("admin")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> criarUsuarioUseCase.execute(request));
    }
}
