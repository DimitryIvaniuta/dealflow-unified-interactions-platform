package com.github.dimitryivaniuta.dealflow.service.auth;

import com.github.dimitryivaniuta.dealflow.domain.auth.AuthAuditEvent;
import com.github.dimitryivaniuta.dealflow.domain.auth.AuthAuditEventType;
import com.github.dimitryivaniuta.dealflow.repo.auth.AuthAuditEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes immutable audit events for authentication flows.
 *
 * <p>Production reasons:
 * <ul>
 *   <li>Track brute-force attempts and investigate suspicious IPs</li>
 *   <li>Understand why refresh/reset fails without exposing sensitive details to clients</li>
 *   <li>Support compliance / operational monitoring</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthAuditService {

    private final AuthAuditEventRepository repo;

    @Transactional
    public void record(AuthAuditEventType type, boolean success, String subject, String username,
                       HttpServletRequest req, String details) {
        AuthAuditEvent e = new AuthAuditEvent();
        e.setId(UUID.randomUUID());
        e.setEventType(type);
        e.setOccurredAt(Instant.now());
        e.setSuccess(success);
        e.setSubject(subject);
        e.setUsername(username);
        e.setIpAddress(resolveClientIp(req));
        e.setUserAgent(truncate(req.getHeader("User-Agent"), 512));
        e.setDetails(truncate(details, 1024));
        repo.save(e);
    }

    static String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return request.getRemoteAddr();
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }
}
