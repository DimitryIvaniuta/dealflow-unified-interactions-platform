package com.github.dimitryivaniuta.dealflow.domain.timeline;

/**
 * High-level types for a unified customer timeline feed.
 *
 * Keep this list stable: the frontend treats it as a contract.
 */
public enum CustomerEventType {
    EMAIL_SENT,
    EMAIL_RECEIVED,

    CONTACT_ADDED,
    CONTACT_UPDATED,

    TASK_CREATED,
    TASK_COMPLETED,

    TRANSACTION_ACCEPTED,
    TRANSACTION_REJECTED,

    LISTING_SHARED,

    NOTE_ADDED,
    STATUS_CHANGED
}
