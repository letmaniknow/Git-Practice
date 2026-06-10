package com.mmva.newsapp.domain.appuser.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for transferring App User Audit Log data.
 * 
 * <p>
 * This DTO is used for exposing audit log information via API responses
 * without directly exposing the entity.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserAuditLogDto {

    /**
     * Unique identifier for the audit log entry.
     */
    private Long appUsersAuditLogId;

    /**
     * ID of the user whose action is being logged.
     */
    private UUID appUsersAuditLogUserId;

    /**
     * The action that was performed (e.g., CREATE, UPDATE, LOGIN, etc.).
     */
    private String appUsersAuditLogAction;

    /**
     * Human-readable description of the action.
     */
    private String appUsersAuditLogActionDescription;

    /**
     * Additional details about the action in JSON format.
     */
    private String appUsersAuditLogDetails;

    /**
     * Timestamp when the action occurred.
     */
    private Instant appUsersAuditLogCreatedAt;

    /**
     * ID of the user/admin who performed the action.
     */
    private UUID appUsersAuditLogCreatedBy;
}
