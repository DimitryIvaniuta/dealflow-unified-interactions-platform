package com.github.dimitryivaniuta.dealflow.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Auth-related beans.
 *
 * <p>We keep these beans in a dedicated config so the auth module stays self-contained.
 * The application uses BCrypt for password hashing (one-way), which is the standard
 * production choice for password-based auth.
 */
@Configuration
public class AuthBeansConfig {

    /**
     * BCrypt encoder used for password hashing.
     *
     * <p>Cost factor 12 is a good baseline. Increase if your latency budget allows it.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
