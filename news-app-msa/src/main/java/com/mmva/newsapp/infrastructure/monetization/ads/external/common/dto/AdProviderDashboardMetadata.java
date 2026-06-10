package com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dashboard metadata DTO containing request and response information
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdProviderDashboardMetadata {

    /**
     * Request timestamp
     */
    private LocalDateTime adProviderRequestTimestamp;

    /**
     * Response timestamp
     */
    private LocalDateTime adProviderResponseTimestamp;

    /**
     * Processing time in milliseconds
     */
    private Long adProviderProcessingTimeMs;

    /**
     * Data date range start (if filtered)
     */
    private LocalDate adProviderDataStartDate;

    /**
     * Data date range end (if filtered)
     */
    private LocalDate adProviderDataEndDate;

    /**
     * Tenant ID for multi-tenant isolation
     */
    private String adProviderTenantId;

    /**
     * API version
     */
    private String adProviderApiVersion;

    /**
     * Request ID for tracing
     */
    private String adProviderRequestId;

    /**
     * Cache status (HIT, MISS, BYPASS)
     */
    private String adProviderCacheStatus;

    /**
     * Data source (DATABASE, CACHE, MIXED)
     */
    private String adProviderDataSource;

    /**
     * Total records processed
     */
    private Long adProviderTotalRecordsProcessed;
}