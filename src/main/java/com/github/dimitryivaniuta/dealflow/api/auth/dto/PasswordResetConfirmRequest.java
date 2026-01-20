package com.github.dimitryivaniuta.dealflow.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Confirms password reset. */
public record PasswordResetConfirmRequest(
    @NotBlank String resetToken,
    @NotBlank String newPassword
) {
}
