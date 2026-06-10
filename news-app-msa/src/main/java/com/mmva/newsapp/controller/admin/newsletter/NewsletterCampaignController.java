package com.mmva.newsapp.controller.admin.newsletter;

import com.mmva.newsapp.domain.newsletter.dto.core.NewsletterCampaignRequestDto;
import com.mmva.newsapp.domain.newsletter.dto.core.NewsletterCampaignResponseDto;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterCampaignStatus;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterCampaignType;
import com.mmva.newsapp.domain.newsletter.service.core.NewsletterCampaignService;
import static com.mmva.newsapp.domain.newsletter.service.core.NewsletterCampaignService.CampaignAnalytics;
import static com.mmva.newsapp.domain.newsletter.service.core.NewsletterCampaignService.CampaignPerformanceData;
import static com.mmva.newsapp.domain.newsletter.service.core.NewsletterCampaignService.OverallCampaignStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for newsletter campaign operations.
 *
 * <p>
 * Provides endpoints for campaign management including
 * creation, scheduling, execution, and analytics.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/newsletter/campaigns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Newsletter Campaigns API", description = "Newsletter campaign management operations")
public class NewsletterCampaignController {

    private final NewsletterCampaignService campaignService;

    // =========================
    // Campaign Management
    // =========================

    /**
     * Creates a new newsletter campaign.
     *
     * @param request the campaign creation request
     * @return ResponseEntity containing the created campaign
     */
    @PostMapping
    @Operation(summary = "Create campaign", description = "Creates a new newsletter campaign")
    public ResponseEntity<NewsletterCampaignResponseDto> createCampaign(
            @Parameter(description = "Campaign creation request") @RequestBody NewsletterCampaignRequestDto request) {

        log.info("Newsletter campaign creation request: {}", request.getNewsletterCampaignName());

        NewsletterCampaignResponseDto response = campaignService.createCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing campaign.
     *
     * @param campaignId the campaign ID
     * @param request    the campaign update request
     * @return ResponseEntity containing the updated campaign
     */
    @PutMapping("/{campaignId}")
    @Operation(summary = "Update campaign", description = "Updates an existing newsletter campaign")
    public ResponseEntity<NewsletterCampaignResponseDto> updateCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Campaign update request") @RequestBody NewsletterCampaignRequestDto request) {

        log.info("Newsletter campaign update request for ID: {}", campaignId);

        NewsletterCampaignResponseDto response = campaignService.updateCampaign(campaignId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Schedules a campaign for sending.
     *
     * @param campaignId    the campaign ID
     * @param scheduledTime the scheduled send time
     * @return ResponseEntity containing the updated campaign
     */
    @PostMapping("/{campaignId}/schedule")
    @Operation(summary = "Schedule campaign", description = "Schedules a campaign for sending at a specific time")
    public ResponseEntity<NewsletterCampaignResponseDto> scheduleCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Scheduled send time (ISO-8601)", example = "2024-01-01T10:00:00Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant scheduledTime) {

        log.info("Newsletter campaign schedule request for ID: {} at {}", campaignId, scheduledTime);

        NewsletterCampaignResponseDto response = campaignService.scheduleCampaign(campaignId, scheduledTime);
        return ResponseEntity.ok(response);
    }

    /**
     * Sends a campaign immediately.
     *
     * @param campaignId the campaign ID
     * @return ResponseEntity containing the updated campaign
     */
    @PostMapping("/{campaignId}/send")
    @Operation(summary = "Send campaign", description = "Sends a campaign immediately")
    public ResponseEntity<NewsletterCampaignResponseDto> sendCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.info("Newsletter campaign send request for ID: {}", campaignId);

        NewsletterCampaignResponseDto response = campaignService.sendCampaign(campaignId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels a scheduled campaign.
     *
     * @param campaignId the campaign ID
     * @return ResponseEntity containing the updated campaign
     */
    @PostMapping("/{campaignId}/cancel")
    @Operation(summary = "Cancel campaign", description = "Cancels a scheduled or active campaign")
    public ResponseEntity<NewsletterCampaignResponseDto> cancelCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.info("Newsletter campaign cancel request for ID: {}", campaignId);

        NewsletterCampaignResponseDto response = campaignService.cancelCampaign(campaignId);
        return ResponseEntity.ok(response);
    }

    /**
     * Pauses an active campaign.
     *
     * @param campaignId the campaign ID
     * @return ResponseEntity containing the updated campaign
     */
    @PostMapping("/{campaignId}/pause")
    @Operation(summary = "Pause campaign", description = "Pauses an actively sending campaign")
    public ResponseEntity<NewsletterCampaignResponseDto> pauseCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.info("Newsletter campaign pause request for ID: {}", campaignId);

        NewsletterCampaignResponseDto response = campaignService.pauseCampaign(campaignId);
        return ResponseEntity.ok(response);
    }

    /**
     * Resumes a paused campaign.
     *
     * @param campaignId the campaign ID
     * @return ResponseEntity containing the updated campaign
     */
    @PostMapping("/{campaignId}/resume")
    @Operation(summary = "Resume campaign", description = "Resumes a paused campaign")
    public ResponseEntity<NewsletterCampaignResponseDto> resumeCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.info("Newsletter campaign resume request for ID: {}", campaignId);

        NewsletterCampaignResponseDto response = campaignService.resumeCampaign(campaignId);
        return ResponseEntity.ok(response);
    }

    // =========================
    // Query Operations
    // =========================

    /**
     * Gets a campaign by ID.
     *
     * @param campaignId the campaign ID
     * @return ResponseEntity containing the campaign
     */
    @GetMapping("/{campaignId}")
    @Operation(summary = "Get campaign by ID", description = "Retrieves a campaign by its ID")
    public ResponseEntity<NewsletterCampaignResponseDto> getCampaignById(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.debug("Get campaign by ID request: {}", campaignId);

        NewsletterCampaignResponseDto response = campaignService.getCampaignById(campaignId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets campaigns by status with pagination.
     *
     * @param status the campaign status
     * @param page   page number (0-based)
     * @param size   page size
     * @return ResponseEntity containing paginated campaigns
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get campaigns by status", description = "Retrieves campaigns filtered by status")
    public ResponseEntity<Page<NewsletterCampaignResponseDto>> getCampaignsByStatus(
            @Parameter(description = "Campaign status", example = "DRAFT") @PathVariable NewsletterCampaignStatus status,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get campaigns by status request - status: {}, page: {}, size: {}", status, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterCampaignResponseDto> response = campaignService.getCampaignsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets campaigns by type with pagination.
     *
     * @param type the campaign type
     * @param page page number (0-based)
     * @param size page size
     * @return ResponseEntity containing paginated campaigns
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Get campaigns by type", description = "Retrieves campaigns filtered by type")
    public ResponseEntity<Page<NewsletterCampaignResponseDto>> getCampaignsByType(
            @Parameter(description = "Campaign type", example = "REGULAR") @PathVariable NewsletterCampaignType type,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get campaigns by type request - type: {}, page: {}, size: {}", type, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterCampaignResponseDto> response = campaignService.getCampaignsByType(type, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all campaigns with pagination.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return ResponseEntity containing paginated campaigns
     */
    @GetMapping
    @Operation(summary = "Get all campaigns", description = "Retrieves all campaigns with pagination")
    public ResponseEntity<Page<NewsletterCampaignResponseDto>> getAllCampaigns(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get all campaigns request - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterCampaignResponseDto> response = campaignService.getAllCampaigns(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets scheduled campaigns ready for sending.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return ResponseEntity containing paginated scheduled campaigns
     */
    @GetMapping("/scheduled")
    @Operation(summary = "Get scheduled campaigns", description = "Retrieves campaigns that are scheduled and ready for sending")
    public ResponseEntity<Page<NewsletterCampaignResponseDto>> getScheduledCampaignsReadyForSending(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get scheduled campaigns ready for sending - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterCampaignResponseDto> response = campaignService.getScheduledCampaignsReadyForSending(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets campaigns within a date range.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @param page      page number (0-based)
     * @param size      page size
     * @return ResponseEntity containing paginated campaigns
     */
    @GetMapping("/daterange")
    @Operation(summary = "Get campaigns by date range", description = "Retrieves campaigns sent within a specific date range")
    public ResponseEntity<Page<NewsletterCampaignResponseDto>> getCampaignsByDateRange(
            @Parameter(description = "Start date (ISO-8601)", example = "2024-01-01T00:00:00Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @Parameter(description = "End date (ISO-8601)", example = "2024-01-31T23:59:59Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get campaigns by date range request - start: {}, end: {}, page: {}, size: {}", startDate, endDate,
                page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterCampaignResponseDto> response = campaignService.getCampaignsByDateRange(startDate, endDate,
                pageable);
        return ResponseEntity.ok(response);
    }

    // =========================
    // Analytics & Statistics
    // =========================

    /**
     * Gets campaign analytics.
     *
     * @param campaignId the campaign ID
     * @return ResponseEntity containing campaign analytics
     */
    @GetMapping("/{campaignId}/analytics")
    @Operation(summary = "Get campaign analytics", description = "Retrieves detailed analytics for a specific campaign")
    public ResponseEntity<CampaignAnalytics> getCampaignAnalytics(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.debug("Get campaign analytics request for ID: {}", campaignId);

        CampaignAnalytics response = campaignService.getCampaignAnalytics(campaignId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets overall campaign statistics.
     *
     * @return ResponseEntity containing overall statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get overall campaign statistics", description = "Retrieves overall statistics across all campaigns")
    public ResponseEntity<OverallCampaignStatistics> getOverallCampaignStatistics() {

        log.debug("Get overall campaign statistics request");

        OverallCampaignStatistics response = campaignService.getOverallCampaignStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Gets average open rate.
     *
     * @return ResponseEntity containing average open rate
     */
    @GetMapping("/metrics/open-rate")
    @Operation(summary = "Get average open rate", description = "Calculates the average open rate across all campaigns")
    public ResponseEntity<Double> getAverageOpenRate() {

        log.debug("Get average open rate request");

        double response = campaignService.getAverageOpenRate();
        return ResponseEntity.ok(response);
    }

    /**
     * Gets average click rate.
     *
     * @return ResponseEntity containing average click rate
     */
    @GetMapping("/metrics/click-rate")
    @Operation(summary = "Get average click rate", description = "Calculates the average click rate across all campaigns")
    public ResponseEntity<Double> getAverageClickRate() {

        log.debug("Get average click rate request");

        double response = campaignService.getAverageClickRate();
        return ResponseEntity.ok(response);
    }

    /**
     * Gets campaign performance trends.
     *
     * @param days number of days to look back
     * @return ResponseEntity containing performance trends
     */
    @GetMapping("/trends")
    @Operation(summary = "Get campaign performance trends", description = "Retrieves campaign performance trends over time")
    public ResponseEntity<List<CampaignPerformanceData>> getCampaignPerformanceTrends(
            @Parameter(description = "Number of days to look back", example = "30") @RequestParam(defaultValue = "30") int days) {

        log.debug("Get campaign performance trends request - days: {}", days);

        List<CampaignPerformanceData> response = campaignService.getCampaignPerformanceTrends(days);
        return ResponseEntity.ok(response);
    }

    // =========================
    // Content Management
    // =========================

    /**
     * Adds content to a campaign.
     *
     * @param campaignId the campaign ID
     * @param contentId  the content ID
     * @return ResponseEntity containing the updated campaign
     */
    @PostMapping("/{campaignId}/content/{contentId}")
    @Operation(summary = "Add content to campaign", description = "Adds existing content to a newsletter campaign")
    public ResponseEntity<NewsletterCampaignResponseDto> addContentToCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Content ID", example = "1") @PathVariable Long contentId) {

        log.debug("Add content to campaign request - campaignId: {}, contentId: {}", campaignId, contentId);

        NewsletterCampaignResponseDto response = campaignService.addContentToCampaign(campaignId, contentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Removes content from a campaign.
     *
     * @param campaignId the campaign ID
     * @param contentId  the content ID
     * @return ResponseEntity containing the updated campaign
     */
    @DeleteMapping("/{campaignId}/content/{contentId}")
    @Operation(summary = "Remove content from campaign", description = "Removes content from a newsletter campaign")
    public ResponseEntity<NewsletterCampaignResponseDto> removeContentFromCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Content ID", example = "1") @PathVariable Long contentId) {

        log.debug("Remove content from campaign request - campaignId: {}, contentId: {}", campaignId, contentId);

        NewsletterCampaignResponseDto response = campaignService.removeContentFromCampaign(campaignId, contentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets campaigns with multiple languages.
     *
     * @param pageable pagination information
     * @return ResponseEntity containing page of campaigns
     */
    @GetMapping("/multilingual")
    @Operation(summary = "Get multilingual campaigns", description = "Retrieves campaigns that support multiple languages")
    public ResponseEntity<Page<NewsletterCampaignResponseDto>> getCampaignsWithMultipleLanguages(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get multilingual campaigns request - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterCampaignResponseDto> response = campaignService.getCampaignsWithMultipleLanguages(pageable);
        return ResponseEntity.ok(response);
    }

    // =========================
    // Administrative Operations
    // =========================

    /**
     * Deletes a campaign.
     *
     * @param campaignId the campaign ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{campaignId}")
    @Operation(summary = "Delete campaign", description = "Deletes a newsletter campaign permanently")
    public ResponseEntity<Void> deleteCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.debug("Delete campaign request - campaignId: {}", campaignId);

        campaignService.deleteCampaign(campaignId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Duplicates a campaign.
     *
     * @param campaignId the campaign ID to duplicate
     * @param newName    the name for the duplicated campaign
     * @return ResponseEntity containing the duplicated campaign
     */
    @PostMapping("/{campaignId}/duplicate")
    @Operation(summary = "Duplicate campaign", description = "Creates a copy of an existing newsletter campaign")
    public ResponseEntity<NewsletterCampaignResponseDto> duplicateCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "New campaign name", example = "Weekly Newsletter Copy") @RequestParam String newName) {

        log.debug("Duplicate campaign request - campaignId: {}, newName: {}", campaignId, newName);

        NewsletterCampaignResponseDto response = campaignService.duplicateCampaign(campaignId, newName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}