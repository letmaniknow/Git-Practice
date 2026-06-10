package com.mmva.newsapp.domain.adminuser.repository.refreshtokenvalidation;

import com.mmva.newsapp.domain.adminuser.model.refreshtokenvalidation.AdminRefreshTokenValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AdminRefreshTokenValidationRepository extends JpaRepository<AdminRefreshTokenValidation, UUID> {
    Optional<AdminRefreshTokenValidation> findByToken(String token);

    void deleteByToken(String token);
}
