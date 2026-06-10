package com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.entity;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity for ad provider metrics
 * 
 * Maps to database table: provider_metrics_sync
 * Stores normalized metrics from all ad providers (Google AdSense, AdMob,
 * Facebook, Criteo, PubMatic)
 * 
 * Naming Convention:
 * - Database column: ad_provider_*, provider_*
 * - JPA field: adProvider*, provider*
 * - Consistency with DTO field names
 * 
 * Design:
 * - Single table for all providers (no subclasses)
 * - provider_type discriminates provider
 * - Multi-tenant support via tenant_id
 * - Soft deletes via deleted_at
 * - Audit trail via created_at, updated_at, created_by, updated_by
 * - Calculated columns in database (CTR, CPM, CPC)
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "provider_metrics_sync", indexes = {
        @Index(name = "idx_provider_type_period", columnList = "provider_type,metrics_period_start,metrics_period_end"),
        @Index(name = "idx_account_id", columnList = "account_id"),
        @Index(name = "idx_synced_at", columnList = "synced_at DESC"),
        @Index(name = "idx_tenant_id", columnList = "tenant_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
public class ProviderMetricsSync {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================
    // Provider Identification
    // ========================================

    /**
     * Ad provider type
     * GOOGLE_ADSENSE, GOOGLE_ADMOB, FACEBOOK_AUDIENCE_NETWORK, CRITEO, PUBMATIC
     */
    @Column(name = "provider_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ProviderType adProviderType;

    /**
     * Provider code for easy filtering
     * Example: google-adsense, google-admob
     */
    @Column(name = "provider_code", nullable = false, length = 50)
    private String adProviderCode;

    /**
     * Provider-specific account ID
     * Example: pub-xxxxx for AdSense, ca-app-pub-xxxxx for AdMob
     */
    @Column(name = "account_id", nullable = false, length = 255)
    private String adProviderAccountId;

    /**
     * Human-readable account name
     * Example: "My Publisher Account", "MyApp - iOS"
     */
    @Column(name = "account_name", length = 255)
    private String adProviderAccountName;

    // ========================================
    // Core Metrics
    // ========================================

    /**
     * Total ad impressions (views)
     */
    @Column(name = "total_impressions", nullable = false)
    private Long adProviderTotalImpressions;

    /**
     * Total ad clicks
     */
    @Column(name = "total_clicks", nullable = false)
    private Long adProviderTotalClicks;

    /**
     * Estimated earnings in USD
     */
    @Column(name = "estimated_earnings_usd", nullable = false, precision = 18, scale = 6)
    private BigDecimal adProviderEstimatedEarningsUsd;

    // ========================================
    // Calculated Metrics (computed in SQL)
    // ========================================

    /**
     * Click-through rate percentage
     * Calculated: (clicks / impressions) * 100
     */
    @Column(name = "ctr_percentage", nullable = false, precision = 18, scale = 6)
    private BigDecimal adProviderCtrPercentage;

    /**
     * Cost per mille (cost per 1000 impressions)
     * Calculated: (earnings / impressions) * 1000
     */
    @Column(name = "cpm_usd", nullable = false, precision = 18, scale = 6)
    private BigDecimal adProviderCpmUsd;

    /**
     * Cost per click
     * Calculated: earnings / clicks
     */
    @Column(name = "cpc_usd", nullable = false, precision = 18, scale = 6)
    private BigDecimal adProviderCpcUsd;

    // ========================================
    // Period Information
    // ========================================

    /**
     * Start date of metrics period
     */
    @Column(name = "metrics_period_start", nullable = false)
    private LocalDate adProviderMetricsPeriodStart;

    /**
     * End date of metrics period
     */
    @Column(name = "metrics_period_end", nullable = false)
    private LocalDate adProviderMetricsPeriodEnd;

    // ========================================
    // Sync Information
    // ========================================

    /**
     * Source of metrics
     * Example: GOOGLE_ADSENSE_API, GOOGLE_ADMOB_API, MANUAL
     */
    @Column(name = "sync_source", nullable = false, length = 100)
    private String adProviderSyncSource;

    /**
     * Sync status
     * SUCCESS, PARTIAL, FAILED
     */
    @Column(name = "sync_status", nullable = false, length = 50)
    private String adProviderSyncStatus;

    /**
     * Error message if sync failed
     */
    @Column(name = "sync_error_message")
    private String adProviderSyncErrorMessage;

    /**
     * When metrics were synced from provider
     */
    @Column(name = "synced_at", nullable = false)
    private Instant adProviderSyncedAt;

    // ========================================
    // Extended Data & Metadata
    // ========================================

    /**
     * JSON object for provider-specific metadata
     * Examples:
     * - AdSense: {"networkType": "display", "adFormat": "responsive"}
     * - AdMob: {"platform": "iOS", "adFormat": "banner", "mediationNetworks":
     * [...]}
     */
    @Column(name = "metadata")
    private String adProviderMetadataJson;

    // ========================================
    // Multi-Tenant & Audit
    // ========================================

    /**
     * Tenant identifier for multi-tenant isolation
     */
    @Column(name = "tenant_id", nullable = false)
    private UUID adProviderTenantId;

    /**
     * Who created this record
     */
    @Column(name = "created_by", length = 255)
    private String adProviderCreatedBy;

    /**
     * When this record was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant adProviderCreatedAt;

    /**
     * Who last updated this record
     */
    @Column(name = "updated_by", length = 255)
    private String adProviderUpdatedBy;

    /**
     * When this record was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant adProviderUpdatedAt;

    /**
     * Soft delete timestamp
     * If null, record is active. If not null, record is deleted.
     */
    @Column(name = "deleted_at")
    private Instant adProviderDeletedAt;

    // ========================================
    // Lifecycle & Convenience Methods
    // ========================================

    /**
     * Check if this record is active (not soft-deleted)
     */
    public boolean isActive() {
        return adProviderDeletedAt == null;
    }

    /**
     * Soft delete this record
     */
    public void softDelete(String deletedByUser) {
        this.adProviderDeletedAt = Instant.now();
        this.adProviderUpdatedBy = deletedByUser;
    }

    /**
     * Restore a soft-deleted record
     */
    public void restore(String restoredByUser) {
        this.adProviderDeletedAt = null;
        this.adProviderUpdatedBy = restoredByUser;
    }

    /**
     * Calculate CTR if not already set
     */
    @PrePersist
    @PreUpdate
    public void calculateMetrics() {
        if (adProviderTotalImpressions != null && adProviderTotalImpressions > 0) {
            if (adProviderCtrPercentage == null) {
                BigDecimal impressions = new BigDecimal(adProviderTotalImpressions);
                BigDecimal clicks = new BigDecimal(adProviderTotalClicks != null ? adProviderTotalClicks : 0);
                adProviderCtrPercentage = clicks.divide(impressions, 6, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            if (adProviderCpmUsd == null && adProviderEstimatedEarningsUsd != null) {
                BigDecimal impressions = new BigDecimal(adProviderTotalImpressions);
                adProviderCpmUsd = adProviderEstimatedEarningsUsd
                        .divide(impressions, 6, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("1000"));
            }
        }

        if (adProviderTotalClicks != null && adProviderTotalClicks > 0 && adProviderCpcUsd == null) {
            if (adProviderEstimatedEarningsUsd != null) {
                BigDecimal clicks = new BigDecimal(adProviderTotalClicks);
                adProviderCpcUsd = adProviderEstimatedEarningsUsd
                        .divide(clicks, 6, java.math.RoundingMode.HALF_UP);
            }
        }
    }
}
