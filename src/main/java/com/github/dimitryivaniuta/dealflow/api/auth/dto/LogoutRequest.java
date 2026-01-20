package com.github.dimitryivaniuta.dealflow.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Logout request: revoke the provided refresh token. */
public record LogoutRequest(
    @NotBlank String refreshToken
) {
}
