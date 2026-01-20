package com.github.dimitryivaniuta.dealflow.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Refresh token request. */
public record RefreshRequest(
    @NotBlank String refreshToken
) {
}
