package com.mmva.newsapp.controller.admin.ads;

import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestInfoService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdClickRecordRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdCreativeResponseDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdCreativeUploadRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdImpressionRecordRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdPlacementRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdPlacementResponseDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdType;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.PlacementPosition;
import com.mmva.newsapp.infrastructure.monetization.ads.local.service.AdsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin REST Controller for Ad Placement management and tracking.
 * 
 * <p>
 * Located in controller/admin/monetization/ per PROJECT_PRINCIPLES.md:
 * Admin APIs should be in controller/admin/
 * </p>
 * 
 * Base Path: /api/v1/admin/ads
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/admin/ads")
@RequiredArgsConstructor
@Slf4j
public class AdminAdsController {

    private final AdsService adsService;
    private final RequestInfoService requestInfoService;

    private static final String DEFAULT_TENANT = "default";

    // ========================================
    // Placement CRUD Endpoints
    // ========================================

    /**
     * Creates a new ad placement.
     * 
     * POST /api/v1/admin/ads/placements
     */
    @PostMapping("/placements")
    public ResponseEntity<ApiResponseDto<AdPlacementResponseDto>> createPlacement(
            @Valid @RequestBody AdPlacementRequestDto request,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.info("Creating placement: {} for tenant: {}", request.getAdPlacementCode(), tenantId);
        AdPlacementResponseDto response = adsService.createPlacement(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success("Placement created", response));
    }

    /**
     * Gets a placement by ID.
     * 
     * GET /api/v1/admin/ads/placements/{id}
     */
    @GetMapping("/placements/{id}")
    public ResponseEntity<ApiResponseDto<AdPlacementResponseDto>> getPlacement(@PathVariable("id") UUID placementId) {
        log.debug("Getting placement: {}", placementId);
        AdPlacementResponseDto response = adsService.getPlacementById(placementId);
        return ResponseEntity.ok(ApiResponseDto.success("Placement retrieved", response));
    }

    /**
     * Gets a placement by code.
     * 
     * GET /api/v1/admin/ads/placements/code/{code}
     */
    @GetMapping("/placements/code/{code}")
    public ResponseEntity<ApiResponseDto<AdPlacementResponseDto>> getPlacementByCode(
            @PathVariable("code") String placementCode,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.debug("Getting placement by code: {}", placementCode);
        AdPlacementResponseDto response = adsService.getPlacementByCode(placementCode, tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Placement retrieved", response));
    }

    /**
     * Updates a placement.
     * 
     * PUT /api/v1/admindashboard/ads/placements/{id}
     */
    @PutMapping("/placements/{id}")
    public ResponseEntity<ApiResponseDto<AdPlacementResponseDto>> updatePlacement(
            @PathVariable("id") UUID placementId,
            @Valid @RequestBody AdPlacementRequestDto request) {
        log.info("Updating placement: {}", placementId);
        AdPlacementResponseDto response = adsService.updatePlacement(placementId, request);
        return ResponseEntity.ok(ApiResponseDto.success("Placement updated", response));
    }

    /**
     * Deletes a placement (soft delete).
     * 
     * DELETE /api/v1/admin/ads/placements/{id}
     */
    @DeleteMapping("/placements/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deletePlacement(@PathVariable("id") UUID placementId) {
        log.info("Deleting placement: {}", placementId);
        adsService.deletePlacement(placementId);
        return ResponseEntity.ok(ApiResponseDto.success("Placement deleted successfully", null));
    }

    // ========================================
    // Placement Listing Endpoints
    // ========================================

    /**
     * Gets all placements with pagination.
     * 
     * GET /api/v1/admindashboard/ads/placements
     */
    @GetMapping("/placements")
    public ResponseEntity<ApiResponseDto<Page<AdPlacementResponseDto>>> getAllPlacements(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            @PageableDefault(size = 20, sort = "adPlacementDisplayOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("Getting all placements for tenant: {}", tenantId);
        Page<AdPlacementResponseDto> response = adsService.getAllPlacements(tenantId, pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Placements retrieved", response));
    }

    /**
     * Gets active placements.
     * 
     * GET /api/v1/admindashboard/ads/placements/active
     */
    @GetMapping("/placements/active")
    public ResponseEntity<ApiResponseDto<List<AdPlacementResponseDto>>> getActivePlacements(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.debug("Getting active placements for tenant: {}", tenantId);
        List<AdPlacementResponseDto> response = adsService.getActivePlacements(tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Active placements retrieved", response));
    }

    /**
     * Gets placements by page type.
     * 
     * GET /api/v1/admindashboard/ads/placements/page/{pageType}
     */
    @GetMapping("/placements/page/{pageType}")
    public ResponseEntity<ApiResponseDto<List<AdPlacementResponseDto>>> getPlacementsByPage(
            @PathVariable("pageType") String pageType,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.debug("Getting placements for page: {} tenant: {}", pageType, tenantId);
        List<AdPlacementResponseDto> response = adsService.getPlacementsByPage(pageType, tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Placements retrieved", response));
    }

    /**
     * Gets placements by position.
     * 
     * GET /api/v1/admindashboard/ads/placements/position/{position}
     */
    @GetMapping("/placements/position/{position}")
    public ResponseEntity<ApiResponseDto<List<AdPlacementResponseDto>>> getPlacementsByPosition(
            @PathVariable("position") PlacementPosition position,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.debug("Getting placements for position: {} tenant: {}", position, tenantId);
        List<AdPlacementResponseDto> response = adsService.getPlacementsByPosition(position, tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Placements retrieved", response));
    }

    /**
     * Gets placements by ad type.
     * 
     * GET /api/v1/admindashboard/ads/placements/type/{adType}
     */
    @GetMapping("/placements/type/{adType}")
    public ResponseEntity<ApiResponseDto<List<AdPlacementResponseDto>>> getPlacementsByAdType(
            @PathVariable("adType") AdType adType,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.debug("Getting placements for ad type: {} tenant: {}", adType, tenantId);
        List<AdPlacementResponseDto> response = adsService.getPlacementsByAdType(adType, tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Placements retrieved", response));
    }

    /**
     * Gets premium placements.
     * 
     * GET /api/v1/admindashboard/ads/placements/premium
     */
    @GetMapping("/placements/premium")
    public ResponseEntity<ApiResponseDto<List<AdPlacementResponseDto>>> getPremiumPlacements(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.debug("Getting premium placements for tenant: {}", tenantId);
        List<AdPlacementResponseDto> response = adsService.getPremiumPlacements(tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Premium placements retrieved", response));
    }

    // ========================================
    // Placement Status Endpoints
    // ========================================

    /**
     * Activates a placement.
     * 
     * POST /api/v1/admindashboard/ads/placements/{id}/activate
     */
    @PostMapping("/placements/{id}/activate")
    public ResponseEntity<ApiResponseDto<AdPlacementResponseDto>> activatePlacement(
            @PathVariable("id") UUID placementId) {
        log.info("Activating placement: {}", placementId);
        AdPlacementResponseDto response = adsService.activatePlacement(placementId);
        return ResponseEntity.ok(ApiResponseDto.success("Placement activated", response));
    }

    /**
     * Deactivates a placement.
     * 
     * POST /api/v1/admindashboard/ads/placements/{id}/deactivate
     */
    @PostMapping("/placements/{id}/deactivate")
    public ResponseEntity<ApiResponseDto<AdPlacementResponseDto>> deactivatePlacement(
            @PathVariable("id") UUID placementId) {
        log.info("Deactivating placement: {}", placementId);
        AdPlacementResponseDto response = adsService.deactivatePlacement(placementId);
        return ResponseEntity.ok(ApiResponseDto.success("Placement deactivated", response));
    }

    // ========================================
    // Tracking Endpoints
    // ========================================

    /**
     * Records an ad impression.
     * 
     * POST /api/v1/admindashboard/ads/impressions
     */
    @PostMapping("/impressions")
    public ResponseEntity<ApiResponseDto<Map<String, String>>> recordImpression(
            @Valid @RequestBody AdImpressionRecordRequestDto request,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            HttpServletRequest httpRequest) {
        String ipHash = hashIp(requestInfoService.getClientIpAddress(httpRequest));
        String userAgent = httpRequest.getHeader("User-Agent");

        log.debug("Recording impression for campaign: {}", request.getAdImpressionCampaignId());
        UUID impressionId = adsService.recordImpression(request, tenantId, ipHash, userAgent);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.success("Impression recorded", Map.of("impressionId", impressionId.toString())));
    }

    /**
     * Records an ad click.
     * 
     * POST /api/v1/admindashboard/ads/clicks
     */
    @PostMapping("/clicks")
    public ResponseEntity<ApiResponseDto<Map<String, String>>> recordClick(
            @Valid @RequestBody AdClickRecordRequestDto request,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            HttpServletRequest httpRequest) {
        String ipHash = hashIp(requestInfoService.getClientIpAddress(httpRequest));
        String userAgent = httpRequest.getHeader("User-Agent");

        log.debug("Recording click for campaign: {}", request.getAdClickCampaignId());
        UUID clickId = adsService.recordClick(request, tenantId, ipHash, userAgent);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseDto.success("Click recorded", Map.of("clickId", clickId.toString())));
    }

    /**
     * Marks a click as converted.
     * 
     * POST /api/v1/admindashboard/ads/clicks/{clickId}/convert
     */
    @PostMapping("/clicks/{clickId}/convert")
    public ResponseEntity<ApiResponseDto<Void>> markClickAsConverted(
            @PathVariable("clickId") UUID clickId,
            @RequestBody Map<String, Object> body) {
        UUID conversionId = body.containsKey("conversionId") ? UUID.fromString((String) body.get("conversionId"))
                : UUID.randomUUID();
        Long conversionTime = body.containsKey("conversionTimeSeconds")
                ? ((Number) body.get("conversionTimeSeconds")).longValue()
                : null;

        log.info("Marking click {} as converted", clickId);
        adsService.markClickAsConverted(clickId, conversionId, conversionTime);

        return ResponseEntity.ok(
                ApiResponseDto.success("Click marked as converted", null));
    }

    // ========================================
    // Analytics Endpoints
    // ========================================

    /**
     * Gets campaign CTR.
     * 
     * GET /api/v1/admindashboard/ads/analytics/campaign/{campaignId}/ctr
     */
    @GetMapping("/analytics/campaign/{campaignId}/ctr")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getCampaignCtr(
            @PathVariable("campaignId") UUID campaignId) {
        double ctr = adsService.getCampaignCtr(campaignId);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign CTR retrieved", Map.of(
                "campaignId", campaignId.toString(),
                "ctr", ctr,
                "ctrFormatted", String.format("%.2f%%", ctr))));
    }

    /**
     * Gets placement CTR.
     * 
     * GET /api/v1/admindashboard/ads/analytics/placement/{placementId}/ctr
     */
    @GetMapping("/analytics/placement/{placementId}/ctr")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getPlacementCtr(
            @PathVariable("placementId") UUID placementId) {
        double ctr = adsService.getPlacementCtr(placementId);
        return ResponseEntity.ok(ApiResponseDto.success("Placement CTR retrieved", Map.of(
                "placementId", placementId.toString(),
                "ctr", ctr,
                "ctrFormatted", String.format("%.2f%%", ctr))));
    }

    /**
     * Gets daily impressions for date range.
     * 
     * GET /api/v1/admindashboard/ads/analytics/impressions/daily
     */
    @GetMapping("/analytics/impressions/daily")
    public ResponseEntity<ApiResponseDto<List<Object[]>>> getDailyImpressions(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }
        List<Object[]> data = adsService.getDailyImpressionCounts(tenantId, startDate, endDate);
        return ResponseEntity.ok(ApiResponseDto.success("Daily impressions retrieved", data));
    }

    /**
     * Gets daily clicks for date range.
     * 
     * GET /api/v1/admindashboard/ads/analytics/clicks/daily
     */
    @GetMapping("/analytics/clicks/daily")
    public ResponseEntity<ApiResponseDto<List<Object[]>>> getDailyClicks(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }
        List<Object[]> data = adsService.getDailyClickCounts(tenantId, startDate, endDate);
        return ResponseEntity.ok(ApiResponseDto.success("Daily clicks retrieved", data));
    }

    /**
     * Gets impressions by device type.
     * 
     * GET /api/v1/admindashboard/ads/analytics/impressions/by-device
     */
    @GetMapping("/analytics/impressions/by-device")
    public ResponseEntity<ApiResponseDto<List<Object[]>>> getImpressionsByDevice(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }
        List<Object[]> data = adsService.getImpressionsByDeviceType(tenantId, startDate, endDate);
        return ResponseEntity.ok(ApiResponseDto.success("Impressions by device retrieved", data));
    }

    /**
     * Gets impressions by country.
     * 
     * GET /api/v1/admindashboard/ads/analytics/impressions/by-country
     */
    @GetMapping("/analytics/impressions/by-country")
    public ResponseEntity<ApiResponseDto<List<Object[]>>> getImpressionsByCountry(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }
        List<Object[]> data = adsService.getImpressionsByCountry(tenantId, startDate, endDate);
        return ResponseEntity.ok(ApiResponseDto.success("Impressions by country retrieved", data));
    }

    /**
     * Gets impression count for a campaign within date range.
     *
     * GET /api/v1/admin/ads/analytics/campaign/{campaignId}/impressions
     */
    @GetMapping("/analytics/campaign/{campaignId}/impressions")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getCampaignImpressionCount(
            @PathVariable UUID campaignId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        long count = adsService.getImpressionCount(campaignId, startDate, endDate);
        Map<String, Object> result = Map.of(
                "campaignId", campaignId,
                "impressionCount", count,
                "startDate", startDate,
                "endDate", endDate);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign impression count retrieved", result));
    }

    /**
     * Gets impression count for a placement within date range.
     *
     * GET /api/v1/admin/ads/analytics/placement/{placementId}/impressions
     */
    @GetMapping("/analytics/placement/{placementId}/impressions")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getPlacementImpressionCount(
            @PathVariable UUID placementId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        long count = adsService.getPlacementImpressionCount(placementId, startDate, endDate);
        Map<String, Object> result = Map.of(
                "placementId", placementId,
                "impressionCount", count,
                "startDate", startDate,
                "endDate", endDate);
        return ResponseEntity.ok(ApiResponseDto.success("Placement impression count retrieved", result));
    }

    /**
     * Gets click count for a campaign within date range.
     *
     * GET /api/v1/admin/ads/analytics/campaign/{campaignId}/clicks
     */
    @GetMapping("/analytics/campaign/{campaignId}/clicks")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getCampaignClickCount(
            @PathVariable UUID campaignId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        long count = adsService.getClickCount(campaignId, startDate, endDate);
        Map<String, Object> result = Map.of(
                "campaignId", campaignId,
                "clickCount", count,
                "startDate", startDate,
                "endDate", endDate);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign click count retrieved", result));
    }

    /**
     * Gets billable click count for a campaign.
     *
     * GET /api/v1/admin/ads/analytics/campaign/{campaignId}/billable-clicks
     */
    @GetMapping("/analytics/campaign/{campaignId}/billable-clicks")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getCampaignBillableClickCount(
            @PathVariable UUID campaignId) {
        long count = adsService.getBillableClickCount(campaignId);
        Map<String, Object> result = Map.of(
                "campaignId", campaignId,
                "billableClickCount", count);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign billable click count retrieved", result));
    }

    /**
     * Gets suspicious impression count within date range.
     *
     * GET /api/v1/admin/ads/analytics/suspicious-impressions
     */
    @GetMapping("/analytics/suspicious-impressions")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getSuspiciousImpressionCount(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().minus(7, ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        long count = adsService.getSuspiciousImpressionCount(tenantId, startDate, endDate);
        Map<String, Object> result = Map.of(
                "suspiciousImpressionCount", count,
                "startDate", startDate,
                "endDate", endDate,
                "tenantId", tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Suspicious impression count retrieved", result));
    }

    /**
     * Gets ad performance metrics.
     *
     * GET /api/v1/admin/ads/analytics/performance
     */
    @GetMapping("/analytics/performance")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getAdPerformance(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        Map<String, Object> metrics = adsService.getPublicPerformanceMetrics(startDate, endDate);

        return ResponseEntity.ok(ApiResponseDto.success("Ad performance metrics retrieved", metrics));
    }

    // ========================================
    // Fraud Detection Endpoints
    // ========================================

    /**
     * Gets fraud statistics.
     * 
     * GET /api/v1/admindashboard/ads/fraud/stats
     */
    @GetMapping("/fraud/stats")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getFraudStats(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {
        if (startDate == null) {
            startDate = Instant.now().minus(7, ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        long suspiciousImpressions = adsService.getSuspiciousImpressionCount(tenantId, startDate, endDate);
        long suspiciousClicks = adsService.getSuspiciousClickCount(tenantId, startDate, endDate);

        return ResponseEntity.ok(ApiResponseDto.success("Fraud stats retrieved", Map.of(
                "suspiciousImpressions", suspiciousImpressions,
                "suspiciousClicks", suspiciousClicks,
                "startDate", startDate.toString(),
                "endDate", endDate.toString())));
    }

    // ========================================
    // Creative Management Endpoints
    // ========================================

    /**
     * Creates a new creative (with optional file upload).
     *
     * POST /api/v1/admin/ads/creatives
     */
    @PostMapping(value = "/creatives", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<AdCreativeResponseDto>> createCreative(
            @Valid @ModelAttribute AdCreativeUploadRequestDto request,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            HttpServletRequest httpRequest) {

        log.info("Creating creative: {} for tenant: {}", request.getAdCreativeCode(), tenantId);

        AdCreativeResponseDto response = adsService.createCreative(request, tenantId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Creative created successfully", response));
    }

    /**
     * Gets a creative by ID.
     *
     * GET /api/v1/admin/ads/creatives/{id}
     */
    @GetMapping("/creatives/{id}")
    public ResponseEntity<ApiResponseDto<AdCreativeResponseDto>> getCreative(@PathVariable UUID id) {
        log.debug("Getting creative: {}", id);

        AdCreativeResponseDto response = adsService.getCreativeById(id);

        return ResponseEntity.ok(ApiResponseDto.success("Creative retrieved", response));
    }

    /**
     * Gets a creative by code.
     *
     * GET /api/v1/admin/ads/creatives/code/{code}
     */
    @GetMapping("/creatives/code/{code}")
    public ResponseEntity<ApiResponseDto<AdCreativeResponseDto>> getCreativeByCode(
            @PathVariable String code,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            HttpServletRequest request) {

        log.debug("Getting creative by code: {} for tenant: {}", code, tenantId);

        AdCreativeResponseDto response = adsService.getCreativeByCode(code, tenantId);

        return ResponseEntity.ok(ApiResponseDto.success("Creative retrieved", response));
    }

    /**
     * Updates a creative with file upload (same pattern as NewsController).
     *
     * PUT /api/v1/admin/ads/creatives/{id}
     */
    @PutMapping(value = "/creatives/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<AdCreativeResponseDto>> updateCreative(
            @PathVariable UUID id,
            @Valid @ModelAttribute AdCreativeUploadRequestDto request) {

        log.info("Updating creative with upload: {}", id);

        AdCreativeResponseDto response = adsService.updateCreative(id, request);

        return ResponseEntity.ok(ApiResponseDto.success("Creative updated successfully", response));
    }

    /**
     * Deletes a creative (soft delete).
     *
     * DELETE /api/v1/admin/ads/creatives/{id}
     */
    @DeleteMapping("/creatives/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteCreative(@PathVariable UUID id) {
        log.info("Deleting creative: {}", id);

        adsService.deleteCreative(id);

        return ResponseEntity.ok(ApiResponseDto.success("Creative deleted successfully", null));
    }

    /**
     * Gets all creatives with pagination.
     *
     * GET /api/v1/admin/ads/creatives
     */
    @GetMapping("/creatives")
    public ResponseEntity<ApiResponseDto<Page<AdCreativeResponseDto>>> getAllCreatives(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            HttpServletRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Getting all creatives for tenant: {}", tenantId);

        Page<AdCreativeResponseDto> response = adsService.getAllCreatives(tenantId, pageable);

        return ResponseEntity.ok(ApiResponseDto.success("Creatives retrieved", response));
    }

    /**
     * Gets all active creatives.
     *
     * GET /api/v1/admin/ads/creatives/active
     */
    @GetMapping("/creatives/active")
    public ResponseEntity<ApiResponseDto<List<AdCreativeResponseDto>>> getActiveCreatives(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            HttpServletRequest request) {

        log.debug("Getting active creatives for tenant: {}", tenantId);

        List<AdCreativeResponseDto> response = adsService.getActiveCreatives(tenantId);

        return ResponseEntity.ok(ApiResponseDto.success("Active creatives retrieved", response));
    }

    /**
     * Gets all available creatives (active and approved).
     *
     * GET /api/v1/admin/ads/creatives/available
     */
    @GetMapping("/creatives/available")
    public ResponseEntity<ApiResponseDto<List<AdCreativeResponseDto>>> getAvailableCreatives(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            HttpServletRequest request) {

        log.debug("Getting available creatives for tenant: {}", tenantId);

        List<AdCreativeResponseDto> response = adsService.getAvailableCreatives(tenantId);

        return ResponseEntity.ok(ApiResponseDto.success("Available creatives retrieved", response));
    }

    /**
     * Approves a creative for serving.
     *
     * POST /api/v1/admin/ads/creatives/{id}/approve
     */
    @PostMapping("/creatives/{id}/approve")
    public ResponseEntity<ApiResponseDto<AdCreativeResponseDto>> approveCreative(@PathVariable UUID id) {
        log.info("Approving creative: {}", id);

        AdCreativeResponseDto response = adsService.approveCreative(id);

        return ResponseEntity.ok(ApiResponseDto.success("Creative approved successfully", response));
    }

    /**
     * Rejects a creative with reason.
     *
     * POST /api/v1/admin/ads/creatives/{id}/reject
     */
    @PostMapping("/creatives/{id}/reject")
    public ResponseEntity<ApiResponseDto<AdCreativeResponseDto>> rejectCreative(
            @PathVariable UUID id,
            @RequestParam("reason") String reason) {

        log.info("Rejecting creative: {} with reason: {}", id, reason);

        AdCreativeResponseDto response = adsService.rejectCreative(id, reason);

        return ResponseEntity.ok(ApiResponseDto.success("Creative rejected", response));
    }

    /**
     * Activates a creative for serving.
     *
     * POST /api/v1/admin/ads/creatives/{id}/activate
     */
    @PostMapping("/creatives/{id}/activate")
    public ResponseEntity<ApiResponseDto<AdCreativeResponseDto>> activateCreative(@PathVariable UUID id) {
        log.info("Activating creative: {}", id);

        AdCreativeResponseDto response = adsService.activateCreative(id);

        return ResponseEntity.ok(ApiResponseDto.success("Creative activated successfully", response));
    }

    /**
     * Deactivates a creative.
     *
     * POST /api/v1/admin/ads/creatives/{id}/deactivate
     */
    @PostMapping("/creatives/{id}/deactivate")
    public ResponseEntity<ApiResponseDto<AdCreativeResponseDto>> deactivateCreative(@PathVariable UUID id) {
        log.info("Deactivating creative: {}", id);

        AdCreativeResponseDto response = adsService.deactivateCreative(id);

        return ResponseEntity.ok(ApiResponseDto.success("Creative deactivated successfully", response));
    }

    // ========================================
    // Creative File Serving
    // ========================================

    /**
     * Serves a creative file by filename.
     *
     * GET /api/v1/admin/ads/creatives/files/{filename}
     */
    @GetMapping("/creatives/files/{filename}")
    public ResponseEntity<Resource> getCreativeFile(@PathVariable String filename) {
        log.debug("Serving creative file: {}", filename);

        Resource resource = adsService.getCreativeFile(filename);
        String contentType = determineContentType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    // ========================================
    // Private Helper Methods
    // ========================================
    // AdSense Integration Endpoints
    // ========================================

    /**
     * Initiate OAuth login with Google AdSense.
     * 
     * GET /api/v1/admin/ads/adsense/auth
     * Returns: Authorization URL for user to visit
     */
    @GetMapping("/adsense/auth")
    public ResponseEntity<ApiResponseDto<Map<String, String>>> initiateAdSenseAuth() {
        log.info("Initiating AdSense OAuth authentication");

        try {
            // TODO: Inject AdsenseIntegrationService
            String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=dummy-client-id&redirect_uri=http://localhost:8080/auth/google/callback&response_type=code&scope=https://www.googleapis.com/auth/adsense";

            Map<String, String> response = Map.of(
                    "authUrl", authUrl,
                    "message", "Visit the URL to authorize with Google AdSense");

            return ResponseEntity.ok(ApiResponseDto.success("AdSense authorization initiated", response));
        } catch (Exception e) {
            log.error("Failed to initiate AdSense auth", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to initiate AdSense authentication: " + e.getMessage()));
        }
    }

    /**
     * OAuth callback handler from Google AdSense.
     * 
     * GET /auth/google/callback?code=...&state=...
     */
    @GetMapping("/auth/google/callback")
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleAdSenseCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error) {

        log.info("Handling AdSense OAuth callback");

        if (error != null) {
            log.error("AdSense OAuth error: {}", error);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("AdSense authentication failed: " + error));
        }

        if (code == null || code.isEmpty()) {
            log.error("Missing authorization code");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("Missing authorization code"));
        }

        try {
            // TODO: Exchange code for credential using AdsenseIntegrationService
            Map<String, String> response = Map.of(
                    "status", "success",
                    "message", "AdSense authentication successful",
                    "code", code);

            return ResponseEntity.ok(ApiResponseDto.success("AdSense authenticated successfully", response));
        } catch (Exception e) {
            log.error("Failed to process AdSense callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to process authentication: " + e.getMessage()));
        }
    }

    /**
     * Fetch revenue metrics from Google AdSense.
     * 
     * GET /api/v1/admin/ads/adsense/metrics
     */
    @GetMapping("/adsense/metrics")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getAdSenseMetrics(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {

        log.info("Fetching AdSense metrics for tenant: {}", tenantId);

        try {
            // TODO: Inject AdsenseIntegrationService and fetch real metrics
            Map<String, Object> metrics = Map.of(
                    "impressions", 1000L,
                    "clicks", 50L,
                    "estimatedEarnings", 25.00,
                    "ctr", 5.0,
                    "cpm", 25.00,
                    "cpc", 0.50,
                    "syncedAt", Instant.now().toString());

            return ResponseEntity.ok(ApiResponseDto.success("AdSense metrics retrieved", metrics));
        } catch (Exception e) {
            log.error("Failed to fetch AdSense metrics", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponseDto.error("Failed to fetch AdSense metrics: " + e.getMessage()));
        }
    }

    /**
     * Manually trigger AdSense metrics sync.
     * 
     * POST /api/v1/admin/ads/adsense/sync
     */
    @PostMapping("/adsense/sync")
    public ResponseEntity<ApiResponseDto<Map<String, String>>> triggerAdSenseSync(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {

        log.info("Triggering manual AdSense sync for tenant: {}", tenantId);

        try {
            // TODO: Inject AdsenseMetricsSync and trigger sync
            Map<String, String> response = Map.of(
                    "status", "SYNCING",
                    "message", "AdSense metrics sync initiated",
                    "syncedAt", Instant.now().toString());

            return ResponseEntity.accepted()
                    .body(ApiResponseDto.success("AdSense sync initiated", response));
        } catch (Exception e) {
            log.error("Failed to trigger AdSense sync", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to trigger sync: " + e.getMessage()));
        }
    }

    /**
     * Get AdSense integration status.
     * 
     * GET /api/v1/admin/ads/adsense/status
     */
    @GetMapping("/adsense/status")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getAdSenseStatus() {

        log.info("Getting AdSense integration status");

        try {
            // TODO: Inject AdsenseIntegrationService and AdsenseMetricsSync
            Map<String, Object> status = Map.of(
                    "enabled", false,
                    "authenticated", false,
                    "lastSync", "Never",
                    "nextSync", "TBD",
                    "status", "DISABLED - Using dummy credentials",
                    "message", "Replace dummy credentials with real Google credentials to enable");

            return ResponseEntity.ok(ApiResponseDto.success("AdSense status retrieved", status));
        } catch (Exception e) {
            log.error("Failed to get AdSense status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to get status: " + e.getMessage()));
        }
    }

    // ========================================

    /**
     * Hashes IP address for privacy.
     */
    private String hashIp(String ip) {
        if (ip == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(ip.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            log.warn("Failed to hash IP: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Determines the content type based on file extension.
     */
    private String determineContentType(String filename) {
        if (filename == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String extension = getFileExtension(filename).toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "gif" -> MediaType.IMAGE_GIF_VALUE;
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "html" -> MediaType.TEXT_HTML_VALUE;
            case "htm" -> MediaType.TEXT_HTML_VALUE;
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }

    /**
     * Gets the file extension from a filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
