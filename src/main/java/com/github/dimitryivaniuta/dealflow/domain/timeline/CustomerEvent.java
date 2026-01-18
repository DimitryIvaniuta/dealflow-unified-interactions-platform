package com.github.dimitryivaniuta.dealflow.domain.timeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.dimitryivaniuta.dealflow.domain.common.AuditedEntity;
import com.github.dimitryivaniuta.dealflow.domain.customer.Customer;
import io.leangen.graphql.annotations.GraphQLQuery;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(
    name = "df_customer_events",
    indexes = {
        @Index(name = "ix_df_customer_events_ws_customer_time", columnList = "workspace_id,customer_id,occurred_at"),
        @Index(name = "ix_df_customer_events_ws_type", columnList = "workspace_id,event_type"),
        @Index(name = "ix_df_customer_events_ws_category", columnList = "workspace_id,category"),
        @Index(name = "ix_df_customer_events_ws_listing", columnList = "workspace_id,listing_id"),
        @Index(name = "ix_df_customer_events_ws_opportunity", columnList = "workspace_id,opportunity_id"),
        @Index(name = "ix_df_customer_events_ws_transaction", columnList = "workspace_id,transaction_id")
    }
)
public class CustomerEvent extends AuditedEntity {

    @Column(name = "workspace_id", nullable = false)
    @GraphQLQuery
    private UUID workspaceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    @GraphQLQuery
    private CustomerEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 64)
    @GraphQLQuery
    private CustomerEventCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 64)
    @GraphQLQuery
    private CustomerEventSource source;

    @Column(name = "occurred_at", nullable = false)
    @GraphQLQuery
    private Instant occurredAt;

    @Column(name = "no_time", nullable = false)
    @GraphQLQuery
    private boolean noTime;

    @Column(name = "actor_subject", nullable = false, length = 128)
    @GraphQLQuery
    private String actorSubject;

    @Column(name = "summary", nullable = false, length = 512)
    @GraphQLQuery
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @GraphQLQuery
    private JsonNode payload;

    @Column(name = "listing_id")
    @GraphQLQuery
    private UUID listingId;

    @Column(name = "opportunity_id")
    @GraphQLQuery
    private UUID opportunityId;

    @Column(name = "transaction_id")
    @GraphQLQuery
    private UUID transactionId;
}
