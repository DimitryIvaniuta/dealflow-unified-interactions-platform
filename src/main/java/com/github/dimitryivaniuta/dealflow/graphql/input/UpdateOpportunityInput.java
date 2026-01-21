package com.github.dimitryivaniuta.dealflow.graphql.input;

import com.github.dimitryivaniuta.dealflow.domain.pipeline.OpportunityStage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Patch-style update for an Opportunity.
 * <p>
 * Only non-null fields are applied. For nullable relations, use clear flags to remove links.
 */
public record UpdateOpportunityInput(
    UUID workspaceId,
    UUID opportunityId,
    String title,
    BigDecimal amount,
    LocalDate expectedCloseDate,
    OpportunityStage stage,
    UUID customerId,
    UUID ownerMemberId,
    Boolean clearCustomer,
    Boolean clearOwner
) {
}
