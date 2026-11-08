package com.danmaciel.agendador_backend.feature.auth.domain.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenFingerprintTest {

    @Test
    void generatesUniqueOpaqueSecretsAndStableNonReversibleFingerprints() {
        String first = RefreshTokenFingerprint.issueSecret();
        String second = RefreshTokenFingerprint.issueSecret();
        assertNotEquals(first, second);
        assertEquals(43, first.length()); // 32 random bytes, unpadded URL-safe Base64
        assertNotEquals(first, RefreshTokenFingerprint.fingerprint(first));
        assertEquals(71, RefreshTokenFingerprint.fingerprint(first).length());
        assertEquals(RefreshTokenFingerprint.fingerprint(first),
                RefreshTokenFingerprint.fingerprint(first));
        assertNotEquals(RefreshTokenFingerprint.fingerprint(first),
                RefreshTokenFingerprint.fingerprint(second));
    }

    @Test
    void legacyFallbackOnlyAcceptsCanonicalUuidV4() {
        assertTrue(RefreshTokenFingerprint.isLegacyUuid("f47ac10b-58cc-4372-a567-0e02b2c3d479"));
        assertFalse(RefreshTokenFingerprint.isLegacyUuid(RefreshTokenFingerprint.fingerprint("secret")));
        assertFalse(RefreshTokenFingerprint.isLegacyUuid("00000000-0000-0000-0000-000000000000"));
        assertFalse(RefreshTokenFingerprint.isLegacyUuid("not-a-legacy-token"));
        assertFalse(RefreshTokenFingerprint.isLegacyUuid(null));
    }

    @Test
    void persistedEntityDoesNotExposeRawSecretThroughTokenField() {
        var user = new com.danmaciel.agendador_backend.feature.usuario.domain.entity.Usuario(
                "user", "test-hash", "User");
        var entity = new com.danmaciel.agendador_backend.feature.auth.domain.entity.RefreshToken(user, 30);
        assertNotNull(entity.getIssuedSecret());
        assertEquals(RefreshTokenFingerprint.fingerprint(entity.getIssuedSecret()), entity.getToken());
        assertFalse(entity.getToken().contains(entity.getIssuedSecret()));
    }
}
