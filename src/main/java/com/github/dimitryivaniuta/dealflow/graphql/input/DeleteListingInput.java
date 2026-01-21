package com.github.dimitryivaniuta.dealflow.graphql.input;

import java.util.UUID;

/**
 * Soft-delete command for a listing.
 * <p>
 * The listing is moved to {@code ARCHIVED} status instead of being physically deleted.
 */
public record DeleteListingInput(
    UUID workspaceId,
    UUID listingId
) {
}
