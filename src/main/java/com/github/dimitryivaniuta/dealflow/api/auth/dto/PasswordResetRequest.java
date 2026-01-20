package com.github.dimitryivaniuta.dealflow.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Starts the reset flow.
 *
 * <p>The response is intentionally generic to avoid user enumeration.
 */
public record PasswordResetRequest(
    @NotBlank String usernameOrEmail
) {
}
