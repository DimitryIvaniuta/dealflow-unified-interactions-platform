package com.github.dimitryivaniuta.dealflow.config.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Password policy used for user creation and password reset.
 *
 * <p>Designed to be simple, deterministic and interview-friendly:
 * min length + require upper/lower/digit/special.
 */
@ConfigurationProperties(prefix = "app.security.password.policy")
public record PasswordPolicyProps(
    int minLength,
    boolean requireUpper,
    boolean requireLower,
    boolean requireDigit,
    boolean requireSpecial
) {
}
