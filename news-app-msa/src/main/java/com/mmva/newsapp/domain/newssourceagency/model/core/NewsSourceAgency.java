package com.mmva.newsapp.domain.newssourceagency.model.core;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * News Source Agency entity for managing external newsapp sources.
 * 
 * <p>
 * Represents wire services, partner publications, and other external sources
 * that provide syndicated content. Normalized to avoid data duplication
 * across newsapp articles.
 * </p>
 * 
 * <h3>Examples:</h3>
 * <ul>
 * <li>Wire Services: Associated Press (AP), Reuters, AFP</li>
 * <li>Partners: Local newsapp stations, specialized publications</li>
 * <li>Aggregators: News aggregation services</li>
 * </ul>
 * 
 * <p>
 * Soft-delete filtering is handled via {@code SoftDeleteSpec} in repository
 * queries.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "news_source_agencies", indexes = {
        @Index(name = "idx_news_source_agencies_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_news_source_agencies_agency_code", columnList = "agency_code"),
        @Index(name = "idx_news_source_agencies_is_active", columnList = "is_active")
})
public class NewsSourceAgency extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "agency_id", nullable = false, updatable = false)
    private UUID agencyId;

    // ========================================
    // Identification
    // ========================================

    /**
     * Unique agency code for lookups and API references.
     * Examples: "AP", "REUTERS", "AFP", "BBC"
     */
    @Column(name = "agency_code", nullable = false, unique = true, length = 50)
    private String agencyCode;

    /**
     * Display name of the agency.
     * Examples: "Associated Press", "Reuters", "Agence France-Presse"
     */
    @Column(name = "agency_name", nullable = false, length = 255)
    private String agencyName;

    // ========================================
    // Branding & Contact
    // ========================================

    /**
     * URL to the agency's logo for attribution display.
     */
    @Column(name = "agency_logo_url", length = 2000)
    private String agencyLogoUrl;

    /**
     * Agency's official website URL.
     */
    @Column(name = "agency_website_url", length = 2000)
    private String agencyWebsiteUrl;

    // ========================================
    // Status & Trust
    // ========================================

    /**
     * Whether this agency is a trusted/verified newsapp source.
     * Trusted sources may receive preferential treatment in feeds.
     */
    @Column(name = "is_trusted", nullable = false)
    private Boolean isTrusted = true;

    /**
     * Whether this agency is currently active.
     * Inactive agencies cannot be assigned to new articles.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // ========================================
    // Description
    // ========================================

    /**
     * Optional description of the agency.
     * Can include partnership details, coverage areas, etc.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
