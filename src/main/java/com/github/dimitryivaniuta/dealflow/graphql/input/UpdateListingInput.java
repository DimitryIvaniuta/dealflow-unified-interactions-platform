package com.github.dimitryivaniuta.dealflow.graphql.input;

import com.github.dimitryivaniuta.dealflow.domain.listing.ListingStatus;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Patch-style update for a Listing.
 * <p>
 * Only non-null fields are applied. For nullable relations, use explicit clear flags to avoid
 * ambiguity between "not provided" and "set to null" in GraphQL.
 */
public record UpdateListingInput(
    UUID workspaceId,
    UUID listingId,
    String title,
    String city,
    BigDecimal askingPrice,
    ListingStatus status,
    UUID customerId,
    Boolean clearCustomer
) {
}
