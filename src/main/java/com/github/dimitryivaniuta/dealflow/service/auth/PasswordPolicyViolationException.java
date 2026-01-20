package com.github.dimitryivaniuta.dealflow.service.auth;

import org.springframework.http.HttpStatus;

/** Thrown when password does not satisfy configured policy. */
public class PasswordPolicyViolationException extends AuthException {
    public PasswordPolicyViolationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
