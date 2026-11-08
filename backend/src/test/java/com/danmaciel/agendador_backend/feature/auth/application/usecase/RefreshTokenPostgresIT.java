package com.danmaciel.agendador_backend.feature.auth.application.usecase;

import com.danmaciel.agendador_backend.feature.auth.domain.entity.RefreshToken;
import com.danmaciel.agendador_backend.feature.auth.application.dto.LoginRequest;
import com.danmaciel.agendador_backend.shared.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
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

    @Autowired
    private LoginUseCase login;

    @Autowired
    private PasswordEncoder passwords;

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
            assertEquals(1L, tokens.countByUsuarioId(account.getId()),
                    "Only the single successor row should remain for the account");
        } finally {
            start.countDown();
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
    @Test
    void newLoginWinsOverConcurrentRefreshAndRemainsOnlyValidSession() throws Exception {
        String username = "login_refresh_" + UUID.randomUUID();
        String password = "local-test-password-not-for-production";
        Usuario account = users.saveAndFlush(new Usuario(
                username, passwords.encode(password), "Concurrent test account"));
        RefreshToken existing = tokens.saveAndFlush(new RefreshToken(account, 60));
        String oldSecret = existing.getIssuedSecret();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        var workers = Executors.newFixedThreadPool(2);
        try {
            Callable<String> doLogin = () -> {
                ready.countDown();
                if (!start.await(15, TimeUnit.SECONDS)) {
                    throw new AssertionError("Login barrier expired");
                }
                return login.execute(new LoginRequest(username, password)).refreshToken();
            };
            Callable<String> doRefresh = () -> {
                ready.countDown();
                if (!start.await(15, TimeUnit.SECONDS)) {
                    throw new AssertionError("Refresh barrier expired");
                }
                try {
                    return refresh.execute(oldSecret).refreshToken();
                } catch (BusinessException rejected) {
                    return null;
                }
            };
            var loginResult = workers.submit(doLogin);
            var refreshResult = workers.submit(doRefresh);
            assertTrue(ready.await(15, TimeUnit.SECONDS));
            start.countDown();

            String loginSecret = loginResult.get(30, TimeUnit.SECONDS);
            String refreshSecret = refreshResult.get(30, TimeUnit.SECONDS);

            assertNotNull(loginSecret);
            assertEquals(1L, tokens.countByUsuarioId(account.getId()),
                    "Only the new-login session may remain");
            assertTrue(tokens.findByToken(
                    RefreshTokenFingerprint.fingerprint(loginSecret)).isPresent());
            assertFalse(tokens.findByToken(
                    RefreshTokenFingerprint.fingerprint(oldSecret)).isPresent());
            if (refreshSecret != null) {
                assertFalse(tokens.findByToken(
                        RefreshTokenFingerprint.fingerprint(refreshSecret)).isPresent(),
                        "An earlier refresh must be superseded by the new login");
            }
        } finally {
            start.countDown();
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

}
