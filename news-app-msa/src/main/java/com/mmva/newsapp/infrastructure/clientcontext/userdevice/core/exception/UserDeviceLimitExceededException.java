package com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.exception;

import java.util.UUID;

/**
 * Exception thrown when user exceeds the maximum number of allowed devices.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class UserDeviceLimitExceededException extends RuntimeException {

    private static final int DEFAULT_MAX_DEVICES = 10;

    public UserDeviceLimitExceededException(String message) {
        super(message);
    }

    public UserDeviceLimitExceededException(UUID userId) {
        super("User device limit exceeded for user: " + userId + " (max: " + DEFAULT_MAX_DEVICES + ")");
    }

    public UserDeviceLimitExceededException(UUID userId, int maxDevices) {
        super("User device limit exceeded for user: " + userId + " (max: " + maxDevices + ")");
    }

    public UserDeviceLimitExceededException(UUID userId, int currentCount, int maxDevices) {
        super("User device limit exceeded for user: " + userId + " (current: " + currentCount + ", max: " + maxDevices
                + ")");
    }
}
