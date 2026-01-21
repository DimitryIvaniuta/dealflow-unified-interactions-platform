insert into df_user_accounts (
    id, username, email, display_name, password_hash, status,
    failed_attempts, locked_until, last_login_at,
    row_version, created_at, created_by, updated_at, updated_by
) values (
             gen_random_uuid(),
             'admin',
             'admin@example.com',
             'Admin',
             crypt('ChangeMe-123!', gen_salt('bf', 12)),
             'ACTIVE',
             0, null, null,
             0, now(), 'system', now(), 'system'
         );