package com.mmva.newsapp.domain.news.service.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmva.newsapp.domain.news.audit.model.NewsAuditLog;
import com.mmva.newsapp.domain.news.repository.audit.NewsAuditLogRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link NewsAuditLogService}.
 * 
 * <p>
 * Provides audit logging functionality for news article operations.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsAuditLogServiceImpl implements NewsAuditLogService {

    private final NewsAuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logAction(UUID newsId, String action, String details, UUID actorId,
            String actorDisplayName, String resourceName) {
        log.debug("NewsAuditLogService: Logging action '{}' for article: {} by actor: {}",
                action, resourceName, actorDisplayName);

        NewsAuditLog auditLog = new NewsAuditLog();

        // WHO: Actor information
        auditLog.setActorId(actorId);
        auditLog.setActorDisplayName(actorDisplayName);

        // WHAT: Action information
        auditLog.setAction(action);
        auditLog.setDomain("NEWS"); // Always NEWS domain for this service
        auditLog.setSource("REST_API"); // Default source (can be overridden)

        // WHICH: Resource information
        auditLog.setNewsId(newsId);
        auditLog.setResourceId(newsId);
        auditLog.setResourceName(resourceName);

        // WHEN: Timestamp
        auditLog.setCreatedAt(Instant.now());

        // WHY: Reason and details
        auditLog.setReason(details);
        auditLog.setDetails(details);

        // HOW: Success indicators
        auditLog.setIsSuccess(true); // Logs are created for successful operations
        auditLog.setAffectedRows(1);

        // CORRELATE: Severity (default to HIGH for audit creation)
        auditLog.setSeverity("HIGH");

        auditLogRepository.save(auditLog);

        log.debug("NewsAuditLogService: Audit log saved for news ID: {} - Title: {}",
                newsId, resourceName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsAuditLog> findByNewsId(UUID newsId) {
        log.debug("NewsAuditLogService: Finding audit logs for news ID: {}", newsId);
        return auditLogRepository.findByNewsId(newsId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsAuditLog> findByNewsIdPaginated(UUID newsId, Pageable pageable) {
        log.debug("NewsAuditLogService: Finding paginated audit logs for news ID: {}", newsId);
        return auditLogRepository.findByNewsIdOrderByCreatedAtDesc(newsId, pageable);
    }
}
