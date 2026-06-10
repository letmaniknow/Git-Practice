package com.mmva.newsapp.domain.appuser.model.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit log entity for tracking app user profile changes.
 * 
 * <p>
 * Table: app_users_audit_log
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * 
 * <p>
 * Note: This entity does NOT extend BaseAuditLogEntity to maintain
 * feature-specific column naming convention.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_users_audit_log", indexes = {
        @Index(name = "idx_app_users_audit_log_user_id", columnList = "app_users_audit_log_user_id"),
        @Index(name = "idx_app_users_audit_log_action", columnList = "app_users_audit_log_action"),
        @Index(name = "idx_app_users_audit_log_created_at", columnList = "app_users_audit_log_created_at"),
        @Index(name = "idx_app_users_audit_log_created_by", columnList = "app_users_audit_log_created_by")
})
public class AppUserAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_users_audit_log_id")
    private Long appUsersAuditLogId;

    @Column(name = "app_users_audit_log_user_id", nullable = false)
    private UUID appUsersAuditLogUserId;

    @Column(name = "app_users_audit_log_action", nullable = false)
    private String appUsersAuditLogAction;

    @Column(name = "app_users_audit_log_details", columnDefinition = "TEXT")
    private String appUsersAuditLogDetails;

    @Column(name = "app_users_audit_log_created_at", nullable = false, updatable = false)
    private Instant appUsersAuditLogCreatedAt;

    @Column(name = "app_users_audit_log_created_by")
    private UUID appUsersAuditLogCreatedBy;
}
