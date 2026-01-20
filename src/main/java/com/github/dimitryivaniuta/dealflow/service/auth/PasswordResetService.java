package com.github.dimitryivaniuta.dealflow.service.auth;

import com.github.dimitryivaniuta.dealflow.config.auth.PasswordResetProps;
import com.github.dimitryivaniuta.dealflow.domain.auth.PasswordResetToken;
import com.github.dimitryivaniuta.dealflow.domain.auth.UserAccount;
import com.github.dimitryivaniuta.dealflow.domain.auth.UserAccountStatus;
import com.github.dimitryivaniuta.dealflow.repo.auth.PasswordResetTokenRepository;
import com.github.dimitryivaniuta.dealflow.repo.auth.UserAccountRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles the password reset flow.
 *
 * <p>Security properties:
 * <ul>
 *   <li>Reset request does not reveal whether the user exists (no enumeration).</li>
 *   <li>Reset tokens are one-time and stored hashed in DB.</li>
 *   <li>On successful password change, existing refresh tokens are revoked.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserAccountRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordResetProps props;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokens;

    /**
     * Starts the password reset flow.
     *
     * @return plaintext token only if {@code exposeToken=true} (dev/testing); otherwise {@code null}
     */
    @Transactional
    public String requestReset(String usernameOrEmail) {
        UserAccount user = users.findByUsernameIgnoreCase(usernameOrEmail)
            .or(() -> users.findByEmailIgnoreCase(usernameOrEmail))
            .orElse(null);

        // Always behave the same to prevent user enumeration.
        if (user == null || user.getStatus() != UserAccountStatus.ACTIVE) {
            return null;
        }

        String plain = CryptoTokens.newOpaqueToken(32);
        String hash = CryptoTokens.sha256(plain);

        PasswordResetToken t = new PasswordResetToken();
        t.setId(UUID.randomUUID());
        t.setUserId(user.getId());
        t.setTokenHash(hash);
        t.setCreatedAt(Instant.now());
        t.setExpiresAt(Instant.now().plus(props.tokenTtlMinutes(), ChronoUnit.MINUTES));
        t.setUsedAt(null);
        tokens.save(t);

        return props.exposeToken() ? plain : null;
    }

    /**
     * Confirms reset using the plaintext token and a new password.
     */
    @Transactional
    public void confirmReset(String resetToken, String newPassword) {
        passwordPolicy.validateOrThrow(newPassword);

        String hash = CryptoTokens.sha256(resetToken);
        PasswordResetToken t = tokens.findByTokenHash(hash)
            .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (t.getUsedAt() != null) {
            throw new InvalidTokenException("Reset token already used");
        }
        if (t.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Reset token expired");
        }

        UserAccount user = users.findById(t.getUserId())
            .orElseThrow(() -> new InvalidTokenException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        users.save(user);

        t.setUsedAt(Instant.now());
        tokens.save(t);

        // Force re-auth everywhere.
        refreshTokens.revokeAllForUser(user.getId());
    }
}
