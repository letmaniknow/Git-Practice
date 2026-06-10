package com.mmva.newsapp.infrastructure.monetization.campaign.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Request DTO for approving or rejecting a sponsorship campaign.
 * All fields use sponsorshipCampaign prefix for consistency.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorshipCampaignApprovalRequestDto {

    /**
     * ID of the campaign to approve/reject.
     */
    @NotNull(message = "Sponsorship Campaign ID is required")
    private UUID sponsorshipCampaignId;

    /**
     * Whether to approve (true) or reject (false) the campaign.
     */
    @NotNull(message = "Approval decision is required")
    private Boolean sponsorshipCampaignApproved;

    /**
     * Reason for rejection (required if approved = false).
     */
    @Size(max = 1000, message = "Rejection reason must not exceed 1000 characters")
    private String sponsorshipCampaignRejectionReason;

    /**
     * Internal notes (not shown to advertiser).
     */
    @Size(max = 500, message = "Internal notes must not exceed 500 characters")
    private String sponsorshipCampaignInternalNotes;
}
