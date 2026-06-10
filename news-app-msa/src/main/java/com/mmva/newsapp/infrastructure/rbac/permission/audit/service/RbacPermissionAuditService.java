package com.mmva.newsapp.infrastructure.rbac.permission.audit.service;

import com.mmva.newsapp.infrastructure.rbac.permission.audit.model.RbacPermissionAuditLog;
import com.mmva.newsapp.infrastructure.rbac.permission.audit.repository.PermissionAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for querying permission audit logs.
 * Read-only service - no audit logging here (that happens via AuditingUtility).
 * 
 * Provides simple, focused queries for permission-related audit trails.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RbacPermissionAuditService {

    private final PermissionAuditLogRepository permissionAuditLogRepository;

    /**
     * Get all audit logs for a specific permission.
     */
    public Page<RbacPermissionAuditLog> getAuditLogsForPermission(UUID permissionId, Pageable pageable) {
        log.debug("Fetching audit logs for permission: {}", permissionId);
        return permissionAuditLogRepository.findByPermissionIdOrderByCreatedAtDesc(permissionId, pageable);
    }

    /**
     * Get failed permission change attempts.
     */
    public List<RbacPermissionAuditLog> getFailedPermissionChanges() {
        log.debug("Fetching failed permission change attempts");
        return permissionAuditLogRepository.findByIsSuccessFalse(null).getContent();
    }

    /**
     * Count total audit logs for a permission.
     */
    public long countAuditLogsForPermission(UUID permissionId) {
        log.debug("Counting audit logs for permission: {}", permissionId);
        return permissionAuditLogRepository.findByPermissionId(permissionId).size();
    }

    /**
     * Get audit logs performed by a specific actor.
     */
    public Page<RbacPermissionAuditLog> getAuditLogsForActor(UUID actorId, Pageable pageable) {
        log.debug("Fetching permission audit logs for actor: {}", actorId);
        return permissionAuditLogRepository.findByActorId(actorId, pageable);
    }

    /**
     * Get audit logs for a specific action.
     */
    public Page<RbacPermissionAuditLog> getAuditLogsForAction(String action, Pageable pageable) {
        log.debug("Fetching permission audit logs for action: {}", action);
        return permissionAuditLogRepository.findByAction(action, pageable);
    }

    /**
     * Get audit logs within a date range.
     */
    public Page<RbacPermissionAuditLog> getAuditLogsByDateRange(Instant startDate, Instant endDate, Pageable pageable) {
        log.debug("Fetching permission audit logs between {} and {}", startDate, endDate);
        return permissionAuditLogRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    /**
     * Get critical severity permission audit logs.
     */
    public Page<RbacPermissionAuditLog> getCriticalPermissionAudits(Pageable pageable) {
        log.debug("Fetching critical severity permission audits");
        return permissionAuditLogRepository.findBySeverity("CRITICAL", pageable);
    }
}
