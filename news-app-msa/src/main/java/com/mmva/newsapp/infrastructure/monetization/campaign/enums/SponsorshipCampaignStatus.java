package com.mmva.newsapp.infrastructure.monetization.campaign.enums;

/**
 * Enum representing the lifecycle status of a sponsorship campaign.
 * 
 * <p>
 * Tracks campaigns through their complete lifecycle from creation
 * to completion. Used for campaign scheduling, budget control,
 * and reporting.
 * </p>
 * 
 * <h3>Status Flow:</h3>
 * 
 * <pre>
 * DRAFT ──► PENDING_APPROVAL ──► APPROVED ──► ACTIVE ──► COMPLETED
 *   │              │                 │          │
 *   ▼              ▼                 ▼          ▼
 * CANCELLED    REJECTED          CANCELLED   PAUSED ──► ACTIVE
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum SponsorshipCampaignStatus {

    /**
     * Draft - campaign is being created, not yet submitted.
     * Editable, not visible to public, no budget consumed.
     */
    DRAFT("Draft", false, false),

    /**
     * Pending approval - campaign submitted for review.
     * Awaiting editorial/admindashboard approval before activation.
     */
    PENDING_APPROVAL("Pending Approval", false, false),

    /**
     * Approved - campaign reviewed and approved.
     * Ready to be activated, waiting for start date or manual activation.
     */
    APPROVED("Approved", false, false),

    /**
     * Rejected - campaign rejected during review.
     * Requires revision and resubmission.
     */
    REJECTED("Rejected", false, true),

    /**
     * Active - campaign is running.
     * Content is served, budget is consumed, impressions tracked.
     */
    ACTIVE("Active", true, false),

    /**
     * Paused - campaign temporarily stopped.
     * No new impressions, budget preserved, can resume.
     */
    PAUSED("Paused", false, false),

    /**
     * Completed - campaign finished (end date reached or budget exhausted).
     * Final state for successful campaigns.
     */
    COMPLETED("Completed", false, true),

    /**
     * Cancelled - campaign terminated before completion.
     * No further impressions, partial refund may apply.
     */
    CANCELLED("Cancelled", false, true);

    private final String displayName;
    private final boolean servingContent;
    private final boolean terminal;

    SponsorshipCampaignStatus(String displayName, boolean servingContent, boolean terminal) {
        this.displayName = displayName;
        this.servingContent = servingContent;
        this.terminal = terminal;
    }

    /**
     * Returns a human-readable display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this status allows content serving.
     * Only ACTIVE campaigns serve content.
     *
     * @return true if content should be served
     */
    public boolean isServingContent() {
        return servingContent;
    }

    /**
     * Checks if this is a terminal status (no further transitions).
     *
     * @return true if terminal
     */
    public boolean isTerminal() {
        return terminal;
    }

    /**
     * Checks if campaign can be edited in this status.
     *
     * @return true if editable
     */
    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }

    /**
     * Checks if campaign can be activated from this status.
     *
     * @return true if can activate
     */
    public boolean canActivate() {
        return this == APPROVED || this == PAUSED;
    }

    /**
     * Checks if this status consumes budget.
     *
     * @return true if budget is consumed
     */
    public boolean consumesBudget() {
        return this == ACTIVE;
    }
}
