package com.github.dimitryivaniuta.dealflow.graphql.input.timeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventCategory;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventSource;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventType;
import java.time.Instant;
import java.util.UUID;

public record AppendCustomerEventInput(
        UUID workspaceId,
        UUID customerId,

        CustomerEventType eventType,
        CustomerEventCategory category,
        CustomerEventSource source,

        Instant occurredAt,
        Boolean noTime,

        String summary,
        JsonNode payload,

        UUID listingId,
        UUID opportunityId,
        UUID transactionId
) {}
