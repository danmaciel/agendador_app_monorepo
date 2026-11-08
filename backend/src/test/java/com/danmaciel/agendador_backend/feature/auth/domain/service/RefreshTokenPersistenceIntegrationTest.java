package com.danmaciel.agendador_backend.feature.auth.domain.service;

import com.danmaciel.agendador_backend.feature.auth.domain.entity.RefreshToken;
import com.danmaciel.agendador_backend.feature.auth.domain.repository.RefreshTokenRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RefreshTokenPersistenceIntegrationTest {

    @Autowired
    private UsuarioRepository users;

    @Autowired
    private RefreshTokenRepository tokens;

    @Autowired
    private EntityManager entityManager;

    @Test
    void reloadFromDatabaseContainsFingerprintButNeverIssuedSecret() {
        Usuario user = users.saveAndFlush(new Usuario(
                "fingerprint_" + UUID.randomUUID(), "test-hash", "Test user"));
        RefreshToken issued = new RefreshToken(user, 60);
        String raw = issued.getIssuedSecret();
        String fingerprint = RefreshTokenFingerprint.fingerprint(raw);
        tokens.saveAndFlush(issued);
        entityManager.clear();

        RefreshToken reloaded = tokens.findByToken(fingerprint).orElseThrow();
        assertEquals(fingerprint, reloaded.getToken());
        assertNull(reloaded.getIssuedSecret());
        assertFalse(tokens.findByToken(raw).isPresent());
    }
}
