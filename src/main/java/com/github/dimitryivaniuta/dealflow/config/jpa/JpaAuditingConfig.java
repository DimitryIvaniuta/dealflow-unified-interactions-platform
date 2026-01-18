package com.github.dimitryivaniuta.dealflow.config.jpa;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("system"); // prevents NOT NULL audit failures
            }
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                return Optional.ofNullable(jwtAuth.getToken().getSubject()).or(() -> Optional.of(jwtAuth.getName()));
            }
            return Optional.ofNullable(auth.getName()).or(() -> Optional.of("system"));
        };
    }
}
