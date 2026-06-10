package com.mmva.newsapp.infrastructure.rbac.role.core.service;

import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleAssignPermissionsRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.BulkRoleActionRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.BulkRoleActionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleCloneRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleExistsResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleResponseDto;
import com.mmva.newsapp.infrastructure.common.exception.DuplicateResourceException;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.mmva.newsapp.infrastructure.rbac.exception.PermissionNotFoundException;
import com.mmva.newsapp.infrastructure.rbac.exception.RoleNotFoundException;
import com.mmva.newsapp.infrastructure.rbac.role.core.mapper.RoleMapper;
import com.mmva.newsapp.domain.adminuser.model.core.AdminUser;
import com.mmva.newsapp.infrastructure.rbac.permission.core.model.RbacPermission;
import com.mmva.newsapp.infrastructure.rbac.role.core.model.RbacRole;
import com.mmva.newsapp.infrastructure.rbac.role.audit.model.RbacRoleAuditLog;
import com.mmva.newsapp.domain.adminuser.repository.core.AdminUserRepository;
import com.mmva.newsapp.infrastructure.common.audit.repository.SoftDeleteSpec;
import com.mmva.newsapp.infrastructure.rbac.permission.core.repository.PermissionRepository;
import com.mmva.newsapp.infrastructure.rbac.role.core.repository.RoleRepository;
import com.mmva.newsapp.infrastructure.rbac.role.audit.repository.RoleAuditLogRepository;
import com.mmva.newsapp.infrastructure.common.audit.service.AuditingUtility;
import com.mmva.newsapp.infrastructure.rbac.audit.constants.RbacAuditActions;
import com.mmva.newsapp.infrastructure.rbac.audit.constants.RbacAuditDomain;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the RoleService interface.
 *
 * <p>
 * Provides comprehensive role management functionality including:
 * <ul>
 * <li>Role CRUD operations with audit logging</li>
 * <li>Role status management (activate/deactivate)</li>
 * <li>Role-permission relationship management</li>
 * <li>Role search and cloning</li>
 * <li>Bulk operations for multiple roles</li>
 * </ul>
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    // ========================================
    // Dependencies
    // ========================================

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final AdminUserRepository adminUserRepository;
    private final PermissionRepository permissionRepository;
    private final RoleAuditLogRepository roleAuditLogRepository;
    private final AuditingUtility auditingUtility;

    // ========================================
    // Role CRUD Operations
    // ========================================

    @Override
    @Transactional
    public RoleResponseDto createRole(RoleRequestDto dto, RequestClientInfoDto clientInfo) {
        log.info("Creating role with name: {}", dto.getRoleName());

        if (roleRepository.existsByRoleName(dto.getRoleName())) {
            log.warn("Role creation failed: role name '{}' already exists", dto.getRoleName());
            throw new DuplicateResourceException("Role", "name", dto.getRoleName());
        }

        RbacRole role = roleMapper.toEntity(dto);
        role.setCreatedAt(Instant.now());
        role.setIsActive(true);
        role.setCreatedBy(dto.getAdminId());

        try {
            RbacRole saved = roleRepository.save(role);
            log.info("Role '{}' created successfully with ID: {}", dto.getRoleName(), saved.getRoleId());

            // ONE-LINE AUDIT - clientInfo passed from controller
            auditingUtility.audit(
                    "RBAC",
                    dto.getAdminId(),
                    RbacAuditActions.ROLE_CREATED,
                    saved.getRoleId(),
                    saved.getRoleName(),
                    "Role created via admin API",
                    clientInfo,
                    "CRITICAL",
                    com.mmva.newsapp.infrastructure.rbac.role.audit.model.RbacRoleAuditLog.class,
                    roleAuditLogRepository);

            return roleMapper.toResponseDto(saved);
        } catch (Exception e) {
            log.error("Failed to create role '{}': {}", dto.getRoleName(), e.getMessage());
            throw e;
        }
    }

    @Override
    public RoleResponseDto getRoleById(UUID id, UUID userId) {
        log.debug("Fetching role by ID: {}", id);

        RbacRole role = findRoleOrThrow(id);

        log.debug("Role fetched successfully: {}", id);
        return roleMapper.toResponseDto(role);
    }

    @Override
    public Page<RoleResponseDto> getAllRoles(UUID userId, Pageable pageable) {
        log.debug("Fetching all roles (including deleted) - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Admin API: Use SoftDeleteSpec.includeDeleted() to see all records
        Page<RbacRole> page = roleRepository.findAll(SoftDeleteSpec.includeDeleted(), pageable);

        log.debug("Retrieved {} roles on page {}", page.getNumberOfElements(), pageable.getPageNumber());
        return page.map(roleMapper::toResponseDto);
    }

    /**
     * Get only active (non-deleted) roles.
     * Use for public-facing APIs or dropdowns.
     *
     * @param pageable pagination info
     * @return page of active roles
     */
    @Override
    public Page<RoleResponseDto> getActiveRoles(Pageable pageable) {
        log.debug("Fetching active roles only - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Public API: Use SoftDeleteSpec.notDeleted() to exclude soft-deleted
        Page<RbacRole> page = roleRepository.findAll(SoftDeleteSpec.notDeleted(), pageable);

        log.debug("Retrieved {} active roles on page {}", page.getNumberOfElements(), pageable.getPageNumber());
        return page.map(roleMapper::toResponseDto);
    }

    /**
     * Get only soft-deleted roles for admindashboard restore listing.
     *
     * @param pageable pagination info
     * @return page of deleted roles
     */
    @Override
    public Page<RoleResponseDto> getDeletedRoles(Pageable pageable) {
        log.debug("Fetching deleted roles only - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Admin restore listing: Use SoftDeleteSpec.onlyDeleted()
        Page<RbacRole> page = roleRepository.findAll(SoftDeleteSpec.onlyDeleted(), pageable);

        log.debug("Retrieved {} deleted roles on page {}", page.getNumberOfElements(), pageable.getPageNumber());
        return page.map(roleMapper::toResponseDto);
    }

    @Override
    @Transactional
    public RoleResponseDto updateRole(UUID id, RoleRequestDto dto, RequestClientInfoDto clientInfo) {
        log.info("Updating role ID: {}", id);

        RbacRole role = findRoleOrThrow(id);
        String oldName = role.getRoleName();

        role.setRoleName(dto.getRoleName());
        role.setRoleDescription(dto.getRoleDescription());
        role.setIsActive(dto.getIsActive());
        role.setUpdatedAt(Instant.now());
        role.setUpdatedBy(dto.getAdminId());

        try {
            RbacRole updated = roleRepository.save(role);
            log.info("Role {} updated successfully", id);

            // ONE-LINE AUDIT - clientInfo passed from controller
            auditingUtility.audit(
                    "RBAC",
                    dto.getAdminId(),
                    RbacAuditActions.ROLE_UPDATED,
                    id,
                    updated.getRoleName(),
                    "Role updated via admin API",
                    clientInfo,
                    "HIGH",
                    com.mmva.newsapp.infrastructure.rbac.role.audit.model.RbacRoleAuditLog.class,
                    roleAuditLogRepository);

            return roleMapper.toResponseDto(updated);
        } catch (Exception e) {
            log.error("Failed to update role {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteRole(UUID id, UUID userId, RequestClientInfoDto clientInfo) {
        log.info("Deleting role ID: {}", id);

        RbacRole role = findRoleOrThrow(id);
        String roleName = role.getRoleName();

        role.setDeletedAt(Instant.now());
        role.setDeletedBy(userId);
        role.setIsActive(false);

        try {
            roleRepository.save(role);
            log.info("Role {} soft deleted successfully", id);

            // ONE-LINE AUDIT - clientInfo passed from controller
            auditingUtility.audit(
                    "RBAC",
                    userId,
                    RbacAuditActions.ROLE_DELETED,
                    id,
                    roleName,
                    "Role soft-deleted via admin API",
                    clientInfo,
                    "CRITICAL",
                    com.mmva.newsapp.infrastructure.rbac.role.audit.model.RbacRoleAuditLog.class,
                    roleAuditLogRepository);
        } catch (Exception e) {
            log.error("Failed to delete role {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Restores a soft-deleted role.
     * Uses Specification to find deleted records.
     *
     * @param id     the role UUID
     * @param userId the admindashboard user performing the restore
     * @return the restored role
     */
    @Override
    @Transactional
    public RoleResponseDto restoreRole(UUID id, UUID userId, RequestClientInfoDto clientInfo) {
        log.info("Restoring role ID: {}", id);

        // Use Specification to find deleted role
        RbacRole role = roleRepository.findOne(
                SoftDeleteSpec.<RbacRole>onlyDeleted()
                        .and((root, query, cb) -> cb.equal(root.get("roleId"), id)))
                .orElseThrow(() -> {
                    log.warn("Deleted role not found: {}", id);
                    return new RoleNotFoundException("id", id.toString());
                });

        String roleName = role.getRoleName();
        role.setDeletedAt(null);
        role.setDeletedBy(null);
        role.setIsActive(true);
        role.setUpdatedAt(Instant.now());
        role.setUpdatedBy(userId);

        try {
            RbacRole restored = roleRepository.save(role);
            log.info("Role {} restored successfully", id);

            // ONE-LINE AUDIT - clientInfo passed from controller
            auditingUtility.audit(
                    "RBAC",
                    userId,
                    RbacAuditActions.ROLE_RESTORED,
                    id,
                    roleName,
                    "Role restored from soft-deleted state via admin API",
                    clientInfo,
                    "CRITICAL",
                    com.mmva.newsapp.infrastructure.rbac.role.audit.model.RbacRoleAuditLog.class,
                    roleAuditLogRepository);

            return roleMapper.toResponseDto(restored);
        } catch (Exception e) {
            log.error("Failed to restore role {}: {}", id, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Role Status Operations
    // ========================================

    @Override
    @Transactional
    public void activateRole(UUID roleId, UUID adminUserId) {
        log.info("Activating role ID: {}", roleId);

        RbacRole role = findRoleOrThrow(roleId);
        role.setIsActive(true);
        role.setUpdatedAt(Instant.now());
        role.setUpdatedBy(adminUserId);

        try {
            roleRepository.save(role);
            log.info("Role {} activated successfully", roleId);
        } catch (Exception e) {
            log.error("Failed to activate role {}: {}", roleId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deactivateRole(UUID roleId, UUID adminUserId) {
        log.info("Deactivating role ID: {}", roleId);

        RbacRole role = findRoleOrThrow(roleId);
        role.setIsActive(false);
        role.setUpdatedAt(Instant.now());
        role.setUpdatedBy(adminUserId);

        try {
            roleRepository.save(role);
            log.info("Role {} deactivated successfully", roleId);
        } catch (Exception e) {
            log.error("Failed to deactivate role {}: {}", roleId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public BulkRoleActionResponseDto bulkActivateRoles(BulkRoleActionRequestDto request) {
        log.info("Bulk activating {} roles", request.getRoleIds().size());

        List<UUID> successfulIds = new ArrayList<>();
        List<BulkRoleActionResponseDto.FailedRoleOperation> failedOps = new ArrayList<>();

        for (UUID roleId : request.getRoleIds()) {
            try {
                RbacRole role = roleRepository.findById(roleId).orElse(null);
                if (role == null) {
                    failedOps.add(BulkRoleActionResponseDto.FailedRoleOperation.builder()
                            .roleId(roleId)
                            .reason("Role not found")
                            .build());
                    continue;
                }
                if (Boolean.TRUE.equals(role.getIsActive())) {
                    failedOps.add(BulkRoleActionResponseDto.FailedRoleOperation.builder()
                            .roleId(roleId)
                            .reason("Role is already active")
                            .build());
                    continue;
                }
                role.setIsActive(true);
                role.setUpdatedAt(Instant.now());
                role.setUpdatedBy(request.getAdminId());
                roleRepository.save(role);
                successfulIds.add(roleId);

                // NEW UNIFIED AUDIT PATTERN (TODO: Add in Phase 2.1-B)
                // Use AuditingUtility.persistAudit() with RbacAuditActions.ROLE_ACTIVATED
            } catch (Exception e) {
                failedOps.add(BulkRoleActionResponseDto.FailedRoleOperation.builder()
                        .roleId(roleId)
                        .reason(e.getMessage())
                        .build());
            }
        }

        log.info("Bulk activate completed: {} successful, {} failed", successfulIds.size(), failedOps.size());

        return BulkRoleActionResponseDto.builder()
                .action("BULK_ACTIVATE")
                .totalProcessed(request.getRoleIds().size())
                .successCount(successfulIds.size())
                .failedCount(failedOps.size())
                .successfulRoleIds(successfulIds)
                .failedOperations(failedOps)
                .timestamp(Instant.now().toString())
                .performedBy(request.getAdminId())
                .build();
    }

    @Override
    @Transactional
    public BulkRoleActionResponseDto bulkDeactivateRoles(BulkRoleActionRequestDto request) {
        log.info("Bulk deactivating {} roles", request.getRoleIds().size());

        List<UUID> successfulIds = new ArrayList<>();
        List<BulkRoleActionResponseDto.FailedRoleOperation> failedOps = new ArrayList<>();

        for (UUID roleId : request.getRoleIds()) {
            try {
                RbacRole role = roleRepository.findById(roleId).orElse(null);
                if (role == null) {
                    failedOps.add(BulkRoleActionResponseDto.FailedRoleOperation.builder()
                            .roleId(roleId)
                            .reason("Role not found")
                            .build());
                    continue;
                }
                if (Boolean.FALSE.equals(role.getIsActive())) {
                    failedOps.add(BulkRoleActionResponseDto.FailedRoleOperation.builder()
                            .roleId(roleId)
                            .reason("Role is already inactive")
                            .build());
                    continue;
                }
                role.setIsActive(false);
                role.setUpdatedAt(Instant.now());
                role.setUpdatedBy(request.getAdminId());
                roleRepository.save(role);
                successfulIds.add(roleId);

                // NEW UNIFIED AUDIT PATTERN (TODO: Add in Phase 2.1-B)
                // Use AuditingUtility.persistAudit() with RbacAuditActions.ROLE_DEACTIVATED
            } catch (Exception e) {
                failedOps.add(BulkRoleActionResponseDto.FailedRoleOperation.builder()
                        .roleId(roleId)
                        .reason(e.getMessage())
                        .build());
            }
        }

        log.info("Bulk deactivate completed: {} successful, {} failed", successfulIds.size(), failedOps.size());

        return BulkRoleActionResponseDto.builder()
                .action("BULK_DEACTIVATE")
                .totalProcessed(request.getRoleIds().size())
                .successCount(successfulIds.size())
                .failedCount(failedOps.size())
                .successfulRoleIds(successfulIds)
                .failedOperations(failedOps)
                .timestamp(Instant.now().toString())
                .performedBy(request.getAdminId())
                .build();
    }

    // ========================================
    // Role Search & Lookup Operations
    // ========================================

    @Override
    public List<RoleResponseDto> searchRoles(String query) {
        log.debug("Searching roles with query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            log.debug("Search query is empty, returning empty list");
            return Collections.emptyList();
        }

        List<RoleResponseDto> roles = roleRepository.searchByNameOrDescription(query.trim()).stream()
                .map(roleMapper::toResponseDto)
                .toList();

        log.debug("Found {} roles matching query: {}", roles.size(), query);
        return roles;
    }

    @Override
    public RoleResponseDto getRoleByName(String name, UUID userId) {
        log.debug("Fetching role by name: {}", name);

        RbacRole role = roleRepository.findByRoleName(name)
                .orElseThrow(() -> {
                    log.warn("Role not found with name: {}", name);
                    return new RoleNotFoundException("name", name);
                });

        log.debug("Role fetched successfully by name: {}", name);
        return roleMapper.toResponseDto(role);
    }

    @Override
    public RoleExistsResponseDto checkRoleNameExists(String roleName) {
        log.debug("Checking if role name exists: {}", roleName);

        boolean exists = roleRepository.existsByRoleName(roleName);
        String message = exists
                ? String.format("Role name '%s' already exists", roleName)
                : String.format("Role name '%s' is available", roleName);

        log.debug("Role name '{}' exists: {}", roleName, exists);
        return RoleExistsResponseDto.builder()
                .roleName(roleName)
                .exists(exists)
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public RoleResponseDto cloneRole(UUID sourceRoleId, RoleCloneRequestDto request) {
        log.info("Cloning role {} to '{}'", sourceRoleId, request.getNewRoleName());

        // Use findRoleWithPermissionsOrThrow if copying permissions, otherwise regular
        // find
        RbacRole sourceRole = Boolean.TRUE.equals(request.getCopyPermissions())
                ? findRoleWithPermissionsOrThrow(sourceRoleId)
                : findRoleOrThrow(sourceRoleId);

        if (roleRepository.existsByRoleName(request.getNewRoleName())) {
            log.warn("Role clone failed: role name '{}' already exists", request.getNewRoleName());
            throw new DuplicateResourceException("Role", "name", request.getNewRoleName());
        }

        RbacRole clonedRole = new RbacRole();
        clonedRole.setRoleName(request.getNewRoleName());
        clonedRole.setRoleDescription(request.getNewRoleDescription() != null
                ? request.getNewRoleDescription()
                : sourceRole.getRoleDescription());
        clonedRole.setIsActive(true);
        clonedRole.setCreatedAt(Instant.now());
        clonedRole.setCreatedBy(request.getAdminId());

        if (Boolean.TRUE.equals(request.getCopyPermissions()) && sourceRole.getPermissions() != null) {
            clonedRole.setPermissions(new HashSet<>(sourceRole.getPermissions()));
        }

        try {
            RbacRole saved = roleRepository.save(clonedRole);
            log.info("Role cloned successfully from {} to {} with ID: {}",
                    sourceRoleId, request.getNewRoleName(), saved.getRoleId());

            // NEW UNIFIED AUDIT PATTERN (TODO: Add in Phase 2.1-C)
            // Use AuditingUtility.persistAudit() with RbacAuditActions for ROLE_CLONED

            return roleMapper.toResponseDto(saved);
        } catch (Exception e) {
            log.error("Failed to clone role {}: {}", sourceRoleId, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Role-Permission Relationship Operations
    // ========================================

    @Override
    public Set<RbacPermission> getPermissionsOfRole(UUID roleId) {
        log.debug("Fetching permissions for role: {}", roleId);

        RbacRole role = findRoleWithPermissionsOrThrow(roleId);

        log.debug("Retrieved {} permissions for role: {}", role.getPermissions().size(), roleId);
        return role.getPermissions();
    }

    @Override
    @Transactional
    public void assignPermissionsToRole(UUID roleId, RoleAssignPermissionsRequestDto requestDto, UUID adminUserId) {
        log.info("Assigning permissions to role: {}", roleId);

        RbacRole role = findRoleOrThrow(roleId);

        if (requestDto == null || requestDto.getPermissionIds() == null) {
            log.warn("Permission assignment failed: permission IDs not provided for role {}", roleId);
            throw new InvalidRequestException("permissions", "Permission IDs must be provided");
        }

        Set<RbacPermission> permissions = new HashSet<>();
        for (UUID permId : requestDto.getPermissionIds()) {
            RbacPermission permission = findPermissionOrThrow(permId);
            permissions.add(permission);
        }

        role.setPermissions(permissions);

        try {
            roleRepository.save(role);
            log.info("Assigned {} permissions to role {}", requestDto.getPermissionIds().size(), roleId);

            // NEW UNIFIED AUDIT PATTERN (TODO: Add in Phase 2.1-C)
            // Use AuditingUtility.persistAudit() with RbacAuditActions for
            // PERMISSION_GRANTED
        } catch (Exception e) {
            log.error("Failed to assign permissions to role {}: {}", roleId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void removePermissionFromRole(UUID roleId, UUID permissionId, UUID adminUserId) {
        log.info("Removing permission {} from role: {}", permissionId, roleId);

        RbacRole role = findRoleWithPermissionsOrThrow(roleId);
        RbacPermission permission = findPermissionOrThrow(permissionId);

        if (!role.getPermissions().contains(permission)) {
            log.warn("Role {} does not have permission {}", roleId, permissionId);
            throw new InvalidRequestException("permission", "Role does not have the specified permission");
        }

        role.getPermissions().remove(permission);
        role.setUpdatedAt(Instant.now());
        role.setUpdatedBy(adminUserId);

        try {
            roleRepository.save(role);
            log.info("Permission {} removed from role {}", permissionId, roleId);
        } catch (Exception e) {
            log.error("Failed to remove permission {} from role {}: {}", permissionId, roleId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void bulkAssignPermissions(UUID roleId, List<UUID> permissionIds, UUID adminUserId) {
        log.info("Bulk assigning {} permissions to role: {}", permissionIds.size(), roleId);

        RbacRole role = findRoleWithPermissionsOrThrow(roleId);

        for (UUID permId : permissionIds) {
            RbacPermission permission = findPermissionOrThrow(permId);
            role.getPermissions().add(permission);
        }

        role.setUpdatedAt(Instant.now());
        role.setUpdatedBy(adminUserId);

        try {
            roleRepository.save(role);
            log.info("Bulk assigned {} permissions to role {}", permissionIds.size(), roleId);
        } catch (Exception e) {
            log.error("Failed to bulk assign permissions to role {}: {}", roleId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void bulkRemovePermissions(UUID roleId, List<UUID> permissionIds, UUID adminUserId) {
        log.info("Bulk removing {} permissions from role: {}", permissionIds.size(), roleId);

        RbacRole role = findRoleWithPermissionsOrThrow(roleId);

        for (UUID permId : permissionIds) {
            RbacPermission permission = findPermissionOrThrow(permId);
            role.getPermissions().remove(permission);
        }

        role.setUpdatedAt(Instant.now());
        role.setUpdatedBy(adminUserId);

        try {
            roleRepository.save(role);
            log.info("Bulk removed {} permissions from role {}", permissionIds.size(), roleId);
        } catch (Exception e) {
            log.error("Failed to bulk remove permissions from role {}: {}", roleId, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<RoleResponseDto> getRolesByPermission(UUID permissionId) {
        log.debug("Fetching roles with permission: {}", permissionId);

        if (!permissionRepository.existsById(permissionId)) {
            log.warn("Permission not found: {}", permissionId);
            throw new PermissionNotFoundException("id", permissionId.toString());
        }

        List<RoleResponseDto> roles = roleRepository.findByPermissionId(permissionId).stream()
                .map(roleMapper::toResponseDto)
                .toList();

        log.debug("Found {} roles with permission: {}", roles.size(), permissionId);
        return roles;
    }

    // ========================================
    // Role Admin Users Operations
    // ========================================

    @Override
    public Page<AdminUserResponseDto> getUsersWithRole(UUID roleId, UUID userId, Pageable pageable) {
        log.debug("Fetching admindashboard users with role {} - page: {}, size: {}",
                roleId, pageable.getPageNumber(), pageable.getPageSize());

        if (!roleRepository.existsById(roleId)) {
            log.warn("Role not found: {}", roleId);
            throw new RoleNotFoundException("id", roleId.toString());
        }

        Page<AdminUser> usersPage = adminUserRepository.findByRoleId(roleId, pageable);

        log.debug("Found {} admindashboard users with role {}", usersPage.getTotalElements(), roleId);
        return usersPage.map(this::mapToAdminUserResponseDto);
    }

    // ========================================
    // Role Audit Log Operations
    // ========================================

    @Override
    public RbacRoleAuditLog createAuditLog(RbacRoleAuditLog auditLog) {
        log.info("Creating role audit log for role: {} action: {}", auditLog.getRoleId(), auditLog.getAction());

        try {
            RbacRoleAuditLog saved = roleAuditLogRepository.save(auditLog);
            log.info("Role audit log created successfully with ID: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Failed to create role audit log for role {}: {}", auditLog.getRoleId(), e.getMessage());
            throw e;
        }
    }

    @Override
    public List<RbacRoleAuditLog> getAuditLogsByRoleId(UUID roleId) {
        log.debug("Fetching audit logs for role: {}", roleId);

        List<RbacRoleAuditLog> logs = roleAuditLogRepository.findByRoleId(roleId);

        log.debug("Retrieved {} audit logs for role: {}", logs.size(), roleId);
        return logs;
    }

    @Override
    public List<RbacRoleAuditLog> filterAuditLogs(UUID roleId, String fromDate, String toDate, String action) {
        log.debug("Filtering audit logs for role {} - fromDate: {}, toDate: {}, action: {}",
                roleId, fromDate, toDate, action);

        if (!roleRepository.existsById(roleId)) {
            log.warn("Role not found: {}", roleId);
            throw new RoleNotFoundException("id", roleId.toString());
        }

        // For now, just fetch all logs for role and filter in memory
        // TODO: Implement proper date range filtering in repository
        List<RbacRoleAuditLog> logs = roleAuditLogRepository.findByRoleId(roleId);
        if (action != null) {
            logs = logs.stream().filter(l -> action.equals(l.getAction())).toList();
        }

        log.debug("Found {} filtered audit logs for role: {}", logs.size(), roleId);
        return logs;
    }

    @Override
    public Page<RbacRoleAuditLog> getAuditLogsPaginated(UUID roleId, String fromDate, String toDate,
            String action, Pageable pageable) {
        log.debug("Fetching paginated audit logs for role {} - page: {}, size: {}",
                roleId, pageable.getPageNumber(), pageable.getPageSize());

        if (!roleRepository.existsById(roleId)) {
            log.warn("Role not found: {}", roleId);
            throw new RoleNotFoundException("id", roleId.toString());
        }

        Page<RbacRoleAuditLog> logsPage = roleAuditLogRepository.findByRoleIdOrderByCreatedAtDesc(roleId, pageable);

        log.debug("Retrieved {} audit logs on page {} for role: {}",
                logsPage.getNumberOfElements(), pageable.getPageNumber(), roleId);
        return logsPage;
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Finds a role by ID or throws RoleNotFoundException.
     *
     * @param roleId the role UUID
     * @return the role entity
     * @throws RoleNotFoundException if role is not found
     */
    private RbacRole findRoleOrThrow(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Role not found: {}", roleId);
                    return new RoleNotFoundException("id", roleId.toString());
                });
    }

    /**
     * Finds a role by ID with permissions eagerly fetched, or throws
     * RoleNotFoundException.
     * Use this method when you need to access role.getPermissions() to avoid
     * LazyInitializationException.
     *
     * @param roleId the role UUID
     * @return the role entity with permissions loaded
     * @throws RoleNotFoundException if role is not found
     */
    private RbacRole findRoleWithPermissionsOrThrow(UUID roleId) {
        return roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> {
                    log.warn("Role not found: {}", roleId);
                    return new RoleNotFoundException("id", roleId.toString());
                });
    }

    /**
     * Finds a permission by ID or throws PermissionNotFoundException.
     *
     * @param permissionId the permission UUID
     * @return the permission entity
     * @throws PermissionNotFoundException if permission is not found
     */
    private RbacPermission findPermissionOrThrow(UUID permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> {
                    log.warn("Permission not found: {}", permissionId);
                    return new PermissionNotFoundException("id", permissionId.toString());
                });
    }

    /**
     * DEPRECATED: Use AuditingUtility.persistAudit() instead.
     * This method has been removed as part of Phase 2 cleanup.
     * All audit logging now goes through the unified AuditingUtility service.
     */

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
     * CRITICAL: Role creation/deletion
     * HIGH: Role modification, permission changes
     * MEDIUM: Status changes (activate/deactivate)
     * LOW: Other operations
     *
     * @param action the action type
     * @param roleId the role ID (for risk assessment)
     * @return severity level (CRITICAL, HIGH, MEDIUM, LOW)
     */
    private String determineSeverity(String action, UUID roleId) {
        return switch (action.toUpperCase()) {
            case "CREATE", "DELETE", "RESTORE" -> "CRITICAL";
            case "UPDATE", "ASSIGN_PERMISSIONS", "CLONE" -> "HIGH";
            case "ACTIVATE", "DEACTIVATE" -> "HIGH";
            case "BULK_ACTIVATE", "BULK_DEACTIVATE" -> "MEDIUM";
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    /**
     * Maps an action to a reason description.
     * (Not currently used but kept for future audit logging enhancements)
     */
    @SuppressWarnings("unused")
    private String mapActionToReason(String action) {
        return switch (action.toUpperCase()) {
            case "CREATE" -> "Role created via admin API";
            case "UPDATE" -> "Role updated via admin API";
            case "DELETE" -> "Role deleted (soft-delete) via admin API";
            case "RESTORE" -> "Deleted role restored via admin API";
            case "ACTIVATE" -> "Role activated";
            case "DEACTIVATE" -> "Role deactivated";
            case "CLONE" -> "Role cloned from existing role";
            case "ASSIGN_PERMISSIONS" -> "Permissions assigned to role";
            case "BULK_ACTIVATE" -> "Multiple roles activated (bulk operation)";
            case "BULK_DEACTIVATE" -> "Multiple roles deactivated (bulk operation)";
            default -> "RBAC operation performed";
        };
    }

    /**
     * Maps AdminUser entity to AdminUserResponseDto.
     *
     * @param user the admindashboard user entity
     * @return the admindashboard user response DTO
     */
    private AdminUserResponseDto mapToAdminUserResponseDto(AdminUser user) {
        return AdminUserResponseDto.builder()
                .adminUsersId(user.getAdminUsersId())
                .adminUsersUsername(user.getAdminUsersUsername())
                .adminUsersEmail(user.getAdminUsersEmail())
                .adminUsersFirstName(user.getAdminUsersFirstName())
                .adminUsersLastName(user.getAdminUsersLastName())
                .adminUsersFullName(user.getAdminUsersFullName())
                .adminUsersRoleId(user.getRole() != null ? user.getRole().getRoleId() : null)
                .adminUsersRoleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .adminUsersStatus(user.getAdminUsersStatus())
                .adminUsersLastLogin(
                        user.getAdminUsersLastLogin() != null ? user.getAdminUsersLastLogin().toString() : null)
                .adminUsersAuthProvider(user.getAdminUsersAuthProvider())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null)
                .deletedAt(user.getDeletedAt() != null ? user.getDeletedAt().toString() : null)
                .build();
    }
}
