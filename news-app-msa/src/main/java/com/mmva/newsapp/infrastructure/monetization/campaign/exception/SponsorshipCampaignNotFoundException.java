package com.mmva.newsapp.infrastructure.monetization.campaign.exception;

import java.util.UUID;

/**
 * Exception thrown when a Sponsorship Campaign is not found.
 * 
 * <p>
 * Feature-specific exception per PROJECT_PRINCIPLES.md §5.3:
 * Feature packages should contain their own exception folder.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class SponsorshipCampaignNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates exception for campaign not found by ID.
     */
    public SponsorshipCampaignNotFoundException(UUID sponsorshipCampaignId) {
        super("Sponsorship Campaign not found with ID: " + sponsorshipCampaignId);
    }

    /**
     * Creates exception for campaign not found by code.
     */
    public SponsorshipCampaignNotFoundException(String sponsorshipCampaignCode) {
        super("Sponsorship Campaign not found with code: " + sponsorshipCampaignCode);
    }

    /**
     * Creates exception with custom message.
     */
    public SponsorshipCampaignNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
