package com.github.dimitryivaniuta.dealflow.domain.auth;

/**
 * Audit event types for authentication-related actions.
 */
public enum AuthAuditEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    ACCOUNT_LOCKED,
    TOKEN_REFRESH_SUCCESS,
    TOKEN_REFRESH_FAILURE,
    LOGOUT,
    PASSWORD_RESET_REQUEST,
    PASSWORD_RESET_CONFIRM_SUCCESS,
    PASSWORD_RESET_CONFIRM_FAILURE
}
