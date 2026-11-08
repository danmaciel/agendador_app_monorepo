package com.danmaciel.agendador_backend.feature.auth.application.usecase;

import com.danmaciel.agendador_backend.feature.auth.domain.entity.RefreshToken;
import com.danmaciel.agendador_backend.feature.auth.domain.repository.RefreshTokenRepository;
import com.danmaciel.agendador_backend.feature.auth.domain.service.RefreshTokenFingerprint;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Two independent transactions against the real local H2 test database.
 * This tests single-winner semantics under concurrent refresh, not PostgreSQL
 * cluster behavior, stolen-token replay detection or refresh families.
 */
@SpringBootTest
class RefreshTokenConcurrentIntegrationTest {

    @Autowired
    private UsuarioRepository users;

    @Autowired
    private RefreshTokenRepository tokens;

    @Autowired
    private RefreshTokenUseCase refreshUseCase;

    @Test
    void onlyOneConcurrentRequestCanConsumeSameRefreshToken() throws Exception {
        Usuario user = users.saveAndFlush(new Usuario(
                "parallel_" + UUID.randomUUID(), "test-hash", "Parallel test"));
        RefreshToken original = tokens.saveAndFlush(new RefreshToken(user, 60));
        String originalSecret = original.getIssuedSecret();
        var start = new CountDownLatch(1);
        var ready = new CountDownLatch(2);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var task = (java.util.concurrent.Callable<String>) () -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new AssertionError("Start barrier timed out");
                }
                try {
                    return refreshUseCase.execute(originalSecret).refreshToken();
                } catch (RuntimeException ex) {
                    return null;
                }
            };
            var first = executor.submit(task);
            var second = executor.submit(task);
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            String firstSecret = first.get(20, TimeUnit.SECONDS);
            String secondSecret = second.get(20, TimeUnit.SECONDS);
            assertEquals(1, (firstSecret != null ? 1 : 0) + (secondSecret != null ? 1 : 0),
                    "Exactly one refresh attempt may produce a successor");
            String successor = firstSecret != null ? firstSecret : secondSecret;
            assertTrue(tokens.findByToken(
                    RefreshTokenFingerprint.fingerprint(successor)).isPresent());
            assertTrue(tokens.findByToken(
                    RefreshTokenFingerprint.fingerprint(originalSecret)).isEmpty());
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
