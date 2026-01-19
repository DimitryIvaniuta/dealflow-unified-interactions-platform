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

    /**
     * Frontend expects a single "search" box to match both the rendered summary and common payload fields.
     *
     * We intentionally do NOT use payload::text here (poor indexability & potentially expensive).
     * Instead, we search summary plus a curated set of high-value keys.
     */
    public static Specification<CustomerEvent> text(String text) {
        if (text == null || text.isBlank()) return null;
        String like = "%" + text.trim().toLowerCase() + "%";

        return (root, q, cb) -> {
            var summary = cb.lower(root.get("summary"));

            // jsonb_extract_path_text(payload, 'key') returns null if key missing
            var note = cb.lower(cb.coalesce(
                    cb.function("jsonb_extract_path_text", String.class, root.get("payload"), cb.literal("note")),
                    ""));
            var subject = cb.lower(cb.coalesce(
                    cb.function("jsonb_extract_path_text", String.class, root.get("payload"), cb.literal("subject")),
                    ""));
            var to = cb.lower(cb.coalesce(
                    cb.function("jsonb_extract_path_text", String.class, root.get("payload"), cb.literal("to")),
                    ""));
            var title = cb.lower(cb.coalesce(
                    cb.function("jsonb_extract_path_text", String.class, root.get("payload"), cb.literal("title")),
                    ""));
            var name = cb.lower(cb.coalesce(
                    cb.function("jsonb_extract_path_text", String.class, root.get("payload"), cb.literal("name")),
                    ""));

            return cb.or(
                    cb.like(summary, like),
                    cb.like(note, like),
                    cb.like(subject, like),
                    cb.like(to, like),
                    cb.like(title, like),
                    cb.like(name, like)
            );
        };
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
