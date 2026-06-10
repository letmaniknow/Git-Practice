package com.mmva.newsapp.domain.newsengagement.comments.exception;

import java.util.UUID;

/**
 * Exception thrown when a news comment is not found.
 * 
 * <p>
 * This is a domain-specific exception for the comments feature.
 * Use this instead of generic ResourceNotFoundException for better
 * error handling and debugging.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsCommentNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "News comment not found";

    /**
     * Constructs exception with default message.
     */
    public NewsCommentNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Constructs exception with custom message.
     *
     * @param message the detail message
     */
    public NewsCommentNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs exception for a specific comment ID.
     *
     * @param commentId the UUID of the comment that was not found
     */
    public NewsCommentNotFoundException(UUID commentId) {
        super(DEFAULT_MESSAGE + " with ID: " + commentId);
    }

    /**
     * Constructs exception with message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public NewsCommentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}