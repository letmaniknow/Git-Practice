package com.mmva.newsapp.domain.newsengagement.comments.exception;

import java.util.UUID;

/**
 * Exception thrown when a news comment report is not found.
 * 
 * <p>
 * This is a domain-specific exception for the comment reports sub-feature.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsCommentReportNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "News comment report not found";

    /**
     * Constructs exception with default message.
     */
    public NewsCommentReportNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    /**
     * Constructs exception with custom message.
     *
     * @param message the detail message
     */
    public NewsCommentReportNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs exception for a specific report ID.
     *
     * @param reportId the UUID of the report that was not found
     */
    public NewsCommentReportNotFoundException(UUID reportId) {
        super(DEFAULT_MESSAGE + " with ID: " + reportId);
    }

    /**
     * Constructs exception for a user-comment combination.
     *
     * @param userId    the user ID
     * @param commentId the comment ID
     */
    public NewsCommentReportNotFoundException(UUID userId, UUID commentId) {
        super(DEFAULT_MESSAGE + " for user: " + userId + " and comment: " + commentId);
    }
}
