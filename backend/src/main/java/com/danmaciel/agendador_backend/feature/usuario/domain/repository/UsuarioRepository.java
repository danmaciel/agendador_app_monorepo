package com.danmaciel.agendador_backend.feature.usuario.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);
    boolean existsByLogin(String login);
}
