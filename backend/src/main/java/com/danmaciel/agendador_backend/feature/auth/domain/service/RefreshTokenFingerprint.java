package com.danmaciel.agendador_backend.feature.auth.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Opaque bearer refresh tokens: raw secret goes only to the client, never to
 * the database. A prefixed SHA-256 fingerprint fits the existing VARCHAR(255).
 * SHA-256 is suitable here because issued secrets have 256 bits of entropy.
 */
public final class RefreshTokenFingerprint {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PREFIX = "sha256:";

    private RefreshTokenFingerprint() {
    }

    public static String issueSecret() {
        byte[] secret = new byte[32];
        RANDOM.nextBytes(secret);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    }

    public static String fingerprint(String rawSecret) {
        if (rawSecret == null || rawSecret.isBlank()) {
            throw new IllegalArgumentException("Refresh token indisponivel");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawSecret.getBytes(StandardCharsets.UTF_8));
            return PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public static boolean isFingerprint(String storedValue) {
        return storedValue != null && storedValue.startsWith(PREFIX);
    }
}
