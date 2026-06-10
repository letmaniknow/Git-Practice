package com.mmva.newsapp.domain.newsengagement.bookmarks.exception;

import java.util.UUID;

/**
 * Exception thrown when a bookmark is not found.
 * 
 * <p>
 * This is a domain-specific exception for the bookmarks feature.
 * Use this instead of generic ResourceNotFoundException for better
 * error handling and debugging.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsBookmarkNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Bookmark not found";

    /**
     * Constructs exception with default message.
     */
    public NewsBookmarkNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Constructs exception with custom message.
     *
     * @param message the detail message
     */
    public NewsBookmarkNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs exception for a specific bookmark ID.
     *
     * @param bookmarkId the UUID of the bookmark that was not found
     */
    public NewsBookmarkNotFoundException(UUID bookmarkId) {
        super(DEFAULT_MESSAGE + " with ID: " + bookmarkId);
    }

    /**
     * Constructs exception for a user-news combination.
     *
     * @param userId the user ID
     * @param newsId the news ID
     */
    public NewsBookmarkNotFoundException(UUID userId, UUID newsId) {
        super(DEFAULT_MESSAGE + " for user: " + userId + " and news: " + newsId);
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public NewsBookmarkNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
