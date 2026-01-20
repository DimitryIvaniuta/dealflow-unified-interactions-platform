package com.github.dimitryivaniuta.dealflow.config.auth;

import com.github.dimitryivaniuta.dealflow.domain.auth.UserAccount;
import com.github.dimitryivaniuta.dealflow.domain.auth.UserAccountStatus;
import com.github.dimitryivaniuta.dealflow.repo.auth.UserAccountRepository;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Optional development bootstrap for a first admin user.
 *
 * <p>Why this exists:
 * <ul>
 *   <li>GraphQL/RBAC integration tests use mock JWTs and do not require a DB user.</li>
 *   <li>For local manual testing of {@code /api/auth/login}, it's convenient to have
 *       a default user without creating one through an admin UI.</li>
 * </ul>
 *
 * <p>Security note: This is disabled by default. Enable only in dev environments.
 */
@Configuration
@RequiredArgsConstructor
public class AdminBootstrapRunner {

    private final BootstrapAdminProps props;
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner bootstrapAdminUser() {
        return args -> {
            if (!props.enabled()) return;

            if (users.findByUsernameIgnoreCase(props.username()).isPresent()) {
                return;
            }

            UserAccount u = new UserAccount();
            u.setUsername(props.username());
            u.setEmail(props.email());
            u.setDisplayName(props.displayName());
            u.setStatus(UserAccountStatus.ACTIVE);
            u.setPasswordHash(passwordEncoder.encode(props.password()));
            u.setRoles(new HashSet<>(props.roles()));

            users.save(u);
        };
    }
}
