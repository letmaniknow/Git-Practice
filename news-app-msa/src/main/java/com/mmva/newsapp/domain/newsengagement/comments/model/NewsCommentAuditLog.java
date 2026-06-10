package com.mmva.newsapp.domain.newsengagement.comments.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditLogEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Audit log entity for tracking newsapp comment changes.
 * 
 * <p>
 * Follows PROJECT_PRINCIPLES.md column naming convention:
 * {@code {table_name}_{field_name}}
 * </p>
 * 
 * <p>
 * Uses IDENTITY (Long) for consistency with all other audit log tables.
 * Audit logs are append-only and benefit from sequential IDs for
 * B-tree index performance and range queries.
 * </p>
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
@Table(name = "news_comment_audit_log", indexes = {
        @Index(name = "idx_news_comment_audit_log_comment_id", columnList = "news_comment_audit_log_comment_id"),
        @Index(name = "idx_news_comment_audit_log_action", columnList = "news_comment_audit_log_action")
})
public class NewsCommentAuditLog extends BaseAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_comment_audit_log_id")
    private Long newsCommentAuditLogId;

    @Column(name = "news_comment_audit_log_comment_id")
    private UUID newsCommentAuditLogCommentId;

    @Column(name = "news_comment_audit_log_action", length = 50, nullable = false)
    private String newsCommentAuditLogAction;

    @Column(name = "news_comment_audit_log_description", length = 255)
    private String newsCommentAuditLogDescription;
}
