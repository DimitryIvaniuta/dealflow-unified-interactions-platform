package com.github.dimitryivaniuta.dealflow.service.auth;

import com.github.dimitryivaniuta.dealflow.config.auth.JwtKeyProps;
import com.github.dimitryivaniuta.dealflow.domain.auth.RefreshToken;
import com.github.dimitryivaniuta.dealflow.domain.auth.UserAccount;
import com.github.dimitryivaniuta.dealflow.repo.auth.RefreshTokenRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues, rotates and revokes refresh tokens.
 *
 * <p>Production-grade details:
 * <ul>
 *   <li>Refresh tokens are opaque random strings (not JWT).</li>
 *   <li>Only a SHA-256 hash is stored in DB (defence in depth).</li>
 *   <li>Rotation: refresh exchanges revoke the old token and mint a new one.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final JwtKeyProps props;

    /** Issues a new refresh token for the given user. */
    @Transactional
    public IssuedRefreshToken issue(UserAccount user) {
        String plain = CryptoTokens.newOpaqueToken(32);
        String hash = CryptoTokens.sha256(plain);

        RefreshToken rt = new RefreshToken();
        rt.setId(UUID.randomUUID());
        rt.setUserId(user.getId());
        rt.setTokenHash(hash);
        rt.setCreatedAt(Instant.now());
        rt.setExpiresAt(Instant.now().plus(props.refreshTokenTtlDays(), ChronoUnit.DAYS));
        rt.setRevoked(false);
        repo.save(rt);

        return new IssuedRefreshToken(plain, rt.getExpiresAt());
    }

    /**
     * Rotates a refresh token.
     *
     * @return userId owning the old token and the new issued token
     */
    @Transactional
    public RotatedRefreshToken rotate(String oldRefreshToken) {
        String hash = CryptoTokens.sha256(oldRefreshToken);
        RefreshToken existing = repo.findByTokenHashAndRevokedIsFalse(hash)
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            existing.setRevoked(true);
            repo.save(existing);
            throw new InvalidTokenException("Refresh token expired");
        }

        existing.setRevoked(true);
        repo.save(existing);

        String newPlain = CryptoTokens.newOpaqueToken(32);
        String newHash = CryptoTokens.sha256(newPlain);
        RefreshToken rt = new RefreshToken();
        rt.setId(UUID.randomUUID());
        rt.setUserId(existing.getUserId());
        rt.setTokenHash(newHash);
        rt.setCreatedAt(Instant.now());
        rt.setExpiresAt(Instant.now().plus(props.refreshTokenTtlDays(), ChronoUnit.DAYS));
        rt.setRevoked(false);
        repo.save(rt);

        return new RotatedRefreshToken(existing.getUserId(), newPlain, rt.getExpiresAt());
    }

    /** Revokes a refresh token (best-effort). */
    @Transactional
    public void revoke(String refreshToken) {
        String hash = CryptoTokens.sha256(refreshToken);
        repo.findByTokenHashAndRevokedIsFalse(hash).ifPresent(rt -> {
            rt.setRevoked(true);
            repo.save(rt);
        });
    }

    /** Revokes all active refresh tokens for a user (e.g., after password change). */
    @Transactional
    public void revokeAllForUser(UUID userId) {
        for (RefreshToken t : repo.findByUserIdAndRevokedIsFalse(userId)) {
            t.setRevoked(true);
        }
        // saveAll not required; JPA dirty checking within TX
    }

    public record IssuedRefreshToken(String token, Instant expiresAt) {}
    public record RotatedRefreshToken(UUID userId, String newToken, Instant expiresAt) {}
}
