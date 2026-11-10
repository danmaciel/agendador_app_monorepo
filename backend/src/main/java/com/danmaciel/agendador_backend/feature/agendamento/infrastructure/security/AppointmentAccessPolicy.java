package com.danmaciel.agendador_backend.feature.agendamento.infrastructure.security;

import com.danmaciel.agendador_backend.feature.agendamento.domain.repository.AgendamentoRepository;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Limits appointment lookup and modification to its owner or an administrator.
 * Missing and non-owned appointments are both denied at the authorization boundary.
 */
@Component("appointmentAccessPolicy")
public class AppointmentAccessPolicy {

    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;

    public AppointmentAccessPolicy(
            AgendamentoRepository agendamentoRepository,
            UsuarioRepository usuarioRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public boolean canAccess(Authentication authentication, Long appointmentId) {
        if (authentication == null || !authentication.isAuthenticated() || appointmentId == null) {
            return false;
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return true;
        }

        return usuarioRepository.findByLoginAndAtivoTrue(authentication.getName())
                .map(Usuario::getId)
                .flatMap(userId -> agendamentoRepository.findByIdAndAtivoTrue(appointmentId)
                        .map(appointment -> userId.equals(appointment.getUsuario().getId())))
                .orElse(false);
    }
}
