package com.mmva.newsapp.domain.newssourceagency.model.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit log entity for tracking news source agency changes.
 * 
 * <p>
 * Table: news_source_agencies_audit_log
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
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
@Table(name = "news_source_agencies_audit_log", indexes = {
        @Index(name = "idx_nsa_audit_log_agency_id", columnList = "news_source_agencies_audit_log_agency_id"),
        @Index(name = "idx_nsa_audit_log_action", columnList = "news_source_agencies_audit_log_action"),
        @Index(name = "idx_nsa_audit_log_created_at", columnList = "news_source_agencies_audit_log_created_at"),
        @Index(name = "idx_nsa_audit_log_created_by", columnList = "news_source_agencies_audit_log_created_by")
})
public class NewsSourceAgencyAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_source_agencies_audit_log_id")
    private Long id;

    @Column(name = "news_source_agencies_audit_log_agency_id", nullable = false)
    private UUID agencyId;

    @Column(name = "news_source_agencies_audit_log_action", nullable = false, length = 50)
    private String action;

    @Column(name = "news_source_agencies_audit_log_details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "news_source_agencies_audit_log_created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "news_source_agencies_audit_log_created_by", nullable = false)
    private UUID createdBy;
}
