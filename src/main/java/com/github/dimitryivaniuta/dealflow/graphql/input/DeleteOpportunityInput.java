package com.github.dimitryivaniuta.dealflow.graphql.input;

import java.util.UUID;

/**
 * Soft-delete command for an opportunity.
 * <p>
 * The opportunity stage is set to {@code ARCHIVED} rather than physically deleting the row.
 */
public record DeleteOpportunityInput(
    UUID workspaceId,
    UUID opportunityId
) {
}
