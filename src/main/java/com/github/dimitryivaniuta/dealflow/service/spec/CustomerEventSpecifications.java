package com.github.dimitryivaniuta.dealflow.service.spec;

import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEvent;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventCategory;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventSource;
import com.github.dimitryivaniuta.dealflow.domain.timeline.CustomerEventType;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class CustomerEventSpecifications {

    private CustomerEventSpecifications() {}

    public static Specification<CustomerEvent> workspace(UUID workspaceId) {
        return (root, q, cb) -> cb.equal(root.get("workspaceId"), workspaceId);
    }

    public static Specification<CustomerEvent> customer(UUID customerId) {
        return (root, q, cb) -> cb.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<CustomerEvent> types(Set<CustomerEventType> types) {
        if (types == null || types.isEmpty()) return null;
        return (root, q, cb) -> root.get("eventType").in(types);
    }

    public static Specification<CustomerEvent> categories(Set<CustomerEventCategory> cats) {
        if (cats == null || cats.isEmpty()) return null;
        return (root, q, cb) -> root.get("category").in(cats);
    }

    public static Specification<CustomerEvent> sources(Set<CustomerEventSource> sources) {
        if (sources == null || sources.isEmpty()) return null;
        return (root, q, cb) -> root.get("source").in(sources);
    }

    public static Specification<CustomerEvent> occurredBetween(Instant from, Instant to) {
        if (from == null && to == null) return null;
        return (root, q, cb) -> {
            if (from != null && to != null) return cb.between(root.get("occurredAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("occurredAt"), from);
            return cb.lessThanOrEqualTo(root.get("occurredAt"), to);
        };
    }

    public static Specification<CustomerEvent> text(String text) {
        if (text == null || text.isBlank()) return null;
        String like = "%" + text.trim().toLowerCase() + "%";
        return (root, q, cb) -> cb.like(cb.lower(root.get("summary")), like);
    }

    public static Specification<CustomerEvent> relatedListing(UUID listingId) {
        if (listingId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("listingId"), listingId);
    }

    public static Specification<CustomerEvent> relatedOpportunity(UUID opportunityId) {
        if (opportunityId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("opportunityId"), opportunityId);
    }

    public static Specification<CustomerEvent> relatedTransaction(UUID transactionId) {
        if (transactionId == null) return null;
        return (root, q, cb) -> cb.equal(root.get("transactionId"), transactionId);
    }
}
