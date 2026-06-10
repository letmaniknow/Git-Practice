package com.mmva.newsapp.infrastructure.monetization.ads.local.repository;

import com.mmva.newsapp.infrastructure.monetization.ads.local.model.AdPlacement;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdType;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.PlacementPosition;
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
 * Repository for {@link AdPlacement} entity operations.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface AdPlacementRepository extends JpaRepository<AdPlacement, UUID> {

        // ========================================
        // Basic Finders
        // ========================================

        /**
         * Finds placement by code and tenant.
         */
        Optional<AdPlacement> findByAdPlacementCodeAndAdPlacementTenantId(String placementCode, String tenantId);

        /**
         * Checks if placement code exists for tenant.
         */
        boolean existsByAdPlacementCodeAndAdPlacementTenantId(String placementCode, String tenantId);

        // ========================================
        // Active Placements
        // ========================================

        /**
         * Finds all active placements for a tenant.
         */
        @Query("""
                        SELECT p FROM AdPlacement p
                        WHERE p.adPlacementIsActive = true
                        AND p.adPlacementTenantId = :tenantId
                        AND p.deletedAt IS NULL
                        ORDER BY p.adPlacementDisplayOrder ASC
                        """)
        List<AdPlacement> findActiveByTenantId(@Param("tenantId") String tenantId);

        /**
         * Finds all placements (active and inactive) for admin.
         */
        @Query("""
                        SELECT p FROM AdPlacement p
                        WHERE p.adPlacementTenantId = :tenantId
                        AND p.deletedAt IS NULL
                        """)
        Page<AdPlacement> findAllByTenantId(
                        @Param("tenantId") String tenantId,
                        Pageable pageable);

        // ========================================
        // Page & Position Queries
        // ========================================

        /**
         * Finds active placements for a specific page type.
         */
        @Query("""
                        SELECT p FROM AdPlacement p
                        WHERE p.adPlacementPageType = :pageType
                        AND p.adPlacementIsActive = true
                        AND p.adPlacementTenantId = :tenantId
                        AND p.deletedAt IS NULL
                        ORDER BY p.adPlacementPosition, p.adPlacementDisplayOrder
                        """)
        List<AdPlacement> findActiveByPageType(
                        @Param("pageType") String pageType,
                        @Param("tenantId") String tenantId);

        /**
         * Finds active placements by position.
         */
        @Query("""
                        SELECT p FROM AdPlacement p
                        WHERE p.adPlacementPosition = :position
                        AND p.adPlacementIsActive = true
                        AND p.adPlacementTenantId = :tenantId
                        AND p.deletedAt IS NULL
                        ORDER BY p.adPlacementDisplayOrder ASC
                        """)
        List<AdPlacement> findActiveByPosition(
                        @Param("position") PlacementPosition position,
                        @Param("tenantId") String tenantId);

        /**
         * Finds placements by ad type.
         */
        @Query("""
                        SELECT p FROM AdPlacement p
                        WHERE p.adPlacementAdType = :adType
                        AND p.adPlacementIsActive = true
                        AND p.adPlacementTenantId = :tenantId
                        AND p.deletedAt IS NULL
                        ORDER BY p.adPlacementDisplayOrder ASC
                        """)
        List<AdPlacement> findActiveByAdType(
                        @Param("adType") AdType adType,
                        @Param("tenantId") String tenantId);

        /**
         * Finds premium placements.
         */
        @Query("""
                        SELECT p FROM AdPlacement p
                        WHERE p.adPlacementIsPremium = true
                        AND p.adPlacementIsActive = true
                        AND p.adPlacementTenantId = :tenantId
                        AND p.deletedAt IS NULL
                        ORDER BY p.adPlacementBaseCpmRate DESC
                        """)
        List<AdPlacement> findPremiumPlacements(@Param("tenantId") String tenantId);

        // ========================================
        // Metrics Update
        // ========================================

        /**
         * Records an impression on a placement.
         */
        @Modifying
        @Query("""
                        UPDATE AdPlacement p
                        SET p.adPlacementTotalImpressions = p.adPlacementTotalImpressions + 1,
                            p.adPlacementLastImpressionAt = :now,
                            p.updatedAt = :now
                        WHERE p.adPlacementId = :placementId
                        """)
        void recordImpression(
                        @Param("placementId") UUID placementId,
                        @Param("now") Instant now);

        /**
         * Records a click on a placement.
         */
        @Modifying
        @Query("""
                        UPDATE AdPlacement p
                        SET p.adPlacementTotalClicks = p.adPlacementTotalClicks + 1,
                            p.updatedAt = :now
                        WHERE p.adPlacementId = :placementId
                        """)
        void recordClick(
                        @Param("placementId") UUID placementId,
                        @Param("now") Instant now);

        /**
         * Adds revenue to a placement.
         */
        @Modifying
        @Query("""
                        UPDATE AdPlacement p
                        SET p.adPlacementTotalRevenue = p.adPlacementTotalRevenue + :amount,
                            p.updatedAt = :now
                        WHERE p.adPlacementId = :placementId
                        """)
        void addRevenue(
                        @Param("placementId") UUID placementId,
                        @Param("amount") BigDecimal amount,
                        @Param("now") Instant now);

        // ========================================
        // Statistics
        // ========================================

        /**
         * Gets total impressions across all placements.
         */
        @Query("""
                        SELECT COALESCE(SUM(p.adPlacementTotalImpressions), 0)
                        FROM AdPlacement p
                        WHERE p.adPlacementTenantId = :tenantId
                        AND p.deletedAt IS NULL
                        """)
        Long getTotalImpressions(@Param("tenantId") String tenantId);

        /**
         * Gets total revenue across all placements.
         */
        @Query("""
                        SELECT COALESCE(SUM(p.adPlacementTotalRevenue), 0)
                        FROM AdPlacement p
                        WHERE p.adPlacementTenantId = :tenantId
                        AND p.deletedAt IS NULL
                        """)
        BigDecimal getTotalRevenue(@Param("tenantId") String tenantId);

        /**
         * Gets placement performance summary.
         */
        @Query("""
                        SELECT p.adPlacementCode, p.adPlacementName, p.adPlacementTotalImpressions,
                               p.adPlacementTotalClicks, p.adPlacementTotalRevenue
                        FROM AdPlacement p
                        WHERE p.adPlacementTenantId = :tenantId
                        AND p.deletedAt IS NULL
                        ORDER BY p.adPlacementTotalRevenue DESC
                        """)
        List<Object[]> getPerformanceSummary(@Param("tenantId") String tenantId);

        /**
         * Counts active placements by type.
         */
        @Query("""
                        SELECT p.adPlacementAdType, COUNT(p)
                        FROM AdPlacement p
                        WHERE p.adPlacementIsActive = true
                        AND p.adPlacementTenantId = :tenantId
                        AND p.deletedAt IS NULL
                        GROUP BY p.adPlacementAdType
                        """)
        List<Object[]> countByAdType(@Param("tenantId") String tenantId);
}
