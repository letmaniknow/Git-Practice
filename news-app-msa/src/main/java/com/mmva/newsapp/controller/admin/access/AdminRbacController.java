package com.mmva.newsapp.controller.admin.access;

import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserResponseDto;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionRequestDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleAssignPermissionsRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleAssignPermissionsResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.BulkRoleActionRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.BulkRoleActionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RolePermissionBulkOperationResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleActionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleCloneRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleExistsResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleResponseDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.model.RbacPermission;
import com.mmva.newsapp.infrastructure.rbac.permission.audit.model.RbacPermissionAuditLog;
import com.mmva.newsapp.infrastructure.rbac.role.audit.model.RbacRoleAuditLog;
import com.mmva.newsapp.infrastructure.common.audit.dto.BaseAuditLogDto;
import com.mmva.newsapp.infrastructure.common.audit.mapper.CommonAuditLogMapper;
import com.mmva.newsapp.infrastructure.rbac.role.audit.service.RbacRoleAuditService;
import com.mmva.newsapp.infrastructure.rbac.permission.audit.service.RbacPermissionAuditService;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestInfoService;
import com.mmva.newsapp.domain.adminuser.service.validation.AdminValidationService;
import com.mmva.newsapp.infrastructure.rbac.permission.core.service.PermissionService;
import com.mmva.newsapp.infrastructure.rbac.role.core.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Admin API Controller for Role-Based Access Control (RBAC) Management.
 * 
 * <p>
 * Consolidated controller providing full CRUD operations for roles and
 * permissions:
 * </p>
 * 
 * <table border="1">
 * <caption>RBAC Management Endpoints</caption>
 * <tr>
 * <th>#</th>
 * <th>Method</th>
 * <th>Endpoint</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>POST</td>
 * <td>/api/v1/admin/access/roles</td>
 * <td>Create new role</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>GET</td>
 * <td>/api/v1/admin/access/roles</td>
 * <td>List all roles (paginated)</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>GET</td>
 * <td>/api/v1/admin/access/roles/{id}</td>
 * <td>Get role by ID</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>PUT</td>
 * <td>/api/v1/admin/access/roles/{id}</td>
 * <td>Update role</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>DELETE</td>
 * <td>/api/v1/admin/access/roles/{id}</td>
 * <td>Delete role</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>POST</td>
 * <td>/api/v1/admin/access/roles/{id}/permissions</td>
 * <td>Assign permissions to role</td>
 * </tr>
 * <tr>
 * <td>7</td>
 * <td>GET</td>
 * <td>/api/v1/admin/access/permissions</td>
 * <td>List all permissions</td>
 * </tr>
 * <tr>
 * <td>8</td>
 * <td>POST</td>
 * <td>/api/v1/admin/access/permissions</td>
 * <td>Create new permission</td>
 * </tr>
 * </table>
 * 
 * <p>
 * Following industry best practices:
 * </p>
 * <ul>
 * <li>Path prefix: /api/v1/admin/access</li>
 * <li>API versioning in path</li>
 * <li>Permissions as subresources of roles domain</li>
 * <li>Authentication required (enforced by security config)</li>
 * </ul>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/admin/access")
@RequiredArgsConstructor
@Tag(name = "Admin - Access Control (RBAC)", description = "Admin operations for role and permission management")
public class AdminRbacController {

        // ========================================
        // Constants
        // ========================================

        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_SIZE = "10";
        private static final int MAX_PAGE_SIZE = 100;

        // ========================================
        // Dependencies
        // ========================================

        private final RoleService roleService;
        private final PermissionService permissionService;
        private final AdminValidationService adminValidationService;
        private final RequestInfoService requestInfoService;
        private final RbacRoleAuditService rbacRoleAuditService;
        private final RbacPermissionAuditService rbacPermissionAuditService;
        private final CommonAuditLogMapper commonAuditLogMapper;

        // ========================================
        // Role CRUD Endpoints
        // ========================================

        @PostMapping("/roles")
        @Operation(summary = "1. Create new role", description = "Creates a new role with the provided details")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Role created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "409", description = "Role name already exists")
        })
        public ResponseEntity<ApiResponseDto<RoleResponseDto>> createRole(
                        @Valid @RequestBody RoleRequestDto dto,
                        HttpServletRequest request) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                dto.setAdminId(authenticatedAdminId); // Set admin ID from JWT token
                log.info("Admin [{}]: Creating role with name: {}", authenticatedAdminId, dto.getRoleName());

                // Extract clientInfo from request
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(request);

                RoleResponseDto response = roleService.createRole(dto, clientInfo);

                log.info("Admin [{}]: Role created successfully with ID: {}", authenticatedAdminId,
                                response.getRoleId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Role created successfully", response));
        }

        @GetMapping("/roles/{roleId}")
        @Operation(summary = "2. Get role by ID", description = "Retrieves a specific role by its UUID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Role retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<RoleResponseDto>> getRoleById(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching role by ID: {}", authenticatedAdminId, roleId);

                RoleResponseDto response = roleService.getRoleById(roleId, authenticatedAdminId);

                log.debug("Admin [{}]: Role retrieved successfully: {}", authenticatedAdminId, roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Role fetched successfully", response));
        }

        @GetMapping("/roles")
        @Operation(summary = "3. Get all roles", description = "Retrieves a paginated list of all roles")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<RoleResponseDto>>> getAllRoles(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size (1-100)") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching all roles - page: {}, size: {}", authenticatedAdminId, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<RoleResponseDto> response = roleService.getAllRoles(authenticatedAdminId, pageable);

                log.debug("Admin [{}]: Retrieved {} roles on page {}", authenticatedAdminId,
                                response.getNumberOfElements(),
                                page);
                return ResponseEntity.ok(ApiResponseDto.success("All roles fetched successfully", response));
        }

        @PutMapping("/roles/{roleId}")
        @Operation(summary = "4. Update role", description = "Updates an existing role with the provided details")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Role updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Role not found"),
                        @ApiResponse(responseCode = "409", description = "Role name already exists")
        })
        public ResponseEntity<ApiResponseDto<RoleResponseDto>> updateRole(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId,
                        @Valid @RequestBody RoleRequestDto dto,
                        HttpServletRequest request) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                dto.setAdminId(authenticatedAdminId); // Set admin ID from JWT token
                log.info("Admin [{}]: Updating role ID: {}", authenticatedAdminId, roleId);

                // Extract clientInfo from request
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(request);

                RoleResponseDto response = roleService.updateRole(roleId, dto, clientInfo);

                log.info("Admin [{}]: Role updated successfully: {}", authenticatedAdminId, roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Role updated successfully", response));
        }

        @DeleteMapping("/roles/{roleId}")
        @Operation(summary = "5. Delete role", description = "Soft deletes a role by its UUID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Role deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> deleteRole(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId,
                        HttpServletRequest request) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Deleting role ID: {}", authenticatedAdminId, roleId);

                // Extract clientInfo from request
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(request);

                roleService.deleteRole(roleId, authenticatedAdminId, clientInfo);

                log.info("Admin [{}]: Role deleted successfully: {}", authenticatedAdminId, roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Role deleted successfully", null));
        }

        // ========================================
        // Role Status Endpoints
        // ========================================

        @PatchMapping("/roles/{roleId}/activate")
        @Operation(summary = "6. Activate role", description = "Activates a role by its UUID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Role activated successfully"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<RoleActionResponseDto>> activateRole(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Activating role ID: {}", authenticatedAdminId, roleId);

                roleService.activateRole(roleId, authenticatedAdminId);
                RoleActionResponseDto response = RoleActionResponseDto.builder()
                                .roleId(roleId)
                                .action("ACTIVATE")
                                .timestamp(Instant.now().toString())
                                .performedBy(authenticatedAdminId)
                                .build();

                log.info("Admin [{}]: Role activated successfully: {}", authenticatedAdminId, roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Role activated successfully", response));
        }

        @PatchMapping("/roles/{roleId}/deactivate")
        @Operation(summary = "7. Deactivate role", description = "Deactivates a role by its UUID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Role deactivated successfully"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<RoleActionResponseDto>> deactivateRole(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Deactivating role ID: {}", authenticatedAdminId, roleId);

                roleService.deactivateRole(roleId, authenticatedAdminId);
                RoleActionResponseDto response = RoleActionResponseDto.builder()
                                .roleId(roleId)
                                .action("DEACTIVATE")
                                .timestamp(Instant.now().toString())
                                .performedBy(authenticatedAdminId)
                                .build();

                log.info("Admin [{}]: Role deactivated successfully: {}", authenticatedAdminId, roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Role deactivated successfully", response));
        }

        @PatchMapping("/roles/bulk-activate")
        @Operation(summary = "8. Bulk activate roles", description = "Activates multiple roles in a single operation")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Bulk activation completed"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ApiResponseDto<BulkRoleActionResponseDto>> bulkActivateRoles(
                        @Valid @RequestBody BulkRoleActionRequestDto request) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                request.setAdminId(authenticatedAdminId);
                log.info("Admin [{}]: Bulk activating {} roles", authenticatedAdminId, request.getRoleIds().size());

                BulkRoleActionResponseDto response = roleService.bulkActivateRoles(request);

                log.info("Admin [{}]: Bulk activation completed for {} roles", authenticatedAdminId,
                                request.getRoleIds().size());
                return ResponseEntity.ok(ApiResponseDto.success("Bulk activate completed", response));
        }

        @PatchMapping("/roles/bulk-deactivate")
        @Operation(summary = "9. Bulk deactivate roles", description = "Deactivates multiple roles in a single operation")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Bulk deactivation completed"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ApiResponseDto<BulkRoleActionResponseDto>> bulkDeactivateRoles(
                        @Valid @RequestBody BulkRoleActionRequestDto request) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                request.setAdminId(authenticatedAdminId);
                log.info("Admin [{}]: Bulk deactivating {} roles", authenticatedAdminId, request.getRoleIds().size());

                BulkRoleActionResponseDto response = roleService.bulkDeactivateRoles(request);

                log.info("Admin [{}]: Bulk deactivation completed for {} roles", authenticatedAdminId,
                                request.getRoleIds().size());
                return ResponseEntity.ok(ApiResponseDto.success("Bulk deactivate completed", response));
        }

        // ========================================
        // Role Search & Lookup Endpoints
        // ========================================

        @GetMapping("/roles/search")
        @Operation(summary = "10. Search roles", description = "Searches roles by name or description")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<List<RoleResponseDto>>> searchRoles(
                        @Parameter(description = "Search query string") @RequestParam String query) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Searching roles with query: {}", authenticatedAdminId, query);

                List<RoleResponseDto> response = roleService.searchRoles(query);

                log.debug("Admin [{}]: Found {} roles matching query", authenticatedAdminId, response.size());
                return ResponseEntity.ok(ApiResponseDto.success("Roles search results fetched successfully", response));
        }

        @GetMapping("/roles/by-name/{name}")
        @Operation(summary = "11. Get role by name", description = "Retrieves a specific role by its name")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Role retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<RoleResponseDto>> getRoleByName(
                        @Parameter(description = "Role name") @PathVariable String name) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching role by name: {}", authenticatedAdminId, name);

                RoleResponseDto response = roleService.getRoleByName(name, authenticatedAdminId);

                log.debug("Admin [{}]: Role retrieved by name successfully: {}", authenticatedAdminId, name);
                return ResponseEntity.ok(ApiResponseDto.success("Role fetched successfully", response));
        }

        @GetMapping("/roles/exists")
        @Operation(summary = "12. Check role name exists", description = "Checks if a role name already exists")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Check completed successfully")
        })
        public ResponseEntity<ApiResponseDto<RoleExistsResponseDto>> checkRoleNameExists(
                        @Parameter(description = "Role name to check") @RequestParam String roleName) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Checking if role name exists: {}", authenticatedAdminId, roleName);

                RoleExistsResponseDto response = roleService.checkRoleNameExists(roleName);

                log.debug("Admin [{}]: Role name exists check completed: {} = {}", authenticatedAdminId, roleName,
                                response.getExists());
                return ResponseEntity.ok(ApiResponseDto.success("Role name check completed", response));
        }

        @PostMapping("/roles/{roleId}/clone")
        @Operation(summary = "13. Clone role", description = "Creates a copy of an existing role with a new name")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Role cloned successfully"),
                        @ApiResponse(responseCode = "404", description = "Source role not found"),
                        @ApiResponse(responseCode = "409", description = "New role name already exists")
        })
        public ResponseEntity<ApiResponseDto<RoleResponseDto>> cloneRole(
                        @Parameter(description = "Source role ID") @PathVariable UUID roleId,
                        @Valid @RequestBody RoleCloneRequestDto request) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                request.setAdminId(authenticatedAdminId);
                log.info("Admin [{}]: Cloning role {} to '{}'", authenticatedAdminId, roleId, request.getNewRoleName());

                RoleResponseDto response = roleService.cloneRole(roleId, request);

                log.info("Admin [{}]: Role cloned successfully: {} -> {}", authenticatedAdminId, roleId,
                                response.getRoleId());
                return ResponseEntity.ok(ApiResponseDto.success("Role cloned successfully", response));
        }

        // ========================================
        // Role-Permission Relationship Endpoints
        // ========================================

        @GetMapping("/roles/{roleId}/permissions")
        @Operation(summary = "14. Get permissions of a role", description = "Lists all permissions assigned to a role")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<Set<RbacPermission>>> getPermissionsOfRole(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching permissions for role: {}", authenticatedAdminId, roleId);

                Set<RbacPermission> response = roleService.getPermissionsOfRole(roleId);

                log.debug("Admin [{}]: Retrieved {} permissions for role: {}", authenticatedAdminId, response.size(),
                                roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Permissions of role fetched successfully", response));
        }

        @PutMapping("/roles/{roleId}/permissions")
        @Operation(summary = "15. Assign permissions to role", description = "Assigns a list of permissions to a role")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permissions assigned successfully"),
                        @ApiResponse(responseCode = "404", description = "Role or permission not found")
        })
        public ResponseEntity<ApiResponseDto<RoleAssignPermissionsResponseDto>> assignPermissionsToRole(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId,
                        @Valid @RequestBody RoleAssignPermissionsRequestDto requestDto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Assigning {} permissions to role: {}",
                                authenticatedAdminId, requestDto.getPermissionIds().size(), roleId);

                roleService.assignPermissionsToRole(roleId, requestDto, authenticatedAdminId);
                RoleAssignPermissionsResponseDto response = new RoleAssignPermissionsResponseDto(
                                roleId, requestDto.getPermissionIds(), "Permissions assigned successfully.");

                log.info("Admin [{}]: Permissions assigned successfully to role: {}", authenticatedAdminId, roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Permissions assigned successfully", response));
        }

        @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
        @Operation(summary = "16. Remove permission from role", description = "Removes a specific permission from a role")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permission removed successfully"),
                        @ApiResponse(responseCode = "404", description = "Role or permission not found")
        })
        public ResponseEntity<ApiResponseDto<RoleActionResponseDto>> removePermissionFromRole(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId,
                        @Parameter(description = "Permission ID") @PathVariable UUID permissionId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Removing permission {} from role: {}", authenticatedAdminId, permissionId,
                                roleId);

                roleService.removePermissionFromRole(roleId, permissionId, authenticatedAdminId);
                RoleActionResponseDto response = RoleActionResponseDto.builder()
                                .roleId(roleId)
                                .action("REMOVE_PERMISSION")
                                .timestamp(Instant.now().toString())
                                .performedBy(authenticatedAdminId)
                                .build();

                log.info("Admin [{}]: Permission removed successfully from role: {}", authenticatedAdminId, roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Permission removed successfully", response));
        }

        @PostMapping("/roles/{roleId}/permissions/bulk-assign")
        @Operation(summary = "17. Bulk assign permissions to role", description = "Assigns multiple permissions to a role")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Bulk assignment completed"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<RolePermissionBulkOperationResponseDto>> bulkAssignPermissions(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId,
                        @Valid @RequestBody List<UUID> permissionIds) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Bulk assigning {} permissions to role: {}", authenticatedAdminId,
                                permissionIds.size(),
                                roleId);

                roleService.bulkAssignPermissions(roleId, permissionIds, authenticatedAdminId);
                RolePermissionBulkOperationResponseDto response = RolePermissionBulkOperationResponseDto.builder()
                                .roleId(roleId)
                                .action("BULK_ASSIGN")
                                .permissionsCount(permissionIds.size())
                                .permissionIds(permissionIds)
                                .timestamp(Instant.now().toString())
                                .performedBy(authenticatedAdminId)
                                .build();

                log.info("Admin [{}]: Bulk permission assignment completed for role: {}", authenticatedAdminId, roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Permissions assigned successfully", response));
        }

        @PostMapping("/roles/{roleId}/permissions/bulk-remove")
        @Operation(summary = "18. Bulk remove permissions from role", description = "Removes multiple permissions from a role")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Bulk removal completed"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<RolePermissionBulkOperationResponseDto>> bulkRemovePermissions(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId,
                        @Valid @RequestBody List<UUID> permissionIds) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Bulk removing {} permissions from role: {}", authenticatedAdminId,
                                permissionIds.size(),
                                roleId);

                roleService.bulkRemovePermissions(roleId, permissionIds, authenticatedAdminId);
                RolePermissionBulkOperationResponseDto response = RolePermissionBulkOperationResponseDto.builder()
                                .roleId(roleId)
                                .action("BULK_REMOVE")
                                .permissionsCount(permissionIds.size())
                                .permissionIds(permissionIds)
                                .timestamp(Instant.now().toString())
                                .performedBy(authenticatedAdminId)
                                .build();

                log.info("Admin [{}]: Bulk permission removal completed for role: {}", authenticatedAdminId, roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Permissions removed successfully", response));
        }

        @GetMapping("/roles/by-permission/{permissionId}")
        @Operation(summary = "19. Get roles by permission", description = "Lists all roles that have a specific permission")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Permission not found")
        })
        public ResponseEntity<ApiResponseDto<List<RoleResponseDto>>> getRolesByPermission(
                        @Parameter(description = "Permission ID") @PathVariable UUID permissionId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching roles with permission: {}", authenticatedAdminId, permissionId);

                List<RoleResponseDto> response = roleService.getRolesByPermission(permissionId);

                log.debug("Admin [{}]: Found {} roles with permission: {}", authenticatedAdminId, response.size(),
                                permissionId);
                return ResponseEntity.ok(ApiResponseDto.success("Roles by permission fetched successfully", response));
        }

        // ========================================
        // Role Admin Users Endpoints
        // ========================================

        @GetMapping("/roles/{roleId}/admindashboard-users")
        @Operation(summary = "20. Get admindashboard users with role", description = "Retrieves a paginated list of admindashboard users assigned to a role")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin users retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<Page<AdminUserResponseDto>>> getAdminUsersWithRole(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size (1-100)") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching admindashboard users with role {} - page: {}, size: {}",
                                authenticatedAdminId,
                                roleId,
                                page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<AdminUserResponseDto> response = roleService.getUsersWithRole(roleId, authenticatedAdminId,
                                pageable);

                log.debug("Admin [{}]: Retrieved {} admindashboard users with role: {}", authenticatedAdminId,
                                response.getNumberOfElements(),
                                roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Users with role fetched successfully", response));
        }

        // ========================================
        // Role Audit Log Endpoints
        // ========================================

        @PostMapping("/roles/audit-logs")
        @Operation(summary = "21. Create role audit log", description = "Creates a new audit log entry for a role action")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Audit log created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ApiResponseDto<RbacRoleAuditLog>> createRoleAuditLog(
                        @Valid @RequestBody RbacRoleAuditLog auditLog) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Creating role audit log for role: {}", authenticatedAdminId,
                                auditLog.getRoleId());

                RbacRoleAuditLog response = roleService.createAuditLog(auditLog);

                log.info("Admin [{}]: Role audit log created successfully", authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Role audit log created successfully", response));
        }

        @GetMapping("/roles/{roleId}/audit-logs")
        @Operation(summary = "22. Get audit logs by role ID", description = "Retrieves audit logs for a specific role")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<List<RbacRoleAuditLog>>> getRoleAuditLogs(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching audit logs for role: {}", authenticatedAdminId, roleId);

                List<RbacRoleAuditLog> response = roleService.getAuditLogsByRoleId(roleId);

                log.debug("Admin [{}]: Retrieved {} audit logs for role: {}", authenticatedAdminId, response.size(),
                                roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Audit logs fetched successfully", response));
        }

        @GetMapping("/roles/{roleId}/audit-logs/filter")
        @Operation(summary = "23. Filter role audit logs", description = "Filters audit logs by date or action")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Filtered audit logs retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<List<RbacRoleAuditLog>>> filterRoleAuditLogs(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId,
                        @Parameter(description = "Start date filter (yyyy-MM-dd)") @RequestParam(required = false) String fromDate,
                        @Parameter(description = "End date filter (yyyy-MM-dd)") @RequestParam(required = false) String toDate,
                        @Parameter(description = "Action type filter") @RequestParam(required = false) String action) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Filtering audit logs for role {} - fromDate: {}, toDate: {}, action: {}",
                                authenticatedAdminId, roleId, fromDate, toDate, action);

                List<RbacRoleAuditLog> response = roleService.filterAuditLogs(roleId, fromDate, toDate, action);

                log.debug("Admin [{}]: Retrieved {} filtered audit logs for role: {}", authenticatedAdminId,
                                response.size(),
                                roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Filtered audit logs fetched successfully", response));
        }

        @GetMapping("/roles/{roleId}/audit-logs/paginated")
        @Operation(summary = "24. Get paginated role audit logs", description = "Retrieves paginated audit logs for a role")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Paginated audit logs retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<Page<RbacRoleAuditLog>>> getRoleAuditLogsPaginated(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId,
                        @Parameter(description = "Start date filter (yyyy-MM-dd)") @RequestParam(required = false) String fromDate,
                        @Parameter(description = "End date filter (yyyy-MM-dd)") @RequestParam(required = false) String toDate,
                        @Parameter(description = "Action type filter") @RequestParam(required = false) String action,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size (1-100)") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching paginated audit logs for role {} - page: {}, size: {}",
                                authenticatedAdminId, roleId, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<RbacRoleAuditLog> response = roleService.getAuditLogsPaginated(roleId, fromDate, toDate, action,
                                pageable);

                log.debug("Admin [{}]: Retrieved {} audit logs on page {} for role: {}",
                                authenticatedAdminId, response.getNumberOfElements(), page, roleId);
                return ResponseEntity.ok(ApiResponseDto.success("Paginated audit logs fetched successfully", response));
        }

        // ========================================
        // Permission CRUD Endpoints
        // ========================================

        @PostMapping("/permissions")
        @Operation(summary = "25. Create new permission", description = "Creates a new permission")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Permission created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "409", description = "Permission name already exists")
        })
        public ResponseEntity<ApiResponseDto<PermissionResponseDto>> createPermission(
                        @Valid @RequestBody PermissionRequestDto dto,
                        HttpServletRequest httpRequest) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                dto.setAdminId(authenticatedAdminId); // Set admin ID from JWT token
                log.info("Admin [{}]: Creating permission with name: {}", authenticatedAdminId,
                                dto.getPermissionName());

                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);
                PermissionResponseDto response = permissionService.createPermission(dto, clientInfo);

                log.info("Admin [{}]: Permission created successfully with ID: {}", authenticatedAdminId,
                                response.getPermissionId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Permission created successfully", response));
        }

        @GetMapping("/permissions")
        @Operation(summary = "26. Get all permissions", description = "Retrieves a paginated list of all permissions")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<PermissionResponseDto>>> getAllPermissions(
                        @Parameter(hidden = true) Pageable pageable) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching all permissions - page: {}, size: {}",
                                authenticatedAdminId, pageable.getPageNumber(), pageable.getPageSize());

                Page<PermissionResponseDto> response = permissionService.getAllPermissions(pageable);

                log.debug("Admin [{}]: Retrieved {} permissions on page {}",
                                authenticatedAdminId, response.getNumberOfElements(), pageable.getPageNumber());
                return ResponseEntity.ok(ApiResponseDto.success("All permissions fetched successfully", response));
        }

        @GetMapping("/permissions/{permissionId}")
        @Operation(summary = "27. Get permission by ID", description = "Retrieves a specific permission by its ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permission retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Permission not found")
        })
        public ResponseEntity<ApiResponseDto<PermissionResponseDto>> getPermissionById(
                        @Parameter(description = "Permission ID") @PathVariable UUID permissionId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching permission by ID: {}", authenticatedAdminId, permissionId);

                PermissionResponseDto response = permissionService.getPermissionById(permissionId);

                log.debug("Admin [{}]: Permission retrieved successfully: {}", authenticatedAdminId, permissionId);
                return ResponseEntity.ok(ApiResponseDto.success("Permission fetched successfully", response));
        }

        @PutMapping("/permissions/{permissionId}")
        @Operation(summary = "28. Update permission", description = "Updates an existing permission")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permission updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Permission not found"),
                        @ApiResponse(responseCode = "409", description = "Permission name already exists")
        })
        public ResponseEntity<ApiResponseDto<PermissionResponseDto>> updatePermission(
                        @Parameter(description = "Permission ID") @PathVariable UUID permissionId,
                        @Valid @RequestBody PermissionRequestDto dto,
                        HttpServletRequest httpRequest) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                dto.setAdminId(authenticatedAdminId); // Set admin ID from JWT token
                log.info("Admin [{}]: Updating permission ID: {}", authenticatedAdminId, permissionId);

                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);
                PermissionResponseDto response = permissionService.updatePermission(permissionId, dto, clientInfo);

                log.info("Admin [{}]: Permission updated successfully: {}", authenticatedAdminId, permissionId);
                return ResponseEntity.ok(ApiResponseDto.success("Permission updated successfully", response));
        }

        @DeleteMapping("/permissions/{permissionId}")
        @Operation(summary = "29. Delete permission", description = "Soft deletes a permission by its ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permission deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Permission not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> deletePermission(
                        @Parameter(description = "Permission ID") @PathVariable UUID permissionId,
                        HttpServletRequest httpRequest) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Deleting permission ID: {}", authenticatedAdminId, permissionId);

                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);
                permissionService.deletePermission(permissionId, clientInfo);

                log.info("Admin [{}]: Permission deleted successfully: {}", authenticatedAdminId, permissionId);
                return ResponseEntity.ok(ApiResponseDto.success("Permission deleted successfully", null));
        }

        // ========================================
        // Permission Status Endpoints
        // ========================================

        @PatchMapping("/permissions/{permissionId}/enable")
        @Operation(summary = "30. Enable permission", description = "Enables a permission by its ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permission enabled successfully"),
                        @ApiResponse(responseCode = "404", description = "Permission not found")
        })
        public ResponseEntity<ApiResponseDto<PermissionResponseDto>> enablePermission(
                        @Parameter(description = "Permission ID") @PathVariable UUID permissionId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Enabling permission ID: {}", authenticatedAdminId, permissionId);

                PermissionResponseDto response = permissionService.enablePermission(permissionId);

                log.info("Admin [{}]: Permission enabled successfully: {}", authenticatedAdminId, permissionId);
                return ResponseEntity.ok(ApiResponseDto.success("Permission enabled successfully", response));
        }

        @PatchMapping("/permissions/{permissionId}/disable")
        @Operation(summary = "31. Disable permission", description = "Disables a permission by its ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permission disabled successfully"),
                        @ApiResponse(responseCode = "404", description = "Permission not found")
        })
        public ResponseEntity<ApiResponseDto<PermissionResponseDto>> disablePermission(
                        @Parameter(description = "Permission ID") @PathVariable UUID permissionId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Disabling permission ID: {}", authenticatedAdminId, permissionId);

                PermissionResponseDto response = permissionService.disablePermission(permissionId);

                log.info("Admin [{}]: Permission disabled successfully: {}", authenticatedAdminId, permissionId);
                return ResponseEntity.ok(ApiResponseDto.success("Permission disabled successfully", response));
        }

        @PatchMapping("/permissions/{permissionId}/restore")
        @Operation(summary = "32. Restore permission", description = "Restores a soft-deleted permission by its ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permission restored successfully"),
                        @ApiResponse(responseCode = "404", description = "Permission not found")
        })
        public ResponseEntity<ApiResponseDto<PermissionResponseDto>> restorePermission(
                        @Parameter(description = "Permission ID") @PathVariable UUID permissionId,
                        HttpServletRequest httpRequest) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Restoring permission ID: {}", authenticatedAdminId, permissionId);

                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);
                PermissionResponseDto response = permissionService.restorePermission(permissionId, clientInfo);

                log.info("Admin [{}]: Permission restored successfully: {}", authenticatedAdminId, permissionId);
                return ResponseEntity.ok(ApiResponseDto.success("Permission restored successfully", response));
        }

        // ========================================
        // Permission Audit Log Endpoints
        // ========================================

        @PostMapping("/permissions/audit-logs")
        @Operation(summary = "33. Create permission audit log", description = "Creates a new audit log entry for a permission action")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Audit log created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ApiResponseDto<RbacPermissionAuditLog>> createPermissionAuditLog(
                        @Valid @RequestBody RbacPermissionAuditLog auditLog) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Creating permission audit log for permission: {}", authenticatedAdminId,
                                auditLog.getPermissionId());

                RbacPermissionAuditLog response = permissionService.createAuditLog(auditLog);

                log.info("Admin [{}]: Permission audit log created successfully", authenticatedAdminId);
                return ResponseEntity.ok(ApiResponseDto.success("Permission audit log created successfully", response));
        }

        @GetMapping("/permissions/{permissionId}/audit-logs")
        @Operation(summary = "34. Get audit logs by permission ID", description = "Retrieves audit logs for a specific permission")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Permission not found")
        })
        public ResponseEntity<ApiResponseDto<List<RbacPermissionAuditLog>>> getPermissionAuditLogs(
                        @Parameter(description = "Permission ID") @PathVariable UUID permissionId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching audit logs for permission: {}", authenticatedAdminId, permissionId);

                List<RbacPermissionAuditLog> response = permissionService.getAuditLogsByPermissionId(permissionId);

                log.debug("Admin [{}]: Retrieved {} audit logs for permission: {}", authenticatedAdminId,
                                response.size(),
                                permissionId);
                return ResponseEntity.ok(ApiResponseDto.success("Audit logs fetched successfully", response));
        }

        // ========================================
        // RBAC Audit Log Endpoints
        // ========================================

        @GetMapping("/audit-logs/role/{roleId}")
        @Operation(summary = "Get audit logs for a role", description = "Retrieves paginated audit logs for a specific role")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Role not found")
        })
        public ResponseEntity<ApiResponseDto<Page<BaseAuditLogDto>>> getAuditLogsForRole(
                        @Parameter(description = "Role ID") @PathVariable UUID roleId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching audit logs for role: {}", authenticatedAdminId, roleId);

                Pageable pageable = PageRequest.of(page, size);
                Page<BaseAuditLogDto> response = rbacRoleAuditService.getAuditLogsForRole(roleId, pageable)
                                .map(commonAuditLogMapper::toDto);

                log.debug("Admin [{}]: Retrieved audit logs for role: {} (total: {})", authenticatedAdminId, roleId,
                                response.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("Audit logs fetched successfully", response));
        }

        @GetMapping("/audit-logs/permission/{permissionId}")
        @Operation(summary = "Get audit logs for a permission", description = "Retrieves paginated audit logs for a specific permission")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Permission not found")
        })
        public ResponseEntity<ApiResponseDto<Page<BaseAuditLogDto>>> getAuditLogsForPermission(
                        @Parameter(description = "Permission ID") @PathVariable UUID permissionId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching audit logs for permission: {}", authenticatedAdminId, permissionId);

                Pageable pageable = PageRequest.of(page, size);
                Page<BaseAuditLogDto> response = rbacPermissionAuditService
                                .getAuditLogsForPermission(permissionId, pageable)
                                .map(commonAuditLogMapper::toDto);

                log.debug("Admin [{}]: Retrieved audit logs for permission: {} (total: {})", authenticatedAdminId,
                                permissionId, response.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("Audit logs fetched successfully", response));
        }

        @GetMapping("/audit-logs/user/{userId}")
        @Operation(summary = "Get audit logs for a user", description = "Retrieves paginated audit logs for a specific user (role assignments)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Page<BaseAuditLogDto>>> getAuditLogsForUser(
                        @Parameter(description = "User ID") @PathVariable UUID userId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching audit logs for user: {}", authenticatedAdminId, userId);

                Pageable pageable = PageRequest.of(page, size);
                // Query role audits for user (where most user-related changes are tracked)
                Page<BaseAuditLogDto> response = rbacRoleAuditService.getAuditLogsForActor(userId, pageable)
                                .map(commonAuditLogMapper::toDto);

                log.debug("Admin [{}]: Retrieved audit logs for user: {} (total: {})", authenticatedAdminId, userId,
                                response.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("Audit logs fetched successfully", response));
        }

        @GetMapping("/audit-logs/action/{action}")
        @Operation(summary = "Get audit logs by action type", description = "Retrieves paginated audit logs for a specific action (ROLE_CREATED, PERMISSION_GRANTED, etc.)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid action type")
        })
        public ResponseEntity<ApiResponseDto<Page<BaseAuditLogDto>>> getAuditLogsByAction(
                        @Parameter(description = "Action type (e.g., ROLE_CREATED, PERMISSION_GRANTED)") @PathVariable String action,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching audit logs for action: {}", authenticatedAdminId, action);

                Pageable pageable = PageRequest.of(page, size);
                Page<BaseAuditLogDto> response = rbacRoleAuditService.getAuditLogsForAction(action, pageable)
                                .map(commonAuditLogMapper::toDto);

                log.debug("Admin [{}]: Retrieved audit logs for action: {} (total: {})", authenticatedAdminId, action,
                                response.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("Audit logs fetched successfully", response));
        }

        @GetMapping("/audit-logs/failed-attempts")
        @Operation(summary = "Get failed RBAC audit attempts", description = "Retrieves paginated list of failed RBAC operations for security investigation")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Failed attempts retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<BaseAuditLogDto>>> getFailedAuditAttempts(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching failed RBAC audit attempts", authenticatedAdminId);

                Pageable pageable = PageRequest.of(page, size);
                Page<BaseAuditLogDto> response = rbacRoleAuditService.getFailedRoleChanges().stream()
                                .skip((long) page * size)
                                .limit(size)
                                .map(commonAuditLogMapper::toDto)
                                .toList()
                                .isEmpty() ? Page.empty(pageable)
                                                : new org.springframework.data.domain.PageImpl<>(
                                                                rbacRoleAuditService.getFailedRoleChanges().stream()
                                                                                .skip((long) page * size)
                                                                                .limit(size)
                                                                                .map(commonAuditLogMapper::toDto)
                                                                                .toList(),
                                                                pageable,
                                                                rbacRoleAuditService.getFailedRoleChanges().size());

                log.debug("Admin [{}]: Retrieved {} failed audit attempts", authenticatedAdminId,
                                response.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("Failed attempts fetched successfully", response));
        }

        @GetMapping("/audit-logs/recent")
        @Operation(summary = "Get recent RBAC audit logs", description = "Retrieves the most recent RBAC role audit log entries for dashboard display")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Recent audit logs retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<List<BaseAuditLogDto>>> getRecentAuditLogs() {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching recent RBAC audit logs", authenticatedAdminId);

                // Get recent role audits (last 50) - use empty UUID to get all
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0,
                                50,
                                org.springframework.data.domain.Sort
                                                .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
                List<BaseAuditLogDto> response = rbacRoleAuditService
                                .getAuditLogsForActor(UUID.fromString("00000000-0000-0000-0000-000000000000"), pageable)
                                .stream()
                                .map(commonAuditLogMapper::toDto)
                                .toList();

                log.debug("Admin [{}]: Retrieved {} recent audit logs", authenticatedAdminId, response.size());
                return ResponseEntity.ok(ApiResponseDto.success("Recent audit logs fetched successfully", response));
        }

        // ========================================
        // Helper Methods
        // ========================================

}
