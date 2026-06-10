package com.mmva.newsapp.domain.adminuser.service.audit;

import com.mmva.newsapp.domain.adminuser.model.audit.AdminUserAuditLog;
import com.mmva.newsapp.domain.adminuser.repository.audit.AdminUserAuditLogRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing admin user audit logs.
 * 
 * <p>
 * Table: admin_users_audit_log
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AdminUserAuditLogServiceImpl implements AdminUserAuditLogService {
    private final AdminUserAuditLogRepository auditLogRepository;

    /**
     * Logs an action performed on an admin user.
     * Uses REQUIRES_NEW propagation to run in a separate transaction,
     * preventing audit log failures from rolling back the main transaction.
     *
     * @param adminUserId the UUID of the admin user
     * @param action      the action performed
     * @param details     additional details about the action
     * @param createdBy   the UUID of the user who performed the action
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(UUID adminUserId, String action, String details, UUID createdBy) {
        AdminUserAuditLog log = AdminUserAuditLog.builder()
                .adminId(adminUserId)
                .action(action)
                .reason(details)
                .actorId(createdBy)
                .createdAt(Instant.now())
                .ipAddress("UNKNOWN")
                .userAgent("UNKNOWN")
                .requestUri("")
                .requestMethod("")
                .severity("MEDIUM")
                .resourceName("")
                .build();
        auditLogRepository.save(log);
    }

    /**
     * Retrieves all audit logs for a specific admin user.
     *
     * @param adminUserId the UUID of the admin user
     * @return list of audit logs for the admin user
     */
    @Override
    public List<AdminUserAuditLog> findByAdminUserId(UUID adminUserId) {
        return auditLogRepository
                .findByAdminIdOrderByCreatedAtDesc(adminUserId, org.springframework.data.domain.PageRequest.of(0, 100))
                .getContent();
    }
}
