package com.mmva.newsapp.domain.news.model.core;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

import com.mmva.newsapp.domain.news.enums.core.ContentFormat;
import com.mmva.newsapp.domain.news.enums.core.ContentOrigin;
import com.mmva.newsapp.domain.news.enums.core.UrgencyLevel;
import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;

/**
 * News Master Entity - Core entity for news content management.
 * 
 * <p>
 * Organized by <b>Domain Sections</b> with Required fields first in each
 * section.
 * This hybrid approach follows industry best practices for maintainability.
 * </p>
 * 
 * <h3>Section Order (Hybrid Domain-First Organization):</h3>
 * <ol>
 * <li>PRIMARY IDENTIFIER - System-generated UUID</li>
 * <li>CONTENT - Titles, body text (bilingual) [Required first, then
 * Optional]</li>
 * <li>MEDIA - Files, thumbnails, URLs [System-generated from upload]</li>
 * <li>CATEGORIZATION & SOURCE - Category, tags, source URL [Required
 * first]</li>
 * <li>LOCATION - Geographic targeting [All Optional]</li>
 * <li>WORKFLOW & PUBLISHING - Status, featured, breaking [Required first]</li>
 * <li>MONETIZATION - Sponsored, premium content [All Optional]</li>
 * <li>SEO - Meta tags, keywords, canonical URL [All Optional,
 * auto-generated]</li>
 * <li>SERIES - Multi-part story support [All Optional]</li>
 * <li>ENGAGEMENT METRICS - Views, likes, shares [System-managed]</li>
 * <li>CONTENT ANALYTICS - Word count, readability [System-calculated]</li>
 * <li>PUSH NOTIFICATIONS - Sent tracking [System-managed]</li>
 * <li>INTERNAL & ADMIN - Editor notes, priority, versioning</li>
 * <li>AUDIT - Inherited from BaseAuditEntity</li>
 * </ol>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "news", uniqueConstraints = {
        // ═══════════════════════════════════════════════════════════════════════════
        // UNIQUE CONSTRAINTS (Business Identifiers)
        // ═══════════════════════════════════════════════════════════════════════════
        @UniqueConstraint(name = "uk_news_title_en", columnNames = "news_title_en"),
        @UniqueConstraint(name = "uk_news_title_es", columnNames = "news_title_es")
}, indexes = {
        // ═══════════════════════════════════════════════════════════════════════════
        // SOFT DELETE & EXPIRATION INDEXES
        // ═══════════════════════════════════════════════════════════════════════════
        @Index(name = "idx_news_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_news_expires_at", columnList = "news_expires_at"),

        // ═══════════════════════════════════════════════════════════════════════════
        // WORKFLOW & STATUS INDEXES (High-traffic queries)
        // ═══════════════════════════════════════════════════════════════════════════
        @Index(name = "idx_news_workflow_status", columnList = "news_workflow_status"),
        @Index(name = "idx_news_is_active", columnList = "news_is_active"),
        @Index(name = "idx_news_is_featured", columnList = "news_is_featured"),
        @Index(name = "idx_news_is_breaking", columnList = "news_is_breaking"),

        // ═══════════════════════════════════════════════════════════════════════════
        // MONETIZATION & CONTENT TYPE INDEXES
        // ═══════════════════════════════════════════════════════════════════════════
        @Index(name = "idx_news_content_origin", columnList = "news_content_origin"),
        @Index(name = "idx_news_is_sponsored", columnList = "news_is_sponsored"),
        @Index(name = "idx_news_is_premium", columnList = "news_is_premium"),

        // ═══════════════════════════════════════════════════════════════════════════
        // FOREIGN KEY & LOOKUP INDEXES
        // ═══════════════════════════════════════════════════════════════════════════
        @Index(name = "idx_news_source_agency_id", columnList = "news_source_agency_id"),
        @Index(name = "idx_news_category_id", columnList = "news_news_category_id"),
        @Index(name = "idx_news_country_code", columnList = "news_country_code"),
        @Index(name = "idx_news_series_id", columnList = "news_series_id"),
        @Index(name = "idx_news_urgency_level", columnList = "news_urgency_level"),

        // ═══════════════════════════════════════════════════════════════════════════
        // SORTING & PAGINATION INDEXES
        // ═══════════════════════════════════════════════════════════════════════════
        @Index(name = "idx_news_published_at", columnList = "news_published_at DESC"),
        @Index(name = "idx_news_created_at", columnList = "created_at DESC"),
        @Index(name = "idx_news_created_by", columnList = "created_by"),

        // ═══════════════════════════════════════════════════════════════════════════
        // IMAGE URL INDEXES (Performance optimization for image lookups)
        // ═══════════════════════════════════════════════════════════════════════════
        @Index(name = "idx_news_image_card_url", columnList = "news_image_card_url"),
        @Index(name = "idx_news_image_hero_url", columnList = "news_image_hero_url"),

        // ═══════════════════════════════════════════════════════════════════════════
        // ELASTICSEARCH ADMIN SEARCH INDEXES
        // Purpose: Optimize AdminNewsElasticSearchBatchService batch fetch queries
        // Pattern: ES returns newsNewsIds → Batch fetch from DB with these indexes
        // Context: Handles millions of records with sub-second response times
        // ═══════════════════════════════════════════════════════════════════════════
        @Index(name = "idx_news_id_batch_fetch", columnList = "news_news_id"),
        @Index(name = "idx_news_published_at_desc_batch", columnList = "news_published_at DESC"),
        @Index(name = "idx_workflow_published_batch", columnList = "news_workflow_status, news_published_at DESC"),
        @Index(name = "idx_category_published_batch", columnList = "news_news_category_id, news_published_at DESC"),
        @Index(name = "idx_source_agency_batch", columnList = "news_source_agency_id, news_published_at DESC"),

        // ═══════════════════════════════════════════════════════════════════════════
        // COMPOSITE INDEXES (Optimized for common query patterns)
        // ═══════════════════════════════════════════════════════════════════════════
        @Index(name = "idx_news_published_country", columnList = "news_workflow_status, news_country_code, news_published_at DESC"),
        @Index(name = "idx_news_content_flags", columnList = "news_is_breaking, news_is_premium, news_is_sponsored, news_published_at DESC"),

        // ═══════════════════════════════════════════════════════════════════════════
        // FULL-TEXT SEARCH INDEXES (PostgreSQL FTS optimization)
        // GIN index for fast search_vector lookups
        // ═══════════════════════════════════════════════════════════════════════════
        @Index(name = "idx_news_search_vector_gin", columnList = "news_search_vector")
})
public class NewsMasterEntity extends BaseAuditEntity {

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 1: PRIMARY IDENTIFIER (System-generated)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Unique identifier for the news article.
     * Auto-generated UUID, never provided by client.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "news_news_id", nullable = false)
    private UUID newsNewsId;

    /**
     * SEO-friendly URL slug, auto-generated from title.
     * Note: Uniqueness is enforced at service layer via title checks
     * (news_title_en, news_title_es).
     */
    @Column(name = "news_slug", nullable = false, columnDefinition = "TEXT")
    private String newsSlug;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 2: CONTENT - Core article content (Bilingual)
    // Required: titleEn, titleEs, contentEn, contentEs
    // Optional: excerpts, HTML versions, format, tags
    // ═══════════════════════════════════════════════════════════════════════════

    // --- Required Content Fields ---

    @NotBlank(message = "Title in English is required")
    @Column(name = "news_title_en", nullable = false, columnDefinition = "TEXT")
    private String newsTitleEn;

    @NotBlank(message = "Title in Spanish is required")
    @Column(name = "news_title_es", nullable = false, columnDefinition = "TEXT")
    private String newsTitleEs;

    @Column(name = "news_content_en", nullable = false, columnDefinition = "TEXT")
    private String newsContentEn;

    @Column(name = "news_content_es", nullable = false, columnDefinition = "TEXT")
    private String newsContentEs;

    // --- Optional Content Fields ---

    @Column(name = "news_excerpt_en", columnDefinition = "TEXT")
    private String newsExcerptEn;

    @Column(name = "news_excerpt_es", columnDefinition = "TEXT")
    private String newsExcerptEs;

    @Column(name = "news_excerpt_auto_generated")
    private Boolean newsExcerptAutoGenerated;

    @Column(name = "news_content_html_en", columnDefinition = "TEXT")
    private String newsContentHtmlEn;

    @Column(name = "news_content_html_es", columnDefinition = "TEXT")
    private String newsContentHtmlEs;

    @Enumerated(EnumType.STRING)
    @Column(name = "news_content_format", nullable = false, length = 20)
    @Builder.Default
    private ContentFormat newsContentFormat = ContentFormat.PLAIN_TEXT;

    @Column(name = "news_tags", columnDefinition = "TEXT")
    private String newsTags;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 3: MEDIA - Images, videos, thumbnails
    // Required: mediaFileUrl (system sets from upload)
    // Optional: thumbnailUrl, imageCardUrl, imageHeroUrl (auto-generated)
    // ═══════════════════════════════════════════════════════════════════════════

    // --- System-Generated from File Upload ---

    @Column(name = "news_media_file_url", columnDefinition = "TEXT")
    private String newsMediaFileUrl;

    @Column(name = "news_media_file_name", columnDefinition = "TEXT")
    private String newsMediaFileName;

    @Column(name = "news_media_file_type", columnDefinition = "TEXT")
    private String newsMediaFileType;

    @Column(name = "news_media_file_size")
    private Long newsMediaFileSize;

    @Column(name = "news_media_type", columnDefinition = "TEXT")
    private String newsMediaType;

    // --- Thumbnail (auto-generated or custom) ---

    @Column(name = "news_thumbnail_url", columnDefinition = "TEXT")
    private String newsThumbnailUrl;

    // --- Multiple Image Sizes (auto-generated from processing) ---

    @Column(name = "news_image_card_url", columnDefinition = "TEXT")
    private String newsImageCardUrl;

    @Column(name = "news_image_hero_url", columnDefinition = "TEXT")
    private String newsImageHeroUrl;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 4: CATEGORIZATION & SOURCE
    // Required: categoryId, sourceUrl
    // Optional: authorName, agencyId, contentOrigin
    // ═══════════════════════════════════════════════════════════════════════════

    // --- Required Fields ---

    @Column(name = "news_news_category_id", nullable = false)
    private UUID newsNewsCategoryId;

    @Column(name = "news_source_url", columnDefinition = "TEXT")
    private String newsSourceUrl;

    // --- Optional Attribution ---

    @Column(name = "news_source_author_name", columnDefinition = "TEXT")
    private String newsSourceAuthorName;

    /**
     * Optional reference to news source agency.
     * No FK constraint - allows flexibility for external/unknown agencies.
     */
    @Column(name = "news_source_agency_id", nullable = true)
    private UUID newsSourceAgencyId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ContentOrigin newsContentOrigin = ContentOrigin.ORIGINAL;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 5: LOCATION - Geographic targeting
    // All Optional
    // ═══════════════════════════════════════════════════════════════════════════

    @Column(name = "news_country_code", columnDefinition = "VARCHAR(2)")
    private String newsCountryCode;

    @Column(name = "news_region", columnDefinition = "VARCHAR(100)")
    private String newsRegion;

    @Column(name = "news_city", columnDefinition = "VARCHAR(100)")
    private String newsCity;

    @Column(name = "news_latitude")
    private Double newsLatitude;

    @Column(name = "news_longitude")
    private Double newsLongitude;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 6: WORKFLOW & PUBLISHING
    // Required: workflowStatus, isFeatured
    // Optional: breaking, embargo, expiration, urgency, audience
    // ═══════════════════════════════════════════════════════════════════════════

    // --- Required Publishing Fields ---

    @Enumerated(EnumType.STRING)
    @Column(name = "news_workflow_status", nullable = false)
    @Builder.Default
    private WorkflowStatus newsWorkflowStatus = WorkflowStatus.DRAFT;

    @Builder.Default
    @Column(name = "news_is_featured")
    private Boolean newsIsFeatured = false;

    @NotNull
    @Builder.Default
    @Column(name = "news_is_active", nullable = false)
    private Boolean newsIsActive = true;

    // --- System-Managed Publishing ---

    @Column(name = "news_published_at")
    private Instant newsPublishedAt;

    /**
     * Username or user ID of the person who published the news.
     * Set automatically when status transitions to PUBLISHED.
     */
    @Column(name = "news_published_by", length = 100)
    private UUID newsPublishedBy;

    @Column(name = "news_scheduled_publish_at")
    private Instant newsScheduledPublishAt;

    /**
     * User ID of the person who scheduled the news for publication.
     * Set automatically when status transitions to SCHEDULED.
     */
    @Column(name = "news_scheduled_by", length = 100)
    private UUID newsScheduledBy;

    // --- Optional: Breaking News ---

    @Builder.Default
    @Column(name = "news_is_breaking", nullable = false)
    private Boolean newsIsBreaking = false;

    @Column(name = "news_breaking_expires_at")
    private Instant newsBreakingExpiresAt;

    // --- Optional: Advanced Publishing Controls ---

    @Column(name = "news_embargo_until")
    private Instant newsEmbargoUntil;

    @Column(name = "news_expires_at")
    private Instant newsExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "news_urgency_level")
    private UrgencyLevel newsUrgencyLevel;

    @Column(name = "news_target_audience", length = 50)
    private String newsTargetAudience;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 7: MONETIZATION - Sponsored & Premium content
    // All Optional
    // ═══════════════════════════════════════════════════════════════════════════

    // --- Sponsored Content ---

    @Builder.Default
    @Column(name = "news_is_sponsored", nullable = false)
    private Boolean newsIsSponsored = false;

    @Column(name = "news_sponsor_name", columnDefinition = "TEXT")
    private String newsSponsorName;

    @Column(name = "news_sponsor_logo_url", columnDefinition = "TEXT")
    private String newsSponsorLogoUrl;

    @Column(name = "news_sponsor_website_url", columnDefinition = "TEXT")
    private String newsSponsorWebsiteUrl;

    // --- Premium/Paywall Content ---

    @Builder.Default
    @Column(name = "news_is_premium", nullable = false)
    private Boolean newsIsPremium = false;

    @Column(name = "news_premium_tier", columnDefinition = "TEXT")
    private String newsPremiumTier;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 8: SEO - Search engine optimization
    // All Optional (auto-generated if not provided)
    // ═══════════════════════════════════════════════════════════════════════════

    @Column(name = "news_meta_title", columnDefinition = "TEXT")
    private String newsMetaTitle;

    @Column(name = "news_meta_description", columnDefinition = "TEXT")
    private String newsMetaDescription;

    @Column(name = "news_keywords", columnDefinition = "TEXT")
    private String newsKeywords;

    @Column(name = "news_canonical_url", columnDefinition = "TEXT")
    private String newsCanonicalUrl;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 9: SERIES - Multi-part story support
    // All Optional
    // ═══════════════════════════════════════════════════════════════════════════

    @Column(name = "news_series_id")
    private UUID newsSeriesId;

    @Column(name = "news_series_order")
    private Integer newsSeriesOrder;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 10: ENGAGEMENT METRICS (System-managed, runtime)
    // Never set by admin - updated by user interactions
    // ═══════════════════════════════════════════════════════════════════════════

    @Builder.Default
    @Column(name = "news_view_count")
    private Long newsViewCount = 0L;

    @Builder.Default
    @Column(name = "news_share_count")
    private Long newsShareCount = 0L;

    @Builder.Default
    @Column(name = "news_like_count")
    private Long newsLikeCount = 0L;

    @Builder.Default
    @Column(name = "news_comment_count", nullable = false)
    private Long newsCommentCount = 0L;

    @Builder.Default
    @Column(name = "news_bookmark_count", nullable = false)
    private Long newsBookmarkCount = 0L;

    @Builder.Default
    @Column(name = "news_reply_count", nullable = false)
    private Long newsReplyCount = 0L;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 11: CONTENT ANALYTICS (System-calculated on create/update)
    // ═══════════════════════════════════════════════════════════════════════════

    @Column(name = "news_word_count")
    private Integer newsWordCount;

    @Column(name = "news_character_count")
    private Integer newsCharacterCount;

    @Column(name = "news_read_time_minutes")
    private Integer newsReadTimeMinutes;

    @Column(name = "news_readability_score")
    private Double newsReadabilityScore;

    @Column(name = "news_last_edited_at")
    private Instant newsLastEditedAt;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 12: PUSH NOTIFICATIONS (System-managed)
    // Automatically sent when status transitions to PUBLISHED
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Whether push notification has been sent for this news.
     * Prevents duplicate notifications on status changes.
     * Automatically set to true when news is PUBLISHED.
     */
    @Builder.Default
    @Column(name = "news_push_notification_sent", nullable = false)
    private Boolean newsPushNotificationSent = false;

    /**
     * When the push notification was sent.
     * Used for audit and tracking purposes.
     */
    @Column(name = "news_push_notification_sent_at")
    private Instant newsPushNotificationSentAt;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 13: INTERNAL & ADMIN
    // Editor notes, priority, versioning
    // ═══════════════════════════════════════════════════════════════════════════

    @Column(name = "news_editor_notes", columnDefinition = "TEXT")
    private String newsEditorNotes;

    @Builder.Default
    @Column(name = "news_priority")
    private Integer newsPriority = 0;

    /**
     * Content version number for tracking news revisions.
     */
    @Builder.Default
    @Column(name = "news_version")
    private Integer newsVersion = 1;

    /**
     * JPA optimistic lock version to prevent concurrent update conflicts.
     */
    @Version
    @Column(name = "news_lock_version")
    private Long newsLockVersion;

    @Column(name = "news_previous_version_id")
    private UUID newsPreviousVersionId;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 13: SOCIAL MEDIA SHARING
    // Optional: social sharing configuration and tracking
    // ═══════════════════════════════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 14: FULL-TEXT SEARCH
    // Search vector for PostgreSQL FTS (generated automatically)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Concatenated search vector for full-text search.
     * Auto-generated from titles, content, keywords, and metadata.
     * Weighted for relevance: titles > keywords > content > excerpts.
     * 
     * Used by:
     * - Elasticsearch for distributed full-text search
     * - PostgreSQL native full-text search (FTS)
     * - In-memory ranking in service layer
     * 
     * Index: GIN index on news_search_vector for O(log n) lookups
     */
    @Column(name = "news_search_vector", columnDefinition = "TEXT")
    private String newsSearchVector;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 15: AUDIT TRAIL
    // Inherited from BaseAuditEntity: createdAt, createdBy, updatedAt, updatedBy
    // ═══════════════════════════════════════════════════════════════════════════

    // ═══════════════════════════════════════════════════════════════════════════
    // LIFECYCLE CALLBACKS: Full-Text Search Vector Generation
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Auto-generate search vector before persisting new entity.
     * Combines all searchable fields into a single vector.
     */
    @PrePersist
    protected void generateSearchVectorBeforePersist() {
        generateSearchVector();
    }

    /**
     * Auto-generate search vector before updating existing entity.
     * Ensures search vector stays in sync with content changes.
     */
    @PreUpdate
    protected void generateSearchVectorBeforeUpdate() {
        generateSearchVector();
    }

    /**
     * Generate search vector by concatenating and weighting searchable content
     * fields.
     * This vector is used for:
     * - Elasticsearch indexing
     * - PostgreSQL full-text search (FTS)
     * - In-memory search ranking (Java code)
     *
     * Weighting strategy (field repetition for importance):
     * A) CRITICAL (repeated 3x): Titles - highest relevance
     * B) HIGH (repeated 2x): Keywords, Tags - strong signals
     * C) MEDIUM (1x): Content, Excerpts - main information
     * D) LOW (1x): Author, Category, Source - contextual info
     *
     * Fields indexed (in weighted order):
     * 1. Titles (EN + ES) - repeated 3x for ranking priority
     * 2. Keywords and Tags - repeated 2x (curated search signals)
     * 3. Content (EN + ES) - main searchable text
     * 4. Excerpts (EN + ES) - summaries for context
     * 5. Source Author - attribution
     * 6. Category Name - categorization signal
     */
    private void generateSearchVector() {
        StringBuilder vector = new StringBuilder();

        // TIER A: Critical fields - repeated 3x for ranking boost
        // Titles are the strongest relevance signals
        appendIfNotBlank(vector, newsTitleEn);
        appendIfNotBlank(vector, newsTitleEs);
        // Repeat titles for search ranking boost
        appendIfNotBlank(vector, newsTitleEn);
        appendIfNotBlank(vector, newsTitleEs);
        appendIfNotBlank(vector, newsTitleEn);
        appendIfNotBlank(vector, newsTitleEs);

        // TIER B: High priority fields - repeated 2x
        // Keywords and tags are strong relevance signals
        appendIfNotBlank(vector, newsKeywords);
        appendIfNotBlank(vector, newsTags);
        // Repeat for ranking importance
        appendIfNotBlank(vector, newsKeywords);
        appendIfNotBlank(vector, newsTags);

        // TIER C: Medium priority - main content
        appendIfNotBlank(vector, newsContentEn);
        appendIfNotBlank(vector, newsContentEs);

        // TIER D: Supporting contextual info
        appendIfNotBlank(vector, newsExcerptEn);
        appendIfNotBlank(vector, newsExcerptEs);
        appendIfNotBlank(vector, newsSourceAuthorName);

        this.newsSearchVector = vector.toString().trim();
    }

    /**
     * Append text to search vector if not blank.
     * Adds a space separator between fields.
     * Automatically lowercases text for case-insensitive search.
     */
    private void appendIfNotBlank(StringBuilder vector, String text) {
        if (text != null && !text.isBlank()) {
            if (vector.length() > 0) {
                vector.append(" ");
            }
            // Lowercase for case-insensitive searching
            vector.append(text.toLowerCase());
        }
    }

    // --- Workflow Status Enum ---

    public enum WorkflowStatus {
        DRAFT,
        PENDING_APPROVAL,
        SCHEDULED,
        PUBLISHED
    }

}
