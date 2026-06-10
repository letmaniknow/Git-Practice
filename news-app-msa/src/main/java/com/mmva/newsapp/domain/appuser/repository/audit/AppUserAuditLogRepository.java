package com.mmva.newsapp.domain.appuser.repository.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mmva.newsapp.domain.appuser.model.audit.AppUserAuditLog;

import java.util.UUID;
import java.util.List;

/**
 * Repository for AppUserAuditLog entity.
 * 
 * <p>
 * Table: app_users_audit_log
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface AppUserAuditLogRepository extends JpaRepository<AppUserAuditLog, Long> {

    /**
     * Find all audit logs for a specific user.
     */
    List<AppUserAuditLog> findByAppUsersAuditLogUserId(UUID userId);

    /**
     * Paginated query for user audit logs ordered by createdAt descending.
     */
    Page<AppUserAuditLog> findByAppUsersAuditLogUserIdOrderByAppUsersAuditLogCreatedAtDesc(UUID userId,
            Pageable pageable);
}
