package com.mmva.newsapp.domain.newsengagement.views.exception;

import java.util.UUID;

/**
 * Exception thrown when a news view record is not found.
 * 
 * <p>
 * This exception is used for explicit lookup scenarios such as:
 * </p>
 * <ul>
 * <li>Admin retrieving a specific view by ID</li>
 * <li>Debug/audit operations requiring explicit view existence</li>
 * </ul>
 * 
 * <p>
 * <strong>Design Note:</strong> Views are EVENTS, not STATES. This exception
 * should only be thrown for explicit admin/debug lookups, not for normal
 * analytics operations which should handle empty results gracefully.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsViewNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with a default message.
     */
    public NewsViewNotFoundException() {
        super("News view not found");
    }

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message the detail message
     */
    public NewsViewNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception for a specific view ID.
     *
     * @param viewId the view ID that was not found
     */
    public NewsViewNotFoundException(Long viewId) {
        super("News view not found with ID: " + viewId);
    }

    /**
     * Constructs a new exception for a specific news and user combination.
     *
     * @param newsId the news ID
     * @param userId the user ID
     */
    public NewsViewNotFoundException(UUID newsId, UUID userId) {
        super("News view not found for news ID: " + newsId + " and user ID: " + userId);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public NewsViewNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
