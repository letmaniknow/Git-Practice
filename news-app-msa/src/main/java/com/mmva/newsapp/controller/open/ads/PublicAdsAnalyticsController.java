package com.mmva.newsapp.controller.open.ads;

import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.service.AdsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Public Ads Analytics Controller - For ad performance monitoring.
 *
 * <p>
 * Provides read-only analytics endpoints for ad performance metrics.
 * These endpoints are public but rate-limited to prevent abuse.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/api/v1/public/ads/analytics")
@Tag(name = "Public Ads Analytics", description = "Public ad performance analytics APIs")
@RequiredArgsConstructor
public class PublicAdsAnalyticsController {

    private final AdsService adsService;

    /**
     * Get basic ad performance metrics for a date range.
     *
     * <p>
     * Returns aggregated impression and click counts for public monitoring.
     * Rate limited to prevent excessive API usage.
     * </p>
     */
    @GetMapping("/performance")
    @Operation(summary = "Get Ad Performance Metrics", description = "Retrieve basic ad performance metrics for monitoring")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date parameters"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getPerformanceMetrics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        log.debug("Public: Retrieving ad performance metrics from {} to {}", startDate, endDate);

        // Default to last 7 days if no dates provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(7);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Get aggregated metrics (implement in AdsService)
        Map<String, Object> metrics = adsService.getPublicPerformanceMetrics(startDate, endDate);

        log.info("Public: Retrieved ad performance metrics for period {} to {}", startDate, endDate);

        return ResponseEntity.ok(
                ApiResponseDto.success("Ad performance metrics retrieved successfully", metrics));
    }

    /**
     * Get campaign-specific metrics (public summary only).
     *
     * <p>
     * Returns basic metrics for a specific campaign without sensitive data.
     * </p>
     */
    @GetMapping("/campaigns/{campaignId}")
    @Operation(summary = "Get Campaign Metrics", description = "Retrieve public metrics for a specific ad campaign")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campaign metrics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Campaign not found"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getCampaignMetrics(
            @PathVariable String campaignId) {

        log.debug("Public: Retrieving metrics for campaign: {}", campaignId);

        // Get public campaign metrics (implement in AdsService)
        Map<String, Object> metrics = adsService.getPublicCampaignMetrics(campaignId);

        if (metrics.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        log.info("Public: Retrieved public metrics for campaign: {}", campaignId);

        return ResponseEntity.ok(
                ApiResponseDto.success("Campaign metrics retrieved successfully", metrics));
    }
}