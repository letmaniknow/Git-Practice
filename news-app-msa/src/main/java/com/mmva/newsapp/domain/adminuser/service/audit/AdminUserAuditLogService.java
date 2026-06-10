package com.mmva.newsapp.domain.adminuser.service.audit;

import com.mmva.newsapp.domain.adminuser.model.audit.AdminUserAuditLog;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing admin user audit logs.
 * 
 * <p>
 * Provides operations for logging and retrieving admin user actions
 * for security auditing and compliance purposes.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 * @see AdminUserAuditLogServiceImpl for the implementation
 */
public interface AdminUserAuditLogService {

    /**
     * Logs an action performed on an admin user.
     *
     * @param adminUserId the UUID of the admin user
     * @param action      the action performed
     * @param details     additional details about the action
     * @param createdBy   the UUID of the user who performed the action
     */
    void logAction(UUID adminUserId, String action, String details, UUID createdBy);

    /**
     * Retrieves all audit logs for a specific admin user.
     *
     * @param adminUserId the UUID of the admin user
     * @return list of audit logs for the admin user
     */
    List<AdminUserAuditLog> findByAdminUserId(UUID adminUserId);
}
