package com.github.dimitryivaniuta.dealflow.service.auth;

import org.springframework.http.HttpStatus;

/** Thrown when a password does not meet the configured policy. */
public class PasswordPolicyException extends AuthException {
    public PasswordPolicyException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
