package com.github.dimitryivaniuta.dealflow.service.auth;

import org.springframework.http.HttpStatus;

/**
 * Base exception for the auth module, carrying an HTTP status.
 */
public class AuthException extends RuntimeException {

    private final HttpStatus status;

    public AuthException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
