package com.mmva.newsapp.infrastructure.rbac.role.core.service;

import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleAssignPermissionsRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.BulkRoleActionRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.BulkRoleActionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleCloneRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleExistsResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleResponseDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.model.RbacPermission;
import com.mmva.newsapp.infrastructure.rbac.role.audit.model.RbacRoleAuditLog;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for role management operations.
 *
 * <p>
 * Provides methods for:
 * <ul>
 * <li>Role CRUD operations</li>
 * <li>Role status management (activate/deactivate)</li>
 * <li>Role-permission relationship management</li>
 * <li>Role search and lookup</li>
 * <li>Role audit logging</li>
 * </ul>
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
public interface RoleService {

        // ========================================
        // Role CRUD Operations
        // ========================================

        /**
         * Creates a new role.
         *
         * @param dto        the role request data
         * @param clientInfo the client request information (IP, UserAgent, etc.) from
         *                   controller
         * @return the created role response
         */
        RoleResponseDto createRole(RoleRequestDto dto, RequestClientInfoDto clientInfo);

        /**
         * Retrieves a role by its ID.
         *
         * @param id     the role UUID
         * @param userId the requesting user UUID
         * @return the role response
         */
        RoleResponseDto getRoleById(UUID id, UUID userId);

        /**
         * Retrieves all roles with pagination.
         *
         * @param userId   the requesting user UUID
         * @param pageable pagination parameters
         * @return paginated role responses
         */
        Page<RoleResponseDto> getAllRoles(UUID userId, Pageable pageable);

        /**
         * Updates an existing role.
         *
         * @param id         the role UUID
         * @param dto        the role update data
         * @param clientInfo the client request information (IP, UserAgent, etc.) from
         *                   controller
         * @return the updated role response
         */
        RoleResponseDto updateRole(UUID id, RoleRequestDto dto, RequestClientInfoDto clientInfo);

        /**
         * Soft deletes a role.
         *
         * @param id         the role UUID
         * @param userId     the requesting user UUID
         * @param clientInfo the client request information (IP, UserAgent, etc.) from
         *                   controller
         */
        void deleteRole(UUID id, UUID userId, RequestClientInfoDto clientInfo);

        /**
         * Restores a soft-deleted role.
         *
         * @param id         the role UUID
         * @param userId     the admin user UUID performing the restore
         * @param clientInfo the client request information (IP, UserAgent, etc.) from
         *                   controller
         * @return the restored role response
         */
        RoleResponseDto restoreRole(UUID id, UUID userId, RequestClientInfoDto clientInfo);

        /**
         * Retrieves only active (non-deleted) roles with pagination.
         * Use for public-facing APIs or role selection dropdowns.
         *
         * @param pageable pagination parameters
         * @return paginated active role responses
         */
        Page<RoleResponseDto> getActiveRoles(Pageable pageable);

        /**
         * Retrieves only soft-deleted roles with pagination.
         * Use for admindashboard restore listing.
         *
         * @param pageable pagination parameters
         * @return paginated deleted role responses
         */
        Page<RoleResponseDto> getDeletedRoles(Pageable pageable);

        // ========================================
        // Role Status Operations
        // ========================================

        /**
         * Activates a role.
         *
         * @param roleId      the role UUID
         * @param adminUserId the admindashboard user UUID
         */
        void activateRole(UUID roleId, UUID adminUserId);

        /**
         * Deactivates a role.
         *
         * @param roleId      the role UUID
         * @param adminUserId the admindashboard user UUID
         */
        void deactivateRole(UUID roleId, UUID adminUserId);

        /**
         * Bulk activates multiple roles.
         *
         * @param request the bulk action request
         * @return the bulk action response
         */
        BulkRoleActionResponseDto bulkActivateRoles(BulkRoleActionRequestDto request);

        /**
         * Bulk deactivates multiple roles.
         *
         * @param request the bulk action request
         * @return the bulk action response
         */
        BulkRoleActionResponseDto bulkDeactivateRoles(BulkRoleActionRequestDto request);

        // ========================================
        // Role Search & Lookup Operations
        // ========================================

        /**
         * Searches roles by name or description.
         *
         * @param query the search query
         * @return list of matching roles
         */
        List<RoleResponseDto> searchRoles(String query);

        /**
         * Retrieves a role by its name.
         *
         * @param name   the role name
         * @param userId the requesting user UUID
         * @return the role response
         */
        RoleResponseDto getRoleByName(String name, UUID userId);

        /**
         * Checks if a role name already exists.
         *
         * @param roleName the role name to check
         * @return the exists response
         */
        RoleExistsResponseDto checkRoleNameExists(String roleName);

        /**
         * Clones an existing role with a new name.
         *
         * @param sourceRoleId the source role UUID
         * @param request      the clone request with new name
         * @return the cloned role response
         */
        RoleResponseDto cloneRole(UUID sourceRoleId, RoleCloneRequestDto request);

        // ========================================
        // Role-Permission Relationship Operations
        // ========================================

        /**
         * Retrieves all permissions assigned to a role.
         *
         * @param roleId the role UUID
         * @return set of permissions
         */
        Set<RbacPermission> getPermissionsOfRole(UUID roleId);

        /**
         * Assigns permissions to a role.
         *
         * @param roleId      the role UUID
         * @param requestDto  the assign permissions request
         * @param adminUserId the admindashboard user UUID
         */
        void assignPermissionsToRole(UUID roleId, RoleAssignPermissionsRequestDto requestDto, UUID adminUserId);

        /**
         * Removes a specific permission from a role.
         *
         * @param roleId       the role UUID
         * @param permissionId the permission UUID
         * @param adminUserId  the admindashboard user UUID
         */
        void removePermissionFromRole(UUID roleId, UUID permissionId, UUID adminUserId);

        /**
         * Bulk assigns permissions to a role.
         *
         * @param roleId        the role UUID
         * @param permissionIds list of permission UUIDs
         * @param adminUserId   the admindashboard user UUID
         */
        void bulkAssignPermissions(UUID roleId, List<UUID> permissionIds, UUID adminUserId);

        /**
         * Bulk removes permissions from a role.
         *
         * @param roleId        the role UUID
         * @param permissionIds list of permission UUIDs
         * @param adminUserId   the admindashboard user UUID
         */
        void bulkRemovePermissions(UUID roleId, List<UUID> permissionIds, UUID adminUserId);

        /**
         * Retrieves all roles that have a specific permission.
         *
         * @param permissionId the permission UUID
         * @return list of roles with the permission
         */
        List<RoleResponseDto> getRolesByPermission(UUID permissionId);

        // ========================================
        // Role Admin Users Operations
        // ========================================

        /**
         * Retrieves admindashboard users assigned to a role.
         *
         * @param roleId   the role UUID
         * @param userId   the requesting user UUID
         * @param pageable pagination parameters
         * @return paginated admindashboard user responses
         */
        Page<AdminUserResponseDto> getUsersWithRole(UUID roleId, UUID userId, Pageable pageable);

        // ========================================
        // Role Audit Log Operations
        // ========================================

        /**
         * Creates an audit log entry for a role action.
         *
         * @param auditLog the audit log entity
         * @return the created audit log
         */
        RbacRoleAuditLog createAuditLog(RbacRoleAuditLog auditLog);

        /**
         * Retrieves all audit logs for a specific role.
         *
         * @param roleId the role UUID
         * @return list of audit logs
         */
        List<RbacRoleAuditLog> getAuditLogsByRoleId(UUID roleId);

        /**
         * Filters audit logs by date range and/or action.
         *
         * @param roleId   the role UUID
         * @param fromDate start date filter (yyyy-MM-dd)
         * @param toDate   end date filter (yyyy-MM-dd)
         * @param action   action type filter
         * @return list of filtered audit logs
         */
        List<RbacRoleAuditLog> filterAuditLogs(UUID roleId, String fromDate, String toDate, String action);

        /**
         * Retrieves paginated audit logs for a role.
         *
         * @param roleId   the role UUID
         * @param fromDate start date filter (yyyy-MM-dd)
         * @param toDate   end date filter (yyyy-MM-dd)
         * @param action   action type filter
         * @param pageable pagination parameters
         * @return paginated audit logs
         */
        Page<RbacRoleAuditLog> getAuditLogsPaginated(UUID roleId, String fromDate, String toDate,
                        String action, Pageable pageable);
}
