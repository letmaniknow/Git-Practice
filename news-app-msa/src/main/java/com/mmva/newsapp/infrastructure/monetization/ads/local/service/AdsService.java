package com.mmva.newsapp.infrastructure.monetization.ads.local.service;

import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdPlacementRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdPlacementResponseDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdClickRecordRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdImpressionRecordRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdCreativeRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdCreativeResponseDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdCreativeUploadRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdType;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.PlacementPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Ad Placement management and tracking.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface AdsService {

    // ========================================
    // Placement CRUD
    // ========================================

    /**
     * Creates a new ad placement.
     */
    AdPlacementResponseDto createPlacement(AdPlacementRequestDto request, String tenantId);

    /**
     * Gets placement by ID.
     */
    AdPlacementResponseDto getPlacementById(UUID placementId);

    /**
     * Gets placement by code.
     */
    AdPlacementResponseDto getPlacementByCode(String placementCode, String tenantId);

    /**
     * Updates an existing placement.
     */
    AdPlacementResponseDto updatePlacement(UUID placementId, AdPlacementRequestDto request);

    /**
     * Deletes a placement (soft delete).
     */
    void deletePlacement(UUID placementId);

    // ========================================
    // Placement Queries
    // ========================================

    /**
     * Gets all placements for a tenant with pagination.
     */
    Page<AdPlacementResponseDto> getAllPlacements(String tenantId, Pageable pageable);

    /**
     * Gets active placements for a tenant.
     */
    List<AdPlacementResponseDto> getActivePlacements(String tenantId);

    /**
     * Gets placements by page type.
     */
    List<AdPlacementResponseDto> getPlacementsByPage(String pageType, String tenantId);

    /**
     * Gets placements by position.
     */
    List<AdPlacementResponseDto> getPlacementsByPosition(PlacementPosition position, String tenantId);

    /**
     * Gets placements by ad type.
     */
    List<AdPlacementResponseDto> getPlacementsByAdType(AdType adType, String tenantId);

    /**
     * Gets premium placements.
     */
    List<AdPlacementResponseDto> getPremiumPlacements(String tenantId);

    // ========================================
    // Placement Status
    // ========================================

    /**
     * Activates a placement.
     */
    AdPlacementResponseDto activatePlacement(UUID placementId);

    /**
     * Deactivates a placement.
     */
    AdPlacementResponseDto deactivatePlacement(UUID placementId);

    // ========================================
    // Impression Tracking
    // ========================================

    /**
     * Records an ad impression.
     * Returns the impression ID for click tracking.
     */
    UUID recordImpression(AdImpressionRecordRequestDto request, String tenantId, String ipHash, String userAgent);

    /**
     * Gets impressions for a campaign within date range.
     */
    long getImpressionCount(UUID campaignId, Instant startDate, Instant endDate);

    /**
     * Gets impressions for a placement within date range.
     */
    long getPlacementImpressionCount(UUID placementId, Instant startDate, Instant endDate);

    // ========================================
    // Click Tracking
    // ========================================

    /**
     * Records an ad click.
     * Returns the click ID.
     */
    UUID recordClick(AdClickRecordRequestDto request, String tenantId, String ipHash, String userAgent);

    /**
     * Gets clicks for a campaign within date range.
     */
    long getClickCount(UUID campaignId, Instant startDate, Instant endDate);

    /**
     * Gets billable clicks for a campaign.
     */
    long getBillableClickCount(UUID campaignId);

    /**
     * Marks a click as converted.
     */
    void markClickAsConverted(UUID clickId, UUID conversionId, Long conversionTimeSeconds);

    // ========================================
    // Analytics
    // ========================================

    /**
     * Gets click-through rate for a campaign.
     */
    double getCampaignCtr(UUID campaignId);

    /**
     * Gets click-through rate for a placement.
     */
    double getPlacementCtr(UUID placementId);

    /**
     * Gets daily impression counts.
     */
    List<Object[]> getDailyImpressionCounts(String tenantId, Instant startDate, Instant endDate);

    /**
     * Gets daily click counts.
     */
    List<Object[]> getDailyClickCounts(String tenantId, Instant startDate, Instant endDate);

    /**
     * Gets impressions by device type.
     */
    List<Object[]> getImpressionsByDeviceType(String tenantId, Instant startDate, Instant endDate);

    /**
     * Gets clicks by device type.
     */
    List<Object[]> getClicksByDeviceType(String tenantId, Instant startDate, Instant endDate);

    /**
     * Gets impressions by country.
     */
    List<Object[]> getImpressionsByCountry(String tenantId, Instant startDate, Instant endDate);

    // ========================================
    // Fraud Detection
    // ========================================

    /**
     * Checks if impression should be flagged as suspicious.
     */
    boolean isSuspiciousImpression(UUID campaignId, String ipHash);

    /**
     * Checks if click should be flagged as suspicious.
     */
    boolean isSuspiciousClick(UUID campaignId, UUID userId, String ipHash);

    /**
     * Gets suspicious activity count.
     */
    long getSuspiciousImpressionCount(String tenantId, Instant startDate, Instant endDate);

    /**
     * Gets suspicious click count.
     */
    long getSuspiciousClickCount(String tenantId, Instant startDate, Instant endDate);

    // ========================================
    // Creative Management
    // ========================================

    /**
     * Creates a new ad creative (with optional file upload).
     */
    AdCreativeResponseDto createCreative(AdCreativeUploadRequestDto request, String tenantId);

    /**
     * Gets creative by ID.
     */
    AdCreativeResponseDto getCreativeById(UUID creativeId);

    /**
     * Gets creative by code.
     */
    AdCreativeResponseDto getCreativeByCode(String creativeCode, String tenantId);

    /**
     * Updates an existing creative with file upload.
     */
    AdCreativeResponseDto updateCreative(UUID creativeId, AdCreativeUploadRequestDto request);

    /**
     * Deletes a creative (soft delete).
     */
    void deleteCreative(UUID creativeId);

    /**
     * Gets all creatives for a tenant with pagination.
     */
    Page<AdCreativeResponseDto> getAllCreatives(String tenantId, Pageable pageable);

    /**
     * Gets active creatives for a tenant.
     */
    List<AdCreativeResponseDto> getActiveCreatives(String tenantId);

    /**
     * Gets available creatives for serving.
     */
    List<AdCreativeResponseDto> getAvailableCreatives(String tenantId);

    /**
     * Approves a creative for use.
     */
    AdCreativeResponseDto approveCreative(UUID creativeId);

    /**
     * Rejects a creative with reason.
     */
    AdCreativeResponseDto rejectCreative(UUID creativeId, String reason);

    /**
     * Activates a creative.
     */
    AdCreativeResponseDto activateCreative(UUID creativeId);

    /**
     * Deactivates a creative.
     */
    AdCreativeResponseDto deactivateCreative(UUID creativeId);

    // ========================================
    // Public Analytics (Limited)
    // ========================================

    /**
     * Gets public performance metrics (aggregated, no sensitive data).
     */
    java.util.Map<String, Object> getPublicPerformanceMetrics(java.time.LocalDate startDate,
            java.time.LocalDate endDate);

    /**
     * Gets public campaign metrics (basic counts only).
     */
    java.util.Map<String, Object> getPublicCampaignMetrics(String campaignId);

    // ========================================
    // Creative File Serving
    // ========================================

    /**
     * Gets a creative file resource by filename.
     * Used for serving creative assets to clients.
     */
    org.springframework.core.io.Resource getCreativeFile(String filename);
}
