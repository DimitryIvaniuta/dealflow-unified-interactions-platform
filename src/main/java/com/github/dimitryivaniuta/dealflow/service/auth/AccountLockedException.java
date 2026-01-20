package com.github.dimitryivaniuta.dealflow.service.auth;

import java.time.Instant;
import org.springframework.http.HttpStatus;

/** Thrown when account is temporarily locked due to repeated failures. */
public class AccountLockedException extends AuthException {
    public AccountLockedException(Instant lockedUntil) {
        super(HttpStatus.LOCKED, "Account locked until " + lockedUntil);
    }

    /** Convenience constructor for non-time-based locks (e.g. DISABLED/BANNED). */
    public AccountLockedException(String message) {
        super(HttpStatus.LOCKED, message);
    }
}
