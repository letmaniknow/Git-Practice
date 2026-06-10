package com.mmva.newsapp.domain.newsengagement.comments.service;

import com.mmva.newsapp.domain.newsengagement.comments.model.NewsCommentAuditLog;

import java.util.UUID;

/**
 * Service interface for managing news comment audit logs.
 * 
 * <p>
 * Provides operations for logging and tracking actions performed
 * on news comments for audit and compliance purposes.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 * @see NewsCommentAuditLogServiceImpl
 */
public interface NewsCommentAuditLogService {

    /**
     * Logs an action performed on a news comment.
     *
     * @param actorId     the UUID of the user who performed the action
     * @param commentId   the UUID of the comment (can be null for general actions)
     * @param action      the action performed (e.g., CREATE, UPDATE, DELETE,
     *                    APPROVE, REJECT)
     * @param description additional description about the action
     */
    void logAction(UUID actorId, UUID commentId, String action, String description);

    /**
     * Saves an audit log entry.
     *
     * @param auditLog the audit log to save
     * @return the saved audit log
     */
    NewsCommentAuditLog save(NewsCommentAuditLog auditLog);
}
