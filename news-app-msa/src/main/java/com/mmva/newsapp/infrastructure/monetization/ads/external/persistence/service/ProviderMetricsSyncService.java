package com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.service;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderMetricsDto;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.exception.AdProviderException;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.exception.AdProviderSyncException;
import com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.entity.ProviderMetricsSync;
import com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.mapper.AdProviderMetricsDtoEntityMapper;
import com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.repository.ProviderMetricsSyncRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing ad provider metrics persistence
 * 
 * Handles:
 * - Saving metrics to database
 * - Retrieving metrics from database
 * - Querying metrics by various criteria
 * - Duplicate detection (prevent multiple saves for same period)
 * 
 * Naming Convention: {Entity}Service
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderMetricsSyncService {

    private final ProviderMetricsSyncRepository repository;
    private final AdProviderMetricsDtoEntityMapper mapper;

    /**
     * Save provider metrics to database
     * 
     * Performs:
     * 1. Checks if metrics already exist for this period
     * 2. Converts DTO to Entity
     * 3. Sets default values (tenant, sync source, etc.)
     * 4. Saves to database
     * 
     * @param metricsDto Metrics DTO from provider
     * @return Saved entity
     * @throws AdProviderSyncException if save fails
     */
    @Transactional
    public ProviderMetricsSync saveProviderMetrics(AdProviderMetricsDto metricsDto) {
        try {
            log.debug("💾 Saving metrics for provider: {}", metricsDto.getAdProviderType());

            if (metricsDto == null) {
                log.error("❌ Cannot save null metrics DTO");
                throw new AdProviderSyncException(
                        "Metrics DTO cannot be null",
                        null,
                        "NULL_METRICS_DTO");
            }

            // Check for duplicate sync
            String adProviderCode = metricsDto.getAdProviderType().getAdProviderCode();
            if (repository.existsMetricsForPeriod(
                    adProviderCode,
                    metricsDto.getAdProviderMetricsPeriodStart(),
                    metricsDto.getAdProviderMetricsPeriodEnd())) {

                log.warn("⚠️  Metrics already exist for provider {} for period {} to {}",
                        adProviderCode,
                        metricsDto.getAdProviderMetricsPeriodStart(),
                        metricsDto.getAdProviderMetricsPeriodEnd());

                // Return latest instead of duplicate
                return repository.findLatestByProviderType(metricsDto.getAdProviderType())
                        .orElse(null);
            }

            // Convert DTO to Entity
            ProviderMetricsSync entity = mapper.toEntity(metricsDto);

            // Set defaults
            entity.setAdProviderCode(adProviderCode);
            if (entity.getAdProviderTenantId() == null) {
                // Use a consistent default tenant UUID
                entity.setAdProviderTenantId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            }
            if (entity.getAdProviderCreatedBy() == null) {
                entity.setAdProviderCreatedBy("SYSTEM");
            }
            if (entity.getAdProviderUpdatedBy() == null) {
                entity.setAdProviderUpdatedBy("SYSTEM");
            }

            // Save to database
            ProviderMetricsSync saved = repository.save(entity);

            log.info("✅ Metrics saved for provider {} (id: {})",
                    metricsDto.getAdProviderType().getAdProviderDisplayName(),
                    saved.getId());

            return saved;

        } catch (AdProviderSyncException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Error saving provider metrics", e);
            throw new AdProviderSyncException(
                    "Failed to save provider metrics: " + e.getMessage(),
                    metricsDto != null ? metricsDto.getAdProviderType() : null,
                    "SAVE_ERROR",
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Get metrics for specific provider and date range
     * 
     * @param adProviderType Provider type
     * @param startDate      Period start
     * @param endDate        Period end
     * @return List of metrics DTOs
     */
    @Transactional(readOnly = true)
    public List<AdProviderMetricsDto> getMetricsByProviderAndDateRange(
            ProviderType adProviderType,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("📊 Getting metrics for provider {} from {} to {}",
                adProviderType, startDate, endDate);

        List<ProviderMetricsSync> entities = repository.findMetricsByProviderAndDateRange(
                adProviderType, startDate, endDate);

        return entities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get latest metrics for specific provider
     * 
     * @param adProviderType Provider type
     * @return Latest metrics DTO or null if not found
     */
    @Transactional(readOnly = true)
    public AdProviderMetricsDto getLatestMetrics(ProviderType adProviderType) {
        log.debug("📊 Getting latest metrics for provider: {}", adProviderType);

        return repository.findLatestByProviderType(adProviderType)
                .map(mapper::toDto)
                .orElse(null);
    }

    /**
     * Get metrics for specific account and date range
     * 
     * @param adProviderAccountId Account ID (pub-xxx, ca-app-pub-xxx)
     * @param startDate           Period start
     * @param endDate             Period end
     * @return List of metrics DTOs
     */
    @Transactional(readOnly = true)
    public List<AdProviderMetricsDto> getMetricsByAccountAndDateRange(
            String adProviderAccountId,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("📊 Getting metrics for account {} from {} to {}",
                adProviderAccountId, startDate, endDate);

        List<ProviderMetricsSync> entities = repository.findMetricsByAccountAndDateRange(
                adProviderAccountId, startDate, endDate);

        return entities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all metrics for all providers in date range
     * 
     * @param startDate Period start
     * @param endDate   Period end
     * @return List of all metrics DTOs aggregated
     */
    @Transactional(readOnly = true)
    public List<AdProviderMetricsDto> getAllMetricsInDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("📊 Getting metrics for all providers from {} to {}", startDate, endDate);

        List<ProviderMetricsSync> entities = repository.findAllMetricsInDateRange(startDate, endDate);

        return entities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get latest metrics for each provider
     * 
     * @return Map of provider type to latest metrics
     */
    @Transactional(readOnly = true)
    public List<AdProviderMetricsDto> getLatestMetricsPerProvider() {
        log.debug("📊 Getting latest metrics for each provider");

        List<ProviderMetricsSync> entities = repository.findLatestMetricsPerProvider();

        return entities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get metrics by tenant
     * 
     * @param tenantId Tenant identifier
     * @return List of metrics for tenant
     */
    @Transactional(readOnly = true)
    public List<AdProviderMetricsDto> getMetricsByTenant(String tenantId) {
        log.debug("📊 Getting metrics for tenant: {}", tenantId);

        List<ProviderMetricsSync> entities = repository.findByTenantId(tenantId);

        return entities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get metrics count for provider
     * 
     * @param adProviderType Provider type
     * @return Number of metrics records
     */
    @Transactional(readOnly = true)
    public long getMetricsCount(ProviderType adProviderType) {
        log.debug("📊 Counting metrics for provider: {}", adProviderType);

        return repository.findByTenantIdAndProviderType("default-tenant", adProviderType)
                .stream()
                .count();
    }

    /**
     * Update existing metrics (for correcting data)
     * 
     * @param metricsDto Updated metrics
     * @return Updated entity
     */
    @Transactional
    public ProviderMetricsSync updateProviderMetrics(AdProviderMetricsDto metricsDto) {
        try {
            log.debug("🔄 Updating metrics for provider: {}", metricsDto.getAdProviderType());

            ProviderMetricsSync entity = mapper.toEntity(metricsDto);
            entity.setAdProviderUpdatedBy("SYSTEM");

            ProviderMetricsSync updated = repository.save(entity);

            log.info("✅ Metrics updated for provider {} (id: {})",
                    metricsDto.getAdProviderType().getAdProviderDisplayName(),
                    updated.getId());

            return updated;

        } catch (Exception e) {
            log.error("❌ Error updating provider metrics", e);
            throw new AdProviderSyncException(
                    "Failed to update provider metrics: " + e.getMessage(),
                    metricsDto.getAdProviderType(),
                    "UPDATE_ERROR",
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Soft delete metrics (mark as deleted without removing)
     * 
     * @param metricsId ID of metrics to delete
     */
    @Transactional
    public void softDeleteMetrics(Long metricsId) {
        try {
            log.debug("🗑️  Soft deleting metrics with id: {}", metricsId);

            repository.findById(metricsId).ifPresent(metrics -> {
                metrics.softDelete("SYSTEM");
                repository.save(metrics);
                log.info("✅ Metrics soft deleted (id: {})", metricsId);
            });

        } catch (Exception e) {
            log.error("❌ Error soft deleting metrics", e);
            throw new AdProviderException(
                    "Failed to delete metrics: " + e.getMessage(),
                    null,
                    "DELETE_ERROR");
        }
    }

    /**
     * Get metrics by tenant and date range
     *
     * @param tenantId  Tenant identifier
     * @param startDate Period start
     * @param endDate   Period end
     * @return List of metrics for tenant in date range
     */
    @Transactional(readOnly = true)
    public List<AdProviderMetricsDto> getMetricsByTenantAndDateRange(
            String tenantId,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("📊 Getting metrics for tenant {} from {} to {}", tenantId, startDate, endDate);

        List<ProviderMetricsSync> entities = repository.findByTenantIdAndDateRange(
                tenantId, startDate, endDate);

        return entities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get metrics by provider, tenant and date range
     *
     * @param tenantId     Tenant identifier
     * @param providerType Provider type
     * @param startDate    Period start
     * @param endDate      Period end
     * @return List of metrics for provider and tenant in date range
     */
    @Transactional(readOnly = true)
    public List<AdProviderMetricsDto> getMetricsByProviderAndDateRange(
            String tenantId,
            ProviderType providerType,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("📊 Getting metrics for tenant {}, provider {} from {} to {}",
                tenantId, providerType, startDate, endDate);

        List<ProviderMetricsSync> entities = repository.findByTenantIdAndProviderTypeAndDateRange(
                tenantId, providerType, startDate, endDate);

        return entities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get recent metrics by tenant
     *
     * @param tenantId Tenant identifier
     * @param limit    Maximum number of records to return
     * @return List of recent metrics
     */
    @Transactional(readOnly = true)
    public List<AdProviderMetricsDto> getRecentMetricsByTenant(String tenantId, int limit) {
        log.debug("📊 Getting recent metrics for tenant {}, limit: {}", tenantId, limit);

        List<ProviderMetricsSync> entities = repository.findRecentByTenantId(tenantId, limit);

        return entities.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get last successful sync time for tenant
     *
     * @param tenantId Tenant identifier
     * @return Last sync timestamp or null if never synced
     */
    @Transactional(readOnly = true)
    public LocalDateTime getLastSuccessfulSyncTime(String tenantId) {
        log.debug("📊 Getting last sync time for tenant: {}", tenantId);

        return repository.findLastSuccessfulSyncTimeByTenantId(tenantId);
    }

    /**
     * Count stale providers (no sync in specified hours)
     *
     * @param tenantId Tenant identifier
     * @param hours    Hours threshold for staleness
     * @return Number of stale providers
     */
    @Transactional(readOnly = true)
    public int countStaleProviders(String tenantId, int hours) {
        log.debug("📊 Counting stale providers for tenant {} (threshold: {} hours)", tenantId, hours);

        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        return repository.countStaleProvidersByTenantId(tenantId, threshold);
    }
}
