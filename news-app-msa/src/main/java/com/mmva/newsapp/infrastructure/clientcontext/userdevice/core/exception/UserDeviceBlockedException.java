package com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.exception;

import java.util.UUID;

/**
 * Exception thrown when attempting to use a blocked user device.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class UserDeviceBlockedException extends RuntimeException {

    public UserDeviceBlockedException(String message) {
        super(message);
    }

    public UserDeviceBlockedException(UUID deviceId) {
        super("User device is blocked: " + deviceId);
    }

    public UserDeviceBlockedException(UUID deviceId, String reason) {
        super("User device is blocked: " + deviceId + " - Reason: " + reason);
    }
}
