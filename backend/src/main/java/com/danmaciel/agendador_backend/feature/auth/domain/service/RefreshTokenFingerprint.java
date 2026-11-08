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

    /**
     * Only the historical UUIDv4 wire format qualifies for plaintext migration.
     * Never interpret a stored sha256: fingerprint as a bearer secret.
     */
    public static boolean isLegacyUuid(String rawSecret) {
        if (rawSecret == null || !rawSecret.matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            return false;
        }
        return true;
    }

    public static boolean isFingerprint(String storedValue) {
        return storedValue != null && storedValue.startsWith(PREFIX);
    }
}
