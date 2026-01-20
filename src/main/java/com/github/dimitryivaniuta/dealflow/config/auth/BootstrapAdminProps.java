package com.github.dimitryivaniuta.dealflow.config.auth;

import com.github.dimitryivaniuta.dealflow.domain.auth.UserRole;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Optional bootstrap admin user.
 *
 * <p>Disabled by default. Enable only in dev/test environments.
 *
 * <p>In production you typically provision users via an admin UI or an external IdP.
 */
@ConfigurationProperties(prefix = "app.security.bootstrap.admin")
public record BootstrapAdminProps(
    boolean enabled,
    String username,
    String email,
    String displayName,
    String password,
    Set<UserRole> roles
) {
}
