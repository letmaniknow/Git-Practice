package com.mmva.newsapp.domain.newsengagement.likes.exception;

import java.util.UUID;

/**
 * Exception thrown when a news like record is not found.
 * 
 * <p>
 * This is a domain-specific exception for the likes feature.
 * Use this instead of generic RuntimeException for better
 * error handling and debugging.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsLikeNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "News like not found";

    /**
     * Constructs exception with default message.
     */
    public NewsLikeNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Constructs exception with custom message.
     *
     * @param message the detail message
     */
    public NewsLikeNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs exception for a specific like ID.
     *
     * @param likeId the ID of the like that was not found
     */
    public NewsLikeNotFoundException(Long likeId) {
        super(DEFAULT_MESSAGE + " with ID: " + likeId);
    }

    /**
     * Constructs exception for a specific user and news combination.
     *
     * @param newsId the UUID of the news article
     * @param userId the UUID of the user
     */
    public NewsLikeNotFoundException(UUID newsId, UUID userId) {
        super(DEFAULT_MESSAGE + " for news ID: " + newsId + " and user ID: " + userId);
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public NewsLikeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
