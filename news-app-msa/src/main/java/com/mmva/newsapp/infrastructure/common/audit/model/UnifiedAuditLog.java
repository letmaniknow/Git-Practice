package com.mmva.newsapp.infrastructure.common.audit.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Unified audit log entity for ALL features (RBAC, News, Push, Admin).
 * 
 * <p>
 * Single concrete @Entity that maps to the {@code audit_log} table.
 * All audit records for all domains write to this single table.
 * Features are distinguished by the {@code domain} field (RBAC, NEWS, PUSH,
 * ADMIN).
 * </p>
 * 
 * <p>
 * Inherits 24 audit fields from {@link BaseAuditLogEntity}:
 * WHO, WHAT, WHICH, WHEN, WHERE, WHY, HOW, CORRELATE & RISK.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_created_at", columnList = "created_at"),
        @Index(name = "idx_audit_actor_id", columnList = "actor_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_domain", columnList = "domain"),
        @Index(name = "idx_audit_source", columnList = "source"),
        @Index(name = "idx_audit_severity", columnList = "severity"),
        @Index(name = "idx_audit_resource_id", columnList = "resource_id"),
        @Index(name = "idx_audit_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_audit_session_id", columnList = "session_id"),
        @Index(name = "idx_audit_is_success", columnList = "is_success")
})
public class UnifiedAuditLog extends BaseAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
