package com.mmva.newsapp.domain.news.repository.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.mmva.newsapp.domain.news.audit.model.NewsAuditLog;
import com.mmva.newsapp.infrastructure.common.audit.repository.UnifiedAuditLogRepository;

import java.util.UUID;

/**
 * Repository for News audit log operations.
 * Extends UnifiedAuditLogRepository to support polymorphic audit logging via
 * AuditingUtility.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsAuditLogRepository extends UnifiedAuditLogRepository<NewsAuditLog> {

    /**
     * Finds all audit logs for a specific news article.
     *
     * @param newsId the news article UUID
     * @return list of audit logs
     */
    java.util.List<NewsAuditLog> findByNewsId(UUID newsId);

    /**
     * Finds paginated audit logs for a specific news article, ordered by creation
     * date descending.
     *
     * @param newsId   the news article UUID
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<NewsAuditLog> findByNewsIdOrderByCreatedAtDesc(UUID newsId, Pageable pageable);
}
