package com.github.dimitryivaniuta.dealflow.config.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed configuration for JWT signing and verification.
 *
 * <p>Access tokens are short-lived and signed with the RSA private key.
 * Refresh tokens are stored server-side (hashed) and rotated.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtKeyProps(
    String issuer,
    String privateKey,
    String publicKey,
    long accessTokenTtlMinutes,
    long refreshTokenTtlDays
) {
}
