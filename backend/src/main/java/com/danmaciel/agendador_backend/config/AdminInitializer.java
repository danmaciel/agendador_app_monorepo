package com.danmaciel.agendador_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Role;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;
import com.danmaciel.agendador_backend.feature.usuario.domain.repository.UsuarioRepository;

@Configuration
public class AdminInitializer {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    @Value("${app.admin.login}")
    private String adminLogin;

    @Value("${app.admin.senha}")
    private String adminSenha;

    @Value("${app.admin.nome}")
    private String adminNome;

    @Bean
    public ApplicationRunner initAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.findByLoginAndAtivoTrue(adminLogin).isEmpty()) {
                Usuario admin = new Usuario(
                        adminLogin,
                        passwordEncoder.encode(adminSenha),
                        adminNome
                );
                admin.addRole(Role.ROLE_ADMIN);
                usuarioRepository.save(admin);
                log.info("Usuário administrador criado com sucesso: {}", adminLogin);
            } else {
                log.debug("Usuário administrador já existe: {}", adminLogin);
            }
        };
    }
}
