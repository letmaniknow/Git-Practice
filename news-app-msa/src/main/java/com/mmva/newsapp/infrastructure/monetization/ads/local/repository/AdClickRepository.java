package com.mmva.newsapp.infrastructure.monetization.ads.local.repository;

import com.mmva.newsapp.infrastructure.monetization.ads.local.model.AdClick;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link AdClick} entity operations.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface AdClickRepository extends JpaRepository<AdClick, UUID> {

    // ========================================
    // Campaign Queries
    // ========================================

    /**
     * Counts clicks for a campaign.
     */
    long countByAdClickCampaignId(UUID campaignId);

    /**
     * Counts clicks for a campaign within date range.
     */
    @Query("""
            SELECT COUNT(c) FROM AdClick c
            WHERE c.adClickCampaignId = :campaignId
            AND c.adClickRecordedAt BETWEEN :startDate AND :endDate
            """)
    long countByCampaignIdAndDateRange(
            @Param("campaignId") UUID campaignId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Gets clicks for a campaign.
     */
    Page<AdClick> findByAdClickCampaignIdOrderByAdClickRecordedAtDesc(UUID campaignId, Pageable pageable);

    // ========================================
    // Placement Queries
    // ========================================

    /**
     * Counts clicks for a placement.
     */
    long countByAdClickPlacementId(UUID placementId);

    /**
     * Counts clicks for a placement within date range.
     */
    @Query("""
            SELECT COUNT(c) FROM AdClick c
            WHERE c.adClickPlacementId = :placementId
            AND c.adClickRecordedAt BETWEEN :startDate AND :endDate
            """)
    long countByPlacementIdAndDateRange(
            @Param("placementId") UUID placementId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // ========================================
    // User Queries
    // ========================================

    /**
     * Counts clicks for a user.
     */
    long countByAdClickUserId(UUID userId);

    /**
     * Gets recent clicks for a user.
     */
    @Query("""
            SELECT c FROM AdClick c
            WHERE c.adClickUserId = :userId
            AND c.adClickRecordedAt >= :since
            ORDER BY c.adClickRecordedAt DESC
            """)
    List<AdClick> findRecentByUserId(
            @Param("userId") UUID userId,
            @Param("since") Instant since);

    // ========================================
    // Revenue Queries
    // ========================================

    /**
     * Gets total revenue for a campaign.
     */
    @Query("""
            SELECT COALESCE(SUM(c.adClickRevenueAmount), 0) FROM AdClick c
            WHERE c.adClickCampaignId = :campaignId
            """)
    BigDecimal getTotalRevenueByCampaign(@Param("campaignId") UUID campaignId);

    /**
     * Gets total revenue within date range.
     */
    @Query("""
            SELECT COALESCE(SUM(c.adClickRevenueAmount), 0) FROM AdClick c
            WHERE c.adClickTenantId = :tenantId
            AND c.adClickRecordedAt BETWEEN :startDate AND :endDate
            """)
    BigDecimal getTotalRevenueForPeriod(
            @Param("tenantId") String tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Gets billable clicks (validated, not suspicious, not duplicate).
     */
    @Query("""
            SELECT COUNT(c) FROM AdClick c
            WHERE c.adClickCampaignId = :campaignId
            AND c.adClickIsValidated = true
            AND c.adClickIsSuspicious = false
            AND c.adClickIsDuplicate = false
            """)
    long countBillableByCampaignId(@Param("campaignId") UUID campaignId);

    // ========================================
    // Conversion Queries
    // ========================================

    /**
     * Counts conversions for a campaign.
     */
    @Query("""
            SELECT COUNT(c) FROM AdClick c
            WHERE c.adClickCampaignId = :campaignId
            AND c.adClickConverted = true
            """)
    long countConversionsByCampaignId(@Param("campaignId") UUID campaignId);

    /**
     * Updates click as converted.
     */
    @Modifying
    @Query("""
            UPDATE AdClick c
            SET c.adClickConverted = true,
                c.adClickConversionId = :conversionId,
                c.adClickConversionTimeSeconds = :conversionTime
            WHERE c.adClickId = :clickId
            """)
    void markAsConverted(
            @Param("clickId") UUID clickId,
            @Param("conversionId") UUID conversionId,
            @Param("conversionTime") Long conversionTime);

    // ========================================
    // Analytics Queries
    // ========================================

    /**
     * Gets clicks by device type.
     */
    @Query("""
            SELECT c.adClickDeviceType, COUNT(c) FROM AdClick c
            WHERE c.adClickTenantId = :tenantId
            AND c.adClickRecordedAt BETWEEN :startDate AND :endDate
            GROUP BY c.adClickDeviceType
            """)
    List<Object[]> countByDeviceType(
            @Param("tenantId") String tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Gets clicks by country.
     */
    @Query("""
            SELECT c.adClickCountryCode, COUNT(c) FROM AdClick c
            WHERE c.adClickTenantId = :tenantId
            AND c.adClickRecordedAt BETWEEN :startDate AND :endDate
            GROUP BY c.adClickCountryCode
            ORDER BY COUNT(c) DESC
            """)
    List<Object[]> countByCountry(
            @Param("tenantId") String tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Gets daily click counts.
     */
    @Query(value = """
            SELECT CAST(ad_click_recorded_at AS DATE) as day, COUNT(*) as count
            FROM monetization_ad_click
            WHERE ad_click_tenant_id = :tenantId
            AND ad_click_recorded_at BETWEEN :startDate AND :endDate
            GROUP BY CAST(ad_click_recorded_at AS DATE)
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> getDailyClickCounts(
            @Param("tenantId") String tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    // ========================================
    // Fraud Detection
    // ========================================

    /**
     * Counts clicks from same IP within time window.
     */
    @Query("""
            SELECT COUNT(c) FROM AdClick c
            WHERE c.adClickCampaignId = :campaignId
            AND c.adClickIpHash = :ipHash
            AND c.adClickRecordedAt >= :since
            """)
    long countByIpHashWithinWindow(
            @Param("campaignId") UUID campaignId,
            @Param("ipHash") String ipHash,
            @Param("since") Instant since);

    /**
     * Finds potential duplicate clicks.
     */
    @Query("""
            SELECT c FROM AdClick c
            WHERE c.adClickCampaignId = :campaignId
            AND c.adClickUserId = :userId
            AND c.adClickRecordedAt >= :since
            ORDER BY c.adClickRecordedAt DESC
            """)
    List<AdClick> findPotentialDuplicates(
            @Param("campaignId") UUID campaignId,
            @Param("userId") UUID userId,
            @Param("since") Instant since);

    /**
     * Counts suspicious clicks.
     */
    @Query("""
            SELECT COUNT(c) FROM AdClick c
            WHERE c.adClickTenantId = :tenantId
            AND c.adClickIsSuspicious = true
            AND c.adClickRecordedAt BETWEEN :startDate AND :endDate
            """)
    long countSuspicious(
            @Param("tenantId") String tenantId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Gets average time from impression to click.
     */
    @Query("""
            SELECT AVG(c.adClickTimeSinceImpressionMs) FROM AdClick c
            WHERE c.adClickCampaignId = :campaignId
            AND c.adClickTimeSinceImpressionMs IS NOT NULL
            """)
    Double getAverageTimeToClick(@Param("campaignId") UUID campaignId);

    // ========================================
    // Analytics Queries (Public API)
    // ========================================

    /**
     * Counts all clicks within date range.
     */
    long countByAdClickRecordedAtBetween(Instant startDate, Instant endDate);
}
