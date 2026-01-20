package com.github.dimitryivaniuta.dealflow.api.auth.dto;

/**
 * Reset request response.
 *
 * <p>In production we do not expose the reset token. If configured for dev/testing,
 * token may be returned to allow manual confirmation.
 */
public record PasswordResetRequestResponse(
    boolean accepted,
    String resetToken
) {
}
