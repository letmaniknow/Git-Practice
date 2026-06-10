package com.mmva.newsapp.domain.newssourceagency.service.audit;

import com.mmva.newsapp.domain.newssourceagency.dto.audit.NewsSourceAgencyAuditLogDto;
import com.mmva.newsapp.domain.newssourceagency.enums.core.NewsSourceAgencyAuditAction;
import com.mmva.newsapp.domain.newssourceagency.mapper.audit.NewsSourceAgencyAuditLogMapper;
import com.mmva.newsapp.domain.newssourceagency.model.audit.NewsSourceAgencyAuditLog;
import com.mmva.newsapp.domain.newssourceagency.repository.audit.NewsSourceAgencyAuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link NewsSourceAgencyAuditLogService}.
 * Manages audit log operations for news source agency changes.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSourceAgencyAuditLogServiceImpl implements NewsSourceAgencyAuditLogService {

        private final NewsSourceAgencyAuditLogRepository auditLogRepository;
        private final NewsSourceAgencyAuditLogMapper auditLogMapper;

        @Override
        public void logAction(UUID agencyId, NewsSourceAgencyAuditAction action, String details, UUID createdBy) {
                log.debug("Logging audit action - agencyId={}, action={}, createdBy={}",
                                agencyId, action, createdBy);

                NewsSourceAgencyAuditLog auditLog = NewsSourceAgencyAuditLog.builder()
                                .agencyId(agencyId)
                                .action(action.name())
                                .details(details)
                                .createdBy(createdBy)
                                .createdAt(Instant.now())
                                .build();

                auditLogRepository.save(auditLog);
                log.info("Audit log saved - agencyId={}, action={}", agencyId, action);
        }

        @Override
        public List<NewsSourceAgencyAuditLogDto> findByAgencyId(UUID agencyId) {
                log.debug("Fetching audit logs for agencyId={}", agencyId);

                List<NewsSourceAgencyAuditLog> logs = auditLogRepository
                                .findByAgencyIdOrderByCreatedAtDesc(agencyId);

                return logs.stream()
                                .map(auditLogMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Override
        public Page<NewsSourceAgencyAuditLogDto> findByAgencyId(UUID agencyId, Pageable pageable) {
                log.debug("Fetching paginated audit logs - agencyId={}, page={}, size={}",
                                agencyId, pageable.getPageNumber(), pageable.getPageSize());

                Page<NewsSourceAgencyAuditLog> page = auditLogRepository
                                .findByAgencyIdOrderByCreatedAtDesc(agencyId, pageable);

                return page.map(auditLogMapper::toDto);
        }
}
