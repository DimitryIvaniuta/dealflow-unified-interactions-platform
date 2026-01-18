create table if not exists df_customer_events
(
    id              uuid primary key,

    workspace_id     uuid         not null,
    customer_id      uuid         not null,

    event_type       varchar(64)  not null,
    category         varchar(64)  not null,
    source           varchar(64)  not null,

    occurred_at      timestamptz  not null,
    no_time          boolean      not null default false,

    actor_subject    varchar(128) not null,
    summary          varchar(512) not null,

    payload          jsonb        not null default '{}'::jsonb,

    listing_id       uuid,
    opportunity_id   uuid,
    transaction_id   uuid,

    row_version      bigint       not null,
    created_at       timestamptz  not null,
    created_by       varchar(128) not null,
    updated_at       timestamptz  not null,
    updated_by       varchar(128) not null,

    constraint df_customer_events_fk_customer
        foreign key (customer_id) references df_customers (id) on delete cascade
);

create index if not exists ix_df_customer_events_ws_customer_time
    on df_customer_events (workspace_id, customer_id, occurred_at desc);

create index if not exists ix_df_customer_events_ws_type
    on df_customer_events (workspace_id, event_type);

create index if not exists ix_df_customer_events_ws_category
    on df_customer_events (workspace_id, category);

create index if not exists ix_df_customer_events_ws_listing
    on df_customer_events (workspace_id, listing_id);

create index if not exists ix_df_customer_events_ws_opportunity
    on df_customer_events (workspace_id, opportunity_id);

create index if not exists ix_df_customer_events_ws_transaction
    on df_customer_events (workspace_id, transaction_id);

create index if not exists ix_df_customer_events_payload_gin
    on df_customer_events using gin (payload);
