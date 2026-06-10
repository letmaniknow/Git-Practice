package com.mmva.newsapp.infrastructure.push.exception;

import java.util.UUID;

/**
 * Exception thrown when a push notification is not found.
 * 
 * <p>
 * Feature-specific exception per PROJECT_PRINCIPLES.md §5.3:
 * Feature packages should contain their own exception folder.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class PushNotificationNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates exception for notification not found by ID.
     *
     * @param pushNotificationId the notification ID that was not found
     */
    public PushNotificationNotFoundException(UUID pushNotificationId) {
        super("Push notification not found with ID: " + pushNotificationId);
    }

    /**
     * Creates exception for notification not found by idempotency key.
     *
     * @param idempotencyKey the idempotency key that was not found
     */
    public PushNotificationNotFoundException(String idempotencyKey) {
        super("Push notification not found with idempotency key: " + idempotencyKey);
    }

    /**
     * Creates exception with custom message.
     *
     * @param message custom error message
     * @param cause   the underlying cause
     */
    public PushNotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
