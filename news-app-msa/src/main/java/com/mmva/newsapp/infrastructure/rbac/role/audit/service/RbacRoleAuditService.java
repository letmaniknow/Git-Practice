package com.mmva.newsapp.infrastructure.rbac.role.audit.service;

import com.mmva.newsapp.infrastructure.rbac.role.audit.model.RbacRoleAuditLog;
import com.mmva.newsapp.infrastructure.rbac.role.audit.repository.RoleAuditLogRepository;
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
 * Service for querying role audit logs.
 * Read-only service - no audit logging here (that happens via AuditingUtility).
 * 
 * Provides simple, focused queries for role-related audit trails.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RbacRoleAuditService {

    private final RoleAuditLogRepository roleAuditLogRepository;

    /**
     * Get all audit logs for a specific role.
     */
    public Page<RbacRoleAuditLog> getAuditLogsForRole(UUID roleId, Pageable pageable) {
        log.debug("Fetching audit logs for role: {}", roleId);
        return roleAuditLogRepository.findByRoleIdOrderByCreatedAtDesc(roleId, pageable);
    }

    /**
     * Get failed role change attempts.
     */
    public List<RbacRoleAuditLog> getFailedRoleChanges() {
        log.debug("Fetching failed role change attempts");
        return roleAuditLogRepository.findByIsSuccessFalse(null).getContent();
    }

    /**
     * Count total audit logs for a role.
     */
    public long countAuditLogsForRole(UUID roleId) {
        log.debug("Counting audit logs for role: {}", roleId);
        return roleAuditLogRepository.findByRoleId(roleId).size();
    }

    /**
     * Get audit logs performed by a specific actor.
     */
    public Page<RbacRoleAuditLog> getAuditLogsForActor(UUID actorId, Pageable pageable) {
        log.debug("Fetching audit logs for actor: {}", actorId);
        return roleAuditLogRepository.findByActorId(actorId, pageable);
    }

    /**
     * Get audit logs for a specific action.
     */
    public Page<RbacRoleAuditLog> getAuditLogsForAction(String action, Pageable pageable) {
        log.debug("Fetching audit logs for action: {}", action);
        return roleAuditLogRepository.findByAction(action, pageable);
    }

    /**
     * Get audit logs within a date range.
     */
    public Page<RbacRoleAuditLog> getAuditLogsByDateRange(Instant startDate, Instant endDate, Pageable pageable) {
        log.debug("Fetching audit logs between {} and {}", startDate, endDate);
        return roleAuditLogRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    /**
     * Get critical severity role audit logs.
     */
    public Page<RbacRoleAuditLog> getCriticalRoleAudits(Pageable pageable) {
        log.debug("Fetching critical severity role audits");
        return roleAuditLogRepository.findBySeverity("CRITICAL", pageable);
    }
}
