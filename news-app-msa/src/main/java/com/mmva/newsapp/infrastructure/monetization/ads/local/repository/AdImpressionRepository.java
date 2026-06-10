package com.mmva.newsapp.infrastructure.monetization.ads.local.repository;

import com.mmva.newsapp.infrastructure.monetization.ads.local.model.AdImpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link AdImpression} entity operations.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface AdImpressionRepository extends JpaRepository<AdImpression, UUID> {

        // ========================================
        // Campaign Queries
        // ========================================

        /**
         * Counts impressions for a campaign.
         */
        long countByAdImpressionCampaignId(UUID campaignId);

        /**
         * Counts impressions for a campaign within date range.
         */
        @Query("""
                        SELECT COUNT(i) FROM AdImpression i
                        WHERE i.adImpressionCampaignId = :campaignId
                        AND i.adImpressionRecordedAt BETWEEN :startDate AND :endDate
                        """)
        long countByCampaignIdAndDateRange(
                        @Param("campaignId") UUID campaignId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        /**
         * Gets impressions for a campaign.
         */
        Page<AdImpression> findByAdImpressionCampaignIdOrderByAdImpressionRecordedAtDesc(UUID campaignId,
                        Pageable pageable);

        // ========================================
        // Placement Queries
        // ========================================

        /**
         * Counts impressions for a placement.
         */
        long countByAdImpressionPlacementId(UUID placementId);

        /**
         * Counts impressions for a placement within date range.
         */
        @Query("""
                        SELECT COUNT(i) FROM AdImpression i
                        WHERE i.adImpressionPlacementId = :placementId
                        AND i.adImpressionRecordedAt BETWEEN :startDate AND :endDate
                        """)
        long countByPlacementIdAndDateRange(
                        @Param("placementId") UUID placementId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        // ========================================
        // User Queries
        // ========================================

        /**
         * Counts impressions for a user.
         */
        long countByAdImpressionUserId(UUID userId);

        /**
         * Gets recent impressions for a user.
         */
        @Query("""
                        SELECT i FROM AdImpression i
                        WHERE i.adImpressionUserId = :userId
                        AND i.adImpressionRecordedAt >= :since
                        ORDER BY i.adImpressionRecordedAt DESC
                        """)
        List<AdImpression> findRecentByUserId(
                        @Param("userId") UUID userId,
                        @Param("since") Instant since);

        // ========================================
        // Revenue Queries
        // ========================================

        /**
         * Gets total revenue for a campaign.
         */
        @Query("""
                        SELECT COALESCE(SUM(i.adImpressionRevenueAmount), 0) FROM AdImpression i
                        WHERE i.adImpressionCampaignId = :campaignId
                        """)
        BigDecimal getTotalRevenueByCampaign(@Param("campaignId") UUID campaignId);

        /**
         * Gets total revenue within date range.
         */
        @Query("""
                        SELECT COALESCE(SUM(i.adImpressionRevenueAmount), 0) FROM AdImpression i
                        WHERE i.adImpressionTenantId = :tenantId
                        AND i.adImpressionRecordedAt BETWEEN :startDate AND :endDate
                        """)
        BigDecimal getTotalRevenueForPeriod(
                        @Param("tenantId") String tenantId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        // ========================================
        // Analytics Queries
        // ========================================

        /**
         * Counts viewable impressions for a campaign.
         */
        @Query("""
                        SELECT COUNT(i) FROM AdImpression i
                        WHERE i.adImpressionCampaignId = :campaignId
                        AND i.adImpressionIsViewable = true
                        """)
        long countViewableByCampaignId(@Param("campaignId") UUID campaignId);

        /**
         * Gets impressions by device type.
         */
        @Query("""
                        SELECT i.adImpressionDeviceType, COUNT(i) FROM AdImpression i
                        WHERE i.adImpressionTenantId = :tenantId
                        AND i.adImpressionRecordedAt BETWEEN :startDate AND :endDate
                        GROUP BY i.adImpressionDeviceType
                        """)
        List<Object[]> countByDeviceType(
                        @Param("tenantId") String tenantId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        /**
         * Gets impressions by country.
         */
        @Query("""
                        SELECT i.adImpressionCountryCode, COUNT(i) FROM AdImpression i
                        WHERE i.adImpressionTenantId = :tenantId
                        AND i.adImpressionRecordedAt BETWEEN :startDate AND :endDate
                        GROUP BY i.adImpressionCountryCode
                        ORDER BY COUNT(i) DESC
                        """)
        List<Object[]> countByCountry(
                        @Param("tenantId") String tenantId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        /**
         * Gets daily impression counts.
         */
        @Query(value = """
                        SELECT CAST(ad_impression_recorded_at AS DATE) as day, COUNT(*) as count
                        FROM monetization_ad_impression
                        WHERE ad_impression_tenant_id = :tenantId
                        AND ad_impression_recorded_at BETWEEN :startDate AND :endDate
                        GROUP BY CAST(ad_impression_recorded_at AS DATE)
                        ORDER BY day
                        """, nativeQuery = true)
        List<Object[]> getDailyImpressionCounts(
                        @Param("tenantId") String tenantId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        // ========================================
        // Fraud Detection
        // ========================================

        /**
         * Counts impressions from same IP within time window.
         */
        @Query("""
                        SELECT COUNT(i) FROM AdImpression i
                        WHERE i.adImpressionCampaignId = :campaignId
                        AND i.adImpressionIpHash = :ipHash
                        AND i.adImpressionRecordedAt >= :since
                        """)
        long countByIpHashWithinWindow(
                        @Param("campaignId") UUID campaignId,
                        @Param("ipHash") String ipHash,
                        @Param("since") Instant since);

        /**
         * Counts suspicious impressions.
         */
        @Query("""
                        SELECT COUNT(i) FROM AdImpression i
                        WHERE i.adImpressionTenantId = :tenantId
                        AND i.adImpressionIsSuspicious = true
                        AND i.adImpressionRecordedAt BETWEEN :startDate AND :endDate
                        """)
        long countSuspicious(
                        @Param("tenantId") String tenantId,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        // ========================================
        // Analytics Queries (Public API)
        // ========================================

        /**
         * Counts all impressions within date range.
         */
        long countByAdImpressionRecordedAtBetween(Instant startDate, Instant endDate);
}
