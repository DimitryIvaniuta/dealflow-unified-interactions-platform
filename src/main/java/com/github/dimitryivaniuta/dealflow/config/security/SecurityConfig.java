package com.github.dimitryivaniuta.dealflow.config.security;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration.
 *
 * <p>Key points:
 * <ul>
 *   <li>{@code /api/auth/**} is public (login/reset/refresh) and guarded by a rate limiter.</li>
 *   <li>{@code /graphql} requires authentication. Fine-grained authorization is still enforced
 *       by method security on resolvers (workspace RBAC).</li>
 *   <li>JWT roles claim is mapped to Spring authorities for UI hints (role-aware navigation).</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginRateLimitFilter loginRateLimitFilter)
            throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // GraphQL endpoint itself is protected by method security on resolvers (RBAC)
                        .requestMatchers(HttpMethod.POST, "/graphql").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                )
                .build();
    }

    /**
     * Maps token roles to authorities.
     *
     * <p>Supported claim layouts:
     * <ul>
     *   <li>{@code roles: ["ADMIN","VIEWER"]}</li>
     *   <li>{@code realm_access: { roles: [...] }} (Keycloak style)</li>
     * </ul>
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object rolesObj = jwt.getClaims().get("roles");
            if (rolesObj == null) {
                Object realm = jwt.getClaims().get("realm_access");
                if (realm instanceof Map<?, ?> m) {
                    rolesObj = m.get("roles");
                }
            }
            if (rolesObj instanceof Collection<?> roles) {
                return roles.stream()
                        .map(String::valueOf)
                        .map(String::toUpperCase)
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toSet());
            }
            return java.util.Set.of();
        });
        return c;
    }
}
