create table if not exists df_refresh_tokens (
  id          uuid primary key,
  user_id     uuid not null references df_user_accounts(id) on delete cascade,
  token_hash  varchar(255) not null,
  expires_at  timestamptz not null,
  revoked     boolean not null default false,
  created_at  timestamptz not null
);

create index if not exists ix_df_refresh_tokens_user
  on df_refresh_tokens (user_id);

create index if not exists ix_df_refresh_tokens_expires
  on df_refresh_tokens (expires_at);
