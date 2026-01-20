package com.github.dimitryivaniuta.dealflow.api.auth.dto;

import java.time.Instant;

/**
 * Successful login response.
 *
 * <p>Access token is short-lived; refresh token is long-lived and must be stored securely by the client.
 */
public record LoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Instant expiresAt,
    String subject
) {
}
