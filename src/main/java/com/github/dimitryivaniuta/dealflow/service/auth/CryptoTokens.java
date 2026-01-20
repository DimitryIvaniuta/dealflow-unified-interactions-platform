package com.github.dimitryivaniuta.dealflow.service.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Small crypto helper for issuing and hashing opaque tokens.
 *
 * <p>Design:
 * <ul>
 *   <li>Refresh/reset tokens are <b>opaque</b> random values (not JWTs).</li>
 *   <li>DB stores only a <b>SHA-256 hash</b> of the token (defence in depth for DB leaks).</li>
 * </ul>
 *
 * <p>This is intentionally minimal to avoid pulling extra dependencies.
 */
final class CryptoTokens {

    private static final SecureRandom RNG = new SecureRandom();

    private CryptoTokens() {}

    /** Generates a URL-safe random token (base64url, no padding). */
    static String newOpaqueToken(int bytes) {
        byte[] buf = new byte[bytes];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /** Returns a stable SHA-256 hash for storage / lookup. */
    static String sha256(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
