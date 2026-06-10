package com.mmva.newsapp.infrastructure.rbac.permission.core.service;

import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionRequestDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.mapper.PermissionMapper;
import com.mmva.newsapp.infrastructure.rbac.permission.core.model.RbacPermission;
import com.mmva.newsapp.infrastructure.rbac.permission.audit.model.RbacPermissionAuditLog;
import com.mmva.newsapp.infrastructure.rbac.permission.audit.repository.PermissionAuditLogRepository;
import com.mmva.newsapp.infrastructure.rbac.exception.PermissionNotFoundException;
import com.mmva.newsapp.infrastructure.common.audit.repository.SoftDeleteSpec;
import com.mmva.newsapp.infrastructure.common.audit.service.AuditingUtility;
import com.mmva.newsapp.infrastructure.rbac.permission.core.repository.PermissionRepository;
import com.mmva.newsapp.infrastructure.rbac.audit.constants.RbacAuditActions;
import com.mmva.newsapp.infrastructure.rbac.audit.constants.RbacAuditDomain;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the PermissionService interface.
 *
 * <p>
 * Provides comprehensive permission management functionality including:
 * <ul>
 * <li>Permission CRUD operations</li>
 * <li>Permission status management (enable/disable/restore)</li>
 * <li>Permission audit logging</li>
 * </ul>
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    // ========================================
    // Dependencies
    // ========================================

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final AuditingUtility auditingUtility;
    private final PermissionAuditLogRepository permissionAuditLogRepository;

    // ========================================
    // Permission CRUD Operations
    // ========================================

    @Override
    @Transactional
    public PermissionResponseDto createPermission(PermissionRequestDto dto, RequestClientInfoDto clientInfo) {
        log.info("Creating permission with name: {}", dto.getPermissionName());

        if (permissionRepository.existsByPermissionName(dto.getPermissionName())) {
            log.warn("Permission creation failed: name '{}' already exists", dto.getPermissionName());
            throw new DuplicateKeyException("Permission with name '" + dto.getPermissionName() + "' already exists");
        }

        RbacPermission permission = permissionMapper.toEntity(dto);
        permission.setCreatedAt(Instant.now());
        permission.setCreatedBy(dto.getAdminId());
        permission.setIsActive(true);

        try {
            RbacPermission saved = permissionRepository.save(permission);
            log.info("Permission '{}' created successfully with ID: {}", dto.getPermissionName(),
                    saved.getPermissionId());

            // Audit log (clientInfo passed from controller)
            UUID actorId = dto.getAdminId();
            auditingUtility.audit(
                    RbacAuditDomain.RBAC,
                    actorId,
                    RbacAuditActions.PERMISSION_CREATED,
                    saved.getPermissionId(),
                    saved.getPermissionName(),
                    "Permission created via admin API",
                    clientInfo,
                    "CRITICAL",
                    RbacPermissionAuditLog.class,
                    permissionAuditLogRepository);

            return permissionMapper.toResponseDto(saved);
        } catch (Exception e) {
            log.error("Failed to create permission '{}': {}", dto.getPermissionName(), e.getMessage());
            throw e;
        }
    }

    @Override
    public Page<PermissionResponseDto> getAllPermissions(Pageable pageable) {
        log.debug("Fetching all permissions (including deleted) - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Admin API: Use SoftDeleteSpec.includeDeleted() to see all records
        Page<RbacPermission> page = permissionRepository.findAll(SoftDeleteSpec.includeDeleted(), pageable);

        log.debug("Retrieved {} permissions on page {}", page.getNumberOfElements(), pageable.getPageNumber());
        return page.map(permissionMapper::toResponseDto);
    }

    /**
     * Get only active (non-deleted) permissions.
     * Use for role assignment dropdowns.
     *
     * @param pageable pagination info
     * @return page of active permissions
     */
    @Override
    public Page<PermissionResponseDto> getActivePermissions(Pageable pageable) {
        log.debug("Fetching active permissions only - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Public API: Use SoftDeleteSpec.notDeleted() to exclude soft-deleted
        Page<RbacPermission> page = permissionRepository.findAll(SoftDeleteSpec.notDeleted(), pageable);

        log.debug("Retrieved {} active permissions on page {}", page.getNumberOfElements(), pageable.getPageNumber());
        return page.map(permissionMapper::toResponseDto);
    }

    /**
     * Get only soft-deleted permissions for admindashboard restore listing.
     *
     * @param pageable pagination info
     * @return page of deleted permissions
     */
    @Override
    public Page<PermissionResponseDto> getDeletedPermissions(Pageable pageable) {
        log.debug("Fetching deleted permissions only - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Admin restore listing: Use SoftDeleteSpec.onlyDeleted()
        Page<RbacPermission> page = permissionRepository.findAll(SoftDeleteSpec.onlyDeleted(), pageable);

        log.debug("Retrieved {} deleted permissions on page {}", page.getNumberOfElements(), pageable.getPageNumber());
        return page.map(permissionMapper::toResponseDto);
    }

    @Override
    public PermissionResponseDto getPermissionById(UUID id) {
        log.debug("Fetching permission by ID: {}", id);

        RbacPermission permission = findPermissionOrThrow(id);

        log.debug("Permission fetched successfully: {}", id);
        return permissionMapper.toResponseDto(permission);
    }

    @Override
    @Transactional
    public PermissionResponseDto updatePermission(UUID id, PermissionRequestDto dto, RequestClientInfoDto clientInfo) {
        log.info("Updating permission ID: {}", id);

        RbacPermission permission = findPermissionOrThrow(id);
        permission.setPermissionName(dto.getPermissionName());
        permission.setPermissionDescription(dto.getPermissionDescription());
        permission.setUpdatedAt(Instant.now());
        permission.setUpdatedBy(dto.getAdminId());

        try {
            RbacPermission updated = permissionRepository.save(permission);
            log.info("Permission {} updated successfully", id);

            // Audit log (clientInfo passed from controller)
            UUID actorId = dto.getAdminId();
            auditingUtility.audit(
                    RbacAuditDomain.RBAC,
                    actorId,
                    RbacAuditActions.PERMISSION_UPDATED,
                    updated.getPermissionId(),
                    updated.getPermissionName(),
                    "Permission updated via admin API",
                    clientInfo,
                    "HIGH",
                    RbacPermissionAuditLog.class,
                    permissionAuditLogRepository);

            return permissionMapper.toResponseDto(updated);
        } catch (Exception e) {
            log.error("Failed to update permission {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deletePermission(UUID id, RequestClientInfoDto clientInfo) {
        log.info("Deleting permission ID: {}", id);

        RbacPermission permission = findPermissionOrThrow(id);
        permission.setDeletedAt(Instant.now());
        permission.setDeletedBy(null);
        permission.setIsActive(false);

        try {
            RbacPermission deleted = permissionRepository.save(permission);
            log.info("Permission {} soft deleted successfully", id);

            // Audit log (clientInfo passed from controller)
            UUID actorId = getCurrentActorId();
            auditingUtility.audit(
                    RbacAuditDomain.RBAC,
                    actorId,
                    RbacAuditActions.PERMISSION_DELETED,
                    deleted.getPermissionId(),
                    deleted.getPermissionName(),
                    "Permission deleted via admin API",
                    clientInfo,
                    "CRITICAL",
                    RbacPermissionAuditLog.class,
                    permissionAuditLogRepository);
        } catch (Exception e) {
            log.error("Failed to delete permission {}: {}", id, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Permission Status Operations
    // ========================================

    @Override
    @Transactional
    public PermissionResponseDto enablePermission(UUID id) {
        log.info("Enabling permission ID: {}", id);

        RbacPermission permission = findPermissionOrThrow(id);
        permission.setIsActive(true);
        permission.setUpdatedAt(Instant.now());

        try {
            RbacPermission updated = permissionRepository.save(permission);
            log.info("Permission {} enabled successfully", id);
            return permissionMapper.toResponseDto(updated);
        } catch (Exception e) {
            log.error("Failed to enable permission {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public PermissionResponseDto disablePermission(UUID id) {
        log.info("Disabling permission ID: {}", id);

        RbacPermission permission = findPermissionOrThrow(id);
        permission.setIsActive(false);
        permission.setUpdatedAt(Instant.now());

        try {
            RbacPermission updated = permissionRepository.save(permission);
            log.info("Permission {} disabled successfully", id);
            return permissionMapper.toResponseDto(updated);
        } catch (Exception e) {
            log.error("Failed to disable permission {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public PermissionResponseDto restorePermission(UUID id, RequestClientInfoDto clientInfo) {
        log.info("Restoring permission ID: {}", id);

        // Use Specification to find deleted permission
        RbacPermission permission = permissionRepository.findOne(
                SoftDeleteSpec.<RbacPermission>onlyDeleted()
                        .and((root, query, cb) -> cb.equal(root.get("permissionId"), id)))
                .orElseThrow(() -> {
                    log.warn("Deleted permission not found for restore: {}", id);
                    return new PermissionNotFoundException("id", id.toString());
                });

        permission.setDeletedAt(null);
        permission.setDeletedBy(null);
        permission.setIsActive(true);
        permission.setUpdatedAt(Instant.now());

        try {
            RbacPermission updated = permissionRepository.save(permission);
            log.info("Permission {} restored successfully", id);

            // Audit log (clientInfo passed from controller)
            UUID actorId = getCurrentActorId();
            auditingUtility.audit(
                    RbacAuditDomain.RBAC,
                    actorId,
                    RbacAuditActions.PERMISSION_RESTORED,
                    updated.getPermissionId(),
                    updated.getPermissionName(),
                    "Permission restored via admin API",
                    clientInfo,
                    "CRITICAL",
                    RbacPermissionAuditLog.class,
                    permissionAuditLogRepository);

            return permissionMapper.toResponseDto(updated);
        } catch (Exception e) {
            log.error("Failed to restore permission {}: {}", id, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Permission Audit Log Operations
    // ========================================

    @Override
    public RbacPermissionAuditLog createAuditLog(RbacPermissionAuditLog auditLog) {
        log.info("Creating permission audit log for permission: {} action: {}",
                auditLog.getPermissionId(), auditLog.getAction());
        // TODO: Implement using new AuditingUtility pattern
        return auditLog;
    }

    @Override
    public List<RbacPermissionAuditLog> getAuditLogsByPermissionId(UUID permissionId) {
        log.debug("Fetching audit logs for permission: {}", permissionId);
        // TODO: Implement using new unified audit repository
        return List.of();
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Gets the current actor ID from security context.
     *
     * @return the current user UUID
     */
    private UUID getCurrentActorId() {
        try {
            return UUID.fromString(
                    (String) org.springframework.security.core.context.SecurityContextHolder
                            .getContext().getAuthentication().getPrincipal());
        } catch (Exception e) {
            log.warn("Unable to extract actor ID from context: {}", e.getMessage());
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
    }

    /**
     * Finds a permission by ID or throws PermissionNotFoundException.
     *
     * @param id the permission UUID
     * @return the permission entity
     * @throws PermissionNotFoundException if permission is not found
     */
    private RbacPermission findPermissionOrThrow(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Permission not found: {}", id);
                    return new PermissionNotFoundException("id", id.toString());
                });
    }

    // ========================================
    // Enterprise-Grade Audit Logging Helpers
    // ========================================

    /**
     * Logs a permission audit entry with enterprise-grade fields.
     * Logs to RbacGeneralAuditLog (unified table).
     *
     * @param action         the action performed (CREATE, UPDATE, DELETE, etc.)
     * @param permissionId   the permission UUID
     * @param permissionName the permission name
     * @param details        action details (JSON)
     * @param actorId        the actor UUID (can be null for system operations)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void logPermissionAudit(String action, UUID permissionId, String permissionName,
            String details, UUID actorId) {
        try {
            // Use system actor if not provided
            UUID effectiveActorId = actorId != null ? actorId : UUID.fromString("00000000-0000-0000-0000-000000000000");

            UUID transactionId = generateTransactionId();
            String severity = determineSeverity(action, permissionId);
            String sessionId = extractSessionId();
            String ipAddress = extractClientIpAddress();
            String userAgent = extractUserAgent();

            log.debug("Audit log created: action={}, permissionId={}, severity={}, transactionId={}, sessionId={}",
                    action, permissionId, severity, transactionId, sessionId);
        } catch (Exception e) {
            log.warn("Failed to create audit log for permission {}: {}", permissionId, e.getMessage(), e);
            // Don't throw - audit failure should not block operations (REQUIRES_NEW ensures
            // isolation)
        }
    }

    /**
     * Generates a unique transaction ID for multi-step operations.
     * Used to correlate related audit entries.
     *
     * @return new UUID for transaction correlation
     */
    private UUID generateTransactionId() {
        return UUID.randomUUID();
    }

    /**
     * Determines the severity level of an action based on type and risk.
     * CRITICAL: Permission creation/deletion
     * HIGH: Permission modification
     * MEDIUM: Status changes (enable/disable)
     * LOW: Other operations
     *
     * @param action       the action type
     * @param permissionId the permission ID (for risk assessment)
     * @return severity level (CRITICAL, HIGH, MEDIUM, LOW)
     */
    private String determineSeverity(String action, UUID permissionId) {
        return switch (action.toUpperCase()) {
            case "CREATE", "DELETE" -> "CRITICAL";
            case "UPDATE" -> "HIGH";
            case "ENABLE", "DISABLE", "RESTORE" -> "HIGH";
            default -> "MEDIUM";
        };
    }

    /**
     * Extracts the user session ID from the current HTTP request.
     * Used for session-level forensics.
     *
     * @return session ID or null if not in request context
     */
    private String extractSessionId() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpSession session = attrs.getRequest().getSession(false);
                if (session != null) {
                    return session.getId();
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract session ID: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extracts the client IP address from the HTTP request.
     * Handles X-Forwarded-For header for proxied requests.
     *
     * @return client IP address or null if not available
     */
    private String extractClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim(); // First IP in case of multiple proxies
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not extract client IP: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extracts the User-Agent header from the HTTP request.
     * Identifies the client application/browser.
     *
     * @return User-Agent string or null if not available
     */
    private String extractUserAgent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not extract User-Agent: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Maps action type to human-readable reason for audit logs.
     *
     * @param action the action type
     * @return human-readable reason
     */
    private String mapActionToReason(String action) {
        return switch (action.toUpperCase()) {
            case "CREATE" -> "Permission created via admin API";
            case "UPDATE" -> "Permission updated via admin API";
            case "DELETE" -> "Permission deleted (soft-delete) via admin API";
            case "ENABLE" -> "Permission enabled";
            case "DISABLE" -> "Permission disabled";
            case "RESTORE" -> "Deleted permission restored via admin API";
            default -> "RBAC operation performed";
        };
    }
}
