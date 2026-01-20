package com.github.dimitryivaniuta.dealflow.api.auth;

import com.github.dimitryivaniuta.dealflow.api.auth.dto.LoginRequest;
import com.github.dimitryivaniuta.dealflow.api.auth.dto.LoginResponse;
import com.github.dimitryivaniuta.dealflow.api.auth.dto.LogoutRequest;
import com.github.dimitryivaniuta.dealflow.api.auth.dto.LogoutResponse;
import com.github.dimitryivaniuta.dealflow.api.auth.dto.PasswordResetConfirmRequest;
import com.github.dimitryivaniuta.dealflow.api.auth.dto.PasswordResetConfirmResponse;
import com.github.dimitryivaniuta.dealflow.api.auth.dto.PasswordResetRequest;
import com.github.dimitryivaniuta.dealflow.api.auth.dto.PasswordResetRequestResponse;
import com.github.dimitryivaniuta.dealflow.api.auth.dto.RefreshRequest;
import com.github.dimitryivaniuta.dealflow.api.auth.dto.RefreshResponse;
import com.github.dimitryivaniuta.dealflow.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication API.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/auth/login} - username/password login, returns access+refresh</li>
 *   <li>{@code POST /api/auth/refresh} - rotates refresh token and returns a new access token</li>
 *   <li>{@code POST /api/auth/logout} - revokes a refresh token</li>
 *   <li>{@code POST /api/auth/password-reset/request} - starts reset flow (no enumeration)</li>
 *   <li>{@code POST /api/auth/password-reset/confirm} - confirms reset with token + new password</li>
 * </ul>
 */
@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return authService.login(req, http);
    }

    @PostMapping(path = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RefreshResponse refresh(@Valid @RequestBody RefreshRequest req, HttpServletRequest http) {
        return authService.refresh(req, http);
    }

    @PostMapping(path = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LogoutResponse logout(@Valid @RequestBody LogoutRequest req, HttpServletRequest http) {
        return authService.logout(req, http);
    }

    @PostMapping(path = "/password-reset/request", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PasswordResetRequestResponse requestReset(@Valid @RequestBody PasswordResetRequest req, HttpServletRequest http) {
        return authService.requestPasswordReset(req, http);
    }

    @PostMapping(path = "/password-reset/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PasswordResetConfirmResponse confirmReset(@Valid @RequestBody PasswordResetConfirmRequest req, HttpServletRequest http) {
        return authService.confirmPasswordReset(req, http);
    }
}
