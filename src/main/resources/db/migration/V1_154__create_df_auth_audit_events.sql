create table if not exists df_auth_audit_events (
  id           uuid primary key,
  event_type   varchar(64) not null,
  occurred_at  timestamptz not null,
  subject      varchar(128),
  username     varchar(64),
  ip_address   varchar(64),
  user_agent   varchar(512),
  success      boolean not null,
  details      varchar(1024)
);

create index if not exists ix_df_auth_audit_events_type_time
  on df_auth_audit_events (event_type, occurred_at desc);

create index if not exists ix_df_auth_audit_events_subject_time
  on df_auth_audit_events (subject, occurred_at desc);
