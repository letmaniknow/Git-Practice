package com.mmva.newsapp.controller.admin.adminuser;

import com.mmva.newsapp.domain.adminuser.dto.audit.AdminUserAuditLogDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserExportDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserRequestDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserResponseDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserUpdateDto;
import com.mmva.newsapp.domain.adminuser.dto.auth.AdminUserChangePasswordDto;
import com.mmva.newsapp.domain.adminuser.dto.auth.AdminUserLoginRequestDto;
import com.mmva.newsapp.domain.adminuser.dto.auth.AdminEmailVerificationRequestDto;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleResponseDto;
import com.mmva.newsapp.domain.adminuser.model.audit.AdminUserAuditLog;
import com.mmva.newsapp.domain.adminuser.repository.audit.AdminUserAuditLogRepository;
import com.mmva.newsapp.domain.adminuser.service.core.AdminUserService;
import com.mmva.newsapp.domain.adminuser.service.validation.AdminValidationService;
import com.mmva.newsapp.infrastructure.common.util.FileContentTypeUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Admin API Controller for Admin Staff Management.
 * 
 * <p>
 * Provides full CRUD and management operations for internal admin users
 * (employees, admins, moderators). Following industry best practices:
 * </p>
 * 
 * <table border="1">
 * <caption>Staff Management Endpoints</caption>
 * <tr>
 * <th>#</th>
 * <th>Method</th>
 * <th>Endpoint</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>POST</td>
 * <td>/api/v1/admin/staff</td>
 * <td>Create new staff member</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>GET</td>
 * <td>/api/v1/admin/staff</td>
 * <td>List all staff (paginated)</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>GET</td>
 * <td>/api/v1/admin/staff/{id}</td>
 * <td>Get staff by ID</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>PUT</td>
 * <td>/api/v1/admin/staff/{id}</td>
 * <td>Update staff details</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>DELETE</td>
 * <td>/api/v1/admin/staff/{id}</td>
 * <td>Delete staff member</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>GET</td>
 * <td>/api/v1/admin/staff/{id}/audit-logs</td>
 * <td>Get staff audit logs</td>
 * </tr>
 * <tr>
 * <td>7</td>
 * <td>GET</td>
 * <td>/api/v1/admin/staff/export</td>
 * <td>Export staff data</td>
 * </tr>
 * </table>
 * 
 * <p>
 * Note: This controller manages INTERNAL admin users. For app user
 * management, see AdminAppUserController.
 * </p>
 * 
 * @see com.mmva.newsapp.controller.admin.users.AdminAppUserController for app
 *      user management
 * @see com.mmva.newsapp.controller.user.profile.AppUserMeController for user
 *      self-service
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/admin/staff")
@RequiredArgsConstructor
@Tag(name = "Admin - Staff Management", description = "Admin operations for internal staff user management")
public class AdminStaffController {

        // ========================================
        // Constants
        // ========================================

        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_SIZE = "10";
        private static final int MAX_PAGE_SIZE = 100;

        // ========================================
        // Dependencies
        // ========================================

        private final AdminUserService adminUserService;
        private final AdminUserAuditLogRepository auditLogRepository;
        private final AdminValidationService adminValidationService;

        // ========================================
        // Authentication Endpoints
        // ========================================

        @PostMapping("/login")
        @Operation(summary = "1. Authenticate admindashboard", description = "Authenticates admindashboard by username/email and password, returns profile if successful")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin authenticated successfully"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials")
        })
        public ResponseEntity<ApiResponseDto<AdminUserResponseDto>> login(
                        @Valid @RequestBody AdminUserLoginRequestDto loginRequest) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Authenticating admindashboard with login: {}", authenticatedAdminId,
                                loginRequest.getAdminUsersUsernameOrEmail());

                AdminUserResponseDto response = adminUserService.login(loginRequest);

                log.info("Admin [{}]: Admin authentication successful for: {}", authenticatedAdminId,
                                loginRequest.getAdminUsersUsernameOrEmail());
                return ResponseEntity.ok(ApiResponseDto.success("Admin login successful", response));
        }

        // ========================================
        // Permission & Role Query Endpoints
        // ========================================

        @GetMapping("/{adminId}/permissions")
        @Operation(summary = "2. Get all permissions for admindashboard", description = "Lists all permissions assigned to the admindashboard user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<List<PermissionResponseDto>>> getPermissionsForAdmin(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching permissions for admindashboard ID: {}", authenticatedAdminId, adminId);

                List<PermissionResponseDto> permissions = adminUserService.getPermissionsForAdmin(adminId);

                log.debug("Admin [{}]: Retrieved {} permissions for admindashboard ID: {}", authenticatedAdminId,
                                permissions.size(),
                                adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Permissions fetched successfully", permissions));
        }

        @GetMapping("/{adminId}/has-permission")
        @Operation(summary = "3. Check admindashboard's permission", description = "Checks if the admindashboard user has a specific permission")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Permission check completed"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<Boolean>> checkAdminPermission(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId,
                        @Parameter(description = "Permission name to check") @RequestParam String permissionName) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Checking permission '{}' for admindashboard ID: {}", authenticatedAdminId,
                                permissionName,
                                adminId);

                boolean hasPermission = adminUserService.checkAdminPermission(adminId, permissionName);

                log.debug("Admin [{}]: Permission '{}' for admindashboard ID {}: {}", authenticatedAdminId,
                                permissionName,
                                adminId,
                                hasPermission ? "GRANTED" : "DENIED");
                return ResponseEntity.ok(ApiResponseDto.success("Permission check completed", hasPermission));
        }

        @GetMapping("/{adminId}/roles")
        @Operation(summary = "4. Get all roles for admindashboard", description = "Lists all roles assigned to the admindashboard user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<List<RoleResponseDto>>> getRolesForAdmin(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching roles for admindashboard ID: {}", authenticatedAdminId, adminId);

                List<RoleResponseDto> roles = adminUserService.getRolesForAdmin(adminId);

                log.debug("Admin [{}]: Retrieved {} roles for admindashboard ID: {}", authenticatedAdminId,
                                roles.size(),
                                adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Roles fetched successfully", roles));
        }

        // ========================================
        // Account Lifecycle Endpoints
        // ========================================

        @PostMapping
        @Operation(summary = "5. Create new admindashboard", description = "Creates a new admindashboard user with the provided details")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Admin created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "409", description = "Admin already exists")
        })
        public ResponseEntity<ApiResponseDto<AdminUserResponseDto>> createAdmin(
                        @Valid @RequestBody AdminUserRequestDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Creating new admindashboard with username: {}", authenticatedAdminId,
                                dto.getAdminUsersUsername());

                AdminUserResponseDto response = adminUserService.createAdmin(dto);

                log.info("Admin [{}]: Admin created successfully with ID: {}", authenticatedAdminId,
                                response.getAdminUsersId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Admin created successfully", response));
        }

        @GetMapping("/{adminId}")
        @Operation(summary = "6. Get admindashboard by ID", description = "Retrieves a specific admindashboard user by its UUID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<AdminUserResponseDto>> getAdminById(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching admindashboard by ID: {}", authenticatedAdminId, adminId);

                AdminUserResponseDto response = adminUserService.getAdminById(adminId);

                log.debug("Admin [{}]: Admin retrieved successfully: {}", authenticatedAdminId, adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin fetched successfully", response));
        }

        @GetMapping("/username/{adminUsername}")
        @Operation(summary = "7. Get admindashboard by username", description = "Retrieves an admindashboard user by username")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Staff retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Staff not found")
        })
        public ResponseEntity<ApiResponseDto<AdminUserResponseDto>> getAdminByUsername(
                        @Parameter(description = "Staff username") @PathVariable String adminUsername) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching staff by username: {}", authenticatedAdminId, adminUsername);

                AdminUserResponseDto response = adminUserService.getAdminByUsername(adminUsername);

                log.debug("Admin [{}]: Staff retrieved by username: {}", authenticatedAdminId, adminUsername);
                return ResponseEntity.ok(ApiResponseDto.success("Staff fetched successfully", response));
        }

        @GetMapping("/email/{adminEmail}")
        @Operation(summary = "8. Get admindashboard by email", description = "Retrieves an admindashboard user by email address")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<AdminUserResponseDto>> getAdminByEmail(
                        @Parameter(description = "Admin email address") @PathVariable String adminEmail) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching admindashboard by email: {}", authenticatedAdminId, adminEmail);

                AdminUserResponseDto response = adminUserService.getAdminByEmail(adminEmail);

                log.debug("Admin [{}]: Admin retrieved by email: {}", authenticatedAdminId, adminEmail);
                return ResponseEntity.ok(ApiResponseDto.success("Admin fetched successfully", response));
        }

        @GetMapping
        @Operation(summary = "9. Get all admins", description = "Retrieves a paginated list of all admindashboard users")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin list retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<AdminUserResponseDto>>> getAllAdmins(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size (1-100)") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching all admins - page: {}, size: {}", authenticatedAdminId, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<AdminUserResponseDto> response = adminUserService.getAllAdmins(pageable);

                log.debug("Admin [{}]: Retrieved {} admins on page {}", authenticatedAdminId,
                                response.getNumberOfElements(),
                                page);
                return ResponseEntity.ok(ApiResponseDto.success("Admins fetched successfully", response));
        }

        @PutMapping("/{adminId}")
        @Operation(summary = "10. Update admindashboard user", description = "Updates an existing admindashboard user with the provided details")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Admin not found"),
                        @ApiResponse(responseCode = "409", description = "Duplicate username or email")
        })
        public ResponseEntity<ApiResponseDto<AdminUserResponseDto>> updateAdmin(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId,
                        @Valid @RequestBody AdminUserUpdateDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Updating admindashboard ID: {}", authenticatedAdminId, adminId);

                AdminUserResponseDto response = adminUserService.updateAdmin(adminId, dto);

                log.info("Admin [{}]: Admin updated successfully: {}", authenticatedAdminId, adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin updated successfully", response));
        }

        @DeleteMapping("/{adminId}")
        @Operation(summary = "11. Delete admindashboard", description = "Soft deletes an admindashboard user by its UUID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> deleteAdmin(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID deletedBy = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Deleting admindashboard ID: {}", deletedBy, adminId);

                adminUserService.deleteAdmin(adminId, deletedBy);

                log.info("Admin [{}]: Admin deleted successfully: {}", deletedBy, adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin user deleted successfully", null));
        }

        @PatchMapping("/{adminId}/restore")
        @Operation(summary = "12. Restore admindashboard user", description = "Restores a soft-deleted admindashboard user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin restored successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> restoreAdmin(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Restoring admindashboard ID: {}", authenticatedAdminId, adminId);

                adminUserService.restoreAdmin(adminId);

                log.info("Admin [{}]: Admin restored successfully: {}", authenticatedAdminId, adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin user restored successfully", null));
        }

        // ========================================
        // Account Status Endpoints
        // ========================================

        @PatchMapping("/{adminId}/activate")
        @Operation(summary = "13. Activate admindashboard", description = "Reactivates a previously deactivated admindashboard account")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin activated successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> activateAdmin(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Activating admindashboard ID: {}", authenticatedAdminId, adminId);

                adminUserService.activateAdmin(adminId);

                log.info("Admin [{}]: Admin activated successfully: {}", authenticatedAdminId, adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin user activated successfully", null));
        }

        @PatchMapping("/{adminId}/deactivate")
        @Operation(summary = "14. Deactivate admindashboard", description = "Deactivates an admindashboard account without deleting it")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin deactivated successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> deactivateAdmin(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Deactivating admindashboard ID: {}", authenticatedAdminId, adminId);

                adminUserService.deactivateAdmin(adminId);

                log.info("Admin [{}]: Admin deactivated successfully: {}", authenticatedAdminId, adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin user deactivated successfully", null));
        }

        // ========================================
        // Authentication & Security Endpoints
        // ========================================

        @PatchMapping("/{adminId}/login")
        @Operation(summary = "15. Record admindashboard login", description = "Records a successful admindashboard login and updates last login timestamp")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Login recorded successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<AdminUserResponseDto>> recordLogin(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Recording login for admindashboard ID: {}", authenticatedAdminId, adminId);

                AdminUserResponseDto response = adminUserService.recordLogin(adminId);

                log.info("Admin [{}]: Login recorded for admindashboard ID: {}", authenticatedAdminId, adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin login recorded successfully", response));
        }

        @PatchMapping("/{adminId}/change-password")
        @Operation(summary = "16. Change admindashboard password", description = "Changes the password for the specified admindashboard user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid password data"),
                        @ApiResponse(responseCode = "401", description = "Current password incorrect"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> changePassword(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId,
                        @Valid @RequestBody AdminUserChangePasswordDto changePasswordDto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Changing password for admindashboard ID: {}", authenticatedAdminId, adminId);

                adminUserService.changePassword(adminId, changePasswordDto);

                log.info("Admin [{}]: Password changed for admindashboard ID: {}", authenticatedAdminId, adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin password changed successfully", null));
        }

        @PostMapping("/request-password-reset")
        @Operation(summary = "17. Request password reset", description = "Requests a password reset for the admindashboard user by loginId (email or username)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Password reset requested successfully")
        })
        public ResponseEntity<ApiResponseDto<Void>> requestPasswordReset(
                        @Parameter(description = "Admin login ID (email or username)") @RequestParam String loginId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Requesting password reset for login ID: {}", authenticatedAdminId, loginId);

                adminUserService.requestPasswordReset(loginId);

                log.info("Admin [{}]: Password reset requested for login ID: {}", authenticatedAdminId, loginId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin password reset requested successfully", null));
        }

        @PatchMapping("/{adminId}/send-verification-code")
        @Operation(summary = "18. Generate email verification code", description = "Generates and sends an email verification code to the admindashboard user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Verification code sent successfully"),
                        @ApiResponse(responseCode = "400", description = "Email already verified"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<String>> generateEmailVerificationCode(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Generating email verification code for admindashboard ID: {}",
                                authenticatedAdminId,
                                adminId);

                String code = adminUserService.generateEmailVerificationCode(adminId);

                log.info("Admin [{}]: Email verification code sent for admindashboard ID: {}", authenticatedAdminId,
                                adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Email verification code sent successfully", code));
        }

        @PatchMapping("/{adminId}/verify-email")
        @Operation(summary = "19. Verify admindashboard email", description = "Verifies the email of the admindashboard user using a verification code")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid or expired verification code"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> verifyEmail(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId,
                        @Valid @RequestBody AdminEmailVerificationRequestDto verificationDto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Verifying email for admindashboard ID: {}", authenticatedAdminId, adminId);

                adminUserService.verifyEmail(adminId, verificationDto.getVerificationCode());

                log.info("Admin [{}]: Email verified for admindashboard ID: {}", authenticatedAdminId, adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin email verified successfully", null));
        }

        // ========================================
        // Role Management Endpoints
        // ========================================

        @PatchMapping("/{adminId}/assign-role")
        @Operation(summary = "20. Assign role to admin user", description = "Assigns a role to the admin user (replaces existing role)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid roleId (null or missing)"),
                        @ApiResponse(responseCode = "404", description = "Admin or role not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> assignRole(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId,
                        @Parameter(description = "Role ID (UUID) to assign") @RequestParam @NotNull(message = "Role ID cannot be null") UUID roleId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Assigning role ID {} to admin ID: {}", authenticatedAdminId, roleId,
                                adminId);

                adminUserService.assignRole(adminId, roleId);

                log.info("Admin [{}]: Role ID {} assigned to admin ID: {}", authenticatedAdminId, roleId,
                                adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Role assigned to admin successfully", null));
        }

        @PatchMapping("/{adminId}/revoke-role")
        @Operation(summary = "21. Revoke role from admin user", description = "Revokes (removes) a role from the admin user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Role revoked successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid roleId (null or missing)"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> revokeRole(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId,
                        @Parameter(description = "Role ID (UUID) to revoke") @RequestParam @NotNull(message = "Role ID cannot be null") UUID roleId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Revoking role ID {} from admin ID: {}", authenticatedAdminId, roleId,
                                adminId);

                adminUserService.revokeRole(adminId, roleId);

                log.info("Admin [{}]: Role ID {} revoked from admin ID: {}", authenticatedAdminId, roleId,
                                adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Role revoked from admin successfully", null));
        }

        @GetMapping("/by-role")
        @Operation(summary = "22. Get admins by role", description = "Retrieves admindashboard users by role name with pagination")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin list retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<AdminUserResponseDto>>> getAdminsByRole(
                        @Parameter(description = "Role name to filter by") @RequestParam String roleName,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size (1-100)") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching admins by role '{}' - page: {}, size: {}", authenticatedAdminId,
                                roleName, page,
                                size);

                Pageable pageable = PageRequest.of(page, size);
                Page<AdminUserResponseDto> response = adminUserService.getAdminsByRole(roleName, pageable);

                log.debug("Admin [{}]: Retrieved {} admins with role '{}'", authenticatedAdminId,
                                response.getNumberOfElements(),
                                roleName);
                return ResponseEntity.ok(ApiResponseDto.success("Admins by role fetched successfully", response));
        }

        // ========================================
        // Profile & Data Endpoints
        // ========================================

        @PatchMapping("/{adminId}/profile-picture")
        @Operation(summary = "23. Update profile picture URL", description = "Updates the profile picture URL for the admindashboard user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Profile picture updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> updateProfilePicture(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId,
                        @Parameter(description = "Avatar image URL") @RequestParam String avatarUrl) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Updating profile picture URL for admindashboard ID: {}", authenticatedAdminId,
                                adminId);

                adminUserService.updateProfilePicture(adminId, avatarUrl);

                log.info("Admin [{}]: Profile picture URL updated for admindashboard ID: {}", authenticatedAdminId,
                                adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin profile picture updated successfully", null));
        }

        @GetMapping("/{adminId}/export")
        @Operation(summary = "24. Export admindashboard data", description = "Exports admindashboard user data for compliance (excludes sensitive data)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Admin data exported successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<AdminUserExportDto>> exportAdminData(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Exporting data for admindashboard ID: {}", authenticatedAdminId, adminId);

                AdminUserExportDto data = adminUserService.exportAdminData(adminId);

                log.debug("Admin [{}]: Data exported for admindashboard ID: {}", authenticatedAdminId, adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin data exported successfully", data));
        }

        // ========================================
        // Account Deletion & Audit Endpoints
        // ========================================

        @PatchMapping("/{adminId}/request-deletion")
        @Operation(summary = "25. Request account deletion", description = "Requests deletion of the admindashboard user account")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Account deletion requested successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> requestAccountDeletion(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Requesting account deletion for admindashboard ID: {}", authenticatedAdminId,
                                adminId);

                adminUserService.requestAccountDeletion(adminId);

                log.info("Admin [{}]: Account deletion requested for admindashboard ID: {}", authenticatedAdminId,
                                adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin account deletion requested successfully", null));
        }

        @GetMapping("/{adminId}/audit-logs")
        @Operation(summary = "26. Get admindashboard audit logs", description = "Retrieves all audit logs for the admindashboard user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<List<AdminUserAuditLogDto>>> getAdminAuditLogs(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching audit logs for admindashboard ID: {}", authenticatedAdminId, adminId);

                var logs = auditLogRepository.findByAdminIdOrderByCreatedAtDesc(adminId,
                                org.springframework.data.domain.PageRequest.of(0, 100)).getContent();
                List<AdminUserAuditLogDto> logDtos = logs.stream()
                                .map(this::mapToAuditLogDto)
                                .toList();

                log.debug("Admin [{}]: Retrieved {} audit logs for admindashboard ID: {}", authenticatedAdminId,
                                logDtos.size(),
                                adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Admin audit logs fetched successfully", logDtos));
        }

        // ========================================
        // Profile Picture Upload Endpoints
        // ========================================

        @PatchMapping(path = "/{adminId}/profile-picture/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "27. Upload profile picture", description = "Uploads and updates the profile picture for the admindashboard user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid file"),
                        @ApiResponse(responseCode = "404", description = "Admin not found")
        })
        public ResponseEntity<ApiResponseDto<String>> uploadProfilePicture(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId,
                        @RequestPart("avatarFile") MultipartFile avatarFile) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Uploading profile picture for admindashboard ID: {}", authenticatedAdminId,
                                adminId);

                String avatarUrl = adminUserService.updateProfilePicture(adminId, avatarFile);

                log.info("Admin [{}]: Profile picture uploaded for admindashboard ID: {}", authenticatedAdminId,
                                adminId);
                return ResponseEntity
                                .ok(ApiResponseDto.success("Admin profile picture uploaded successfully", avatarUrl));
        }

        @GetMapping("/{adminId}/profile-picture")
        @Operation(summary = "28. Get profile picture", description = "Retrieves the profile picture (avatar image) for the admindashboard user as a file resource")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Profile picture retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Admin or avatar not found")
        })
        public ResponseEntity<Resource> getProfilePicture(
                        @Parameter(description = "Admin user ID") @PathVariable UUID adminId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching profile picture for admindashboard ID: {}", authenticatedAdminId,
                                adminId);

                Resource avatar = adminUserService.getAvatarFile(adminId);
                MediaType contentType = FileContentTypeUtil.getMediaType(avatar);

                log.debug("Admin [{}]: Profile picture retrieved for admindashboard ID: {}", authenticatedAdminId,
                                adminId);
                return ResponseEntity.ok()
                                .contentType(contentType)
                                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=avatar.jpg")
                                .body(avatar);
        }

        /**
         * Autocomplete search for active admin users by partial name, username, or
         * email.
         *
         * Example: /api/v1/admin/staff/autocomplete?query=jo&page=0&size=10
         */
        @GetMapping("/autocomplete")
        @Operation(summary = "Autocomplete admin users", description = "Search active admin users by partial name, username, or email for autocomplete dropdowns.")
        public ResponseEntity<ApiResponseDto<Page<AdminUserResponseDto>>> autocompleteAdminUsers(
                        @RequestParam String query,
                        @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                        @RequestParam(defaultValue = DEFAULT_SIZE) int size) {
                Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
                Page<AdminUserResponseDto> result = adminUserService
                                .searchActiveAdminsByNameOrUsernameOrEmail(query, pageable);
                return ResponseEntity.ok(ApiResponseDto.success("Admin user autocomplete results", result));
        }

        // ========================================
        // Helper Methods
        // ========================================

        /**
         * Validates admindashboard access by extracting the admindashboard ID from JWT
         * and checking
         * /**
         * Maps an AdminUserAuditLog entity to its DTO representation.
         *
         * @param auditLog the audit log entity
         * @return the mapped DTO
         */
        private AdminUserAuditLogDto mapToAuditLogDto(AdminUserAuditLog auditLog) {
                return AdminUserAuditLogDto.builder()
                                .id(auditLog.getId())
                                .adminUserId(auditLog.getAdminId())
                                .action(auditLog.getAction())
                                .details(auditLog.getReason())
                                .createdBy(auditLog.getActorId())
                                .createdAt(auditLog.getCreatedAt() != null
                                                ? auditLog.getCreatedAt().toString()
                                                : null)
                                .build();
        }
}
