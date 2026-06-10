package com.mmva.newsapp.infrastructure.common.config;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Auditing Configuration.
 * 
 * <p>
 * Enables automatic population of audit fields in entities that extend
 * {@link BaseAuditEntity}:
 * </p>
 * <ul>
 * <li>{@code @CreatedDate} - createdAt</li>
 * <li>{@code @LastModifiedDate} - updatedAt</li>
 * <li>{@code @CreatedBy} - createdBy</li>
 * <li>{@code @LastModifiedBy} - updatedBy</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    /**
     * Provides the current auditor (user) for audit fields.
     * 
     * <p>
     * Currently returns empty Optional since this application doesn't have
     * Spring Security context. The createdBy/updatedBy fields are set manually
     * in service layer methods.
     * </p>
     * 
     * <p>
     * TODO: When Spring Security is integrated, update this to extract the
     * current user from SecurityContextHolder:
     * </p>
     * 
     * <pre>{@code
     * return Optional.ofNullable(SecurityContextHolder.getContext())
     *         .map(SecurityContext::getAuthentication)
     *         .filter(Authentication::isAuthenticated)
     *         .map(auth -> UUID.fromString(auth.getName()));
     * }</pre>
     * 
     * @return AuditorAware that provides the current user's UUID
     */
    @Bean
    public AuditorAware<UUID> auditorAware() {
        // Return empty - createdBy/updatedBy are set manually in services
        // This allows @CreatedDate and @LastModifiedDate to work automatically
        return () -> Optional.empty();
    }
}
