package com.github.dimitryivaniuta.dealflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Dealflow Platform backend.
 *
 * <p>This application uses:
 * <ul>
 *   <li>Spring Security OAuth2 Resource Server for JWT authentication</li>
 *   <li>GraphQL SPQR for code-first schema generation</li>
 *   <li>Flyway for schema migrations</li>
 *   <li>JPA/Hibernate for persistence</li>
 * </ul>
 *
 * <p>{@link ConfigurationPropertiesScan} is enabled so strongly-typed configuration records
 * (JWT keys, rate limits, password policy, etc.) are auto-registered.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class DealflowPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(DealflowPlatformApplication.class, args);
    }
}
