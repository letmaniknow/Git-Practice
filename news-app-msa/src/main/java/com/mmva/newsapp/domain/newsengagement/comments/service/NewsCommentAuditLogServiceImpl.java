package com.mmva.newsapp.domain.newsengagement.comments.service;

import com.mmva.newsapp.domain.newsengagement.comments.model.NewsCommentAuditLog;
import com.mmva.newsapp.domain.newsengagement.comments.repository.NewsCommentAuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of {@link NewsCommentAuditLogService} for managing news
 * comment audit logs.
 * 
 * <p>
 * Provides operations for logging and tracking actions performed
 * on news comments for audit and compliance purposes.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsCommentAuditLogServiceImpl implements NewsCommentAuditLogService {

    private final NewsCommentAuditLogRepository auditLogRepository;

    @Override
    public void logAction(UUID actorId, UUID commentId, String action, String description) {
        log.debug("NewsCommentAuditLogService: Logging action '{}' for comment: {} by actor: {}",
                action, commentId, actorId);
        NewsCommentAuditLog auditLog = NewsCommentAuditLog.builder()
                .actorId(actorId)
                .newsCommentAuditLogCommentId(commentId)
                .newsCommentAuditLogAction(action)
                .newsCommentAuditLogDescription(description)
                .build();
        auditLogRepository.save(auditLog);
        log.debug("NewsCommentAuditLogService: Audit log saved for action '{}' on comment: {}", action, commentId);
    }

    @Override
    public NewsCommentAuditLog save(NewsCommentAuditLog auditLog) {
        log.debug("NewsCommentAuditLogService: Saving audit log entry");
        return auditLogRepository.save(auditLog);
    }
}
