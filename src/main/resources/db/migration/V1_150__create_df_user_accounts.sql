create table if not exists df_user_accounts (
  id                uuid primary key,
  username          varchar(64)  not null,
  email             varchar(256) not null,
  display_name      varchar(256) not null,

  password_hash     varchar(255) not null,
  status            varchar(32)  not null,
  failed_attempts   int          not null default 0,
  locked_until      timestamptz,
  last_login_at     timestamptz,

  row_version       bigint       not null,
  created_at        timestamptz  not null,
  created_by        varchar(128) not null,
  updated_at        timestamptz  not null,
  updated_by        varchar(128) not null
);

create unique index if not exists ux_df_user_accounts_username_ci
  on df_user_accounts (lower(username));

create unique index if not exists ux_df_user_accounts_email_ci
  on df_user_accounts (lower(email));
