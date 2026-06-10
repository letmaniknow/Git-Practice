package com.mmva.newsapp.infrastructure.monetization.campaign.service;

import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.infrastructure.monetization.campaign.dto.SponsorshipCampaignApprovalRequestDto;
import com.mmva.newsapp.infrastructure.monetization.campaign.dto.SponsorshipCampaignRequestDto;
import com.mmva.newsapp.infrastructure.monetization.campaign.dto.SponsorshipCampaignResponseDto;
import com.mmva.newsapp.infrastructure.monetization.campaign.mapper.SponsorshipCampaignMapper;
import com.mmva.newsapp.infrastructure.monetization.campaign.model.SponsorshipCampaign;
import com.mmva.newsapp.infrastructure.monetization.campaign.repository.SponsorshipCampaignRepository;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignStatus;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link SponsorshipCampaignService}.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SponsorshipCampaignServiceImpl implements SponsorshipCampaignService {

    private final SponsorshipCampaignRepository sponsorshipCampaignRepository;
    private final SponsorshipCampaignMapper sponsorshipCampaignMapper;

    // ========================================
    // CRUD Operations
    // ========================================

    @Override
    @Transactional
    public SponsorshipCampaignResponseDto create(SponsorshipCampaignRequestDto request, String tenantId) {
        log.info("Creating new campaign: {} for tenant: {}",
                request.getSponsorshipCampaignName(), tenantId);

        // DEBUG: Log the raw request data
        log.info("DEBUG - Request DTO values:");
        log.info("  Name: '{}'", request.getSponsorshipCampaignName());
        log.info("  Email: '{}'", request.getSponsorshipCampaignAdvertiserEmail());
        log.info("  Brand URL: '{}'", request.getSponsorshipCampaignBrandLogoUrl());
        log.info("  Target Categories: '{}'", request.getSponsorshipCampaignTargetCategoriesJson());

        // Validate date range
        validateDateRange(request.getSponsorshipCampaignStartDate(),
                request.getSponsorshipCampaignEndDate());

        // Generate campaign code if not provided
        String campaignCode = request.getSponsorshipCampaignCode();
        if (campaignCode == null || campaignCode.isBlank()) {
            campaignCode = generateCampaignCode(request.getSponsorshipCampaignName());
        }

        // Check for duplicate code
        if (sponsorshipCampaignRepository.existsBySponsorshipCampaignCode(campaignCode)) {
            throw new InvalidRequestException("Campaign code already exists: " + campaignCode);
        }

        // Map and save
        SponsorshipCampaign campaign = sponsorshipCampaignMapper.toEntity(request);

        // DEBUG: Log the entity values after mapping
        log.info("DEBUG - Entity values after mapping:");
        log.info("  Name: '{}'", campaign.getSponsorshipCampaignName());
        log.info("  Email: '{}'", campaign.getSponsorshipCampaignAdvertiserEmail());
        log.info("  Brand URL: '{}'", campaign.getSponsorshipCampaignBrandLogoUrl());

        campaign.setSponsorshipCampaignCode(campaignCode);
        campaign.setSponsorshipCampaignTenantId(tenantId);
        campaign.setSponsorshipCampaignStatus(SponsorshipCampaignStatus.DRAFT);

        SponsorshipCampaign saved = sponsorshipCampaignRepository.save(campaign);

        // DEBUG: Log the saved entity values
        log.info("DEBUG - Saved entity values:");
        log.info("  Name: '{}'", saved.getSponsorshipCampaignName());
        log.info("  Email: '{}'", saved.getSponsorshipCampaignAdvertiserEmail());
        log.info("  Brand URL: '{}'", saved.getSponsorshipCampaignBrandLogoUrl());

        log.info("Created campaign with ID: {} and code: {}",
                saved.getSponsorshipCampaignId(),
                saved.getSponsorshipCampaignCode());

        return sponsorshipCampaignMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SponsorshipCampaignResponseDto getById(UUID sponsorshipCampaignId) {
        SponsorshipCampaign campaign = findCampaignOrThrow(sponsorshipCampaignId);
        return sponsorshipCampaignMapper.toResponseDto(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public SponsorshipCampaignResponseDto getByCode(String sponsorshipCampaignCode) {
        SponsorshipCampaign campaign = sponsorshipCampaignRepository
                .findBySponsorshipCampaignCode(sponsorshipCampaignCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Campaign not found with code: " + sponsorshipCampaignCode));
        return sponsorshipCampaignMapper.toResponseDto(campaign);
    }

    @Override
    @Transactional
    public SponsorshipCampaignResponseDto update(UUID sponsorshipCampaignId, SponsorshipCampaignRequestDto request) {
        log.info("Updating campaign: {}", sponsorshipCampaignId);

        SponsorshipCampaign campaign = findCampaignOrThrow(sponsorshipCampaignId);

        // Only allow updates for DRAFT or REJECTED campaigns
        if (campaign.getSponsorshipCampaignStatus() != SponsorshipCampaignStatus.DRAFT &&
                campaign.getSponsorshipCampaignStatus() != SponsorshipCampaignStatus.REJECTED) {
            throw new InvalidRequestException(
                    "Cannot update campaign in status: " + campaign.getSponsorshipCampaignStatus() +
                            ". Only DRAFT or REJECTED campaigns can be updated.");
        }

        // Validate date range
        validateDateRange(request.getSponsorshipCampaignStartDate(),
                request.getSponsorshipCampaignEndDate());

        // Check code uniqueness if changing
        if (request.getSponsorshipCampaignCode() != null &&
                !request.getSponsorshipCampaignCode().equals(campaign.getSponsorshipCampaignCode()) &&
                sponsorshipCampaignRepository.existsBySponsorshipCampaignCode(request.getSponsorshipCampaignCode())) {
            throw new InvalidRequestException(
                    "Campaign code already exists: " + request.getSponsorshipCampaignCode());
        }

        sponsorshipCampaignMapper.updateEntityFromDto(request, campaign);

        // If rejected, reset to draft for re-submission
        if (campaign.getSponsorshipCampaignStatus() == SponsorshipCampaignStatus.REJECTED) {
            campaign.setSponsorshipCampaignStatus(SponsorshipCampaignStatus.DRAFT);
            campaign.setSponsorshipCampaignRejectionReason(null);
        }

        SponsorshipCampaign saved = sponsorshipCampaignRepository.save(campaign);
        log.info("Updated campaign: {}", sponsorshipCampaignId);

        return sponsorshipCampaignMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void delete(UUID sponsorshipCampaignId) {
        log.info("Deleting campaign: {}", sponsorshipCampaignId);

        SponsorshipCampaign campaign = findCampaignOrThrow(sponsorshipCampaignId);

        // Don't allow deleting active campaigns
        if (campaign.getSponsorshipCampaignStatus() == SponsorshipCampaignStatus.ACTIVE) {
            throw new InvalidRequestException(
                    "Cannot delete an active campaign. Pause or complete it first.");
        }

        campaign.setDeletedAt(Instant.now());
        sponsorshipCampaignRepository.save(campaign);
        log.info("Soft deleted campaign: {}", sponsorshipCampaignId);
    }

    // ========================================
    // Listing & Search
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Page<SponsorshipCampaignResponseDto> getAllCampaigns(String tenantId, Pageable pageable) {
        return sponsorshipCampaignRepository.findByStatusAndTenantId(null, tenantId, pageable)
                .map(sponsorshipCampaignMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SponsorshipCampaignResponseDto> getByStatus(SponsorshipCampaignStatus status, String tenantId,
            Pageable pageable) {
        return sponsorshipCampaignRepository.findByStatusAndTenantId(status, tenantId, pageable)
                .map(sponsorshipCampaignMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SponsorshipCampaignResponseDto> getAllCampaignsIncludingDeleted(SponsorshipCampaignStatus status,
            String tenantId,
            Pageable pageable) {
        return sponsorshipCampaignRepository.findAllByStatusAndTenantIdIncludingDeleted(status, tenantId, pageable)
                .map(sponsorshipCampaignMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SponsorshipCampaignResponseDto> getByType(SponsorshipCampaignType type, String tenantId) {
        return sponsorshipCampaignMapper.toResponseDtoList(
                sponsorshipCampaignRepository.findServingByType(type, Instant.now(), tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SponsorshipCampaignResponseDto> getByAdvertiser(UUID advertiserId) {
        return sponsorshipCampaignMapper.toResponseDtoList(
                sponsorshipCampaignRepository.findByAdvertiserId(advertiserId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SponsorshipCampaignResponseDto> getPendingApproval(String tenantId) {
        return sponsorshipCampaignMapper.toResponseDtoList(
                sponsorshipCampaignRepository.findPendingApproval(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SponsorshipCampaignResponseDto> getServingCampaigns(String tenantId) {
        return sponsorshipCampaignMapper.toResponseDtoList(
                sponsorshipCampaignRepository.findServingCampaigns(Instant.now(), tenantId));
    }

    // ========================================
    // Status Workflow
    // ========================================

    @Override
    @Transactional
    public SponsorshipCampaignResponseDto submitForApproval(UUID sponsorshipCampaignId) {
        log.info("Submitting campaign for approval: {}", sponsorshipCampaignId);

        SponsorshipCampaign campaign = findCampaignOrThrow(sponsorshipCampaignId);

        if (campaign.getSponsorshipCampaignStatus() != SponsorshipCampaignStatus.DRAFT) {
            throw new InvalidRequestException("Only DRAFT campaigns can be submitted for approval");
        }

        // Validate required fields for submission
        validateForSubmission(campaign);

        campaign.setSponsorshipCampaignStatus(SponsorshipCampaignStatus.PENDING_APPROVAL);
        SponsorshipCampaign saved = sponsorshipCampaignRepository.save(campaign);
        log.info("Campaign submitted for approval: {}", sponsorshipCampaignId);

        return sponsorshipCampaignMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public SponsorshipCampaignResponseDto processApproval(SponsorshipCampaignApprovalRequestDto request,
            UUID approverUserId) {
        log.info("Processing approval for campaign: {}, approved: {}",
                request.getSponsorshipCampaignId(), request.getSponsorshipCampaignApproved());

        SponsorshipCampaign campaign = findCampaignOrThrow(request.getSponsorshipCampaignId());

        if (campaign.getSponsorshipCampaignStatus() != SponsorshipCampaignStatus.PENDING_APPROVAL) {
            throw new InvalidRequestException("Only campaigns pending approval can be processed");
        }

        if (Boolean.TRUE.equals(request.getSponsorshipCampaignApproved())) {
            campaign.setSponsorshipCampaignStatus(SponsorshipCampaignStatus.APPROVED);
            campaign.setSponsorshipCampaignApprovedBy(approverUserId);
            campaign.setSponsorshipCampaignApprovedAt(Instant.now());
            campaign.setSponsorshipCampaignRejectionReason(null);
            log.info("Campaign approved: {}", request.getSponsorshipCampaignId());
        } else {
            if (request.getSponsorshipCampaignRejectionReason() == null ||
                    request.getSponsorshipCampaignRejectionReason().isBlank()) {
                throw new InvalidRequestException(
                        "Rejection reason is required when rejecting a campaign");
            }
            campaign.setSponsorshipCampaignStatus(SponsorshipCampaignStatus.REJECTED);
            campaign.setSponsorshipCampaignRejectionReason(
                    request.getSponsorshipCampaignRejectionReason());
            log.info("Campaign rejected: {} - Reason: {}",
                    request.getSponsorshipCampaignId(),
                    request.getSponsorshipCampaignRejectionReason());
        }

        SponsorshipCampaign saved = sponsorshipCampaignRepository.save(campaign);
        return sponsorshipCampaignMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public SponsorshipCampaignResponseDto activate(UUID sponsorshipCampaignId) {
        log.info("Activating campaign: {}", sponsorshipCampaignId);

        SponsorshipCampaign campaign = findCampaignOrThrow(sponsorshipCampaignId);

        if (campaign.getSponsorshipCampaignStatus() != SponsorshipCampaignStatus.APPROVED) {
            throw new InvalidRequestException("Only APPROVED campaigns can be activated");
        }

        // Check if start date hasn't passed too far
        if (campaign.getSponsorshipCampaignEndDate().isBefore(Instant.now())) {
            throw new InvalidRequestException("Cannot activate campaign - end date has passed");
        }

        campaign.setSponsorshipCampaignStatus(SponsorshipCampaignStatus.ACTIVE);
        campaign.setSponsorshipCampaignActivatedAt(Instant.now());
        SponsorshipCampaign saved = sponsorshipCampaignRepository.save(campaign);
        log.info("Campaign activated: {}", sponsorshipCampaignId);

        return sponsorshipCampaignMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public SponsorshipCampaignResponseDto pause(UUID sponsorshipCampaignId) {
        log.info("Pausing campaign: {}", sponsorshipCampaignId);

        SponsorshipCampaign campaign = findCampaignOrThrow(sponsorshipCampaignId);

        if (campaign.getSponsorshipCampaignStatus() != SponsorshipCampaignStatus.ACTIVE) {
            throw new InvalidRequestException("Only ACTIVE campaigns can be paused");
        }

        campaign.setSponsorshipCampaignStatus(SponsorshipCampaignStatus.PAUSED);
        SponsorshipCampaign saved = sponsorshipCampaignRepository.save(campaign);
        log.info("Campaign paused: {}", sponsorshipCampaignId);

        return sponsorshipCampaignMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public SponsorshipCampaignResponseDto resume(UUID sponsorshipCampaignId) {
        log.info("Resuming campaign: {}", sponsorshipCampaignId);

        SponsorshipCampaign campaign = findCampaignOrThrow(sponsorshipCampaignId);

        if (campaign.getSponsorshipCampaignStatus() != SponsorshipCampaignStatus.PAUSED) {
            throw new InvalidRequestException("Only PAUSED campaigns can be resumed");
        }

        // Check if campaign is still within schedule and has budget
        if (campaign.getSponsorshipCampaignEndDate().isBefore(Instant.now())) {
            throw new InvalidRequestException("Cannot resume campaign - end date has passed");
        }

        if (!campaign.hasRemainingBudget()) {
            throw new InvalidRequestException("Cannot resume campaign - budget exhausted");
        }

        campaign.setSponsorshipCampaignStatus(SponsorshipCampaignStatus.ACTIVE);
        SponsorshipCampaign saved = sponsorshipCampaignRepository.save(campaign);
        log.info("Campaign resumed: {}", sponsorshipCampaignId);

        return sponsorshipCampaignMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public SponsorshipCampaignResponseDto cancel(UUID sponsorshipCampaignId, String reason) {
        log.info("Cancelling campaign: {} - Reason: {}", sponsorshipCampaignId, reason);

        SponsorshipCampaign campaign = findCampaignOrThrow(sponsorshipCampaignId);

        // Can cancel from most statuses except COMPLETED
        if (campaign.getSponsorshipCampaignStatus() == SponsorshipCampaignStatus.COMPLETED ||
                campaign.getSponsorshipCampaignStatus() == SponsorshipCampaignStatus.CANCELLED) {
            throw new InvalidRequestException(
                    "Campaign is already " + campaign.getSponsorshipCampaignStatus());
        }

        campaign.setSponsorshipCampaignStatus(SponsorshipCampaignStatus.CANCELLED);
        campaign.setSponsorshipCampaignCompletedAt(Instant.now());
        SponsorshipCampaign saved = sponsorshipCampaignRepository.save(campaign);
        log.info("Campaign cancelled: {}", sponsorshipCampaignId);

        return sponsorshipCampaignMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public SponsorshipCampaignResponseDto complete(UUID sponsorshipCampaignId) {
        log.info("Completing campaign: {}", sponsorshipCampaignId);

        SponsorshipCampaign campaign = findCampaignOrThrow(sponsorshipCampaignId);

        if (campaign.getSponsorshipCampaignStatus() != SponsorshipCampaignStatus.ACTIVE &&
                campaign.getSponsorshipCampaignStatus() != SponsorshipCampaignStatus.PAUSED) {
            throw new InvalidRequestException(
                    "Only ACTIVE or PAUSED campaigns can be completed");
        }

        campaign.setSponsorshipCampaignStatus(SponsorshipCampaignStatus.COMPLETED);
        campaign.setSponsorshipCampaignCompletedAt(Instant.now());
        SponsorshipCampaign saved = sponsorshipCampaignRepository.save(campaign);
        log.info("Campaign completed: {}", sponsorshipCampaignId);

        return sponsorshipCampaignMapper.toResponseDto(saved);
    }

    // ========================================
    // Metrics & Analytics
    // ========================================

    @Override
    @Transactional
    public void recordImpression(UUID sponsorshipCampaignId) {
        sponsorshipCampaignRepository.incrementImpressions(sponsorshipCampaignId, Instant.now());
    }

    @Override
    @Transactional
    public void recordClick(UUID sponsorshipCampaignId) {
        sponsorshipCampaignRepository.incrementClicks(sponsorshipCampaignId, Instant.now());
    }

    @Override
    @Transactional
    public void recordConversion(UUID sponsorshipCampaignId) {
        SponsorshipCampaign campaign = findCampaignOrThrow(sponsorshipCampaignId);
        campaign.setSponsorshipCampaignConversionCount(
                campaign.getSponsorshipCampaignConversionCount() + 1);
        sponsorshipCampaignRepository.save(campaign);
    }

    // ========================================
    // Scheduled Tasks
    // ========================================

    @Override
    @Transactional
    public int activateReadyCampaigns(String tenantId) {
        log.info("Activating ready campaigns for tenant: {}", tenantId);
        int count = sponsorshipCampaignRepository.activateReadyCampaigns(Instant.now(), tenantId);
        if (count > 0) {
            log.info("Activated {} campaigns for tenant: {}", count, tenantId);
        }
        return count;
    }

    @Override
    @Transactional
    public int completeExpiredCampaigns(String tenantId) {
        log.info("Completing expired campaigns for tenant: {}", tenantId);
        int count = sponsorshipCampaignRepository.completeExpiredCampaigns(Instant.now(), tenantId);
        if (count > 0) {
            log.info("Completed {} expired campaigns for tenant: {}", count, tenantId);
        }
        return count;
    }

    @Override
    @Transactional
    public int pauseBudgetExhaustedCampaigns(String tenantId) {
        log.info("Pausing budget-exhausted campaigns for tenant: {}", tenantId);
        List<SponsorshipCampaign> exhausted = sponsorshipCampaignRepository.findBudgetExhaustedCampaigns(tenantId);
        int count = 0;
        for (SponsorshipCampaign campaign : exhausted) {
            campaign.setSponsorshipCampaignStatus(SponsorshipCampaignStatus.PAUSED);
            sponsorshipCampaignRepository.save(campaign);
            log.info("Paused budget-exhausted campaign: {}", campaign.getSponsorshipCampaignId());
            count++;
        }
        return count;
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private SponsorshipCampaign findCampaignOrThrow(UUID sponsorshipCampaignId) {
        return sponsorshipCampaignRepository.findById(sponsorshipCampaignId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Campaign not found with ID: " + sponsorshipCampaignId));
    }

    private void validateDateRange(Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidRequestException("Start date must be before end date");
        }
    }

    private void validateForSubmission(SponsorshipCampaign campaign) {
        if (campaign.getSponsorshipCampaignName() == null ||
                campaign.getSponsorshipCampaignName().isBlank()) {
            throw new InvalidRequestException("Campaign name is required for submission");
        }
        if (campaign.getSponsorshipCampaignStartDate() == null ||
                campaign.getSponsorshipCampaignEndDate() == null) {
            throw new InvalidRequestException("Campaign dates are required for submission");
        }
        if (campaign.getSponsorshipCampaignTotalBudget() == null) {
            throw new InvalidRequestException("Campaign budget is required for submission");
        }
        if (campaign.getSponsorshipCampaignAdvertiserId() == null) {
            throw new InvalidRequestException("Advertiser information is required for submission");
        }
    }

    private String generateCampaignCode(String campaignName) {
        String base = campaignName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .substring(0, Math.min(campaignName.length(), 30));
        return base + "_" + System.currentTimeMillis() % 100000;
    }
}
