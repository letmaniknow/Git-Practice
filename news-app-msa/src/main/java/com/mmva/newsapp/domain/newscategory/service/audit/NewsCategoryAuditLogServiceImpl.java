package com.mmva.newsapp.domain.newscategory.service.audit;

import com.mmva.newsapp.domain.newscategory.dto.audit.NewsCategoryAuditLogDto;
import com.mmva.newsapp.domain.newscategory.enums.core.NewsCategoryAuditAction;
import com.mmva.newsapp.domain.newscategory.mapper.audit.NewsCategoryAuditLogMapper;
import com.mmva.newsapp.domain.newscategory.model.audit.NewsCategoryAuditLog;
import com.mmva.newsapp.domain.newscategory.repository.audit.NewsCategoryAuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link NewsCategoryAuditLogService}.
 * Manages audit log operations for news category changes.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsCategoryAuditLogServiceImpl implements NewsCategoryAuditLogService {

        private final NewsCategoryAuditLogRepository auditLogRepository;
        private final NewsCategoryAuditLogMapper auditLogMapper;

        @Override
        public void logAction(UUID categoryId, NewsCategoryAuditAction action, String details, UUID createdBy) {
                log.debug("Logging audit action - categoryId={}, action={}, createdBy={}",
                                categoryId, action, createdBy);

                NewsCategoryAuditLog auditLog = NewsCategoryAuditLog.builder()
                                .newsCategoryId(categoryId)
                                .action(action.name())
                                .reason(details)
                                .actorId(createdBy)
                                .createdAt(Instant.now())
                                .build();

                auditLogRepository.save(auditLog);
                log.info("Audit log saved - categoryId={}, action={}", categoryId, action);
        }

        @Override
        public List<NewsCategoryAuditLogDto> findByCategoryId(UUID categoryId) {
                log.debug("Fetching audit logs for categoryId={}", categoryId);

                List<NewsCategoryAuditLog> logs = auditLogRepository
                                .findByNewsCategoryIdOrderByCreatedAtDesc(categoryId, PageRequest.of(0, 100))
                                .getContent();

                return logs.stream()
                                .map(auditLogMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Override
        public Page<NewsCategoryAuditLogDto> findByCategoryId(UUID categoryId, Pageable pageable) {
                log.debug("Fetching paginated audit logs - categoryId={}, page={}, size={}",
                                categoryId, pageable.getPageNumber(), pageable.getPageSize());

                Page<NewsCategoryAuditLog> page = auditLogRepository
                                .findByNewsCategoryIdOrderByCreatedAtDesc(categoryId, pageable);

                return page.map(auditLogMapper::toDto);
        }
}
