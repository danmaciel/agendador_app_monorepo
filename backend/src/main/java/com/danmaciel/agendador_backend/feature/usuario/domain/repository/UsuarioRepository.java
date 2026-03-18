package com.danmaciel.agendador_backend.feature.usuario.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLoginAndAtivoTrue(String login);
    boolean existsByLoginAndAtivoTrue(String login);
    
    @Query("SELECT u FROM Usuario u WHERE u.ativo = true")
    List<Usuario> findAllAtivos();
    
    @Query("SELECT u FROM Usuario u WHERE u.ativo = true")
    Page<Usuario> findAllAtivos(Pageable pageable);
    
    default Optional<Usuario> findByLogin(String login) {
        return findByLoginAndAtivoTrue(login);
    }
    
    default boolean existsByLogin(String login) {
        return existsByLoginAndAtivoTrue(login);
    }
    
    default Optional<Usuario> findByIdAndAtivoTrue(Long id) {
        return findById(id).filter(Usuario::getAtivo);
    }
}
