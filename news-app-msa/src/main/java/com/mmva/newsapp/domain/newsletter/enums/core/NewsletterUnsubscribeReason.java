package com.mmva.newsapp.domain.newsletter.enums.core;

/**
 * Newsletter unsubscribe reason enumeration.
 *
 * <p>
 * Tracks why subscribers choose to unsubscribe for analytics and improvement.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public enum NewsletterUnsubscribeReason {

    /**
     * No longer interested in the content.
     */
    NO_LONGER_INTERESTED,

    /**
     * Receiving emails too frequently.
     */
    TOO_FREQUENT,

    /**
     * Content is not relevant to their interests.
     */
    CONTENT_IRRELEVANT,

    /**
     * Technical issues with email delivery or formatting.
     */
    TECHNICAL_ISSUES,

    /**
     * Privacy concerns.
     */
    PRIVACY_CONCERNS,

    /**
     * Other unspecified reason.
     */
    OTHER
}