package com.mmva.newsapp.domain.newsletter.enums.core;

/**
 * Newsletter subscription status enumeration.
 *
 * <p>
 * Defines the possible states of a newsletter subscriber's subscription.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public enum NewsletterSubscriptionStatus {

    /**
     * Active subscription - subscriber receives newsletters.
     */
    ACTIVE,

    /**
     * Pending subscription - awaiting email confirmation.
     */
    PENDING,

    /**
     * Unsubscribed - subscriber has opted out.
     */
    UNSUBSCRIBED,

    /**
     * Bounced - email delivery failed permanently.
     */
    BOUNCED
}