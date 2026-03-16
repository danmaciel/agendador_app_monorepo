package com.danmaciel.agendador_backend.feature.servico.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    Optional<Servico> findByNome(String nome);
    boolean existsByNome(String nome);
}
