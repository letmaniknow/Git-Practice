package com.mmva.newsapp.controller.admin.campaigns;

import com.mmva.newsapp.infrastructure.monetization.campaign.dto.SponsorshipCampaignApprovalRequestDto;
import com.mmva.newsapp.infrastructure.monetization.campaign.dto.SponsorshipCampaignRequestDto;
import com.mmva.newsapp.infrastructure.monetization.campaign.dto.SponsorshipCampaignResponseDto;
import com.mmva.newsapp.infrastructure.monetization.campaign.service.SponsorshipCampaignService;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignStatus;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;

/**
 * Admin REST Controller for Sponsorship Campaign management.
 * 
 * <p>
 * Located in controller/admin/monetization/ per PROJECT_PRINCIPLES.md:
 * Admin APIs should be in controller/admin/
 * </p>
 * 
 * Base Path: /api/v1/admin/campaigns
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/admin/campaigns")
@RequiredArgsConstructor
@Slf4j
public class AdminSponsorshipCampaignController {

    private final SponsorshipCampaignService sponsorshipCampaignService;

    // Default tenant for single-tenant deployments
    private static final String DEFAULT_TENANT = "default";

    // ========================================
    // CRUD Endpoints
    // ========================================

    /**
     * Creates a new campaign.
     * 
     * POST /api/v1/admindashboard/campaigns
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<SponsorshipCampaignResponseDto>> createCampaign(
            @Valid @RequestBody SponsorshipCampaignRequestDto request,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.info("Creating campaign: {} for tenant: {}", request.getSponsorshipCampaignName(), tenantId);

        // DEBUG: Log request details
        log.info("DEBUG - Controller received request:");
        log.info("  Content-Type should be: application/json");
        log.info("  Name: '{}'", request.getSponsorshipCampaignName());
        log.info("  Email: '{}'", request.getSponsorshipCampaignAdvertiserEmail());

        SponsorshipCampaignResponseDto response = sponsorshipCampaignService.create(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success("Campaign created", response));
    }

    /**
     * Gets a campaign by ID.
     * 
     * GET /api/v1/admindashboard/campaigns/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<SponsorshipCampaignResponseDto>> getCampaign(
            @PathVariable("id") UUID sponsorshipCampaignId) {
        log.debug("Getting campaign: {}", sponsorshipCampaignId);
        SponsorshipCampaignResponseDto response = sponsorshipCampaignService.getById(sponsorshipCampaignId);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign retrieved", response));
    }

    /**
     * Gets a campaign by code.
     * 
     * GET /api/v1/admindashboard/campaigns/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponseDto<SponsorshipCampaignResponseDto>> getCampaignByCode(
            @PathVariable("code") String sponsorshipCampaignCode) {
        log.debug("Getting campaign by code: {}", sponsorshipCampaignCode);
        SponsorshipCampaignResponseDto response = sponsorshipCampaignService.getByCode(sponsorshipCampaignCode);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign retrieved", response));
    }

    /**
     * Updates a campaign.
     * 
     * PUT /api/v1/admindashboard/campaigns/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<SponsorshipCampaignResponseDto>> updateCampaign(
            @PathVariable("id") UUID sponsorshipCampaignId,
            @Valid @RequestBody SponsorshipCampaignRequestDto request) {
        log.info("Updating campaign: {}", sponsorshipCampaignId);
        SponsorshipCampaignResponseDto response = sponsorshipCampaignService.update(sponsorshipCampaignId, request);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign updated", response));
    }

    /**
     * Deletes a campaign (soft delete).
     * 
     * DELETE /api/v1/admindashboard/campaigns/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteCampaign(@PathVariable("id") UUID sponsorshipCampaignId) {
        log.info("Deleting campaign: {}", sponsorshipCampaignId);
        sponsorshipCampaignService.delete(sponsorshipCampaignId);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign deleted successfully", null));
    }

    // ========================================
    // Listing Endpoints
    // ========================================

    /**
     * Gets all campaigns with pagination.
     * 
     * GET /api/v1/admindashboard/campaigns
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<SponsorshipCampaignResponseDto>>> getAllCampaigns(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Getting all campaigns for tenant: {}", tenantId);
        Page<SponsorshipCampaignResponseDto> response = sponsorshipCampaignService.getAllCampaigns(tenantId, pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Campaigns retrieved", response));
    }

    /**
     * Gets campaigns by status.
     * 
     * GET /api/v1/admindashboard/campaigns/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponseDto<Page<SponsorshipCampaignResponseDto>>> getCampaignsByStatus(
            @PathVariable("status") SponsorshipCampaignStatus status,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Getting campaigns by status: {} for tenant: {}", status, tenantId);
        Page<SponsorshipCampaignResponseDto> response = sponsorshipCampaignService.getByStatus(status, tenantId,
                pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Campaigns retrieved", response));
    }

    /**
     * Gets all campaigns including soft-deleted ones (admin only).
     * 
     * GET /api/v1/admindashboard/campaigns/admin/all
     */
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponseDto<Page<SponsorshipCampaignResponseDto>>> getAllCampaignsIncludingDeleted(
            @RequestParam(value = "status", required = false) SponsorshipCampaignStatus status,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Getting all campaigns including deleted for tenant: {} with status filter: {}", tenantId, status);
        Page<SponsorshipCampaignResponseDto> response = sponsorshipCampaignService
                .getAllCampaignsIncludingDeleted(status, tenantId, pageable);
        return ResponseEntity.ok(ApiResponseDto.success("All campaigns retrieved (including deleted)", response));
    }

    /**
     * Gets campaigns by type.
     * 
     * GET /api/v1/admindashboard/campaigns/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponseDto<List<SponsorshipCampaignResponseDto>>> getCampaignsByType(
            @PathVariable("type") SponsorshipCampaignType type,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.debug("Getting campaigns by type: {} for tenant: {}", type, tenantId);
        List<SponsorshipCampaignResponseDto> response = sponsorshipCampaignService.getByType(type, tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Campaigns retrieved", response));
    }

    /**
     * Gets campaigns by advertiser.
     * 
     * GET /api/v1/admindashboard/campaigns/advertiser/{advertiserId}
     */
    @GetMapping("/advertiser/{advertiserId}")
    public ResponseEntity<ApiResponseDto<List<SponsorshipCampaignResponseDto>>> getCampaignsByAdvertiser(
            @PathVariable("advertiserId") UUID advertiserId) {
        log.debug("Getting campaigns by advertiser: {}", advertiserId);
        List<SponsorshipCampaignResponseDto> response = sponsorshipCampaignService.getByAdvertiser(advertiserId);
        return ResponseEntity.ok(ApiResponseDto.success("Campaigns retrieved", response));
    }

    /**
     * Gets campaigns pending approval.
     * 
     * GET /api/v1/admindashboard/campaigns/pending-approval
     */
    @GetMapping("/pending-approval")
    public ResponseEntity<ApiResponseDto<List<SponsorshipCampaignResponseDto>>> getPendingApproval(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.debug("Getting pending approval campaigns for tenant: {}", tenantId);
        List<SponsorshipCampaignResponseDto> response = sponsorshipCampaignService.getPendingApproval(tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Pending campaigns retrieved", response));
    }

    /**
     * Gets currently serving campaigns.
     * 
     * GET /api/v1/admindashboard/campaigns/serving
     */
    @GetMapping("/serving")
    public ResponseEntity<ApiResponseDto<List<SponsorshipCampaignResponseDto>>> getServingCampaigns(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.debug("Getting serving campaigns for tenant: {}", tenantId);
        List<SponsorshipCampaignResponseDto> response = sponsorshipCampaignService.getServingCampaigns(tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Serving campaigns retrieved", response));
    }

    // ========================================
    // Workflow Endpoints
    // ========================================

    /**
     * Submits a campaign for approval.
     * 
     * POST /api/v1/admindashboard/campaigns/{id}/submit
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponseDto<SponsorshipCampaignResponseDto>> submitForApproval(
            @PathVariable("id") UUID sponsorshipCampaignId) {
        log.info("Submitting campaign for approval: {}", sponsorshipCampaignId);
        SponsorshipCampaignResponseDto response = sponsorshipCampaignService.submitForApproval(sponsorshipCampaignId);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign submitted for approval", response));
    }

    /**
     * Processes campaign approval (approve or reject).
     * 
     * POST /api/v1/admindashboard/campaigns/approve
     */
    @PostMapping("/approve")
    public ResponseEntity<ApiResponseDto<SponsorshipCampaignResponseDto>> processApproval(
            @Valid @RequestBody SponsorshipCampaignApprovalRequestDto request,
            @RequestHeader(value = "X-User-Id") UUID approverUserId) {
        log.info("Processing approval for campaign: {}, approved: {}",
                request.getSponsorshipCampaignId(), request.getSponsorshipCampaignApproved());
        SponsorshipCampaignResponseDto response = sponsorshipCampaignService.processApproval(request, approverUserId);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign approval processed", response));
    }

    /**
     * Activates a campaign.
     * 
     * POST /api/v1/admindashboard/campaigns/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponseDto<SponsorshipCampaignResponseDto>> activateCampaign(
            @PathVariable("id") UUID sponsorshipCampaignId) {
        log.info("Activating campaign: {}", sponsorshipCampaignId);
        SponsorshipCampaignResponseDto response = sponsorshipCampaignService.activate(sponsorshipCampaignId);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign activated", response));
    }

    /**
     * Pauses a campaign.
     * 
     * POST /api/v1/admindashboard/campaigns/{id}/pause
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponseDto<SponsorshipCampaignResponseDto>> pauseCampaign(
            @PathVariable("id") UUID sponsorshipCampaignId) {
        log.info("Pausing campaign: {}", sponsorshipCampaignId);
        SponsorshipCampaignResponseDto response = sponsorshipCampaignService.pause(sponsorshipCampaignId);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign paused", response));
    }

    /**
     * Resumes a paused campaign.
     * 
     * POST /api/v1/admindashboard/campaigns/{id}/resume
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponseDto<SponsorshipCampaignResponseDto>> resumeCampaign(
            @PathVariable("id") UUID sponsorshipCampaignId) {
        log.info("Resuming campaign: {}", sponsorshipCampaignId);
        SponsorshipCampaignResponseDto response = sponsorshipCampaignService.resume(sponsorshipCampaignId);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign resumed", response));
    }

    /**
     * Cancels a campaign.
     * 
     * POST /api/v1/admindashboard/campaigns/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponseDto<SponsorshipCampaignResponseDto>> cancelCampaign(
            @PathVariable("id") UUID sponsorshipCampaignId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        log.info("Cancelling campaign: {} - Reason: {}", sponsorshipCampaignId, reason);
        SponsorshipCampaignResponseDto response = sponsorshipCampaignService.cancel(sponsorshipCampaignId, reason);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign cancelled", response));
    }

    /**
     * Completes a campaign.
     * 
     * POST /api/v1/admindashboard/campaigns/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponseDto<SponsorshipCampaignResponseDto>> completeCampaign(
            @PathVariable("id") UUID sponsorshipCampaignId) {
        log.info("Completing campaign: {}", sponsorshipCampaignId);
        SponsorshipCampaignResponseDto response = sponsorshipCampaignService.complete(sponsorshipCampaignId);
        return ResponseEntity.ok(ApiResponseDto.success("Campaign completed", response));
    }

    // ========================================
    // Scheduled Task Triggers (for testing)
    // ========================================

    /**
     * Triggers activation of ready campaigns.
     * 
     * POST /api/v1/admindashboard/campaigns/tasks/activate-ready
     */
    @PostMapping("/tasks/activate-ready")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> triggerActivateReady(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.info("Triggering activate-ready task for tenant: {}", tenantId);
        int count = sponsorshipCampaignService.activateReadyCampaigns(tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Activate-ready task completed", Map.of("activated", count)));
    }

    /**
     * Triggers completion of expired campaigns.
     * 
     * POST /api/v1/admindashboard/campaigns/tasks/complete-expired
     */
    @PostMapping("/tasks/complete-expired")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> triggerCompleteExpired(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.info("Triggering complete-expired task for tenant: {}", tenantId);
        int count = sponsorshipCampaignService.completeExpiredCampaigns(tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Complete-expired task completed", Map.of("completed", count)));
    }

    /**
     * Triggers pausing of budget-exhausted campaigns.
     * 
     * POST /api/v1/admindashboard/campaigns/tasks/pause-exhausted
     */
    @PostMapping("/tasks/pause-exhausted")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> triggerPauseExhausted(
            @RequestHeader(value = "X-Tenant-Id", defaultValue = DEFAULT_TENANT) String tenantId) {
        log.info("Triggering pause-exhausted task for tenant: {}", tenantId);
        int count = sponsorshipCampaignService.pauseBudgetExhaustedCampaigns(tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Pause-exhausted task completed", Map.of("paused", count)));
    }
}
