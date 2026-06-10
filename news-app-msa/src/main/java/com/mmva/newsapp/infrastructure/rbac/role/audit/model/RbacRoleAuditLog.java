package com.mmva.newsapp.infrastructure.rbac.role.audit.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditLogEntity;
import com.mmva.newsapp.infrastructure.common.audit.model.DomainAuditLog;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Audit log entity for tracking role changes in RBAC domain.
 * 
 * Implements {@code DomainAuditLog} to auto-populate roleId.
 * Includes comprehensive indexes for efficient audit queries and forensic
 * analysis.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rbac_role_audit_log", indexes = {
        // Single column indexes for basic queries
        @Index(name = "idx_rbac_role_audit_created_at", columnList = "created_at"),
        @Index(name = "idx_rbac_role_audit_action", columnList = "action"),
        @Index(name = "idx_rbac_role_audit_role_id", columnList = "role_id"),
        @Index(name = "idx_rbac_role_audit_actor_id", columnList = "actor_id"),
        @Index(name = "idx_rbac_role_audit_severity", columnList = "severity"),
        @Index(name = "idx_rbac_role_audit_is_success", columnList = "is_success"),

        // Composite indexes for common query patterns
        @Index(name = "idx_rbac_role_audit_created_at_action_success", columnList = "created_at DESC, action, is_success"),
        @Index(name = "idx_rbac_role_audit_created_at_actor", columnList = "created_at DESC, actor_id"),
        @Index(name = "idx_rbac_role_audit_severity_success", columnList = "severity, is_success"),
        @Index(name = "idx_rbac_role_audit_transaction_created", columnList = "transaction_id, created_at DESC"),
        @Index(name = "idx_rbac_role_audit_role_created", columnList = "role_id, created_at DESC")
})
public class RbacRoleAuditLog extends BaseAuditLogEntity implements DomainAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The role ID that was affected by this audit event.
     * NOT NULL: Every role audit must reference the role being modified.
     */
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Override
    public void setDomainSpecificField(UUID resourceId) {
        this.roleId = resourceId;
    }
}
