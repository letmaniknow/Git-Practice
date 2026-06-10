package com.mmva.newsapp.infrastructure.monetization.subscription.exception;

import java.util.UUID;

/**
 * Exception thrown when a User Subscription is not found.
 * 
 * <p>
 * Feature-specific exception per PROJECT_PRINCIPLES.md §5.3:
 * Feature packages should contain their own exception folder.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class UserSubscriptionNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates exception for subscription not found by ID.
     */
    public UserSubscriptionNotFoundException(UUID userSubscriptionId) {
        super("User Subscription not found with ID: " + userSubscriptionId);
    }

    /**
     * Creates exception for subscription not found by user ID.
     */
    public static UserSubscriptionNotFoundException forUser(UUID userId) {
        return new UserSubscriptionNotFoundException("No active subscription found for user: " + userId);
    }

    /**
     * Creates exception with custom message.
     */
    public UserSubscriptionNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates exception with custom message and cause.
     */
    public UserSubscriptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
