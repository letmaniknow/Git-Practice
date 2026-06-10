package com.mmva.newsapp.infrastructure.monetization.subscription.exception;

import java.util.UUID;

/**
 * Exception thrown when a Subscription Plan is not found.
 * 
 * <p>
 * Feature-specific exception per PROJECT_PRINCIPLES.md §5.3:
 * Feature packages should contain their own exception folder.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class SubscriptionPlanNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates exception for plan not found by ID.
     */
    public SubscriptionPlanNotFoundException(UUID subscriptionPlanId) {
        super("Subscription Plan not found with ID: " + subscriptionPlanId);
    }

    /**
     * Creates exception for plan not found by code.
     */
    public SubscriptionPlanNotFoundException(String subscriptionPlanCode) {
        super("Subscription Plan not found with code: " + subscriptionPlanCode);
    }

    /**
     * Creates exception with custom message.
     */
    public SubscriptionPlanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
