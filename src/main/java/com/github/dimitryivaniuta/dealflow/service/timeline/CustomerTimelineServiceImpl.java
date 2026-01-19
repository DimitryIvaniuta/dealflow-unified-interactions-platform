package com.github.dimitryivaniuta.dealflow.service.timeline;

import static org.springframework.data.jpa.domain.Specification.where;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEvent;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventSource;
import com.github.dimitryivaniuta.dealflow.graphql.input.timeline.AppendCustomerEventInput;
import com.github.dimitryivaniuta.dealflow.graphql.input.timeline.CustomerTimelineFilterInput;
import com.github.dimitryivaniuta.dealflow.repo.customer.CustomerRepository;
import com.github.dimitryivaniuta.dealflow.repo.timeline.CustomerEventRepository;
import com.github.dimitryivaniuta.dealflow.service.exception.NotFoundException;
import com.github.dimitryivaniuta.dealflow.service.spec.CustomerEventSpecifications;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerTimelineServiceImpl implements CustomerTimelineService {

    private final CustomerEventRepository events;
    private final CustomerRepository customers;
    private final ObjectMapper om;

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerEvent> timeline(UUID workspaceId, UUID customerId, CustomerTimelineFilterInput filter, Pageable pageable) {
        Specification<CustomerEvent> spec = where(CustomerEventSpecifications.workspace(workspaceId))
                .and(CustomerEventSpecifications.customer(customerId));

        if (filter != null) {
            spec = spec
                    .and(CustomerEventSpecifications.types(filter.types()))
                    .and(CustomerEventSpecifications.categories(filter.categories()))
                    .and(CustomerEventSpecifications.sources(filter.sources()))
                    .and(CustomerEventSpecifications.occurredBetween(filter.from(), filter.to()))
                    .and(CustomerEventSpecifications.text(filter.text()))
                    .and(CustomerEventSpecifications.relatedListing(filter.listingId()))
                    .and(CustomerEventSpecifications.relatedOpportunity(filter.opportunityId()))
                    .and(CustomerEventSpecifications.relatedTransaction(filter.transactionId()));
        }

        // stable order for feed
        return events.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public CustomerEvent append(AppendCustomerEventInput input, String actorSubject) {
        var customer = customers.findByIdAndWorkspace_Id(input.customerId(), input.workspaceId())
                .orElseThrow(() -> new NotFoundException("Customer not found in workspace"));

        JsonNode payload = input.payload() == null ? om.createObjectNode() : input.payload();

        CustomerEvent e = new CustomerEvent();
        e.setWorkspaceId(input.workspaceId());
        e.setCustomer(customer);
        e.setEventType(input.eventType());
        e.setCategory(input.category());
        e.setSource(input.source() == null ? CustomerEventSource.MANUAL : input.source());
        e.setOccurredAt(input.occurredAt() == null ? Instant.now() : input.occurredAt());
        e.setNoTime(Boolean.TRUE.equals(input.noTime()));
        e.setActorSubject(actorSubject == null || actorSubject.isBlank() ? "system" : actorSubject);
        e.setSummary(input.summary());
        e.setPayload(payload);
        e.setListingId(input.listingId());
        e.setOpportunityId(input.opportunityId());
        e.setTransactionId(input.transactionId());

        return events.save(e);
    }
}
