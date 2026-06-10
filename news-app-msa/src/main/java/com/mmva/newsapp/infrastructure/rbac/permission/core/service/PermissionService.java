package com.mmva.newsapp.infrastructure.rbac.permission.core.service;

import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionRequestDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.permission.audit.model.RbacPermissionAuditLog;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for permission management operations.
 *
 * <p>
 * Provides methods for:
 * <ul>
 * <li>Permission CRUD operations</li>
 * <li>Permission status management (enable/disable/restore)</li>
 * <li>Permission audit logging</li>
 * </ul>
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
public interface PermissionService {

    // ========================================
    // Permission CRUD Operations
    // ========================================

    /**
     * Creates a new permission.
     *
     * @param dto        the permission request data
     * @param clientInfo HTTP request context (IP, user agent, etc.)
     * @return the created permission response
     */
    PermissionResponseDto createPermission(PermissionRequestDto dto, RequestClientInfoDto clientInfo);

    /**
     * Retrieves all permissions with pagination.
     *
     * @param pageable pagination parameters
     * @return paginated permission responses
     */
    Page<PermissionResponseDto> getAllPermissions(Pageable pageable);

    /**
     * Retrieves a permission by its ID.
     *
     * @param id the permission UUID
     * @return the permission response
     */
    PermissionResponseDto getPermissionById(UUID id);

    /**
     * Updates an existing permission.
     *
     * @param id         the permission UUID
     * @param dto        the permission update data
     * @param clientInfo HTTP request context (IP, user agent, etc.)
     * @return the updated permission response
     */
    PermissionResponseDto updatePermission(UUID id, PermissionRequestDto dto, RequestClientInfoDto clientInfo);

    /**
     * Soft deletes a permission.
     *
     * @param id         the permission UUID
     * @param clientInfo HTTP request context (IP, user agent, etc.)
     */
    void deletePermission(UUID id, RequestClientInfoDto clientInfo);

    /**
     * Retrieves only active (non-deleted) permissions with pagination.
     * Use for role assignment dropdowns.
     *
     * @param pageable pagination parameters
     * @return paginated active permission responses
     */
    Page<PermissionResponseDto> getActivePermissions(Pageable pageable);

    /**
     * Retrieves only soft-deleted permissions with pagination.
     * Use for admindashboard restore listing.
     *
     * @param pageable pagination parameters
     * @return paginated deleted permission responses
     */
    Page<PermissionResponseDto> getDeletedPermissions(Pageable pageable);

    // ========================================
    // Permission Status Operations
    // ========================================

    /**
     * Enables a permission.
     *
     * @param id the permission UUID
     * @return the enabled permission response
     */
    PermissionResponseDto enablePermission(UUID id);

    /**
     * Disables a permission.
     *
     * @param id the permission UUID
     * @return the disabled permission response
     */
    PermissionResponseDto disablePermission(UUID id);

    /**
     * Restores a soft-deleted permission.
     *
     * @param id         the permission UUID
     * @param clientInfo HTTP request context (IP, user agent, etc.)
     * @return the restored permission response
     */
    PermissionResponseDto restorePermission(UUID id, RequestClientInfoDto clientInfo);

    // ========================================
    // Permission Audit Log Operations
    // ========================================

    /**
     * Creates an audit log entry for a permission action.
     *
     * @param auditLog the audit log entity
     * @return the created audit log
     */
    RbacPermissionAuditLog createAuditLog(RbacPermissionAuditLog auditLog);

    /**
     * Retrieves all audit logs for a specific permission.
     *
     * @param permissionId the permission UUID
     * @return list of audit logs
     */
    List<RbacPermissionAuditLog> getAuditLogsByPermissionId(UUID permissionId);
}
