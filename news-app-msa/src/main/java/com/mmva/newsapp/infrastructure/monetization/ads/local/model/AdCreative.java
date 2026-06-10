package com.mmva.newsapp.infrastructure.monetization.ads.local.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdCreativeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing an Ad Creative - the actual ad content (image/video).
 *
 * Ad creatives are the visual/media assets that get displayed in placements.
 * They are linked to campaigns which determine when and where they appear.
 *
 * Portability Note: This entity uses UUID references instead of entity
 * relationships to ensure the monetization module can be copied to other
 * applications.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "monetization_ad_creative", indexes = {
        @Index(name = "idx_ad_creative_code", columnList = "ad_creative_code"),
        @Index(name = "idx_ad_creative_type", columnList = "ad_creative_type"),
        @Index(name = "idx_ad_creative_tenant", columnList = "ad_creative_tenant_id"),
        @Index(name = "idx_ad_creative_active", columnList = "ad_creative_is_active")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_ad_creative_code_tenant", columnNames = { "ad_creative_code",
                "ad_creative_tenant_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AdCreative extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ad_creative_id", updatable = false, nullable = false)
    private UUID adCreativeId;

    /**
     * Unique code for this creative (e.g., "SUMMER_SALE_BANNER_728x90").
     */
    @Column(name = "ad_creative_code", nullable = false, length = 100)
    private String adCreativeCode;

    /**
     * Human-readable name for the creative.
     */
    @Column(name = "ad_creative_name", nullable = false, length = 200)
    private String adCreativeName;

    /**
     * Detailed description of the creative content.
     */
    @Column(name = "ad_creative_description", length = 1000)
    private String adCreativeDescription;

    // ========================================
    // Creative Type & Content
    // ========================================

    /**
     * Type of creative (IMAGE, VIDEO, HTML, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ad_creative_type", nullable = false, length = 50)
    private AdCreativeType adCreativeType;

    /**
     * Primary file path/URL for the creative content.
     * For images: relative path to image file
     * For videos: relative path to video file
     * For HTML: may be null if content is stored separately
     */
    @Column(name = "ad_creative_file_name", length = 500)
    private String adCreativeFileName;

    /**
     * Thumbnail file path for image/video creatives.
     */
    @Column(name = "ad_creative_thumbnail_filename", length = 500)
    private String adCreativeThumbnailFilename;

    /**
     * Alternative text for accessibility (especially important for images).
     */
    @Column(name = "ad_creative_alt_text", length = 300)
    private String adCreativeAltText;

    /**
     * Click destination URL when ad is clicked.
     */
    @Column(name = "ad_creative_click_url", length = 1000)
    private String adCreativeClickUrl;

    // ========================================
    // Dimensions & Technical Specs
    // ========================================

    /**
     * Width in pixels.
     */
    @Column(name = "ad_creative_width")
    private Integer adCreativeWidth;

    /**
     * Height in pixels.
     */
    @Column(name = "ad_creative_height")
    private Integer adCreativeHeight;

    /**
     * File size in bytes.
     */
    @Column(name = "ad_creative_file_size_bytes")
    private Long adCreativeFileSizeBytes;

    /**
     * MIME type of the creative file.
     */
    @Column(name = "ad_creative_mime_type", length = 100)
    private String adCreativeMimeType;

    /**
     * For video creatives: duration in seconds.
     */
    @Column(name = "ad_creative_duration_seconds")
    private Integer adCreativeDurationSeconds;

    // ========================================
    // Status & Control
    // ========================================

    /**
     * Whether this creative is active and can be served.
     */
    @Column(name = "ad_creative_is_active", nullable = false)
    @Builder.Default
    private Boolean adCreativeIsActive = true;

    /**
     * Whether this creative requires approval before use.
     */
    @Column(name = "ad_creative_requires_approval", nullable = false)
    @Builder.Default
    private Boolean adCreativeRequiresApproval = false;

    /**
     * Approval status: PENDING, APPROVED, REJECTED.
     */
    @Column(name = "ad_creative_approval_status", length = 50)
    @Builder.Default
    private String adCreativeApprovalStatus = "PENDING";

    /**
     * Reason for rejection if applicable.
     */
    @Column(name = "ad_creative_rejection_reason", length = 500)
    private String adCreativeRejectionReason;

    // ========================================
    // Performance Tracking (Denormalized)
    // ========================================

    /**
     * Total impressions served for this creative.
     */
    @Column(name = "ad_creative_total_impressions", nullable = false)
    @Builder.Default
    private Long adCreativeTotalImpressions = 0L;

    /**
     * Total clicks received by this creative.
     */
    @Column(name = "ad_creative_total_clicks", nullable = false)
    @Builder.Default
    private Long adCreativeTotalClicks = 0L;

    /**
     * Total revenue generated by this creative.
     */
    @Column(name = "ad_creative_total_revenue", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal adCreativeTotalRevenue = BigDecimal.ZERO;

    /**
     * Last time this creative was served.
     */
    @Column(name = "ad_creative_last_served_at")
    private Instant adCreativeLastServedAt;

    // ========================================
    // Metadata & External Integration
    // ========================================

    /**
     * Additional metadata as JSON (dimensions, colors, etc.).
     */
    @Column(name = "ad_creative_metadata_json", columnDefinition = "TEXT")
    private String adCreativeMetadataJson;

    /**
     * External creative ID (e.g., from ad server).
     */
    @Column(name = "ad_creative_external_creative_id", length = 200)
    private String adCreativeExternalCreativeId;

    /**
     * External ad server name.
     */
    @Column(name = "ad_creative_external_ad_server", length = 100)
    private String adCreativeExternalAdServer;

    // ========================================
    // Native Ad Content (for NATIVE type creatives)
    // ========================================

    /**
     * Ad title/headline for native ads (separate from creative name).
     */
    @Column(name = "ad_creative_ad_title", length = 200)
    private String adCreativeAdTitle;

    /**
     * Ad media URL for native ads (can be different from creative file).
     */
    @Column(name = "ad_creative_ad_media_url", length = 500)
    private String adCreativeAdMediaUrl;

    /**
     * Ad summary/description for native ads.
     */
    @Column(name = "ad_creative_ad_summary", length = 500)
    private String adCreativeAdSummary;

    // ========================================
    // Multi-tenancy
    // ========================================

    /**
     * Tenant identifier for multi-app support.
     */
    @Column(name = "ad_creative_tenant_id", nullable = false, length = 100)
    @Builder.Default
    private String adCreativeTenantId = "default";

    // ========================================
    // Business Methods
    // ========================================

    /**
     * Checks if this creative is currently available for serving.
     */
    public boolean isAvailable() {
        return Boolean.TRUE.equals(adCreativeIsActive) &&
                "APPROVED".equals(adCreativeApprovalStatus) &&
                !isDeleted();
    }

    /**
     * Gets click-through rate for this creative.
     */
    public double getClickThroughRate() {
        if (adCreativeTotalImpressions == null || adCreativeTotalImpressions == 0) {
            return 0.0;
        }
        return (double) (adCreativeTotalClicks != null ? adCreativeTotalClicks : 0)
                / adCreativeTotalImpressions * 100;
    }

    /**
     * Gets dimensions as a string (e.g., "728x90").
     */
    public String getDimensionsDisplay() {
        if (adCreativeWidth != null && adCreativeHeight != null) {
            return adCreativeWidth + "x" + adCreativeHeight;
        }
        return adCreativeType != null ? adCreativeType.getDefaultDimensions() : "Unknown";
    }

    /**
     * Gets average revenue per impression (RPM).
     */
    public BigDecimal getRevenuePerMille() {
        if (adCreativeTotalImpressions == null || adCreativeTotalImpressions == 0) {
            return BigDecimal.ZERO;
        }
        return adCreativeTotalRevenue.multiply(BigDecimal.valueOf(1000))
                .divide(BigDecimal.valueOf(adCreativeTotalImpressions), 4,
                        java.math.RoundingMode.HALF_UP);
    }

    /**
     * Checks if this is a video creative.
     */
    public boolean isVideo() {
        return adCreativeType != null && adCreativeType.isVideo();
    }

    /**
     * Checks if this creative needs approval.
     */
    public boolean needsApproval() {
        return Boolean.TRUE.equals(adCreativeRequiresApproval) &&
                !"APPROVED".equals(adCreativeApprovalStatus);
    }
}