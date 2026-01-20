package com.github.dimitryivaniuta.dealflow.domain.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Server-stored refresh token.
 *
 * <p>The plaintext token is returned to the client once. In the database we store
 * only a SHA-256 hash so leaked DB dumps cannot be used to mint tokens.
 */
@Entity
@Table(name = "df_refresh_tokens")
@Getter
@Setter
public class RefreshToken {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
