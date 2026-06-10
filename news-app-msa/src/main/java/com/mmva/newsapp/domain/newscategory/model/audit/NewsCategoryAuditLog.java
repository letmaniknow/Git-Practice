package com.mmva.newsapp.domain.newscategory.model.audit;

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
 * Audit log entity for news category operations.
 * Tracks all create, update, delete, and restore actions on news categories.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "news_categories_audit_log", indexes = {
        @Index(name = "idx_news_categories_audit_created_at", columnList = "created_at"),
        @Index(name = "idx_news_categories_audit_actor_id", columnList = "actor_id"),
        @Index(name = "idx_news_categories_audit_action", columnList = "action"),
        @Index(name = "idx_news_categories_audit_category_id", columnList = "news_category_id"),
        @Index(name = "idx_news_categories_audit_severity", columnList = "severity"),
        @Index(name = "idx_news_categories_audit_is_success", columnList = "is_success"),
        @Index(name = "idx_news_categories_audit_transaction_id", columnList = "transaction_id"),
        // Composite indexes for common query patterns
        @Index(name = "idx_news_categories_audit_created_at_action_success", columnList = "created_at DESC, action, is_success"),
        @Index(name = "idx_news_categories_audit_created_at_actor", columnList = "created_at DESC, actor_id"),
        @Index(name = "idx_news_categories_audit_severity_success", columnList = "severity, is_success"),
        @Index(name = "idx_news_categories_audit_transaction_created", columnList = "transaction_id, created_at DESC"),
        @Index(name = "idx_news_categories_audit_category_created", columnList = "news_category_id, created_at DESC")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NewsCategoryAuditLog extends BaseAuditLogEntity implements DomainAuditLog {

    /**
     * Primary key (auto-generated).
     * Each @Entity must declare at least one @Id, even when
     * extending @MappedSuperclass.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The news category ID being audited.
     * This is the domain-specific field that tracks which category was affected.
     */
    @Column(name = "news_category_id", nullable = false)
    private UUID newsCategoryId;

    /**
     * Sets the domain-specific field (newsCategoryId) from the generic resourceId.
     * This method is called by AuditingUtility to populate domain-specific columns.
     *
     * @param resourceId the news category UUID
     */
    @Override
    public void setDomainSpecificField(UUID resourceId) {
        this.newsCategoryId = resourceId;
    }
}
