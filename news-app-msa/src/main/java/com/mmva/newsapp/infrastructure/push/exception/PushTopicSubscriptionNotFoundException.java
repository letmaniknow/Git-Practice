package com.mmva.newsapp.infrastructure.push.exception;

import java.util.UUID;

/**
 * Exception thrown when a push topic subscription is not found.
 * 
 * <p>
 * Feature-specific exception per PROJECT_PRINCIPLES.md §5.3:
 * Feature packages should contain their own exception folder.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class PushTopicSubscriptionNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates exception for subscription not found by ID.
     *
     * @param subscriptionId the subscription ID that was not found
     */
    public PushTopicSubscriptionNotFoundException(UUID subscriptionId) {
        super("Push topic subscription not found with ID: " + subscriptionId);
    }

    /**
     * Creates exception for subscription not found by device and topic.
     *
     * @param deviceId the device ID
     * @param topic    the topic name
     */
    public PushTopicSubscriptionNotFoundException(UUID deviceId, String topic) {
        super("Push topic subscription not found for device: " + deviceId + " and topic: " + topic);
    }

    /**
     * Creates exception with custom message.
     *
     * @param message custom error message
     * @param cause   the underlying cause
     */
    public PushTopicSubscriptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
