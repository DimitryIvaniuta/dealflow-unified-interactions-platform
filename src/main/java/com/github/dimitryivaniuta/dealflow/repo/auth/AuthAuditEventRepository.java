package com.github.dimitryivaniuta.dealflow.repo.auth;

import com.github.dimitryivaniuta.dealflow.domain.auth.AuthAuditEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for auth audit events. */
public interface AuthAuditEventRepository extends JpaRepository<AuthAuditEvent, UUID> {
}
