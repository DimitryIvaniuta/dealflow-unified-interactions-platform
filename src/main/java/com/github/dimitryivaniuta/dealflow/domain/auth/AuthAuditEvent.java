package com.github.dimitryivaniuta.dealflow.domain.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Immutable audit record for authentication flows.
 *
 * <p>Purpose:
 * <ul>
 *   <li>Investigate suspicious activity (brute force attempts)</li>
 *   <li>Operations debugging (why refresh fails)</li>
 *   <li>Compliance logs (who reset password / logged out)</li>
 * </ul>
 */
@Entity
@Table(name = "df_auth_audit_events")
@Getter
@Setter
public class AuthAuditEvent {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private AuthAuditEventType eventType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "subject", length = 128)
    private String subject;

    @Column(name = "username", length = 64)
    private String username;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "details", length = 1024)
    private String details;
}
