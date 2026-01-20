package com.github.dimitryivaniuta.dealflow.config.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rate limit configuration for the login endpoint.
 *
 * <p>Implemented using Bucket4j with an in-memory cache store (Caffeine).
 * The limit is applied per IP address.
 */
@ConfigurationProperties(prefix = "app.security.rate-limit.login")
public record LoginRateLimitProps(
    boolean enabled,
    long capacity,
    long refillTokens,
    long refillPeriodSeconds
) {
}
