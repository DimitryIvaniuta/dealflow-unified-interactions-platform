package com.github.dimitryivaniuta.dealflow.repo.auth;

import com.github.dimitryivaniuta.dealflow.domain.auth.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for one-time password reset tokens. */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    long deleteByExpiresAtBefore(Instant now);
}
