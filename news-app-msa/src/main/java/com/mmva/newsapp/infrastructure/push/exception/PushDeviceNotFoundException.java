package com.mmva.newsapp.infrastructure.push.exception;

import java.util.UUID;

/**
 * Exception thrown when a push device is not found.
 * 
 * <p>
 * Feature-specific exception per PROJECT_PRINCIPLES.md §5.3:
 * Feature packages should contain their own exception folder.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class PushDeviceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates exception for device not found by ID.
     *
     * @param pushDeviceId the device ID that was not found
     */
    public PushDeviceNotFoundException(UUID pushDeviceId) {
        super("Push device not found with ID: " + pushDeviceId);
    }

    /**
     * Creates exception for device not found by FCM token.
     *
     * @param fcmToken the FCM token that was not found
     */
    public PushDeviceNotFoundException(String fcmToken) {
        super("Push device not found with FCM token: " + fcmToken);
    }

    /**
     * Creates exception for device not found by fingerprint.
     *
     * @param fingerprint   the device fingerprint
     * @param isFingerprint flag to distinguish from FCM token constructor
     */
    public PushDeviceNotFoundException(String fingerprint, boolean isFingerprint) {
        super("Push device not found with fingerprint: " + fingerprint);
    }

    /**
     * Creates exception with custom message.
     *
     * @param message custom error message
     * @param cause   the underlying cause
     */
    public PushDeviceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
