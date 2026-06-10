package com.mmva.newsapp.domain.adminuser.repository.audit;

import com.mmva.newsapp.domain.adminuser.model.audit.AdminUserAuditLog;
import com.mmva.newsapp.infrastructure.common.audit.repository.UnifiedAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Repository for admin user audit logs.
 * Extends UnifiedAuditLogRepository for polymorphic audit operations.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface AdminUserAuditLogRepository extends UnifiedAuditLogRepository<AdminUserAuditLog> {
    /**
     * Finds audit logs for a specific admin user.
     * 
     * @param adminId  admin user UUID
     * @param pageable pagination info
     * @return page of audit logs ordered by creation date descending
     */
    Page<AdminUserAuditLog> findByAdminIdOrderByCreatedAtDesc(UUID adminId, Pageable pageable);
}
