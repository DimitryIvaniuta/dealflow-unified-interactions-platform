package com.github.dimitryivaniuta.dealflow.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login request using username/email + password.
 */
public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {
}
