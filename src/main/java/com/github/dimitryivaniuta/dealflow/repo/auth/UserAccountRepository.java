package com.github.dimitryivaniuta.dealflow.repo.auth;

import com.github.dimitryivaniuta.dealflow.domain.auth.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for password-based user accounts. */
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByUsernameIgnoreCase(String username);
    Optional<UserAccount> findByEmailIgnoreCase(String email);
}
