package com.mmva.newsapp.domain.newsengagement.comments.exception;

import java.util.UUID;

/**
 * Exception thrown when a news comment like is not found.
 * 
 * <p>
 * This is a domain-specific exception for the comment likes sub-feature.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsCommentLikeNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "News comment like not found";

    /**
     * Constructs exception with default message.
     */
    public NewsCommentLikeNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Constructs exception with custom message.
     *
     * @param message the detail message
     */
    public NewsCommentLikeNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs exception for a specific like ID.
     *
     * @param likeId the UUID of the like that was not found
     */
    public NewsCommentLikeNotFoundException(UUID likeId) {
        super(DEFAULT_MESSAGE + " with ID: " + likeId);
    }

    /**
     * Constructs exception for a user-comment combination.
     *
     * @param userId    the user ID
     * @param commentId the comment ID
     */
    public NewsCommentLikeNotFoundException(UUID userId, UUID commentId) {
        super(DEFAULT_MESSAGE + " for user: " + userId + " and comment: " + commentId);
    }
}
