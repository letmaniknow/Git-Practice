package com.mmva.newsapp.controller.admin.appuser;

import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.common.api.dto.BulkOperationResultDto;
import com.mmva.newsapp.domain.appuser.model.audit.AppUserAuditLog;
import com.mmva.newsapp.domain.appuser.enums.core.AppUserStatus;
import com.mmva.newsapp.domain.appuser.repository.audit.AppUserAuditLogRepository;
import com.mmva.newsapp.domain.appuser.dto.operations.AppUserBulkOperationDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserChangePasswordDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserOAuthLoginDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserPasswordResetCompleteDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserPasswordResetRequestDto;
import com.mmva.newsapp.domain.appuser.dto.operations.AppUserReactivateAccountDto;
import com.mmva.newsapp.domain.appuser.dto.operations.AppUserDataExportDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileRequestDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileResponseDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileUpdateDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserSessionDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserVerificationCodeDto;
import com.mmva.newsapp.domain.adminuser.service.validation.AdminValidationService;
import com.mmva.newsapp.domain.appuser.service.core.AppUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;
import java.util.UUID;

/**
 * Admin API Controller for App User Management.
 * 
 * <p>
 * Provides admin operations for managing app user profiles (customers, readers,
 * subscribers).
 * Following industry best practices:
 * </p>
 * 
 * <table border="1">
 * <caption>App User Management Endpoints</caption>
 * <tr>
 * <th>#</th>
 * <th>Method</th>
 * <th>Endpoint</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>GET</td>
 * <td>/api/v1/admin/appusers</td>
 * <td>List all app users (paginated)</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>GET</td>
 * <td>/api/v1/admin/appusers/{id}</td>
 * <td>Get app user by ID</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>PUT</td>
 * <td>/api/v1/admin/appusers/{id}</td>
 * <td>Update app user profile</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>DELETE</td>
 * <td>/api/v1/admin/appusers/{id}</td>
 * <td>Delete app user</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>POST</td>
 * <td>/api/v1/admin/appusers/bulk</td>
 * <td>Bulk operations on app users</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>GET</td>
 * <td>/api/v1/admin/appusers/{id}/audit-logs</td>
 * <td>Get app user audit logs</td>
 * </tr>
 * <tr>
 * <td>7</td>
 * <td>GET</td>
 * <td>/api/v1/admin/appusers/{id}/sessions</td>
 * <td>Get app user sessions</td>
 * </tr>
 * </table>
 * 
 * <p>
 * Note: This controller manages APP users (customers, readers, subscribers).
 * For internal staff user management, see AdminStaffController.
 * </p>
 * 
 * @see com.mmva.newsapp.controller.admin.users.AdminStaffController for staff
 *      user management
 * @see com.mmva.newsapp.controller.user.profile.AppUserMeController for user
 *      self-service
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/admin/appusers")
@RequiredArgsConstructor
@Tag(name = "Admin - App User Management", description = "Admin operations for app user management")
public class AdminAppUserController {

        // ========================================
        // Constants
        // ========================================

        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_SIZE = "10";
        private static final int MAX_PAGE_SIZE = 100;

        // ========================================
        // Dependencies
        // ========================================

        private final AppUserService userService;
        private final AppUserAuditLogRepository auditLogRepository;
        private final AdminValidationService adminValidationService;

        // ========================================
        // Account Validation Endpoints
        // ========================================

        @GetMapping("/check-email")
        @Operation(summary = "1. Check if email exists", description = "Validates if an email is already registered (for pre-registration check)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Email check completed successfully")
        })
        public ResponseEntity<ApiResponseDto<Boolean>> checkEmailExists(
                        @Parameter(description = "Email address to check") @RequestParam String email) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Checking if email exists: {}", authenticatedAdminId, email);

                boolean exists = userService.emailExists(email);
                String message = exists ? "Email is already registered" : "Email is available";

                log.debug("Admin [{}]: Email check completed - {} = {}", authenticatedAdminId, email, exists);
                return ResponseEntity.ok(ApiResponseDto.success(message, exists));
        }

        @GetMapping("/check-phone")
        @Operation(summary = "2. Check if phone exists", description = "Validates if a phone number is already registered (for pre-registration check)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Phone check completed successfully")
        })
        public ResponseEntity<ApiResponseDto<Boolean>> checkPhoneExists(
                        @Parameter(description = "Phone number to check") @RequestParam String phone) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Checking if phone exists: {}", authenticatedAdminId, phone);

                boolean exists = userService.phoneExists(phone);
                String message = exists ? "Phone number is already registered" : "Phone number is available";

                log.debug("Admin [{}]: Phone check completed - {} = {}", authenticatedAdminId, phone, exists);
                return ResponseEntity.ok(ApiResponseDto.success(message, exists));
        }

        // ========================================
        // Account Creation & Update Endpoints
        // ========================================

        @PostMapping
        @Operation(summary = "3. Create new app user", description = "Creates a new app user profile with the provided details")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "User created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "409", description = "Email or phone already exists")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> createUser(
                        @Valid @RequestBody AppUserProfileRequestDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Creating public user with email: {}", authenticatedAdminId,
                                dto.getAppUsersEmail());

                AppUserProfileResponseDto response = userService.createAppUser(dto);

                log.info("Admin [{}]: Public user created successfully with ID: {}", authenticatedAdminId,
                                response.getAppUsersId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("User profile created successfully", response));
        }

        @PutMapping("/{userId}")
        @Operation(summary = "4. Update public user profile", description = "Updates an existing public user profile with the provided details")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "409", description = "Email or phone already exists")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> updateUser(
                        @Parameter(description = "User ID") @PathVariable UUID userId,
                        @Valid @RequestBody AppUserProfileUpdateDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Updating public user ID: {}", authenticatedAdminId, userId);

                AppUserProfileResponseDto response = userService.updateUser(userId, dto);

                log.info("Admin [{}]: Public user updated successfully: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("User profile updated successfully", response));
        }

        // ========================================
        // Account Lookup Endpoints
        // ========================================

        @GetMapping("/{userId}")
        @Operation(summary = "5. Get public user by ID", description = "Retrieves a specific public user profile by its UUID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> getUserById(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching public user by ID: {}", authenticatedAdminId, userId);

                AppUserProfileResponseDto response = userService.getUserById(userId);

                log.debug("Admin [{}]: Public user retrieved successfully: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("User fetched successfully", response));
        }

        @GetMapping("/email/{userEmail}")
        @Operation(summary = "6. Get public user by email", description = "Retrieves a public user profile by email address")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> getUserByEmail(
                        @Parameter(description = "User email address") @PathVariable("userEmail") String userEmail) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching public user by email: {}", authenticatedAdminId, userEmail);

                AppUserProfileResponseDto response = userService.getUserByEmail(userEmail);

                log.debug("Admin [{}]: Public user retrieved by email: {}", authenticatedAdminId, userEmail);
                return ResponseEntity.ok(ApiResponseDto.success("User fetched successfully", response));
        }

        @GetMapping("/phone/{userPhoneNumber}")
        @Operation(summary = "7. Get public user by phone number", description = "Retrieves a public user profile by mobile number")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> getUserByPhoneNumber(
                        @Parameter(description = "User phone number") @PathVariable("userPhoneNumber") String userPhoneNumber) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching public user by phone: {}", authenticatedAdminId, userPhoneNumber);

                AppUserProfileResponseDto response = userService.getUserByPhoneNumber(userPhoneNumber);

                log.debug("Admin [{}]: Public user retrieved by phone: {}", authenticatedAdminId, userPhoneNumber);
                return ResponseEntity.ok(ApiResponseDto.success("User fetched successfully", response));
        }

        @GetMapping
        @Operation(summary = "8. Get all public users", description = "Retrieves a paginated list of public user profiles")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<AppUserProfileResponseDto>>> getAllUsers(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size (1-100)") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching all public users - page: {}, size: {}", authenticatedAdminId, page,
                                size);

                Pageable pageable = PageRequest.of(page, size);
                Page<AppUserProfileResponseDto> response = userService.getAllUsers(pageable);

                log.debug("Admin [{}]: Retrieved {} public users on page {}", authenticatedAdminId,
                                response.getNumberOfElements(), page);
                return ResponseEntity.ok(ApiResponseDto.success("Users fetched successfully", response));
        }

        @GetMapping("/search")
        @Operation(summary = "9. Search and filter public users", description = "Search public users by name and/or filter by status. Supports partial name matching.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Search completed successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<AppUserProfileResponseDto>>> searchUsers(
                        @Parameter(description = "Search name query") @RequestParam(required = false) String name,
                        @Parameter(description = "Status filter (ACTIVE, INACTIVE, SUSPENDED, PENDING, DELETED, BANNED)") @RequestParam(required = false) AppUserStatus status,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size (1-100)") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Searching public users - name: {}, status: {}, page: {}, size: {}",
                                authenticatedAdminId, name, status, page, size);

                int limitedSize = Math.min(size, MAX_PAGE_SIZE);
                Pageable pageable = PageRequest.of(page, limitedSize);
                Page<AppUserProfileResponseDto> response = userService.searchUsersWithFilters(
                                name, status, pageable);

                log.debug("Admin [{}]: Search completed - found {} users", authenticatedAdminId,
                                response.getNumberOfElements());
                return ResponseEntity.ok(ApiResponseDto.success("Users search completed successfully", response));
        }

        // ========================================
        // Account Security & Verification Endpoints
        // ========================================

        @PostMapping("/{userId}/change-password")
        @Operation(summary = "10. Change password", description = "Changes the public user's password")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid password data"),
                        @ApiResponse(responseCode = "401", description = "Current password incorrect"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> changePassword(
                        @Parameter(description = "User ID") @PathVariable UUID userId,
                        @Valid @RequestBody AppUserChangePasswordDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Changing password for user ID: {}", authenticatedAdminId, userId);

                userService.changePassword(userId, dto);

                log.info("Admin [{}]: Password changed successfully for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("Password changed successfully", null));
        }

        @PostMapping("/request-password-reset")
        @Operation(summary = "11. Request password reset", description = "Initiates password reset process by sending a reset code to email")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Password reset code sent successfully")
        })
        public ResponseEntity<ApiResponseDto<Void>> requestPasswordReset(
                        @Valid @RequestBody AppUserPasswordResetRequestDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Requesting password reset for: {}", authenticatedAdminId,
                                dto.getAppUsersEmailOrPhone());

                userService.requestPasswordReset(dto);

                log.info("Admin [{}]: Password reset code sent for: {}", authenticatedAdminId,
                                dto.getAppUsersEmailOrPhone());
                return ResponseEntity.ok(ApiResponseDto.success("Password reset code sent to your email", null));
        }

        @PostMapping("/complete-password-reset")
        @Operation(summary = "12. Complete password reset", description = "Completes password reset with verification code")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Password reset completed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid or expired verification code")
        })
        public ResponseEntity<ApiResponseDto<Void>> completePasswordReset(
                        @Valid @RequestBody AppUserPasswordResetCompleteDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Completing password reset for: {}", authenticatedAdminId,
                                dto.getAppUsersEmailOrPhone());

                userService.completePasswordReset(dto);

                log.info("Admin [{}]: Password reset completed for: {}", authenticatedAdminId,
                                dto.getAppUsersEmailOrPhone());
                return ResponseEntity.ok(ApiResponseDto.success("Password reset completed successfully", null));
        }

        @PostMapping("/{userId}/send-email-verification")
        @Operation(summary = "13. Send email verification code", description = "Generates and sends email verification code")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Verification code sent successfully"),
                        @ApiResponse(responseCode = "400", description = "Email already verified"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<String>> sendEmailVerificationCode(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Sending email verification code for user: {}", authenticatedAdminId, userId);

                String result = userService.generateEmailVerificationCode(userId);

                log.info("Admin [{}]: Email verification code sent for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("Verification code sent to email", result));
        }

        @PostMapping("/{userId}/send-phone-verification")
        @Operation(summary = "14. Send phone verification code", description = "Generates and sends phone verification code")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Verification code sent successfully"),
                        @ApiResponse(responseCode = "400", description = "Phone already verified"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<String>> sendPhoneVerificationCode(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Sending phone verification code for user: {}", authenticatedAdminId, userId);

                String result = userService.generatePhoneVerificationCode(userId);

                log.info("Admin [{}]: Phone verification code sent for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("Verification code sent to phone", result));
        }

        @PostMapping("/{userId}/verify-email")
        @Operation(summary = "15. Verify email", description = "Verifies the public user's email address with code")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid or expired verification code"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> verifyEmail(
                        @Parameter(description = "User ID") @PathVariable UUID userId,
                        @Valid @RequestBody AppUserVerificationCodeDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Verifying email for user: {}", authenticatedAdminId, userId);

                userService.verifyEmail(userId, dto.getAppUsersVerificationCode());

                log.info("Admin [{}]: Email verified successfully for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("Email verified successfully", null));
        }

        @PostMapping("/{userId}/verify-phone")
        @Operation(summary = "16. Verify phone", description = "Verifies the public user's mobile number with code")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Phone verified successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid or expired verification code"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> verifyPhone(
                        @Parameter(description = "User ID") @PathVariable UUID userId,
                        @Valid @RequestBody AppUserVerificationCodeDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Verifying phone for user: {}", authenticatedAdminId, userId);

                userService.verifyPhone(userId, dto.getAppUsersVerificationCode());

                log.info("Admin [{}]: Phone verified successfully for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("Phone verified successfully", null));
        }

        // ========================================
        // Profile & Preferences Endpoints
        // ========================================

        @PatchMapping("/{userId}/profile-picture")
        @Operation(summary = "17. Update profile picture", description = "Updates the public user's profile picture")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Profile picture updated successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> updateProfilePicture(
                        @Parameter(description = "User ID") @PathVariable UUID userId,
                        @Parameter(description = "URL of the new avatar image") @RequestParam String avatarUrl) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Updating profile picture for user: {}", authenticatedAdminId, userId);

                userService.updateProfilePicture(userId, avatarUrl);

                log.info("Admin [{}]: Profile picture updated for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("Profile picture updated successfully", null));
        }

        @PatchMapping("/{userId}/preferences")
        @Operation(summary = "18. Update preferences", description = "Updates marketing and privacy preferences")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> updatePreferences(
                        @Parameter(description = "User ID") @PathVariable UUID userId,
                        @Parameter(description = "Marketing opt-in preference") @RequestParam(required = false) Boolean marketingOptIn,
                        @Parameter(description = "Privacy settings JSON") @RequestParam(required = false) String privacySettings) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Updating preferences for user: {}, marketingOptIn: {}", authenticatedAdminId,
                                userId,
                                marketingOptIn);

                userService.updatePreferences(userId, marketingOptIn, privacySettings);

                log.info("Admin [{}]: Preferences updated for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("Preferences updated successfully", null));
        }

        @GetMapping("/{userId}/export-data")
        @Operation(summary = "19. Export user data", description = "Exports all user data for GDPR compliance in structured JSON format")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User data exported successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<AppUserDataExportDto>> exportUserData(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Exporting user data for user: {}", authenticatedAdminId, userId);

                AppUserDataExportDto data = userService.exportUserData(userId);

                log.debug("Admin [{}]: User data exported for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("User data exported successfully", data));
        }

        // ========================================
        // Account Lifecycle Endpoints
        // ========================================

        @PatchMapping("/{userId}/request-deletion")
        @Operation(summary = "20. Request account deletion", description = "Requests account deletion for GDPR compliance")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Account deletion requested successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> requestAccountDeletion(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Requesting account deletion for user: {}", authenticatedAdminId, userId);

                userService.requestAccountDeletion(userId, authenticatedAdminId);

                log.info("Admin [{}]: Account deletion requested for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("Account deletion requested successfully", null));
        }

        @DeleteMapping("/{userId}")
        @Operation(summary = "21. Delete public user", description = "Permanently deletes a public user profile by its UUID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> deleteUser(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Deleting user: {}", authenticatedAdminId, userId);

                userService.deleteUser(userId);

                log.info("Admin [{}]: User deleted successfully: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("User deleted successfully", null));
        }

        @PatchMapping("/{userId}/deactivate")
        @Operation(summary = "22. Deactivate public user", description = "Deactivates a public user account without deleting it")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User deactivated successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> deactivateUser(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Deactivating user: {}", authenticatedAdminId, userId);

                userService.deactivateUser(userId);

                log.info("Admin [{}]: User deactivated successfully: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("User deactivated successfully", null));
        }

        @PatchMapping("/{userId}/activate")
        @Operation(summary = "23. Activate public user", description = "Reactivates a previously deactivated public user account (admindashboard use)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User activated successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> activateUser(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Activating user: {}", authenticatedAdminId, userId);

                userService.activateUser(userId);

                log.info("Admin [{}]: User activated successfully: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("User activated successfully", null));
        }

        @PostMapping("/reactivate")
        @Operation(summary = "24. Self-service account reactivation", description = "Allows public users to reactivate their deactivated account using email/phone and password")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Account reactivated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid credentials"),
                        @ApiResponse(responseCode = "404", description = "Account not found")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> reactivateAccount(
                        @Valid @RequestBody AppUserReactivateAccountDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Reactivating account for: {}", authenticatedAdminId,
                                dto.getAppUsersEmailOrPhone());

                AppUserProfileResponseDto response = userService.reactivateAccount(dto);

                log.info("Admin [{}]: Account reactivated successfully for: {}", authenticatedAdminId,
                                dto.getAppUsersEmailOrPhone());
                return ResponseEntity.ok(ApiResponseDto.success("Account reactivated successfully", response));
        }

        @PostMapping("/{userId}/login")
        @Operation(summary = "25. Record user login", description = "Records a successful public user login and updates last login timestamp")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User login recorded successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> recordLogin(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Recording login for user: {}", authenticatedAdminId, userId);

                AppUserProfileResponseDto response = userService.recordLogin(userId);

                log.info("Admin [{}]: Login recorded successfully for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("User login recorded successfully", response));
        }

        // ========================================
        // Audit & Activity Endpoints
        // ========================================

        @GetMapping("/{userId}/activity-log")
        @Operation(summary = "26. Get user activity log", description = "Fetches public user activity log")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User activity log fetched successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<String>> getUserActivityLog(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching activity log for user: {}", authenticatedAdminId, userId);

                String logData = userService.getUserActivityLog(userId);

                log.debug("Admin [{}]: Activity log fetched for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("User activity log fetched successfully", logData));
        }

        @GetMapping("/{userId}/audit-logs")
        @Operation(summary = "27. Get user profile audit logs", description = "Retrieves paginated audit logs for a public user profile")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User audit logs fetched successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Page<AppUserAuditLog>>> getUserAuditLogs(
                        @Parameter(description = "User ID") @PathVariable UUID userId,
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) @Min(0) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) @Max(MAX_PAGE_SIZE) int size) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching audit logs for user: {}, page: {}, size: {}", authenticatedAdminId,
                                userId,
                                page, size);

                Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
                Page<AppUserAuditLog> logs = auditLogRepository
                                .findByAppUsersAuditLogUserIdOrderByAppUsersAuditLogCreatedAtDesc(userId,
                                                pageable);

                log.debug("Admin [{}]: Audit logs fetched for user: {}, count: {}", authenticatedAdminId, userId,
                                logs.getNumberOfElements());
                return ResponseEntity.ok(ApiResponseDto.success("User audit logs fetched successfully", logs));
        }

        // ========================================
        // Session Management Endpoints
        // ========================================

        @PostMapping("/{userId}/sessions")
        @Operation(summary = "28. Create session", description = "Creates a new session for the public user")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Session created successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<AppUserSessionDto>> createSession(
                        @Parameter(description = "User ID") @PathVariable UUID userId,
                        @Parameter(description = "Device information") @RequestParam(required = false) String deviceInfo,
                        @Parameter(description = "Client IP address") @RequestParam(required = false) String ipAddress,
                        @Parameter(description = "User agent string") @RequestParam(required = false) String userAgent,
                        @Parameter(description = "Geographic location") @RequestParam(required = false) String location) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Creating session for user: {}", authenticatedAdminId, userId);

                AppUserSessionDto session = userService.createSession(userId, deviceInfo, ipAddress, userAgent,
                                location);

                log.info("Admin [{}]: Session created successfully for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Session created successfully", session));
        }

        @GetMapping("/{userId}/sessions")
        @Operation(summary = "29. Get active sessions", description = "Lists all active sessions for the public user")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Active sessions fetched successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<List<AppUserSessionDto>>> getActiveSessions(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Fetching active sessions for user: {}", authenticatedAdminId, userId);

                List<AppUserSessionDto> sessions = userService.getActiveSessions(userId);

                log.debug("Admin [{}]: Found {} active sessions for user: {}", authenticatedAdminId, sessions.size(),
                                userId);
                return ResponseEntity.ok(ApiResponseDto.success("Active sessions fetched successfully", sessions));
        }

        @DeleteMapping("/{userId}/sessions/{sessionId}")
        @Operation(summary = "30. Invalidate session", description = "Invalidates a specific session")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Session invalidated successfully"),
                        @ApiResponse(responseCode = "404", description = "Session or user not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> invalidateSession(
                        @Parameter(description = "User ID") @PathVariable UUID userId,
                        @Parameter(description = "Session ID") @PathVariable UUID sessionId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Invalidating session: {} for user: {}", authenticatedAdminId, sessionId, userId);

                userService.invalidateSession(userId, sessionId);

                log.info("Admin [{}]: Session invalidated: {} for user: {}", authenticatedAdminId, sessionId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("Session invalidated successfully", null));
        }

        @DeleteMapping("/{userId}/sessions")
        @Operation(summary = "31. Invalidate all sessions", description = "Invalidates all sessions for the public user (logout everywhere)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "All sessions invalidated successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> invalidateAllSessions(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Invalidating all sessions for user: {}", authenticatedAdminId, userId);

                userService.invalidateAllSessions(userId);

                log.info("Admin [{}]: All sessions invalidated for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("All sessions invalidated successfully", null));
        }

        // ========================================
        // OAuth/Social Login Endpoints
        // ========================================

        @PostMapping("/oauth/login")
        @Operation(summary = "32. OAuth login", description = "Login or register via OAuth provider (Google, Facebook, etc.)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "OAuth login successful"),
                        @ApiResponse(responseCode = "400", description = "Invalid OAuth data")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> oauthLogin(
                        @Valid @RequestBody AppUserOAuthLoginDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Processing OAuth login for provider: {}", authenticatedAdminId,
                                dto.getAppUsersProvider());

                AppUserProfileResponseDto response = userService.oauthLogin(dto);

                log.info("Admin [{}]: OAuth login successful for provider: {}", authenticatedAdminId,
                                dto.getAppUsersProvider());
                return ResponseEntity.ok(ApiResponseDto.success("OAuth login successful", response));
        }

        // ========================================
        // Account Lockout Endpoints
        // ========================================

        @PostMapping("/{userId}/unlock")
        @Operation(summary = "33. Unlock account", description = "Unlocks a locked public user account (admindashboard use)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Account unlocked successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> unlockAccount(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Unlocking account for user: {}", authenticatedAdminId, userId);

                userService.unlockAccount(userId);

                log.info("Admin [{}]: Account unlocked for user: {}", authenticatedAdminId, userId);
                return ResponseEntity.ok(ApiResponseDto.success("Account unlocked successfully", null));
        }

        @GetMapping("/{userId}/lock-status")
        @Operation(summary = "34. Check lock status", description = "Checks if a public user account is locked")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Lock status retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Boolean>> isAccountLocked(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.debug("Admin [{}]: Checking lock status for user: {}", authenticatedAdminId, userId);

                boolean isLocked = userService.isAccountLocked(userId);

                String message = isLocked ? "Account is locked" : "Account is not locked";
                log.debug("Admin [{}]: Lock status for user {}: {}", authenticatedAdminId, userId, isLocked);
                return ResponseEntity.ok(ApiResponseDto.success(message, isLocked));
        }

        // ========================================
        // Bulk Operations Endpoints
        // ========================================

        @PostMapping("/bulk/activate")
        @Operation(summary = "35. Bulk activate public users", description = "Activates multiple public user accounts (admindashboard use)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Bulk activate completed"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ApiResponseDto<BulkOperationResultDto>> bulkActivateUsers(
                        @Valid @RequestBody AppUserBulkOperationDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Bulk activating {} users", authenticatedAdminId, dto.getUserIds().size());

                BulkOperationResultDto result = userService.bulkActivateUsers(dto.getUserIds());

                log.info("Admin [{}]: Bulk activate completed - success: {}, failed: {}",
                                authenticatedAdminId, result.getSuccessCount(), result.getFailureCount());
                return ResponseEntity.ok(ApiResponseDto.success("Bulk activate completed", result));
        }

        @PostMapping("/bulk/deactivate")
        @Operation(summary = "36. Bulk deactivate public users", description = "Deactivates multiple public user accounts (admindashboard use)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Bulk deactivate completed"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ApiResponseDto<BulkOperationResultDto>> bulkDeactivateUsers(
                        @Valid @RequestBody AppUserBulkOperationDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Bulk deactivating {} users", authenticatedAdminId, dto.getUserIds().size());

                BulkOperationResultDto result = userService.bulkDeactivateUsers(dto.getUserIds());

                log.info("Admin [{}]: Bulk deactivate completed - success: {}, failed: {}",
                                authenticatedAdminId, result.getSuccessCount(), result.getFailureCount());
                return ResponseEntity.ok(ApiResponseDto.success("Bulk deactivate completed", result));
        }

        @PostMapping("/bulk/delete")
        @Operation(summary = "37. Bulk delete public users", description = "Deletes multiple public user accounts permanently (admindashboard use)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Bulk delete completed"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ApiResponseDto<BulkOperationResultDto>> bulkDeleteUsers(
                        @Valid @RequestBody AppUserBulkOperationDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Bulk deleting {} users", authenticatedAdminId, dto.getUserIds().size());

                BulkOperationResultDto result = userService.bulkDeleteUsers(dto.getUserIds());

                log.info("Admin [{}]: Bulk delete completed - success: {}, failed: {}",
                                authenticatedAdminId, result.getSuccessCount(), result.getFailureCount());
                return ResponseEntity.ok(ApiResponseDto.success("Bulk delete completed", result));
        }

        @PostMapping("/bulk/unlock")
        @Operation(summary = "38. Bulk unlock public user accounts", description = "Unlocks multiple locked public user accounts (admindashboard use)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Bulk unlock completed"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data")
        })
        public ResponseEntity<ApiResponseDto<BulkOperationResultDto>> bulkUnlockAccounts(
                        @Valid @RequestBody AppUserBulkOperationDto dto) {
                UUID authenticatedAdminId = adminValidationService.validateAndGetAdminId();
                log.info("Admin [{}]: Bulk unlocking {} user accounts", authenticatedAdminId, dto.getUserIds().size());

                BulkOperationResultDto result = userService.bulkUnlockAccounts(dto.getUserIds());

                log.info("Admin [{}]: Bulk unlock completed - success: {}, failed: {}",
                                authenticatedAdminId, result.getSuccessCount(), result.getFailureCount());
                return ResponseEntity.ok(ApiResponseDto.success("Bulk unlock completed", result));
        }
}
