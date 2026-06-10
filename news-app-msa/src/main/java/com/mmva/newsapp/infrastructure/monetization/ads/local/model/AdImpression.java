package com.mmva.newsapp.infrastructure.monetization.ads.local.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing an Ad Impression - when an ad is displayed to a user.
 * 
 * This is a high-volume tracking table designed for performance.
 * Consider partitioning by adImpressionRecordedAt for large-scale deployments.
 * 
 * Portability Note: Uses UUID references instead of entity relationships.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "monetization_ad_impression", indexes = {
                @Index(name = "idx_ad_impression_campaign", columnList = "ad_impression_campaign_id"),
                @Index(name = "idx_ad_impression_placement", columnList = "ad_impression_placement_id"),
                @Index(name = "idx_ad_impression_user", columnList = "ad_impression_user_id"),
                @Index(name = "idx_ad_impression_content", columnList = "ad_impression_content_id"),
                @Index(name = "idx_ad_impression_recorded_at", columnList = "ad_impression_recorded_at"),
                @Index(name = "idx_ad_impression_tenant_date", columnList = "ad_impression_tenant_id, ad_impression_recorded_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AdImpression extends BaseAuditEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "ad_impression_id", updatable = false, nullable = false)
        private UUID adImpressionId;

        // ========================================
        // References (UUIDs for portability)
        // ========================================

        /**
         * Campaign that served this impression.
         */
        @Column(name = "ad_impression_campaign_id", nullable = false)
        private UUID adImpressionCampaignId;

        /**
         * Placement where the ad was shown.
         */
        @Column(name = "ad_impression_placement_id", nullable = false)
        private UUID adImpressionPlacementId;

        /**
         * User who saw the impression (null for anonymous).
         */
        @Column(name = "ad_impression_user_id")
        private UUID adImpressionUserId;

        /**
         * Content/page where the ad was displayed.
         */
        @Column(name = "ad_impression_content_id")
        private UUID adImpressionContentId;

        // ========================================
        // Impression Details
        // ========================================

        /**
         * When the impression was recorded.
         */
        @Column(name = "ad_impression_recorded_at", nullable = false)
        private Instant adImpressionRecordedAt;

        /**
         * Whether the ad was viewable (met viewability threshold).
         */
        @Column(name = "ad_impression_is_viewable", nullable = false)
        @Builder.Default
        private Boolean adImpressionIsViewable = true;

        /**
         * How long the ad was visible (in milliseconds).
         */
        @Column(name = "ad_impression_view_duration_ms")
        private Long adImpressionViewDurationMs;

        /**
         * Percentage of ad that was visible (0-100).
         */
        @Column(name = "ad_impression_visibility_percent")
        private Integer adImpressionVisibilityPercent;

        // ========================================
        // Revenue
        // ========================================

        /**
         * Revenue generated from this impression (for CPM campaigns).
         */
        @Column(name = "ad_impression_revenue_amount", precision = 10, scale = 6)
        @Builder.Default
        private BigDecimal adImpressionRevenueAmount = BigDecimal.ZERO;

        /**
         * CPM rate applied.
         */
        @Column(name = "ad_impression_cpm_rate", precision = 10, scale = 4)
        private BigDecimal adImpressionCpmRate;

        // ========================================
        // Context
        // ========================================

        /**
         * Page URL where impression occurred.
         */
        @Column(name = "ad_impression_page_url", length = 1000)
        private String adImpressionPageUrl;

        /**
         * Page title.
         */
        @Column(name = "ad_impression_page_title", length = 500)
        private String adImpressionPageTitle;

        /**
         * Category of the content.
         */
        @Column(name = "ad_impression_content_category", length = 100)
        private String adImpressionContentCategory;

        // ========================================
        // Device & Browser
        // ========================================

        /**
         * Device type: DESKTOP, MOBILE, TABLET.
         */
        @Column(name = "ad_impression_device_type", length = 50)
        private String adImpressionDeviceType;

        /**
         * Browser name.
         */
        @Column(name = "ad_impression_browser", length = 100)
        private String adImpressionBrowser;

        /**
         * Operating system.
         */
        @Column(name = "ad_impression_os", length = 100)
        private String adImpressionOs;

        /**
         * Screen resolution.
         */
        @Column(name = "ad_impression_screen_resolution", length = 50)
        private String adImpressionScreenResolution;

        // ========================================
        // Geographic
        // ========================================

        /**
         * Country code (ISO 3166-1 alpha-2).
         */
        @Column(name = "ad_impression_country_code", length = 2)
        private String adImpressionCountryCode;

        /**
         * Region/state.
         */
        @Column(name = "ad_impression_region", length = 100)
        private String adImpressionRegion;

        /**
         * City.
         */
        @Column(name = "ad_impression_city", length = 100)
        private String adImpressionCity;

        // ========================================
        // Session Info
        // ========================================

        /**
         * Session identifier for grouping user activity.
         */
        @Column(name = "ad_impression_session_id", length = 100)
        private String adImpressionSessionId;

        /**
         * IP address (hashed or anonymized for privacy).
         */
        @Column(name = "ad_impression_ip_hash", length = 64)
        private String adImpressionIpHash;

        /**
         * User agent string.
         */
        @Column(name = "ad_impression_user_agent", length = 500)
        private String adImpressionUserAgent;

        // ========================================
        // Fraud Detection
        // ========================================

        /**
         * Whether this impression is flagged as suspicious.
         */
        @Column(name = "ad_impression_is_suspicious", nullable = false)
        @Builder.Default
        private Boolean adImpressionIsSuspicious = false;

        /**
         * Reason for suspicion (if flagged).
         */
        @Column(name = "ad_impression_suspicion_reason", length = 200)
        private String adImpressionSuspicionReason;

        /**
         * Whether this impression has been validated/verified.
         */
        @Column(name = "ad_impression_is_validated", nullable = false)
        @Builder.Default
        private Boolean adImpressionIsValidated = false;

        // ========================================
        // Multi-tenancy
        // ========================================

        /**
         * Tenant identifier for multi-app support.
         */
        @Column(name = "ad_impression_tenant_id", nullable = false, length = 100)
        @Builder.Default
        private String adImpressionTenantId = "default";

        // ========================================
        // Factory Method
        // ========================================

        /**
         * Creates a basic impression record.
         */
        public static AdImpression create(UUID campaignId, UUID placementId,
                        UUID userId, String tenantId) {
                return AdImpression.builder()
                                .adImpressionCampaignId(campaignId)
                                .adImpressionPlacementId(placementId)
                                .adImpressionUserId(userId)
                                .adImpressionTenantId(tenantId)
                                .adImpressionRecordedAt(Instant.now())
                                .adImpressionIsViewable(true)
                                .adImpressionIsValidated(false)
                                .adImpressionIsSuspicious(false)
                                .build();
        }
}
