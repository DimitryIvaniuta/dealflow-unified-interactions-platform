package com.github.dimitryivaniuta.dealflow.domain.listing;

public enum ListingStatus {
    DRAFT,
    PUBLISHED,
    UNDER_OFFER,
    CLOSED,

    /**
     * Soft-deleted listing.
     * <p>
     * We keep the row for audit/history and to avoid foreign-key issues.
     * Archived listings are excluded from search results unless explicitly requested.
     */
    ARCHIVED
}
