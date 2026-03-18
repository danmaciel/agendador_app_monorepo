package com.danmaciel.agendador_backend.feature.agendamento.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.Agendamento;
import com.danmaciel.agendador_backend.feature.agendamento.domain.entity.StatusAgendamento;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByUsuarioIdAndAtivoTrue(Long usuarioId);
    Page<Agendamento> findByUsuarioIdAndAtivoTrue(Long usuarioId, Pageable pageable);
    List<Agendamento> findByUsuarioIdAndDataBetweenAndAtivoTrue(Long usuarioId, LocalDate dataInicio, LocalDate dataFim);
    Page<Agendamento> findByUsuarioIdAndDataBetweenAndAtivoTrue(Long usuarioId, LocalDate dataInicio, LocalDate dataFim, Pageable pageable);
    List<Agendamento> findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(Long usuarioId, LocalDate dataInicio, LocalDate dataFim, StatusAgendamento status);
    List<Agendamento> findByDataBetweenAndAtivoTrue(LocalDate dataInicio, LocalDate dataFim);
    Page<Agendamento> findByDataBetweenAndAtivoTrue(LocalDate dataInicio, LocalDate dataFim, Pageable pageable);
    Optional<Agendamento> findByDataAndHorarioAndUsuarioIdAndAtivoTrue(LocalDate data, LocalTime horario, Long usuarioId);
    boolean existsByDataAndHorarioAndUsuarioIdAndAtivoTrue(LocalDate data, LocalTime horario, Long usuarioId);
    boolean existsByDataAndHorarioAndAtivoTrue(LocalDate data, LocalTime horario);
    List<Agendamento> findByDataAndAtivoTrue(LocalDate data);
    Page<Agendamento> findByStatusAndAtivoTrue(StatusAgendamento status, Pageable pageable);
    
    Optional<Agendamento> findByIdAndAtivoTrue(Long id);
    
    default List<Agendamento> findByUsuarioId(Long usuarioId) {
        return findByUsuarioIdAndAtivoTrue(usuarioId);
    }
    
    default Page<Agendamento> findByUsuarioId(Long usuarioId, Pageable pageable) {
        return findByUsuarioIdAndAtivoTrue(usuarioId, pageable);
    }
    
    default List<Agendamento> findByUsuarioIdAndDataBetween(Long usuarioId, LocalDate dataInicio, LocalDate dataFim) {
        return findByUsuarioIdAndDataBetweenAndAtivoTrue(usuarioId, dataInicio, dataFim);
    }
    
    default Page<Agendamento> findByUsuarioIdAndDataBetween(Long usuarioId, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        return findByUsuarioIdAndDataBetweenAndAtivoTrue(usuarioId, dataInicio, dataFim, pageable);
    }
    
    default List<Agendamento> findByUsuarioIdAndDataBetweenAndStatus(Long usuarioId, LocalDate dataInicio, LocalDate dataFim, StatusAgendamento status) {
        return findByUsuarioIdAndDataBetweenAndStatusAndAtivoTrue(usuarioId, dataInicio, dataFim, status);
    }
    
    default List<Agendamento> findByDataBetween(LocalDate dataInicio, LocalDate dataFim) {
        return findByDataBetweenAndAtivoTrue(dataInicio, dataFim);
    }
    
    default Page<Agendamento> findByDataBetween(LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        return findByDataBetweenAndAtivoTrue(dataInicio, dataFim, pageable);
    }
    
    default Optional<Agendamento> findByDataAndHorarioAndUsuarioId(LocalDate data, LocalTime horario, Long usuarioId) {
        return findByDataAndHorarioAndUsuarioIdAndAtivoTrue(data, horario, usuarioId);
    }
    
    default boolean existsByDataAndHorarioAndUsuarioId(LocalDate data, LocalTime horario, Long usuarioId) {
        return existsByDataAndHorarioAndUsuarioIdAndAtivoTrue(data, horario, usuarioId);
    }
    
    default boolean existsByDataAndHorario(LocalDate data, LocalTime horario) {
        return existsByDataAndHorarioAndAtivoTrue(data, horario);
    }
    
    default List<Agendamento> findByData(LocalDate data) {
        return findByDataAndAtivoTrue(data);
    }
    
    default Page<Agendamento> findByStatus(StatusAgendamento status, Pageable pageable) {
        return findByStatusAndAtivoTrue(status, pageable);
    }
}
