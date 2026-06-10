package com.mmva.newsapp.controller.user.profile;

import com.mmva.newsapp.domain.appuser.dto.auth.AppUserChangePasswordDto;
import com.mmva.newsapp.domain.appuser.dto.operations.AppUserDataExportDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileResponseDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileUpdateDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserSessionDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserVerificationCodeDto;
import com.mmva.newsapp.domain.appuser.service.core.AppUserService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.security.userdetails.AppUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * User Profile Controller.
 * 
 * <p>
 * Handles all profile-related operations for the authenticated user:
 * </p>
 * <ul>
 * <li>Profile management (view, update, preferences)</li>
 * <li>Account management (password, deactivation, deletion)</li>
 * <li>Session management (view, invalidate)</li>
 * <li>Verification (email, phone)</li>
 * <li>GDPR compliance (data export, activity log)</li>
 * </ul>
 * 
 * <p>
 * Path prefix: /api/v1/me
 * </p>
 * 
 * @see com.mmva.newsapp.controller.admin.users.AdminAppUserController for admin
 *      operations
 */
@CrossOrigin(origins = "*", allowedHeaders = { "Range", "Accept", "Content-Type", "Authorization" }, exposedHeaders = {
                "Content-Length", "Content-Range", "Accept-Ranges" })
@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "User Profile", description = "Profile, account, session, and verification operations for authenticated users")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserProfileController {

        private final AppUserService userService;

        // ==========================================
        // PROFILE OPERATIONS (1-5)
        // ==========================================

        @GetMapping("/profile")
        @Operation(summary = "1. Get my profile", description = "Retrieves the current user's profile")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> getMyProfile(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Profile [{}]: Fetching profile", userId);
                AppUserProfileResponseDto response = userService.getUserById(userId);
                log.debug("Profile [{}]: Profile retrieved successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Profile retrieved successfully", response));
        }

        @PutMapping("/profile")
        @Operation(summary = "2. Update my profile", description = "Updates the current user's profile")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> updateMyProfile(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Valid @RequestBody AppUserProfileUpdateDto dto) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Updating profile", userId);
                AppUserProfileResponseDto response = userService.updateUser(userId, dto);
                log.info("Profile [{}]: Profile updated successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Profile updated successfully", response));
        }

        @PostMapping("/change-password")
        @Operation(summary = "3. Change my password", description = "Changes the current user's password")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid password"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Void>> changeMyPassword(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Valid @RequestBody AppUserChangePasswordDto dto) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Changing password", userId);
                userService.changePassword(userId, dto);
                log.info("Profile [{}]: Password changed successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Password changed successfully", null));
        }

        @PatchMapping("/profile-picture")
        @Operation(summary = "4. Update my profile picture", description = "Updates the current user's profile picture")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Profile picture updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid avatar URL"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Void>> updateMyProfilePicture(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "URL of the new profile picture", required = true) @RequestParam String avatarUrl) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Updating profile picture", userId);
                userService.updateProfilePicture(userId, avatarUrl);
                log.info("Profile [{}]: Profile picture updated successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Profile picture updated successfully", null));
        }

        @PostMapping("/profile-picture/upload")
        @Operation(summary = "4.1. Upload profile picture", description = "Uploads a new profile picture file for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid file or upload failed"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<String>> uploadProfilePicture(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Avatar image file to upload", required = true) @RequestParam("avatarFile") MultipartFile avatarFile) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Uploading profile picture file", userId);
                String avatarUrl = userService.updateProfilePicture(userId, avatarFile);
                log.info("Profile [{}]: Profile picture uploaded successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Profile picture uploaded successfully", avatarUrl));
        }

        @PatchMapping("/preferences")
        @Operation(summary = "5. Update my preferences", description = "Updates marketing and privacy preferences")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Void>> updateMyPreferences(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Marketing opt-in preference") @RequestParam(required = false) Boolean marketingOptIn,
                        @Parameter(description = "Privacy settings JSON") @RequestParam(required = false) String privacySettings) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Updating preferences", userId);
                userService.updatePreferences(userId, marketingOptIn, privacySettings);
                log.info("Profile [{}]: Preferences updated successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Preferences updated successfully", null));
        }

        // ==========================================
        // ACCOUNT MANAGEMENT OPERATIONS (6-7)
        // ==========================================

        @PatchMapping("/request-deletion")
        @Operation(summary = "6. Request account deletion", description = "Requests account deletion for GDPR compliance")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Account deletion requested"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Void>> requestMyAccountDeletion(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Requesting account deletion", userId);
                userService.requestAccountDeletion(userId, userId);
                log.info("Profile [{}]: Account deletion requested successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Account deletion requested successfully", null));
        }

        @PatchMapping("/deactivate")
        @Operation(summary = "7. Deactivate my account", description = "Deactivates the current user's account")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Account deactivated"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Void>> deactivateMyAccount(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Deactivating account", userId);
                userService.deactivateUser(userId);
                log.info("Profile [{}]: Account deactivated successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Account deactivated successfully", null));
        }

        // ==========================================
        // GDPR COMPLIANCE OPERATIONS (8-9)
        // ==========================================

        @GetMapping("/export-data")
        @Operation(summary = "8. Export my data", description = "Exports all user data for GDPR compliance")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Data exported successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<AppUserDataExportDto>> exportMyData(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Exporting user data (GDPR)", userId);
                AppUserDataExportDto data = userService.exportUserData(userId);
                log.info("Profile [{}]: User data exported successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Data exported successfully", data));
        }

        @GetMapping("/activity-log")
        @Operation(summary = "9. Get my activity log", description = "Retrieves the current user's activity log")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Activity log retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<String>> getMyActivityLog(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Profile [{}]: Fetching activity log", userId);
                String logData = userService.getUserActivityLog(userId);
                log.debug("Profile [{}]: Activity log retrieved", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Activity log retrieved successfully", logData));
        }

        // ==========================================
        // SESSION MANAGEMENT OPERATIONS (10-12)
        // ==========================================

        @GetMapping("/sessions")
        @Operation(summary = "10. Get my active sessions", description = "Lists all active sessions for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Sessions retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<List<AppUserSessionDto>>> getMySessions(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Profile [{}]: Fetching active sessions", userId);
                List<AppUserSessionDto> sessions = userService.getActiveSessions(userId);
                log.debug("Profile [{}]: Retrieved {} active sessions", userId, sessions.size());
                return ResponseEntity.ok(ApiResponseDto.success("Sessions retrieved successfully", sessions));
        }

        @DeleteMapping("/sessions/{sessionId}")
        @Operation(summary = "11. Invalidate a session", description = "Invalidates a specific session")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Session invalidated"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Session not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> invalidateMySession(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "Session ID to invalidate", required = true) @PathVariable UUID sessionId) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Invalidating session: {}", userId, sessionId);
                userService.invalidateSession(userId, sessionId);
                log.info("Profile [{}]: Session {} invalidated successfully", userId, sessionId);
                return ResponseEntity.ok(ApiResponseDto.success("Session invalidated successfully", null));
        }

        @DeleteMapping("/sessions")
        @Operation(summary = "12. Logout everywhere", description = "Invalidates all sessions for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All sessions invalidated"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Void>> invalidateAllMySessions(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Invalidating all sessions (logout everywhere)", userId);
                userService.invalidateAllSessions(userId);
                log.info("Profile [{}]: All sessions invalidated successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("All sessions invalidated successfully", null));
        }

        // ==========================================
        // VERIFICATION OPERATIONS (13-16)
        // ==========================================

        @PostMapping("/send-email-verification")
        @Operation(summary = "13. Send email verification code", description = "Sends email verification code to the user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Verification code sent"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<String>> sendEmailVerification(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Sending email verification code", userId);
                String result = userService.generateEmailVerificationCode(userId);
                log.info("Profile [{}]: Email verification code sent", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Verification code sent to email", result));
        }

        @PostMapping("/verify-email")
        @Operation(summary = "14. Verify email", description = "Verifies the user's email with the provided code")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Email verified"),
                        @ApiResponse(responseCode = "400", description = "Invalid verification code"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Void>> verifyEmail(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Valid @RequestBody AppUserVerificationCodeDto dto) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Verifying email", userId);
                userService.verifyEmail(userId, dto.getAppUsersVerificationCode());
                log.info("Profile [{}]: Email verified successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Email verified successfully", null));
        }

        @PostMapping("/send-phone-verification")
        @Operation(summary = "15. Send phone verification code", description = "Sends phone verification code to the user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Verification code sent"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<String>> sendPhoneVerification(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Sending phone verification code", userId);
                String result = userService.generatePhoneVerificationCode(userId);
                log.info("Profile [{}]: Phone verification code sent", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Verification code sent to phone", result));
        }

        @PostMapping("/verify-phone")
        @Operation(summary = "16. Verify phone", description = "Verifies the user's phone with the provided code")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phone verified"),
                        @ApiResponse(responseCode = "400", description = "Invalid verification code"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Void>> verifyPhone(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Valid @RequestBody AppUserVerificationCodeDto dto) {
                UUID userId = userDetails.getUserId();
                log.info("Profile [{}]: Verifying phone", userId);
                userService.verifyPhone(userId, dto.getAppUsersVerificationCode());
                log.info("Profile [{}]: Phone verified successfully", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Phone verified successfully", null));
        }

        @GetMapping("/avatar")
        @Operation(summary = "17. Get user avatar", description = "Retrieves the authenticated user's avatar image")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Avatar retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Avatar not found")
        })
        public ResponseEntity<Resource> getAvatar(@AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Profile [{}]: Fetching avatar", userId);

                Resource avatarResource = userService.getAvatarFile(userId);

                log.debug("Profile [{}]: Avatar fetched successfully", userId);
                return ResponseEntity.ok()
                                .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                                .body(avatarResource);
        }
}
