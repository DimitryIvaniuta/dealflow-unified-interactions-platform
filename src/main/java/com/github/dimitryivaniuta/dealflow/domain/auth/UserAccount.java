package com.github.dimitryivaniuta.dealflow.domain.auth;

import com.github.dimitryivaniuta.dealflow.domain.common.AuditedEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Password-based user account used for first-party authentication.
 *
 * <p>Production-grade rules:
 * <ul>
 *   <li>Passwords are stored only as BCrypt hashes ({@code password_hash})</li>
 *   <li>Refresh tokens are stored server-side and hashed, so access can be revoked instantly</li>
 *   <li>Repeated login failures are tracked and can lock the account temporarily</li>
 * </ul>
 */
@Entity
@Table(name = "df_user_accounts")
@Getter
@Setter
public class UserAccount extends AuditedEntity {

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "email", nullable = false, length = 256)
    private String email;

    @Column(name = "display_name", nullable = false, length = 256)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private UserAccountStatus status = UserAccountStatus.ACTIVE;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "df_user_account_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 64)
    private Set<UserRole> roles = new HashSet<>();
}
