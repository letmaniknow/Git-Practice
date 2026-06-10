package com.mmva.newsapp.domain.news.service.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mmva.newsapp.domain.news.audit.model.NewsAuditLog;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing News audit logs.
 * 
 * <p>
 * Provides operations for logging and retrieving audit trail
 * for news article actions.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsAuditLogService {

    /**
     * Logs an audit action with complete context for audit trails, dashboards, and
     * compliance.
     * 
     * <p>
     * Industry-standard enriched audit logging with 24+ fields including:
     * WHO (actor), WHAT (action), WHICH (resource), WHEN (timestamp),
     * WHERE (location), WHY (reason), HOW (result), CORRELATE (tracing).
     * </p>
     *
     * @param newsId           the UUID of the news article
     * @param action           the action performed (CREATE, PUBLISH, UPDATE,
     *                         DELETE, ARCHIVE)
     * @param details          additional details about the action (JSON format or
     *                         text)
     * @param actorId          the UUID of the actor who performed the action
     * @param actorDisplayName human-readable name of actor (email, full name, or
     *                         system name)
     * @param resourceName     human-readable name of resource (article title)
     */
    void logAction(UUID newsId, String action, String details, UUID actorId,
            String actorDisplayName, String resourceName);

    /**
     * Retrieves all audit logs for a specific news article.
     *
     * @param newsId the UUID of the news article
     * @return list of audit logs for the news article
     */
    List<NewsAuditLog> findByNewsId(UUID newsId);

    /**
     * Retrieves paginated audit logs for a specific news article.
     *
     * @param newsId   the UUID of the news article
     * @param pageable pagination information
     * @return page of audit logs for the news article
     */
    Page<NewsAuditLog> findByNewsIdPaginated(UUID newsId, Pageable pageable);
}
