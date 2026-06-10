package com.mmva.newsapp.domain.adminuser.model.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditLogEntity;
import com.mmva.newsapp.infrastructure.common.audit.model.DomainAuditLog;

import java.util.UUID;

/**
 * Unified audit log entity for tracking admin user changes.
 * 
 * Extends BaseAuditLogEntity to inherit 24 common audit fields.
 * Implements DomainAuditLog to support polymorphic audit operations.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "admin_user_audit_log", indexes = {
        @Index(name = "idx_admin_user_audit_log_admin_id", columnList = "admin_id"),
        @Index(name = "idx_admin_user_audit_log_action", columnList = "action"),
        @Index(name = "idx_admin_user_audit_log_created_at", columnList = "created_at"),
        @Index(name = "idx_admin_user_audit_log_actor_id", columnList = "actor_id"),
        @Index(name = "idx_admin_user_audit_log_severity", columnList = "severity"),
        @Index(name = "idx_admin_user_audit_log_is_success", columnList = "is_success"),
        @Index(name = "idx_admin_user_audit_log_ip_address", columnList = "ip_address"),
        // Composite indexes for common query patterns
        @Index(name = "idx_admin_user_audit_created_at_action_success", columnList = "created_at DESC, action, is_success"),
        @Index(name = "idx_admin_user_audit_created_at_actor", columnList = "created_at DESC, actor_id"),
        @Index(name = "idx_admin_user_audit_severity_success", columnList = "severity, is_success"),
        @Index(name = "idx_admin_user_audit_transaction_created", columnList = "transaction_id, created_at DESC"),
        @Index(name = "idx_admin_user_audit_admin_created", columnList = "admin_id, created_at DESC")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminUserAuditLog extends BaseAuditLogEntity implements DomainAuditLog {

    /**
     * Primary key (auto-generated).
     * Each @Entity must declare at least one @Id, even when
     * extending @MappedSuperclass.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The admin user ID being audited.
     * This is the domain-specific field that tracks which admin was affected.
     */
    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Override
    public void setDomainSpecificField(UUID resourceId) {
        this.adminId = resourceId;
    }
}
