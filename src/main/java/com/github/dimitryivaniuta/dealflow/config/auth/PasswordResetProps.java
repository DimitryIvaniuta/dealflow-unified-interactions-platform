package com.github.dimitryivaniuta.dealflow.config.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Password reset configuration.
 *
 * <p>Production-grade behaviour:
 * <ul>
 *   <li>Reset request endpoint does not reveal whether the user exists</li>
 *   <li>Reset token is stored hashed in the DB; only the plaintext token is ever returned/sent</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "app.security.password.reset")
public record PasswordResetProps(
    long tokenTtlMinutes,
    boolean exposeToken
) {
}
