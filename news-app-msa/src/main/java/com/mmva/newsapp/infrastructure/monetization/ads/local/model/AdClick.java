package com.mmva.newsapp.infrastructure.monetization.ads.local.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing an Ad Click - when a user clicks on an ad.
 * 
 * This is a high-volume tracking table designed for performance.
 * Linked to impressions for click-through rate analysis.
 * 
 * Portability Note: Uses UUID references instead of entity relationships.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "monetization_ad_click", indexes = {
                @Index(name = "idx_ad_click_campaign", columnList = "ad_click_campaign_id"),
                @Index(name = "idx_ad_click_placement", columnList = "ad_click_placement_id"),
                @Index(name = "idx_ad_click_impression", columnList = "ad_click_impression_id"),
                @Index(name = "idx_ad_click_user", columnList = "ad_click_user_id"),
                @Index(name = "idx_ad_click_recorded_at", columnList = "ad_click_recorded_at"),
                @Index(name = "idx_ad_click_tenant_date", columnList = "ad_click_tenant_id, ad_click_recorded_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AdClick extends BaseAuditEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "ad_click_id", updatable = false, nullable = false)
        private UUID adClickId;

        // ========================================
        // References (UUIDs for portability)
        // ========================================

        /**
         * Campaign that received the click.
         */
        @Column(name = "ad_click_campaign_id", nullable = false)
        private UUID adClickCampaignId;

        /**
         * Placement where the click occurred.
         */
        @Column(name = "ad_click_placement_id", nullable = false)
        private UUID adClickPlacementId;

        /**
         * Associated impression (if tracked).
         */
        @Column(name = "ad_click_impression_id")
        private UUID adClickImpressionId;

        /**
         * User who clicked (null for anonymous).
         */
        @Column(name = "ad_click_user_id")
        private UUID adClickUserId;

        /**
         * Content where the ad was clicked.
         */
        @Column(name = "ad_click_content_id")
        private UUID adClickContentId;

        // ========================================
        // Click Details
        // ========================================

        /**
         * When the click was recorded.
         */
        @Column(name = "ad_click_recorded_at", nullable = false)
        private Instant adClickRecordedAt;

        /**
         * Destination URL the user was sent to.
         */
        @Column(name = "ad_click_destination_url", length = 1000)
        private String adClickDestinationUrl;

        /**
         * Type of click: PRIMARY (main CTA), SECONDARY, LOGO, etc.
         */
        @Column(name = "ad_click_type", length = 50)
        @Builder.Default
        private String adClickType = "PRIMARY";

        /**
         * Element that was clicked (for detailed analytics).
         */
        @Column(name = "ad_click_clicked_element", length = 100)
        private String adClickClickedElement;

        /**
         * X coordinate of click (percentage of ad width).
         */
        @Column(name = "ad_click_x")
        private Double adClickX;

        /**
         * Y coordinate of click (percentage of ad height).
         */
        @Column(name = "ad_click_y")
        private Double adClickY;

        // ========================================
        // Revenue
        // ========================================

        /**
         * Revenue generated from this click (for CPC campaigns).
         */
        @Column(name = "ad_click_revenue_amount", precision = 10, scale = 6)
        @Builder.Default
        private BigDecimal adClickRevenueAmount = BigDecimal.ZERO;

        /**
         * CPC rate applied.
         */
        @Column(name = "ad_click_cpc_rate", precision = 10, scale = 4)
        private BigDecimal adClickCpcRate;

        // ========================================
        // Context
        // ========================================

        /**
         * Source page URL.
         */
        @Column(name = "ad_click_source_page_url", length = 1000)
        private String adClickSourcePageUrl;

        /**
         * Referrer URL.
         */
        @Column(name = "ad_click_referrer_url", length = 1000)
        private String adClickReferrerUrl;

        // ========================================
        // Device & Browser
        // ========================================

        /**
         * Device type: DESKTOP, MOBILE, TABLET.
         */
        @Column(name = "ad_click_device_type", length = 50)
        private String adClickDeviceType;

        /**
         * Browser name.
         */
        @Column(name = "ad_click_browser", length = 100)
        private String adClickBrowser;

        /**
         * Operating system.
         */
        @Column(name = "ad_click_os", length = 100)
        private String adClickOs;

        // ========================================
        // Geographic
        // ========================================

        /**
         * Country code (ISO 3166-1 alpha-2).
         */
        @Column(name = "ad_click_country_code", length = 2)
        private String adClickCountryCode;

        /**
         * Region/state.
         */
        @Column(name = "ad_click_region", length = 100)
        private String adClickRegion;

        /**
         * City.
         */
        @Column(name = "ad_click_city", length = 100)
        private String adClickCity;

        // ========================================
        // Session Info
        // ========================================

        /**
         * Session identifier for grouping user activity.
         */
        @Column(name = "ad_click_session_id", length = 100)
        private String adClickSessionId;

        /**
         * IP address (hashed or anonymized for privacy).
         */
        @Column(name = "ad_click_ip_hash", length = 64)
        private String adClickIpHash;

        /**
         * User agent string.
         */
        @Column(name = "ad_click_user_agent", length = 500)
        private String adClickUserAgent;

        // ========================================
        // UTM Tracking
        // ========================================

        /**
         * UTM source parameter.
         */
        @Column(name = "ad_click_utm_source", length = 200)
        private String adClickUtmSource;

        /**
         * UTM medium parameter.
         */
        @Column(name = "ad_click_utm_medium", length = 200)
        private String adClickUtmMedium;

        /**
         * UTM campaign parameter.
         */
        @Column(name = "ad_click_utm_campaign", length = 200)
        private String adClickUtmCampaign;

        /**
         * UTM content parameter.
         */
        @Column(name = "ad_click_utm_content", length = 200)
        private String adClickUtmContent;

        /**
         * UTM term parameter.
         */
        @Column(name = "ad_click_utm_term", length = 200)
        private String adClickUtmTerm;

        // ========================================
        // Fraud Detection
        // ========================================

        /**
         * Whether this click is flagged as suspicious.
         */
        @Column(name = "ad_click_is_suspicious", nullable = false)
        @Builder.Default
        private Boolean adClickIsSuspicious = false;

        /**
         * Reason for suspicion (if flagged).
         */
        @Column(name = "ad_click_suspicion_reason", length = 200)
        private String adClickSuspicionReason;

        /**
         * Time since impression in milliseconds (for fraud detection).
         */
        @Column(name = "ad_click_time_since_impression_ms")
        private Long adClickTimeSinceImpressionMs;

        /**
         * Whether this is a duplicate click within threshold.
         */
        @Column(name = "ad_click_is_duplicate", nullable = false)
        @Builder.Default
        private Boolean adClickIsDuplicate = false;

        /**
         * Whether this click has been validated/verified.
         */
        @Column(name = "ad_click_is_validated", nullable = false)
        @Builder.Default
        private Boolean adClickIsValidated = false;

        // ========================================
        // Conversion Tracking
        // ========================================

        /**
         * Whether this click led to a conversion.
         */
        @Column(name = "ad_click_converted", nullable = false)
        @Builder.Default
        private Boolean adClickConverted = false;

        /**
         * Conversion ID if converted.
         */
        @Column(name = "ad_click_conversion_id")
        private UUID adClickConversionId;

        /**
         * Time to conversion in seconds.
         */
        @Column(name = "ad_click_conversion_time_seconds")
        private Long adClickConversionTimeSeconds;

        // ========================================
        // Multi-tenancy
        // ========================================

        /**
         * Tenant identifier for multi-app support.
         */
        @Column(name = "ad_click_tenant_id", nullable = false, length = 100)
        @Builder.Default
        private String adClickTenantId = "default";

        // ========================================
        // Factory Method
        // ========================================

        /**
         * Creates a basic click record.
         */
        public static AdClick create(UUID campaignId, UUID placementId,
                        UUID impressionId, UUID userId, String tenantId) {
                return AdClick.builder()
                                .adClickCampaignId(campaignId)
                                .adClickPlacementId(placementId)
                                .adClickImpressionId(impressionId)
                                .adClickUserId(userId)
                                .adClickTenantId(tenantId)
                                .adClickRecordedAt(Instant.now())
                                .adClickType("PRIMARY")
                                .adClickIsValidated(false)
                                .adClickIsSuspicious(false)
                                .adClickIsDuplicate(false)
                                .adClickConverted(false)
                                .build();
        }

        /**
         * Checks if this is a billable click.
         */
        public boolean isBillable() {
                return Boolean.TRUE.equals(adClickIsValidated) &&
                                !Boolean.TRUE.equals(adClickIsSuspicious) &&
                                !Boolean.TRUE.equals(adClickIsDuplicate);
        }
}
