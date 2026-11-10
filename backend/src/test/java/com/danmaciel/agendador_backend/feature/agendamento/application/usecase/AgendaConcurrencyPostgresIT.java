package com.danmaciel.agendador_backend.feature.agendamento.application.usecase;

import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.application.dto.AprovarAgendamentoRequest;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;
import com.danmaciel.agendador_backend.feature.servico.domain.repository.ServicoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import com.danmaciel.agendador_backend.shared.exception.AgendamentoConflitoException;
import com.danmaciel.agendador_backend.shared.exception.AgendaConflitoException;
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
    @Autowired private AprovarAgendamentoUseCase aprovar;
    @Autowired private AtualizarAgendamentoUseCase atualizar;

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

    @Test
    void onlyOneOfTwoOverlappingPendingAppointmentsCanBeApproved() throws Exception {
        Usuario firstUser = usuarios.saveAndFlush(
                new Usuario("approve_first_" + UUID.randomUUID(), "test", "First"));
        Usuario secondUser = usuarios.saveAndFlush(
                new Usuario("approve_second_" + UUID.randomUUID(), "test", "Second"));
        Servico servico = servicos.saveAndFlush(
                new Servico("Approve test " + UUID.randomUUID(), "Test service",
                        new BigDecimal("20.00"), 30));
        LocalDate date = LocalDate.now().plusDays(21);
        LocalTime time = LocalTime.of(11, 0);

        // Existing pending reservations may overlap: conflict checking on approval
        // must allow only one to become approved.
        Agendamento firstPending = new Agendamento(firstUser, date, time);
        firstPending.setServicos(Set.of(servico));
        firstPending.setTempoTotal(30);
        firstPending = agendamentos.saveAndFlush(firstPending);

        Agendamento secondPending = new Agendamento(secondUser, date, time);
        secondPending.setServicos(Set.of(servico));
        secondPending.setTempoTotal(30);
        secondPending = agendamentos.saveAndFlush(secondPending);

        Long firstId = firstPending.getId();
        Long secondId = secondPending.getId();
        AprovarAgendamentoRequest request = new AprovarAgendamentoRequest(Set.of(servico.getId()));
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> attemptApproval(firstId, request, ready, start));
            var b = pool.submit(() -> attemptApproval(secondId, request, ready, start));
            assertTrue(ready.await(15, TimeUnit.SECONDS));
            start.countDown();
            boolean aSucceeded = a.get(40, TimeUnit.SECONDS);
            boolean bSucceeded = b.get(40, TimeUnit.SECONDS);
            assertEquals(1, (aSucceeded ? 1 : 0) + (bSucceeded ? 1 : 0),
                    "Only one pending reservation may be approved in an occupied slot");

            var sameTime = agendamentos.findByDataAndAtivoTrue(date).stream()
                    .filter(appointment -> appointment.getHorario().equals(time))
                    .toList();
            assertEquals(2, sameTime.size(), "Both pending records should remain in the database");
            assertEquals(1, sameTime.stream()
                    .filter(appointment -> appointment.getStatus() == StatusAgendamento.APROVADO)
                    .count());
        } finally {
            start.countDown();
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void onlyOneOfTwoConcurrentUpdatesCanMoveIntoTheSameSlot() throws Exception {
        Usuario firstUser = usuarios.saveAndFlush(
                new Usuario("update_first_" + UUID.randomUUID(), "test", "First"));
        Usuario secondUser = usuarios.saveAndFlush(
                new Usuario("update_second_" + UUID.randomUUID(), "test", "Second"));
        Servico servico = servicos.saveAndFlush(
                new Servico("Update test " + UUID.randomUUID(), "Test service",
                        new BigDecimal("20.00"), 30));
        LocalDate date = LocalDate.now().plusDays(28);
        LocalTime targetTime = LocalTime.of(12, 0);

        Agendamento firstBooking = new Agendamento(firstUser, date, LocalTime.of(8, 0));
        firstBooking.setServicos(Set.of(servico));
        firstBooking.setTempoTotal(30);
        firstBooking = agendamentos.saveAndFlush(firstBooking);

        Agendamento secondBooking = new Agendamento(secondUser, date, LocalTime.of(9, 0));
        secondBooking.setServicos(Set.of(servico));
        secondBooking.setTempoTotal(30);
        secondBooking = agendamentos.saveAndFlush(secondBooking);

        Long firstId = firstBooking.getId();
        Long secondId = secondBooking.getId();
        AgendamentoRequest firstRequest = new AgendamentoRequest(
                firstUser.getId(), date, targetTime, Set.of(servico.getId()));
        AgendamentoRequest secondRequest = new AgendamentoRequest(
                secondUser.getId(), date, targetTime, Set.of(servico.getId()));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> attemptUpdate(firstId, firstRequest, ready, start));
            var b = pool.submit(() -> attemptUpdate(secondId, secondRequest, ready, start));
            assertTrue(ready.await(15, TimeUnit.SECONDS));
            start.countDown();
            boolean aSucceeded = a.get(40, TimeUnit.SECONDS);
            boolean bSucceeded = b.get(40, TimeUnit.SECONDS);

            assertEquals(1, (aSucceeded ? 1 : 0) + (bSucceeded ? 1 : 0),
                    "Only one update may occupy the target interval");
            var appointments = agendamentos.findByDataAndAtivoTrue(date);
            assertEquals(2, appointments.size(), "Neither original booking may be lost");
            assertEquals(1, appointments.stream()
                    .filter(appointment -> appointment.getHorario().equals(targetTime))
                    .count());
            assertEquals(1, appointments.stream()
                    .filter(appointment -> appointment.getHorario().equals(LocalTime.of(8, 0))
                            || appointment.getHorario().equals(LocalTime.of(9, 0)))
                    .count(), "The rejected update must retain its original time");
        } finally {
            start.countDown();
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private boolean attemptUpdate(Long appointmentId, AgendamentoRequest request,
                                  CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(15, TimeUnit.SECONDS)) {
            throw new AssertionError("Concurrent update barrier timed out");
        }
        try {
            atualizar.execute(appointmentId, request);
            return true;
        } catch (AgendamentoConflitoException conflict) {
            return false;
        }
    }

    private boolean attemptApproval(Long appointmentId, AprovarAgendamentoRequest request,
                                    CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(15, TimeUnit.SECONDS)) {
            throw new AssertionError("Concurrent approval barrier timed out");
        }
        try {
            aprovar.execute(appointmentId, request);
            return true;
        } catch (AgendaConflitoException conflict) {
            return false;
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
