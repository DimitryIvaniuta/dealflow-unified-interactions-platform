package com.github.dimitryivaniuta.dealflow.api.auth.dto;

import java.time.Instant;

/** Response for a successful refresh. Refresh token is rotated. */
public record RefreshResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Instant expiresAt,
    String subject
) {
}
