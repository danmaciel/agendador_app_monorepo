package com.danmaciel.agendador_backend.feature.agendamento.infrastructure.lock;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serializes writes to the single shared appointment calendar, including
 * distinct application instances connected to the same database.
 *
 * Must run inside the caller's transaction so SELECT FOR UPDATE remains held
 * until the booking transaction commits or rolls back.
 */
@Component
public class AgendaWriteLock {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public void acquire() {
        entityManager.createNativeQuery(
                "SELECT id FROM agenda_write_lock WHERE id = 1 FOR UPDATE")
                .getSingleResult();
    }
}
