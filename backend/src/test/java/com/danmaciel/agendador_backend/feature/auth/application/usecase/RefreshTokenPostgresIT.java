package com.danmaciel.agendador_backend.feature.auth.application.usecase;

import com.danmaciel.agendador_backend.feature.auth.domain.entity.RefreshToken;
import com.danmaciel.agendador_backend.feature.auth.domain.repository.RefreshTokenRepository;
import com.danmaciel.agendador_backend.feature.auth.domain.service.RefreshTokenFingerprint;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PostgreSQL/Flyway integration, executed by the opt-in postgres-it Maven profile.
 * Distinct Spring transactions contend for one persisted refresh-token row.
 */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.h2.console.enabled=false"
})
@Testcontainers
class RefreshTokenPostgresIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("agendador_test")
            .withUsername("test_user")
            .withPassword("test_password_local_only");

    @Autowired
    private UsuarioRepository users;

    @Autowired
    private RefreshTokenRepository tokens;

    @Autowired
    private RefreshTokenUseCase refresh;

    @Test
    void sameRefreshSecretHasOnlyOneSuccessfulSuccessorAcrossTransactions() throws Exception {
        Usuario account = users.saveAndFlush(new Usuario(
                "postgres_concurrency_" + UUID.randomUUID(), "not-a-real-password", "Test account"));
        RefreshToken stored = tokens.saveAndFlush(new RefreshToken(account, 60));
        String oldSecret = stored.getIssuedSecret();
        String oldFingerprint = RefreshTokenFingerprint.fingerprint(oldSecret);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        var workers = Executors.newFixedThreadPool(2);
        try {
            Callable<String> attempt = () -> {
                ready.countDown();
                if (!start.await(15, TimeUnit.SECONDS)) {
                    throw new AssertionError("Concurrent test barrier expired");
                }
                try {
                    return refresh.execute(oldSecret).refreshToken();
                } catch (RuntimeException rejected) {
                    return null;
                }
            };
            var first = workers.submit(attempt);
            var second = workers.submit(attempt);
            assertTrue(ready.await(15, TimeUnit.SECONDS), "Both workers must reach the barrier");
            start.countDown();

            String firstSuccessor = first.get(30, TimeUnit.SECONDS);
            String secondSuccessor = second.get(30, TimeUnit.SECONDS);
            assertEquals(1, (firstSuccessor != null ? 1 : 0)
                    + (secondSuccessor != null ? 1 : 0),
                    "Exactly one transaction may mint a successor");
            String successor = firstSuccessor != null ? firstSuccessor : secondSuccessor;
            assertFalse(tokens.findByToken(oldFingerprint).isPresent());
            assertTrue(tokens.findByToken(RefreshTokenFingerprint.fingerprint(successor)).isPresent());
            assertEquals(1L, tokens.count(), "Only the single successor row should remain");
        } finally {
            start.countDown();
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
