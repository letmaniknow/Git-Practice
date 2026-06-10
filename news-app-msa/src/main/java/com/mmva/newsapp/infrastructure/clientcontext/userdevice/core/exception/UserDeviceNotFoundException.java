package com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.exception;

import java.util.UUID;

/**
 * Exception thrown when a user device is not found.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class UserDeviceNotFoundException extends RuntimeException {

    public UserDeviceNotFoundException(String message) {
        super(message);
    }

    public UserDeviceNotFoundException(UUID deviceId) {
        super("User device not found with id: " + deviceId);
    }

    public UserDeviceNotFoundException(UUID deviceId, UUID userId) {
        super("User device not found with id: " + deviceId + " for user: " + userId);
    }
}
