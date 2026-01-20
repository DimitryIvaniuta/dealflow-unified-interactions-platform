package com.github.dimitryivaniuta.dealflow.service.auth;

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
import com.github.dimitryivaniuta.dealflow.domain.auth.AuthAuditEventType;
import com.github.dimitryivaniuta.dealflow.domain.auth.UserAccount;
import com.github.dimitryivaniuta.dealflow.domain.auth.UserAccountStatus;
import com.github.dimitryivaniuta.dealflow.repo.auth.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates authentication flows: login, refresh, logout, password reset.
 *
 * <p>This service intentionally avoids Spring's form-login stack. We expose explicit REST endpoints
 * and issue JWT access tokens + opaque refresh tokens.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuerService jwtIssuer;
    private final RefreshTokenService refreshTokens;
    private final PasswordResetService passwordReset;
    private final AuthAuditService audit;

    /**
     * Username/password login.
     *
     * <p>On success: returns JWT access token and refresh token.
     * On failure: increments failed attempts and may lock account for a short period.
     */
    @Transactional
    public LoginResponse login(LoginRequest req, HttpServletRequest http) {
        // DTO field is named "username" for simplicity, but it accepts either username or email.
        String login = normalize(req.username());
        UserAccount user = users.findByUsernameIgnoreCase(login)
            .or(() -> users.findByEmailIgnoreCase(login))
            .orElse(null);

        if (user == null) {
            audit.record(AuthAuditEventType.LOGIN_FAILURE, false, null, login, http, "Unknown username/email");
            throw new InvalidCredentialsException();
        }

        if (user.getStatus() != UserAccountStatus.ACTIVE) {
            audit.record(AuthAuditEventType.ACCOUNT_LOCKED, false, user.getUsername(), login, http, "Account not active");
            throw new AccountLockedException("Account is not active");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            audit.record(AuthAuditEventType.ACCOUNT_LOCKED, false, user.getUsername(), login, http, "Account locked");
            throw new AccountLockedException(user.getLockedUntil());
        }

        boolean ok = passwordEncoder.matches(req.password(), user.getPasswordHash());
        if (!ok) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(Instant.now().plus(LOCK_DURATION));
                user.setFailedAttempts(0); // reset counter after lock
            }
            users.save(user);
            audit.record(AuthAuditEventType.LOGIN_FAILURE, false, user.getUsername(), login, http, "Bad password");
            throw new InvalidCredentialsException();
        }

        // success
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        users.save(user);

        JwtIssuerService.IssuedToken access = jwtIssuer.issueAccessToken(user);
        RefreshTokenService.IssuedRefreshToken refresh = refreshTokens.issue(user);

        audit.record(AuthAuditEventType.LOGIN_SUCCESS, true, user.getUsername(), login, http, "OK");

        return new LoginResponse(
            access.token(),
            refresh.token(),
            "Bearer",
            access.expiresAt(),
            access.subject()
        );
    }

    /**
     * Refreshes an access token using an existing refresh token.
     *
     * <p>Rotation is enforced (old refresh token is revoked).
     */
    @Transactional
    public RefreshResponse refresh(RefreshRequest req, HttpServletRequest http) {
        try {
            RefreshTokenService.RotatedRefreshToken rotated = refreshTokens.rotate(req.refreshToken());
            UserAccount user = users.findById(rotated.userId())
                .orElseThrow(() -> new InvalidTokenException("User not found"));

            JwtIssuerService.IssuedToken access = jwtIssuer.issueAccessToken(user);
            audit.record(AuthAuditEventType.TOKEN_REFRESH_SUCCESS, true, user.getUsername(), user.getUsername(), http, "OK");

            return new RefreshResponse(
                access.token(),
                rotated.newToken(),
                "Bearer",
                access.expiresAt(),
                access.subject()
            );
        } catch (AuthException e) {
            audit.record(AuthAuditEventType.TOKEN_REFRESH_FAILURE, false, null, null, http, e.getMessage());
            throw e;
        }
    }

    /** Revokes a refresh token (best-effort logout). */
    @Transactional
    public LogoutResponse logout(LogoutRequest req, HttpServletRequest http) {
        refreshTokens.revoke(req.refreshToken());
        audit.record(AuthAuditEventType.LOGOUT, true, null, null, http, "OK");
        return new LogoutResponse(true);
    }

    /** Starts password reset. Always returns accepted=true to prevent user enumeration. */
    @Transactional
    public PasswordResetRequestResponse requestPasswordReset(PasswordResetRequest req, HttpServletRequest http) {
        String token = passwordReset.requestReset(normalize(req.usernameOrEmail()));
        audit.record(AuthAuditEventType.PASSWORD_RESET_REQUEST, true, null, req.usernameOrEmail(), http, "Accepted");
        return new PasswordResetRequestResponse(true, token);
    }

    /** Confirms password reset with token + new password. */
    @Transactional
    public PasswordResetConfirmResponse confirmPasswordReset(PasswordResetConfirmRequest req, HttpServletRequest http) {
        try {
            passwordReset.confirmReset(req.resetToken(), req.newPassword());
            audit.record(AuthAuditEventType.PASSWORD_RESET_CONFIRM_SUCCESS, true, null, null, http, "OK");
            return new PasswordResetConfirmResponse(true);
        } catch (AuthException e) {
            audit.record(AuthAuditEventType.PASSWORD_RESET_CONFIRM_FAILURE, false, null, null, http, e.getMessage());
            throw e;
        }
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim();
    }
}
