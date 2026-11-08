package com.danmaciel.agendador_backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression guard: public signup must not make user data publicly readable.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserRouteSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
}
