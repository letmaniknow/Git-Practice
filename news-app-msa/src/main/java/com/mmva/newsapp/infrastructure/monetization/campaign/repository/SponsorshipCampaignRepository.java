package com.mmva.newsapp.infrastructure.monetization.campaign.repository;

import com.mmva.newsapp.infrastructure.monetization.campaign.model.SponsorshipCampaign;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignStatus;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignType;
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
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link SponsorshipCampaign} entity operations.
 * All queries use sponsorshipCampaign prefixed field names.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface SponsorshipCampaignRepository extends JpaRepository<SponsorshipCampaign, UUID> {

    // ========================================
    // Basic Finders
    // ========================================

    /**
     * Finds campaign by code.
     */
    Optional<SponsorshipCampaign> findBySponsorshipCampaignCode(String sponsorshipCampaignCode);

    /**
     * Checks if campaign code exists.
     */
    boolean existsBySponsorshipCampaignCode(String sponsorshipCampaignCode);

    // ========================================
    // Status Queries
    // ========================================

    /**
     * Finds campaigns by status.
     * Note: ORDER BY is handled by Pageable parameter.
     */
    @Query("""
            SELECT c FROM SponsorshipCampaign c
            WHERE (:status IS NULL OR c.sponsorshipCampaignStatus = :status)
            AND c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            """)
    Page<SponsorshipCampaign> findByStatusAndTenantId(
            @Param("status") SponsorshipCampaignStatus status,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    /**
     * Finds campaigns by status (including soft-deleted for admin).
     * Note: ORDER BY is handled by Pageable parameter.
     */
    @Query("""
            SELECT c FROM SponsorshipCampaign c
            WHERE (:status IS NULL OR c.sponsorshipCampaignStatus = :status)
            AND c.sponsorshipCampaignTenantId = :tenantId
            """)
    Page<SponsorshipCampaign> findAllByStatusAndTenantIdIncludingDeleted(
            @Param("status") SponsorshipCampaignStatus status,
            @Param("tenantId") String tenantId,
            Pageable pageable);

    /**
     * Finds all active campaigns (ACTIVE status within schedule with budget).
     */
    @Query("""
            SELECT c FROM SponsorshipCampaign c
            WHERE c.sponsorshipCampaignStatus = 'ACTIVE'
            AND c.sponsorshipCampaignStartDate <= :now
            AND c.sponsorshipCampaignEndDate >= :now
            AND c.sponsorshipCampaignAmountSpent < c.sponsorshipCampaignTotalBudget
            AND c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            ORDER BY c.sponsorshipCampaignRate DESC
            """)
    List<SponsorshipCampaign> findServingCampaigns(
            @Param("now") Instant now,
            @Param("tenantId") String tenantId);

    /**
     * Finds campaigns pending approval.
     */
    @Query("""
            SELECT c FROM SponsorshipCampaign c
            WHERE c.sponsorshipCampaignStatus = 'PENDING_APPROVAL'
            AND c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            ORDER BY c.createdAt ASC
            """)
    List<SponsorshipCampaign> findPendingApproval(@Param("tenantId") String tenantId);

    // ========================================
    // Advertiser Queries
    // ========================================

    /**
     * Finds campaigns by advertiser.
     */
    @Query("""
            SELECT c FROM SponsorshipCampaign c
            WHERE c.sponsorshipCampaignAdvertiserId = :advertiserId
            AND c.deletedAt IS NULL
            ORDER BY c.createdAt DESC
            """)
    List<SponsorshipCampaign> findByAdvertiserId(@Param("advertiserId") UUID advertiserId);

    /**
     * Finds active campaigns by advertiser.
     */
    @Query("""
            SELECT c FROM SponsorshipCampaign c
            WHERE c.sponsorshipCampaignAdvertiserId = :advertiserId
            AND c.sponsorshipCampaignStatus IN ('ACTIVE', 'APPROVED', 'PENDING_APPROVAL')
            AND c.deletedAt IS NULL
            ORDER BY c.sponsorshipCampaignStartDate ASC
            """)
    List<SponsorshipCampaign> findActiveByAdvertiserId(@Param("advertiserId") UUID advertiserId);

    // ========================================
    // Type & Targeting Queries
    // ========================================

    /**
     * Finds serving campaigns by type.
     */
    @Query("""
            SELECT c FROM SponsorshipCampaign c
            WHERE c.sponsorshipCampaignType = :campaignType
            AND c.sponsorshipCampaignStatus = 'ACTIVE'
            AND c.sponsorshipCampaignStartDate <= :now
            AND c.sponsorshipCampaignEndDate >= :now
            AND c.sponsorshipCampaignAmountSpent < c.sponsorshipCampaignTotalBudget
            AND c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            ORDER BY c.sponsorshipCampaignRate DESC
            """)
    List<SponsorshipCampaign> findServingByType(
            @Param("campaignType") SponsorshipCampaignType campaignType,
            @Param("now") Instant now,
            @Param("tenantId") String tenantId);

    /**
     * Finds campaign by linked content ID.
     */
    Optional<SponsorshipCampaign> findBySponsorshipCampaignContentIdAndDeletedAtIsNull(UUID contentId);

    // ========================================
    // Scheduling Queries
    // ========================================

    /**
     * Finds campaigns ready to activate (approved & start date reached).
     */
    @Query("""
            SELECT c FROM SponsorshipCampaign c
            WHERE c.sponsorshipCampaignStatus = 'APPROVED'
            AND c.sponsorshipCampaignStartDate <= :now
            AND c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            """)
    List<SponsorshipCampaign> findReadyToActivate(
            @Param("now") Instant now,
            @Param("tenantId") String tenantId);

    /**
     * Finds campaigns that should be completed (end date passed).
     */
    @Query("""
            SELECT c FROM SponsorshipCampaign c
            WHERE c.sponsorshipCampaignStatus = 'ACTIVE'
            AND c.sponsorshipCampaignEndDate < :now
            AND c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            """)
    List<SponsorshipCampaign> findExpiredActiveCampaigns(
            @Param("now") Instant now,
            @Param("tenantId") String tenantId);

    /**
     * Finds campaigns with exhausted budget.
     */
    @Query("""
            SELECT c FROM SponsorshipCampaign c
            WHERE c.sponsorshipCampaignStatus = 'ACTIVE'
            AND c.sponsorshipCampaignAmountSpent >= c.sponsorshipCampaignTotalBudget
            AND c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            """)
    List<SponsorshipCampaign> findBudgetExhaustedCampaigns(@Param("tenantId") String tenantId);

    // ========================================
    // Metrics Update
    // ========================================

    /**
     * Increments impression count.
     */
    @Modifying
    @Query("""
            UPDATE SponsorshipCampaign c
            SET c.sponsorshipCampaignImpressionCount = c.sponsorshipCampaignImpressionCount + 1,
                c.updatedAt = :now
            WHERE c.sponsorshipCampaignId = :sponsorshipCampaignId
            """)
    void incrementImpressions(
            @Param("sponsorshipCampaignId") UUID sponsorshipCampaignId,
            @Param("now") Instant now);

    /**
     * Increments click count.
     */
    @Modifying
    @Query("""
            UPDATE SponsorshipCampaign c
            SET c.sponsorshipCampaignClickCount = c.sponsorshipCampaignClickCount + 1,
                c.updatedAt = :now
            WHERE c.sponsorshipCampaignId = :sponsorshipCampaignId
            """)
    void incrementClicks(
            @Param("sponsorshipCampaignId") UUID sponsorshipCampaignId,
            @Param("now") Instant now);

    /**
     * Adds to amount spent.
     */
    @Modifying
    @Query("""
            UPDATE SponsorshipCampaign c
            SET c.sponsorshipCampaignAmountSpent = c.sponsorshipCampaignAmountSpent + :amount,
                c.updatedAt = :now
            WHERE c.sponsorshipCampaignId = :sponsorshipCampaignId
            """)
    void addToAmountSpent(
            @Param("sponsorshipCampaignId") UUID sponsorshipCampaignId,
            @Param("amount") BigDecimal amount,
            @Param("now") Instant now);

    // ========================================
    // Status Updates
    // ========================================

    /**
     * Activates approved campaigns that are ready.
     */
    @Modifying
    @Query("""
            UPDATE SponsorshipCampaign c
            SET c.sponsorshipCampaignStatus = 'ACTIVE',
                c.sponsorshipCampaignActivatedAt = :now,
                c.updatedAt = :now
            WHERE c.sponsorshipCampaignStatus = 'APPROVED'
            AND c.sponsorshipCampaignStartDate <= :now
            AND c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            """)
    int activateReadyCampaigns(
            @Param("now") Instant now,
            @Param("tenantId") String tenantId);

    /**
     * Completes expired campaigns.
     */
    @Modifying
    @Query("""
            UPDATE SponsorshipCampaign c
            SET c.sponsorshipCampaignStatus = 'COMPLETED',
                c.sponsorshipCampaignCompletedAt = :now,
                c.updatedAt = :now
            WHERE c.sponsorshipCampaignStatus = 'ACTIVE'
            AND c.sponsorshipCampaignEndDate < :now
            AND c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            """)
    int completeExpiredCampaigns(
            @Param("now") Instant now,
            @Param("tenantId") String tenantId);

    // ========================================
    // Statistics
    // ========================================

    /**
     * Counts campaigns by status.
     */
    @Query("""
            SELECT c.sponsorshipCampaignStatus, COUNT(c)
            FROM SponsorshipCampaign c
            WHERE c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            GROUP BY c.sponsorshipCampaignStatus
            """)
    List<Object[]> countByStatusGrouped(@Param("tenantId") String tenantId);

    /**
     * Gets total revenue from campaigns.
     */
    @Query("""
            SELECT COALESCE(SUM(c.sponsorshipCampaignAmountSpent), 0)
            FROM SponsorshipCampaign c
            WHERE c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            """)
    BigDecimal getTotalRevenue(@Param("tenantId") String tenantId);

    /**
     * Gets total impressions across campaigns.
     */
    @Query("""
            SELECT COALESCE(SUM(c.sponsorshipCampaignImpressionCount), 0)
            FROM SponsorshipCampaign c
            WHERE c.sponsorshipCampaignTenantId = :tenantId
            AND c.deletedAt IS NULL
            """)
    Long getTotalImpressions(@Param("tenantId") String tenantId);
}
