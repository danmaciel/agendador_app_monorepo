package com.danmaciel.agendador_backend.feature.usuario.infrastructure.security;

import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Owner or administrator may read a user profile. A non-owner is denied
 * before the requested account is loaded, avoiding ID-existence disclosure.
 */
@Component("userProfileAccessPolicy")
public class UserProfileAccessPolicy {

    private final UsuarioRepository usuarioRepository;

    public UserProfileAccessPolicy(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public boolean canRead(Authentication authentication, Long requestedId) {
        if (authentication == null || !authentication.isAuthenticated() || requestedId == null) {
            return false;
        }
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return true;
        }
        return usuarioRepository.findByLoginAndAtivoTrue(authentication.getName())
                .map(Usuario::getId)
                .filter(requestedId::equals)
                .isPresent();
    }
}
