create table if not exists df_password_reset_tokens (
  id           uuid primary key,
  user_id      uuid not null references df_user_accounts(id) on delete cascade,
  token_hash   varchar(255) not null,
  expires_at   timestamptz not null,
  used_at      timestamptz,
  created_at   timestamptz not null
);

create index if not exists ix_df_password_reset_tokens_user
  on df_password_reset_tokens (user_id);

create index if not exists ix_df_password_reset_tokens_expires
  on df_password_reset_tokens (expires_at);
