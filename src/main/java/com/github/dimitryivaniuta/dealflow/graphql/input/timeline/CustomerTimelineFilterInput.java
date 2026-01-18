package com.github.dimitryivaniuta.dealflow.graphql.input.timeline;

import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventCategory;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventSource;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventType;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CustomerTimelineFilterInput(
        Set<CustomerEventType> types,
        Set<CustomerEventCategory> categories,
        Set<CustomerEventSource> sources,
        Instant from,
        Instant to,
        String text,
        UUID listingId,
        UUID opportunityId,
        UUID transactionId
) {}
