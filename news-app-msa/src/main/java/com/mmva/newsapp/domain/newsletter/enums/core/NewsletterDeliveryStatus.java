package com.mmva.newsapp.domain.newsletter.enums.core;

/**
 * Newsletter delivery status enumeration.
 *
 * <p>
 * Tracks the delivery and engagement status of individual newsletter sends.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public enum NewsletterDeliveryStatus {

    /**
     * Queued for delivery - waiting to be sent.
     */
    QUEUED,

    /**
     * Sent - email has been dispatched to mail server.
     */
    SENT,

    /**
     * Delivered - email successfully reached recipient's inbox.
     */
    DELIVERED,

    /**
     * Opened - recipient opened the email.
     */
    OPENED,

    /**
     * Clicked - recipient clicked a link in the email.
     */
    CLICKED,

    /**
     * Bounced - email delivery failed permanently.
     */
    BOUNCED,

    /**
     * Failed - email delivery failed due to technical issues.
     */
    FAILED
}