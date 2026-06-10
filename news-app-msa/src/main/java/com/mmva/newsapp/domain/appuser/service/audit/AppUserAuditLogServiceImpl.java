package com.mmva.newsapp.domain.appuser.service.audit;

import com.mmva.newsapp.domain.appuser.model.audit.AppUserAuditLog;
import com.mmva.newsapp.domain.appuser.repository.audit.AppUserAuditLogRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link AppUserAuditLogService}.
 * 
 * <p>
 * Manages audit logging for app user profile operations.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AppUserAuditLogServiceImpl implements AppUserAuditLogService {

    private final AppUserAuditLogRepository auditLogRepository;

    @Override
    public void logAction(UUID userId, String action, String details, UUID createdBy) {
        AppUserAuditLog log = AppUserAuditLog.builder()
                .appUsersAuditLogUserId(userId)
                .appUsersAuditLogAction(action)
                .appUsersAuditLogDetails(details)
                .appUsersAuditLogCreatedBy(createdBy)
                .appUsersAuditLogCreatedAt(Instant.now())
                .build();
        auditLogRepository.save(log);
    }

    @Override
    public List<AppUserAuditLog> findByUserId(UUID userId) {
        return auditLogRepository.findByAppUsersAuditLogUserId(userId);
    }

    @Override
    public Page<AppUserAuditLog> findByUserIdPaginated(UUID userId, Pageable pageable) {
        return auditLogRepository.findByAppUsersAuditLogUserIdOrderByAppUsersAuditLogCreatedAtDesc(userId, pageable);
    }
}
