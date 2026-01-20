create table if not exists df_user_account_roles (
  user_id uuid not null references df_user_accounts(id) on delete cascade,
  role    varchar(64) not null,
  primary key (user_id, role)
);

create index if not exists ix_df_user_roles_role
  on df_user_account_roles (role);
