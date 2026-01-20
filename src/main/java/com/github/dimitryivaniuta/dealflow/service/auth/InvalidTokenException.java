package com.github.dimitryivaniuta.dealflow.service.auth;

import org.springframework.http.HttpStatus;

/** Thrown when refresh/reset token is invalid, expired or revoked. */
public class InvalidTokenException extends AuthException {
    public InvalidTokenException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
