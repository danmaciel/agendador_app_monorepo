package com.danmaciel.agendador_backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression guard: public signup must not make user data publicly readable.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserRouteSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void anonymousUserCannotReadAnotherAccountById() throws Exception {
        mockMvc.perform(get("/api/v1/usuario/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousUserCannotEnumerateAccounts() throws Exception {
        mockMvc.perform(get("/api/v1/usuario"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void authenticatedUserCanReadOwnProfile() throws Exception {
        Usuario owner = createUser("owner");
        mockMvc.perform(get("/api/v1/usuario/{id}", owner.getId())
                        .with(user(owner.getLogin()).authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserCannotReadAnotherProfile() throws Exception {
        Usuario owner = createUser("owner");
        Usuario other = createUser("other");
        mockMvc.perform(get("/api/v1/usuario/{id}", other.getId())
                        .with(user(owner.getLogin()).authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanReadAnotherProfile() throws Exception {
        Usuario other = createUser("other");
        mockMvc.perform(get("/api/v1/usuario/{id}", other.getId())
                        .with(user("administrador").authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    private Usuario createUser(String prefix) {
        return usuarioRepository.saveAndFlush(
                new Usuario(prefix + "_" + UUID.randomUUID(), "test-only-hash", "Pessoa de teste"));
    }

}
