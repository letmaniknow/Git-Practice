package com.mmva.newsapp.infrastructure.monetization.ads.local.repository;

import com.mmva.newsapp.infrastructure.monetization.ads.local.model.AdCreative;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdCreativeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link AdCreative} entity operations.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface AdCreativeRepository extends JpaRepository<AdCreative, UUID> {

        // ========================================
        // Basic Finders
        // ========================================

        /**
         * Finds creative by code and tenant.
         */
        Optional<AdCreative> findByAdCreativeCodeAndAdCreativeTenantId(String creativeCode, String tenantId);

        /**
         * Checks if creative code exists for tenant.
         */
        boolean existsByAdCreativeCodeAndAdCreativeTenantId(String creativeCode, String tenantId);

        // ========================================
        // Active Creatives
        // ========================================

        /**
         * Finds all active creatives for a tenant.
         */
        @Query("SELECT c FROM AdCreative c WHERE c.adCreativeTenantId = :tenantId AND c.adCreativeIsActive = true AND c.deletedAt IS NULL")
        List<AdCreative> findActiveCreativesByTenantId(@Param("tenantId") String tenantId);

        /**
         * Finds all active and approved creatives for a tenant.
         */
        @Query("SELECT c FROM AdCreative c WHERE c.adCreativeTenantId = :tenantId AND c.adCreativeIsActive = true AND c.adCreativeApprovalStatus = 'APPROVED' AND c.deletedAt IS NULL")
        List<AdCreative> findAvailableCreativesByTenantId(@Param("tenantId") String tenantId);

        /**
         * Finds creatives by type and tenant.
         */
        @Query("SELECT c FROM AdCreative c WHERE c.adCreativeTenantId = :tenantId AND c.adCreativeType = :type AND c.deletedAt IS NULL")
        List<AdCreative> findByAdCreativeTypeAndTenantId(@Param("type") AdCreativeType type,
                        @Param("tenantId") String tenantId);

        // ========================================
        // Approval Workflow
        // ========================================

        /**
         * Finds creatives pending approval.
         */
        @Query("SELECT c FROM AdCreative c WHERE c.adCreativeTenantId = :tenantId AND c.adCreativeApprovalStatus = 'PENDING' AND c.deletedAt IS NULL")
        List<AdCreative> findPendingApprovalCreatives(@Param("tenantId") String tenantId);

        /**
         * Counts creatives by approval status.
         */
        @Query("SELECT COUNT(c) FROM AdCreative c WHERE c.adCreativeTenantId = :tenantId AND c.adCreativeApprovalStatus = :status AND c.deletedAt IS NULL")
        long countByApprovalStatus(@Param("tenantId") String tenantId, @Param("status") String status);

        // ========================================
        // Performance & Analytics
        // ========================================

        /**
         * Finds top performing creatives by impressions.
         */
        @Query("SELECT c FROM AdCreative c WHERE c.adCreativeTenantId = :tenantId AND c.adCreativeIsActive = true AND c.deletedAt IS NULL ORDER BY c.adCreativeTotalImpressions DESC")
        List<AdCreative> findTopCreativesByImpressions(@Param("tenantId") String tenantId, Pageable pageable);

        /**
         * Finds top performing creatives by CTR.
         */
        @Query("SELECT c FROM AdCreative c WHERE c.adCreativeTenantId = :tenantId AND c.adCreativeIsActive = true AND c.adCreativeTotalImpressions > 0 AND c.deletedAt IS NULL ORDER BY (c.adCreativeTotalClicks * 1.0 / c.adCreativeTotalImpressions) DESC")
        List<AdCreative> findTopCreativesByCtr(@Param("tenantId") String tenantId, Pageable pageable);

        /**
         * Gets total impressions for creatives in date range.
         */
        @Query("SELECT SUM(c.adCreativeTotalImpressions) FROM AdCreative c WHERE c.adCreativeTenantId = :tenantId AND c.adCreativeLastServedAt BETWEEN :startDate AND :endDate")
        Long getTotalImpressionsInDateRange(@Param("tenantId") String tenantId, @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        // ========================================
        // Bulk Operations
        // ========================================

        /**
         * Updates approval status for multiple creatives.
         */
        @Modifying
        @Query("UPDATE AdCreative c SET c.adCreativeApprovalStatus = :status, c.updatedAt = :updatedAt, c.updatedBy = :updatedBy WHERE c.adCreativeId IN :creativeIds")
        int updateApprovalStatus(@Param("creativeIds") List<UUID> creativeIds, @Param("status") String status,
                        @Param("updatedAt") Instant updatedAt, @Param("updatedBy") UUID updatedBy);

        /**
         * Deactivates creatives by type (for maintenance).
         */
        @Modifying
        @Query("UPDATE AdCreative c SET c.adCreativeIsActive = false, c.updatedAt = :updatedAt, c.updatedBy = :updatedBy WHERE c.adCreativeTenantId = :tenantId AND c.adCreativeType = :type")
        int deactivateCreativesByType(@Param("tenantId") String tenantId, @Param("type") AdCreativeType type,
                        @Param("updatedAt") Instant updatedAt, @Param("updatedBy") UUID updatedBy);

        // ========================================
        // Statistics Updates (Atomic)
        // ========================================

        /**
         * Increments impression count for a creative.
         */
        @Modifying
        @Query("UPDATE AdCreative c SET c.adCreativeTotalImpressions = c.adCreativeTotalImpressions + 1, c.adCreativeLastServedAt = :servedAt WHERE c.adCreativeId = :creativeId")
        void incrementImpressionCount(@Param("creativeId") UUID creativeId, @Param("servedAt") Instant servedAt);

        /**
         * Increments click count for a creative.
         */
        @Modifying
        @Query("UPDATE AdCreative c SET c.adCreativeTotalClicks = c.adCreativeTotalClicks + 1 WHERE c.adCreativeId = :creativeId")
        void incrementClickCount(@Param("creativeId") UUID creativeId);

        /**
         * Updates revenue for a creative.
         */
        @Modifying
        @Query("UPDATE AdCreative c SET c.adCreativeTotalRevenue = c.adCreativeTotalRevenue + :revenue WHERE c.adCreativeId = :creativeId")
        void addRevenue(@Param("creativeId") UUID creativeId, @Param("revenue") java.math.BigDecimal revenue);

        // ========================================
        // Cleanup & Maintenance
        // ========================================

        /**
         * Finds creatives not served in the last N days.
         */
        @Query("SELECT c FROM AdCreative c WHERE c.adCreativeTenantId = :tenantId AND (c.adCreativeLastServedAt IS NULL OR c.adCreativeLastServedAt < :cutoffDate) AND c.deletedAt IS NULL")
        List<AdCreative> findUnusedCreatives(@Param("tenantId") String tenantId,
                        @Param("cutoffDate") Instant cutoffDate);

        /**
         * Counts creatives by tenant.
         */
        @Query("SELECT COUNT(c) FROM AdCreative c WHERE c.adCreativeTenantId = :tenantId AND c.deletedAt IS NULL")
        long countByTenantId(@Param("tenantId") String tenantId);

        /**
         * Finds creatives by file name (for cleanup).
         */
        @Query("SELECT c FROM AdCreative c WHERE c.adCreativeFileName = :fileName")
        List<AdCreative> findByAdCreativeFileName(@Param("fileName") String fileName);

        /**
         * Finds all creatives for a tenant (paginated).
         */
        @Query("SELECT c FROM AdCreative c WHERE c.adCreativeTenantId = :tenantId AND c.deletedAt IS NULL")
        Page<AdCreative> findAllByAdCreativeTenantIdAndDeletedAtIsNull(@Param("tenantId") String tenantId,
                        Pageable pageable);
}