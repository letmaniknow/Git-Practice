package com.mmva.newsapp.domain.newsletter.enums.core;

/**
 * Newsletter campaign status enumeration.
 *
 * <p>
 * Defines the possible states of a newsletter campaign throughout its
 * lifecycle.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public enum NewsletterCampaignStatus {

    /**
     * Draft campaign - being prepared, not yet scheduled.
     */
    DRAFT,

    /**
     * Scheduled campaign - ready to send at specified time.
     */
    SCHEDULED,

    /**
     * Sending campaign - currently in progress of being sent.
     */
    SENDING,

    /**
     * Paused campaign - temporarily stopped during sending.
     */
    PAUSED,

    /**
     * Sent campaign - successfully delivered to all recipients.
     */
    SENT,

    /**
     * Cancelled campaign - manually stopped before completion.
     */
    CANCELLED,

    /**
     * Failed campaign - encountered errors during sending.
     */
    FAILED
}