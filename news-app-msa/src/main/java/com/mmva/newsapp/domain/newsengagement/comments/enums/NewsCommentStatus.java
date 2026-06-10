package com.mmva.newsapp.domain.newsengagement.comments.enums;

/**
 * Status enumeration for news comments moderation workflow.
 * 
 * <p>
 * Defines the lifecycle states a comment can be in during moderation.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public enum NewsCommentStatus {

    /**
     * Comment is pending moderation review.
     * Initial state for all new comments.
     */
    PENDING,

    /**
     * Comment has been approved and is visible to the public.
     */
    APPROVED,

    /**
     * Comment has been rejected by moderator and is not visible.
     */
    REJECTED,

    /**
     * Comment has been soft-deleted.
     */
    DELETED
}
