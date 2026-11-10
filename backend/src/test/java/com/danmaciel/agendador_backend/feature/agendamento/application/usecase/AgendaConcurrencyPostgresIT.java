package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.AgendamentoConflitoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Two independent requests compete for one slot on a real PostgreSQL database. */
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.h2.console.enabled=false"
})
@Testcontainers
class AgendaConcurrencyPostgresIT {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("agendador_test")
            .withUsername("test_user")
            .withPassword("test_password_local_only");

    @Autowired private UsuarioRepository usuarios;
    @Autowired private ServicoRepository servicos;
    @Autowired private AgendamentoRepository agendamentos;
    @Autowired private CriarAgendamentoUseCase criar;

    @Test
    void onlyOneConcurrentRequestReservesTheSameTimeSlot() throws Exception {
        Usuario firstUser = usuarios.saveAndFlush(
                new Usuario("slot_first_" + UUID.randomUUID(), "test", "First"));
        Usuario secondUser = usuarios.saveAndFlush(
                new Usuario("slot_second_" + UUID.randomUUID(), "test", "Second"));
        Servico servico = servicos.saveAndFlush(
                new Servico("Slot test " + UUID.randomUUID(), "Test service",
                        new BigDecimal("20.00"), 30));
        LocalDate date = LocalDate.now().plusDays(14);
        LocalTime time = LocalTime.of(10, 0);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<Boolean> first = () ->
                    attempt(firstUser.getId(), servico.getId(), date, time, ready, start);
            java.util.concurrent.Callable<Boolean> second = () ->
                    attempt(secondUser.getId(), servico.getId(), date, time, ready, start);
            var a = pool.submit(first);
            var b = pool.submit(second);
            assertTrue(ready.await(15, TimeUnit.SECONDS));
            start.countDown();
            boolean aSucceeded = a.get(40, TimeUnit.SECONDS);
            boolean bSucceeded = b.get(40, TimeUnit.SECONDS);
            assertEquals(1, (aSucceeded ? 1 : 0) + (bSucceeded ? 1 : 0),
                    "Exactly one shared-agenda slot request should be accepted");
            assertEquals(1, agendamentos.findByDataAndAtivoTrue(date).stream()
                    .filter(appointment -> appointment.getHorario().equals(time))
                    .count());
        } finally {
            start.countDown();
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private boolean attempt(Long userId, Long serviceId, LocalDate date, LocalTime time,
                            CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(15, TimeUnit.SECONDS)) {
            throw new AssertionError("Concurrent start timed out");
        }
        try {
            criar.execute(new AgendamentoRequest(userId, date, time, Set.of(serviceId)));
            return true;
        } catch (AgendamentoConflitoException conflict) {
            return false;
        }
    }
}
