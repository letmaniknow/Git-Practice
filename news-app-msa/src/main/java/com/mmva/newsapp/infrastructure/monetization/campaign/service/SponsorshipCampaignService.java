package com.mmva.newsapp.infrastructure.monetization.campaign.service;

import com.mmva.newsapp.infrastructure.monetization.campaign.dto.SponsorshipCampaignApprovalRequestDto;
import com.mmva.newsapp.infrastructure.monetization.campaign.dto.SponsorshipCampaignRequestDto;
import com.mmva.newsapp.infrastructure.monetization.campaign.dto.SponsorshipCampaignResponseDto;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignStatus;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Sponsorship Campaign management.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface SponsorshipCampaignService {

    // ========================================
    // CRUD Operations
    // ========================================

    /**
     * Creates a new campaign in DRAFT status.
     */
    SponsorshipCampaignResponseDto create(SponsorshipCampaignRequestDto request, String tenantId);

    /**
     * Gets campaign by ID.
     */
    SponsorshipCampaignResponseDto getById(UUID sponsorshipCampaignId);

    /**
     * Gets campaign by code.
     */
    SponsorshipCampaignResponseDto getByCode(String sponsorshipCampaignCode);

    /**
     * Updates an existing campaign.
     * Only allowed for campaigns in DRAFT or REJECTED status.
     */
    SponsorshipCampaignResponseDto update(UUID sponsorshipCampaignId, SponsorshipCampaignRequestDto request);

    /**
     * Deletes a campaign (soft delete).
     * Not allowed for ACTIVE campaigns.
     */
    void delete(UUID sponsorshipCampaignId);

    // ========================================
    // Listing & Search
    // ========================================

    /**
     * Gets all campaigns for a tenant with pagination.
     */
    Page<SponsorshipCampaignResponseDto> getAllCampaigns(String tenantId, Pageable pageable);

    /**
     * Gets campaigns by status (including soft-deleted for admin).
     */
    Page<SponsorshipCampaignResponseDto> getAllCampaignsIncludingDeleted(SponsorshipCampaignStatus status,
            String tenantId,
            Pageable pageable);

    /**
     * Gets campaigns by status (active campaigns only).
     */
    Page<SponsorshipCampaignResponseDto> getByStatus(SponsorshipCampaignStatus status, String tenantId,
            Pageable pageable);

    /**
     * Gets campaigns by type.
     */
    List<SponsorshipCampaignResponseDto> getByType(SponsorshipCampaignType type, String tenantId);

    /**
     * Gets campaigns by advertiser.
     */
    List<SponsorshipCampaignResponseDto> getByAdvertiser(UUID advertiserId);

    /**
     * Gets campaigns pending approval.
     */
    List<SponsorshipCampaignResponseDto> getPendingApproval(String tenantId);

    /**
     * Gets currently serving (active and within schedule) campaigns.
     */
    List<SponsorshipCampaignResponseDto> getServingCampaigns(String tenantId);

    // ========================================
    // Status Workflow
    // ========================================

    /**
     * Submits a DRAFT campaign for approval.
     */
    SponsorshipCampaignResponseDto submitForApproval(UUID sponsorshipCampaignId);

    /**
     * Approves or rejects a campaign.
     */
    SponsorshipCampaignResponseDto processApproval(SponsorshipCampaignApprovalRequestDto request, UUID approverUserId);

    /**
     * Activates an APPROVED campaign manually.
     */
    SponsorshipCampaignResponseDto activate(UUID sponsorshipCampaignId);

    /**
     * Pauses an ACTIVE campaign.
     */
    SponsorshipCampaignResponseDto pause(UUID sponsorshipCampaignId);

    /**
     * Resumes a PAUSED campaign.
     */
    SponsorshipCampaignResponseDto resume(UUID sponsorshipCampaignId);

    /**
     * Cancels a campaign.
     */
    SponsorshipCampaignResponseDto cancel(UUID sponsorshipCampaignId, String reason);

    /**
     * Completes a campaign.
     */
    SponsorshipCampaignResponseDto complete(UUID sponsorshipCampaignId);

    // ========================================
    // Metrics & Analytics
    // ========================================

    /**
     * Records an impression for a campaign.
     */
    void recordImpression(UUID sponsorshipCampaignId);

    /**
     * Records a click for a campaign.
     */
    void recordClick(UUID sponsorshipCampaignId);

    /**
     * Records a conversion for a campaign.
     */
    void recordConversion(UUID sponsorshipCampaignId);

    // ========================================
    // Scheduled Tasks
    // ========================================

    /**
     * Activates approved campaigns whose start date has arrived.
     * Called by scheduler.
     */
    int activateReadyCampaigns(String tenantId);

    /**
     * Completes campaigns whose end date has passed.
     * Called by scheduler.
     */
    int completeExpiredCampaigns(String tenantId);

    /**
     * Pauses campaigns that have exhausted their budget.
     * Called by scheduler.
     */
    int pauseBudgetExhaustedCampaigns(String tenantId);
}
