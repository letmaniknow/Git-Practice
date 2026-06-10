package com.mmva.newsapp.domain.newsengagement.shares.exception;

import java.util.UUID;

/**
 * Exception thrown when a news share record is not found.
 * 
 * <p>
 * This exception is used for explicit lookup scenarios such as:
 * </p>
 * <ul>
 * <li>Admin retrieving a specific share by ID</li>
 * <li>Debug/audit operations requiring explicit share existence</li>
 * </ul>
 * 
 * <p>
 * <strong>Note:</strong> This exception should NOT be thrown for normal user
 * operations like removing a share that doesn't exist. Such operations should
 * be idempotent and return silently.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsShareNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with a default message.
     */
    public NewsShareNotFoundException() {
        super("News share not found");
    }

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message the detail message
     */
    public NewsShareNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception for a specific share ID.
     *
     * @param shareId the share ID that was not found
     */
    public NewsShareNotFoundException(Long shareId) {
        super("News share not found with ID: " + shareId);
    }

    /**
     * Constructs a new exception for a specific news and user combination.
     *
     * @param newsId the news ID
     * @param userId the user ID
     */
    public NewsShareNotFoundException(UUID newsId, UUID userId) {
        super("News share not found for news ID: " + newsId + " and user ID: " + userId);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public NewsShareNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
