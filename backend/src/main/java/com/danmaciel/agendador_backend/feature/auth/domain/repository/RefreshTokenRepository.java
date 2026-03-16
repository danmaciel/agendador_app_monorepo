package com.danmaciel.agendador_backend.feature.auth.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.danmaciel.agendador_backend.feature.auth.domain.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUsuarioId(Long usuarioId);
}
