package com.danmaciel.agendador_backend.feature.auth.domain.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Role;
import com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(Usuario usuario) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(usuario.getLogin())
                .claim("user_id", usuario.getId())
                .claim("name", usuario.getNome())
                .claim("roles", usuario.getRoles().stream().map(Role::name).collect(Collectors.joining(",")))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public String extractLogin(String token) {
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractClaims(token).get("user_id", Long.class);
    }

    public String extractNome(String token) {
        return extractClaims(token).get("name", String.class);
    }

    public Set<Role> extractRoles(String token) {
        String rolesClaim = extractClaims(token).get("roles", String.class);
        if (rolesClaim == null || rolesClaim.isEmpty()) {
            return Set.of();
        }
        return Set.of(rolesClaim.split(",")).stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }

    public boolean isTokenValido(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Long getExpiration() {
        return expiration;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
