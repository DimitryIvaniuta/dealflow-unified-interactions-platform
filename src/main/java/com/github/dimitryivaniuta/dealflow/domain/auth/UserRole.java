package com.github.dimitryivaniuta.dealflow.domain.auth;

/**
 * Application-level roles used in JWT claims and for role-aware UI navigation.
 *
 * <p>Workspace permissions are still enforced via workspace roles/permissions
 * (see {@code WorkspaceSecurity}), but these global roles are useful for:
 * <ul>
 *   <li>bootstrap users</li>
 *   <li>admin UI navigation</li>
 *   <li>coarse access checks on REST endpoints (optional)</li>
 * </ul>
 */
public enum UserRole {
    OWNER,
    ADMIN,
    AGENT,
    VIEWER
}
