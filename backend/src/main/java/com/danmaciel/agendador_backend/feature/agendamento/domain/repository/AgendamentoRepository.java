package com.danmaciel.agendador_backend.feature.agendamento.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByUsuarioId(Long usuarioId);
    Page<Agendamento> findByUsuarioId(Long usuarioId, Pageable pageable);
    List<Agendamento> findByUsuarioIdAndDataBetween(Long usuarioId, LocalDate dataInicio, LocalDate dataFim);
    Page<Agendamento> findByUsuarioIdAndDataBetween(Long usuarioId, LocalDate dataInicio, LocalDate dataFim, Pageable pageable);
    List<Agendamento> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);
    Page<Agendamento> findByDataBetween(LocalDate dataInicio, LocalDate dataFim, Pageable pageable);
    Optional<Agendamento> findByDataAndHorarioAndUsuarioId(LocalDate data, LocalTime horario, Long usuarioId);
    boolean existsByDataAndHorarioAndUsuarioId(LocalDate data, LocalTime horario, Long usuarioId);
    boolean existsByDataAndHorario(LocalDate data, LocalTime horario);
    List<Agendamento> findByData(LocalDate data);
    Page<Agendamento> findByStatus(com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento status, Pageable pageable);
}
