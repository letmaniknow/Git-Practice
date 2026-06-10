package com.mmva.newsapp.infrastructure.common.audit.repository;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Generic repository interface for ALL audit logs across all features (RBAC,
 * News, Push, Admin).
 * 
 * This interface defines a unified contract that all feature-specific audit
 * repositories must
 * implement. This enables polymorphic audit logging through a single
 * AuditingUtility service.
 * 
 * <h2>Design Pattern</h2>
 * This is a repository interface hierarchy:
 * 
 * <pre>
 * UnifiedAuditLogRepository (Interface - The Contract)
 *     ↓
 *     ├─ RbacAuditLogRepository (RBAC implements it)
 *     ├─ NewsAuditLogRepository (News implements it - future)
 *     ├─ PushAuditLogRepository (Push implements it - future)
 *     └─ AdminAuditLogRepository (Admin implements it - future)
 * </pre>
 * 
 * <h2>Why @NoRepositoryBean?</h2>
 * This annotation tells Spring Data JPA NOT to create a repository bean for
 * this interface.
 * Instead, Spring will use this as a base interface for concrete repository
 * implementations
 * (like RbacAuditLogRepository).
 * 
 * <h2>Type Parameter T</h2>
 * T extends BaseAuditLogEntity - means any entity that extends
 * BaseAuditLogEntity can use this repo.
 * - RbacGeneralAuditLog extends BaseAuditLogEntity ✓
 * - NewsAuditLog extends BaseAuditLogEntity (future) ✓
 * - PushAuditLog extends BaseAuditLogEntity (future) ✓
 * 
 * <h2>Usage in AuditingUtility</h2>
 * 
 * Instead of this (non-polymorphic - won't work):
 * 
 * <pre>
 * public void auditRbac(RbacGeneralAuditLog log, RbacAuditLogRepository repo) { ... }
 * public void auditNews(NewsAuditLog log, NewsAuditLogRepository repo) { ... }
 * public void auditPush(PushAuditLog log, PushAuditLogRepository repo) { ... }
 * </pre>
 * 
 * We use this (polymorphic - one method for all features):
 * 
 * <pre>
 * public void audit(BaseAuditLogEntity log, UnifiedAuditLogRepository repo) {
 *     repo.save(log); // Works for RBAC, News, Push, Admin
 * }
 * </pre>
 * 
 * @param <T> Entity type that extends BaseAuditLogEntity
 * @author MMVA Team
 * @since 1.0.0
 */
@NoRepositoryBean
public interface UnifiedAuditLogRepository<T extends BaseAuditLogEntity>
        extends JpaRepository<T, Long> {

    /**
     * Find all audit logs created by a specific actor (user).
     * 
     * @param actorId  The user ID who performed the action
     * @param pageable Pagination information
     * @return Page of audit logs for this actor, newest first
     */
    Page<T> findByActorId(UUID actorId, Pageable pageable);

    /**
     * Find all audit logs for a specific action type.
     * 
     * @param action   The action name (e.g., "ROLE_CREATED", "NEWS_PUBLISHED")
     * @param pageable Pagination information
     * @return Page of audit logs for this action, newest first
     */
    Page<T> findByAction(String action, Pageable pageable);

    /**
     * Find all audit logs with a specific severity level.
     * 
     * @param severity The severity level (CRITICAL, HIGH, MEDIUM, LOW)
     * @param pageable Pagination information
     * @return Page of audit logs with this severity, newest first
     */
    Page<T> findBySeverity(String severity, Pageable pageable);

    /**
     * Find all audit logs within a date range.
     * 
     * @param startDate Start of the range (inclusive)
     * @param endDate   End of the range (inclusive)
     * @param pageable  Pagination information
     * @return Page of audit logs within the range, newest first
     */
    Page<T> findByCreatedAtBetween(Instant startDate, Instant endDate, Pageable pageable);

    /**
     * Find all audit logs that are part of a multi-step transaction.
     * 
     * Useful for correlating multiple audit logs that were part of a single
     * business operation (e.g., "grant permission to role AND assign role to
     * user").
     * 
     * @param transactionId The transaction ID
     * @return List of all audit logs in this transaction (no pagination needed)
     */
    List<T> findByTransactionId(UUID transactionId);

    /**
     * Find all audit logs where the operation FAILED.
     * 
     * Useful for compliance reports: "Which operations failed and why?"
     * 
     * @param pageable Pagination information
     * @return Page of failed operations, newest first
     */
    Page<T> findByIsSuccessFalse(Pageable pageable);
}
