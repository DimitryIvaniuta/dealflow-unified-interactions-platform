package com.github.dimitryivaniuta.dealflow.service.auth;

import org.springframework.http.HttpStatus;

/** Thrown when username/password is invalid. */
public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
}
