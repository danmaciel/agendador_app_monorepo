package com.danmaciel.agendador_backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @Autowired
    private AgendamentoRepository agendamentoRepository;

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

    @Test
    void ordinaryUserCannotListPromoteOrDeleteAccounts() throws Exception {
        Usuario account = createUser("ordinary");
        var client = user(account.getLogin()).authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE"));

        mockMvc.perform(get("/api/v1/usuario").with(client))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/usuario/{id}/promover-admin", account.getId())
                        .with(client))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/usuario/{id}", account.getId())
                        .with(client))
                .andExpect(status().isForbidden());

        Usuario stillActive = usuarioRepository.findById(account.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(stillActive.getAtivo());
        org.junit.jupiter.api.Assertions.assertFalse(
                stillActive.getRoles().contains(
                        com.danmaciel.agendador_backend.feature.usuario.domain.entity.Role.ROLE_ADMIN));
    }

    @Test
    void administratorCanListPromoteAndDeleteAccounts() throws Exception {
        Usuario target = createUser("admin_target");
        var admin = user("administrator-test").authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));

        mockMvc.perform(get("/api/v1/usuario").with(admin))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/usuario/{id}/promover-admin", target.getId())
                        .with(admin))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/usuario/{id}", target.getId())
                        .with(admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void clientCannotQueryOtherUsersAppointmentsOrHistory() throws Exception {
        Usuario owner = createUser("appointment_owner");
        Usuario other = createUser("appointment_other");
        var client = user(owner.getLogin()).authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE"));

        mockMvc.perform(get("/api/v1/agendamento/meus-agendamentos")
                        .param("usuarioId", other.getId().toString()).with(client))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/agendamento/historico")
                        .param("usuarioId", other.getId().toString()).with(client))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/agendamento/historico/aprovados-por-dia")
                        .param("usuarioId", other.getId().toString()).with(client))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/agendamento/meus-agendamentos")
                        .param("usuarioId", owner.getId().toString()).with(client))
                .andExpect(status().isOk());
    }

    @Test
    void clientCannotCreateAppointmentsForOtherUsersOrUseForcedRoute() throws Exception {
        Usuario owner = createUser("booking_owner");
        Usuario other = createUser("booking_other");
        var client = user(owner.getLogin()).authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE"));
        String body = "{\"usuarioId\":" + other.getId()
                + ",\"data\":\"2099-01-01\",\"horario\":\"14:00\",\"servicoIds\":[1]}";

        mockMvc.perform(post("/api/v1/agendamento")
                        .with(client).contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/agendamento/aceitar-data-sugestao")
                        .with(client).contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/agendamento/criar-forcado")
                        .with(client).contentType("application/json").content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void appointmentIdRoutesRejectNonOwnerAndAllowOwnerAndAdmin() throws Exception {
        Usuario owner = createUser("booking_id_owner");
        Usuario other = createUser("booking_id_other");
        Agendamento appointment = agendamentoRepository.saveAndFlush(
                new Agendamento(owner, LocalDate.of(2099, 1, 2), LocalTime.of(14, 0)));
        var otherClient = user(other.getLogin()).authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE"));
        var ownerClient = user(owner.getLogin()).authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE"));
        var admin = user("administrator-test").authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));

        mockMvc.perform(get("/api/v1/agendamento/{id}", appointment.getId()).with(otherClient))
                .andExpect(status().isForbidden());
        String validBody = "{\"usuarioId\":" + owner.getId()
                + ",\"data\":\"2099-01-03\",\"horario\":\"15:00\",\"servicoIds\":[1]}";
        mockMvc.perform(put("/api/v1/agendamento/{id}", appointment.getId())
                        .with(otherClient).contentType("application/json")
                        .content(validBody))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/agendamento/{id}", appointment.getId()).with(ownerClient))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/agendamento/{id}", appointment.getId()).with(admin))
                .andExpect(status().isOk());
    }

    private Usuario createUser(String prefix) {
        return usuarioRepository.saveAndFlush(
                new Usuario(prefix + "_" + UUID.randomUUID(), "test-only-hash", "Pessoa de teste"));
    }

}
