package com.danmaciel.agendador_backend.feature.servico.domain.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.danmaciel.agendador_backend.feature.servico.domain.entity.Servico;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    Optional<Servico> findByNomeAndAtivoTrue(String nome);
    boolean existsByNomeAndAtivoTrue(String nome);
    
    @Query("SELECT s FROM Servico s WHERE s.ativo = true")
    List<Servico> findAllAtivos();
    
    @Query("SELECT s FROM Servico s WHERE s.id IN :ids AND s.ativo = true")
    Set<Servico> findAllByIdAndAtivoTrue(Set<Long> ids);
    
    @Query("SELECT s FROM Servico s WHERE s.ativo = true")
    Page<Servico> findAllAtivos(Pageable pageable);
    
    default List<Servico> findAll() {
        return findAllAtivos();
    }
    
    default boolean existsByNome(String nome) {
        return existsByNomeAndAtivoTrue(nome);
    }
    
    default Optional<Servico> findByNome(String nome) {
        return findByNomeAndAtivoTrue(nome);
    }
    
    default Optional<Servico> findByIdAndAtivoTrue(Long id) {
        return findById(id).filter(Servico::getAtivo);
    }
    
    default List<Servico> findAllById(Iterable<Long> ids) {
        Set<Long> idSet = StreamSupport.stream(ids.spliterator(), false)
                .collect(Collectors.toSet());
        return new ArrayList<>(findAllByIdAndAtivoTrue(idSet));
    }
}
