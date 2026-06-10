package com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.repository;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.entity.ProviderMetricsSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for ProviderMetricsSync entity
 * 
 * Provides database access for ad provider metrics
 * Supports queries like:
 * - Find metrics by provider and date range
 * - Find latest metrics per provider
 * - Find all metrics aggregated
 * - Find metrics for specific account
 * 
 * Naming Convention: {Entity}Repository
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface ProviderMetricsSyncRepository extends JpaRepository<ProviderMetricsSync, Long> {

        /**
         * Find all active (non-deleted) metrics for a specific provider and date range
         * 
         * @param adProviderType Provider type (e.g., GOOGLE_ADSENSE)
         * @param startDate      Period start
         * @param endDate        Period end
         * @return List of metrics records
         */
        @Query("SELECT p FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderType = :adProviderType " +
                        "AND p.adProviderMetricsPeriodStart >= :startDate " +
                        "AND p.adProviderMetricsPeriodEnd <= :endDate " +
                        "AND p.adProviderDeletedAt IS NULL " +
                        "ORDER BY p.adProviderMetricsPeriodStart DESC")
        List<ProviderMetricsSync> findMetricsByProviderAndDateRange(
                        @Param("adProviderType") ProviderType adProviderType,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Find the most recent metrics for a specific provider
         * 
         * @param adProviderType Provider type
         * @return Latest metrics record
         */
        @Query("SELECT p FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderType = :adProviderType " +
                        "AND p.adProviderDeletedAt IS NULL " +
                        "ORDER BY p.adProviderMetricsPeriodEnd DESC, p.adProviderSyncedAt DESC " +
                        "LIMIT 1")
        Optional<ProviderMetricsSync> findLatestByProviderType(
                        @Param("adProviderType") ProviderType adProviderType);

        /**
         * Find metrics by provider code and date range
         * 
         * @param adProviderCode Provider code (e.g., "google-adsense")
         * @param startDate      Period start
         * @param endDate        Period end
         * @return List of metrics records
         */
        @Query("SELECT p FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderCode = :adProviderCode " +
                        "AND p.adProviderMetricsPeriodStart >= :startDate " +
                        "AND p.adProviderMetricsPeriodEnd <= :endDate " +
                        "AND p.adProviderDeletedAt IS NULL " +
                        "ORDER BY p.adProviderMetricsPeriodStart DESC")
        List<ProviderMetricsSync> findMetricsByProviderCodeAndDateRange(
                        @Param("adProviderCode") String adProviderCode,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Find metrics for specific account within date range
         * 
         * @param adProviderAccountId Account ID (e.g., "pub-xxxxx")
         * @param startDate           Period start
         * @param endDate             Period end
         * @return List of metrics records
         */
        @Query("SELECT p FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderAccountId = :adProviderAccountId " +
                        "AND p.adProviderMetricsPeriodStart >= :startDate " +
                        "AND p.adProviderMetricsPeriodEnd <= :endDate " +
                        "AND p.adProviderDeletedAt IS NULL " +
                        "ORDER BY p.adProviderMetricsPeriodStart DESC")
        List<ProviderMetricsSync> findMetricsByAccountAndDateRange(
                        @Param("adProviderAccountId") String adProviderAccountId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Find all metrics for all providers within date range
         * 
         * @param startDate Period start
         * @param endDate   Period end
         * @return List of all metrics records
         */
        @Query("SELECT p FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderMetricsPeriodStart >= :startDate " +
                        "AND p.adProviderMetricsPeriodEnd <= :endDate " +
                        "AND p.adProviderDeletedAt IS NULL " +
                        "ORDER BY p.adProviderMetricsPeriodStart DESC, p.adProviderType ASC")
        List<ProviderMetricsSync> findAllMetricsInDateRange(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Find latest metrics for each enabled provider
         * 
         * @return List of most recent metrics per provider
         */
        @Query(value = "SELECT * FROM provider_metrics_sync p " +
                        "WHERE p.deleted_at IS NULL " +
                        "AND p.synced_at = (" +
                        "  SELECT MAX(synced_at) FROM provider_metrics_sync " +
                        "  WHERE provider_type = p.provider_type AND deleted_at IS NULL" +
                        ") " +
                        "ORDER BY p.provider_type ASC", nativeQuery = true)
        List<ProviderMetricsSync> findLatestMetricsPerProvider();

        /**
         * Find metrics by tenant
         * 
         * @param tenantId Tenant identifier
         * @return List of metrics for tenant
         */
        @Query("SELECT p FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderTenantId = :tenantId " +
                        "AND p.adProviderDeletedAt IS NULL " +
                        "ORDER BY p.adProviderMetricsPeriodStart DESC")
        List<ProviderMetricsSync> findByTenantId(
                        @Param("tenantId") String tenantId);

        /**
         * Find metrics by tenant and provider type
         * 
         * @param tenantId       Tenant identifier
         * @param adProviderType Provider type
         * @return List of metrics for tenant and provider
         */
        @Query("SELECT p FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderTenantId = :tenantId " +
                        "AND p.adProviderType = :adProviderType " +
                        "AND p.adProviderDeletedAt IS NULL " +
                        "ORDER BY p.adProviderMetricsPeriodStart DESC")
        List<ProviderMetricsSync> findByTenantIdAndProviderType(
                        @Param("tenantId") String tenantId,
                        @Param("adProviderType") ProviderType adProviderType);

        /**
         * Check if metrics already exist for provider and period
         * Prevents duplicate sync records
         * 
         * @param adProviderCode Provider code
         * @param startDate      Period start
         * @param endDate        Period end
         * @return true if metrics exist for this period
         */
        @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
                        "FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderCode = :adProviderCode " +
                        "AND p.adProviderMetricsPeriodStart = :startDate " +
                        "AND p.adProviderMetricsPeriodEnd = :endDate " +
                        "AND p.adProviderDeletedAt IS NULL")
        boolean existsMetricsForPeriod(
                        @Param("adProviderCode") String adProviderCode,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Find successfully synced metrics (exclude failed syncs)
         * 
         * @param adProviderType Provider type
         * @return List of successfully synced metrics
         */
        @Query("SELECT p FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderType = :adProviderType " +
                        "AND p.adProviderSyncStatus = 'SUCCESS' " +
                        "AND p.adProviderDeletedAt IS NULL " +
                        "ORDER BY p.adProviderMetricsPeriodStart DESC")
        List<ProviderMetricsSync> findSuccessfulSyncsByProvider(
                        @Param("adProviderType") ProviderType adProviderType);

        /**
         * Find all metrics for a tenant within date range
         *
         * @param tenantId  Tenant identifier
         * @param startDate Period start
         * @param endDate   Period end
         * @return List of metrics entities
         */
        @Query("SELECT p FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderTenantId = :tenantId " +
                        "AND p.adProviderMetricsPeriodStart >= :startDate " +
                        "AND p.adProviderMetricsPeriodEnd <= :endDate " +
                        "AND p.adProviderDeletedAt IS NULL " +
                        "ORDER BY p.adProviderSyncedAt DESC")
        List<ProviderMetricsSync> findByTenantIdAndDateRange(
                        @Param("tenantId") String tenantId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Find metrics for specific tenant, provider and date range
         *
         * @param tenantId     Tenant identifier
         * @param providerType Provider type
         * @param startDate    Period start
         * @param endDate      Period end
         * @return List of metrics entities
         */
        @Query("SELECT p FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderTenantId = :tenantId " +
                        "AND p.adProviderType = :providerType " +
                        "AND p.adProviderMetricsPeriodStart >= :startDate " +
                        "AND p.adProviderMetricsPeriodEnd <= :endDate " +
                        "AND p.adProviderDeletedAt IS NULL " +
                        "ORDER BY p.adProviderSyncedAt DESC")
        List<ProviderMetricsSync> findByTenantIdAndProviderTypeAndDateRange(
                        @Param("tenantId") String tenantId,
                        @Param("providerType") ProviderType providerType,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Find recent metrics for tenant (ordered by sync time)
         *
         * @param tenantId Tenant identifier
         * @param limit    Maximum number of records
         * @return List of recent metrics entities
         */
        @Query("SELECT p FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderTenantId = :tenantId " +
                        "AND p.adProviderDeletedAt IS NULL " +
                        "ORDER BY p.adProviderSyncedAt DESC")
        List<ProviderMetricsSync> findRecentByTenantId(
                        @Param("tenantId") String tenantId,
                        @Param("limit") int limit);

        /**
         * Find last successful sync timestamp for tenant
         *
         * @param tenantId Tenant identifier
         * @return Last sync timestamp
         */
        @Query("SELECT MAX(p.adProviderSyncedAt) FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderTenantId = :tenantId " +
                        "AND p.adProviderSyncStatus = 'SUCCESS' " +
                        "AND p.adProviderDeletedAt IS NULL")
        LocalDateTime findLastSuccessfulSyncTimeByTenantId(@Param("tenantId") String tenantId);

        /**
         * Count providers that haven't synced since threshold
         *
         * @param tenantId  Tenant identifier
         * @param threshold Timestamp threshold
         * @return Number of stale providers
         */
        @Query("SELECT COUNT(DISTINCT p.adProviderType) FROM ProviderMetricsSync p " +
                        "WHERE p.adProviderTenantId = :tenantId " +
                        "AND p.adProviderSyncedAt < :threshold " +
                        "AND p.adProviderDeletedAt IS NULL")
        int countStaleProvidersByTenantId(
                        @Param("tenantId") String tenantId,
                        @Param("threshold") LocalDateTime threshold);
}
