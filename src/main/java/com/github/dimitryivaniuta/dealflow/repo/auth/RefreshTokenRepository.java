package com.github.dimitryivaniuta.dealflow.repo.auth;

import com.github.dimitryivaniuta.dealflow.domain.auth.RefreshToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for refresh tokens. */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHashAndRevokedIsFalse(String tokenHash);
    List<RefreshToken> findByUserIdAndRevokedIsFalse(UUID userId);
    long deleteByExpiresAtBefore(Instant now);
}
