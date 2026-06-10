package com.mmva.newsapp.infrastructure.common.exception;

import java.util.UUID;

/**
 * Exception thrown when a user attempts to perform an action without proper
 * authorization.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(UUID userId, String action) {
        super("User " + userId + " is not authorized to perform action: " + action);
    }

    public UnauthorizedAccessException(String userId, String action, boolean isUserId) {
        super("User " + userId + " is not authorized to perform action: " + action);
    }
}
