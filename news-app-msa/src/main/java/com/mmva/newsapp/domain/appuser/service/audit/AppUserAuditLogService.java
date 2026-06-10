package com.mmva.newsapp.domain.appuser.service.audit;

import com.mmva.newsapp.domain.appuser.model.audit.AppUserAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing app user audit logs.
 * 
 * <p>
 * Provides operations for logging and retrieving audit trail
 * of app user profile changes.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface AppUserAuditLogService {

    /**
     * Logs an action performed on an app user profile.
     *
     * @param userId    the UUID of the user
     * @param action    the action performed
     * @param details   additional details about the action
     * @param createdBy the UUID of the user who performed the action
     */
    void logAction(UUID userId, String action, String details, UUID createdBy);

    /**
     * Retrieves all audit logs for a specific user.
     *
     * @param userId the UUID of the user
     * @return list of audit logs for the user
     */
    List<AppUserAuditLog> findByUserId(UUID userId);

    /**
     * Retrieves paginated audit logs for a specific user, ordered by
     * createdAt descending.
     *
     * @param userId   the UUID of the user
     * @param pageable pagination information
     * @return page of audit logs for the user
     */
    Page<AppUserAuditLog> findByUserIdPaginated(UUID userId, Pageable pageable);
}
