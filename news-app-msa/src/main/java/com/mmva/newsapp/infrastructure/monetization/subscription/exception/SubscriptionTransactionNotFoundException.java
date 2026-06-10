package com.mmva.newsapp.infrastructure.monetization.subscription.exception;

import java.util.UUID;

/**
 * Exception thrown when a Subscription Transaction is not found.
 * 
 * <p>
 * Feature-specific exception per PROJECT_PRINCIPLES.md §5.3:
 * Feature packages should contain their own exception folder.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class SubscriptionTransactionNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates exception for transaction not found by ID.
     */
    public SubscriptionTransactionNotFoundException(UUID subscriptionTransactionId) {
        super("Subscription Transaction not found with ID: " + subscriptionTransactionId);
    }

    /**
     * Creates exception for transaction not found by external ID.
     */
    public static SubscriptionTransactionNotFoundException forExternalId(String externalTransactionId) {
        return new SubscriptionTransactionNotFoundException(
                "Subscription Transaction not found with external ID: " + externalTransactionId);
    }

    /**
     * Creates exception with custom message.
     */
    public SubscriptionTransactionNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates exception with custom message and cause.
     */
    public SubscriptionTransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
