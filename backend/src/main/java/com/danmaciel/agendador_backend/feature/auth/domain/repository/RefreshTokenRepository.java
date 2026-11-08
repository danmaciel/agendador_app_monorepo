package com.danmaciel.agendador_backend.feature.auth.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import com.danmaciel.agendador_backend.feature.auth.domain.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    /** Lock the row until the enclosing refresh transaction commits. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RefreshToken t WHERE t.token = :token")
    Optional<RefreshToken> findByTokenForUpdate(String token);
    void deleteByUsuarioId(Long usuarioId);
    long countByUsuarioId(Long usuarioId);
}
