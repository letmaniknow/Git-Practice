package com.mmva.newsapp.domain.appuser.service.core;

// ========================================
// DTO Imports
// ========================================
import com.mmva.newsapp.infrastructure.common.api.dto.BulkOperationResultDto;
import com.mmva.newsapp.domain.appuser.enums.core.AppUserStatus;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserChangePasswordDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserLoginRequestDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserOAuthLoginDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserPasswordResetCompleteDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserPasswordResetRequestDto;
import com.mmva.newsapp.domain.appuser.dto.operations.AppUserReactivateAccountDto;
import com.mmva.newsapp.domain.appuser.dto.operations.AppUserDataExportDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileRequestDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileResponseDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileUpdateDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserSessionDto;

// ========================================
// Spring Framework Imports
// ========================================
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// ========================================
// Java Core Imports
// ========================================
import java.util.List;
import java.util.UUID;

/**
 * Service interface for App User Management.
 *
 * <p>
 * Provides operations for managing public/customer user profiles including:
 * </p>
 * <ul>
 * <li>Account creation, update, and deletion</li>
 * <li>Account lookup and search</li>
 * <li>Security operations (password, verification)</li>
 * <li>Profile and preferences management</li>
 * <li>Session management</li>
 * <li>OAuth/Social login integration</li>
 * <li>Bulk operations for admin dashboard use</li>
 * <li>Audit logging</li>
 * </ul>
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
public interface AppUserService {

        // ========================================
        // Authentication
        // ========================================

        /**
         * Authenticates a user and returns their profile.
         *
         * @param loginRequest the login credentials
         * @return the authenticated user's profile
         */
        AppUserProfileResponseDto login(AppUserLoginRequestDto loginRequest);

        // ========================================
        // Account Validation
        // ========================================

        /**
         * Checks if an email already exists in the system.
         *
         * @param email the email to check
         * @return true if email exists, false otherwise
         */
        boolean emailExists(String email);

        /**
         * Checks if a phone number already exists in the system.
         *
         * @param phoneNumber the phone number to check
         * @return true if phone number exists, false otherwise
         */
        boolean phoneExists(String phoneNumber);

        // ========================================
        // Account Creation & Update
        // ========================================

        /**
         * Creates a new app user.
         *
         * @param dto the user profile data
         * @return the created user profile
         */
        AppUserProfileResponseDto createAppUser(AppUserProfileRequestDto dto);

        /**
         * Updates an existing user profile.
         *
         * @param id  the user ID
         * @param dto the update data
         * @return the updated user profile
         */
        AppUserProfileResponseDto updateUser(UUID id, AppUserProfileUpdateDto dto);

        // ========================================
        // Account Lookup & Search
        // ========================================

        /**
         * Retrieves a user profile by ID.
         *
         * @param id the user ID
         * @return the user profile
         */
        AppUserProfileResponseDto getUserById(UUID id);

        /**
         * Retrieves a user profile by email.
         *
         * @param userEmail the user's email
         * @return the user profile
         */
        AppUserProfileResponseDto getUserByEmail(String userEmail);

        /**
         * Retrieves a user profile by phone number.
         *
         * @param userPhoneNumber the user's phone number
         * @return the user profile
         */
        AppUserProfileResponseDto getUserByPhoneNumber(String userPhoneNumber);

        /**
         * Retrieves all users with pagination.
         *
         * @param pageable pagination parameters
         * @return paginated list of user profiles
         */
        Page<AppUserProfileResponseDto> getAllUsers(Pageable pageable);

        /**
         * Searches users by name (first name, last name, full name, or username).
         *
         * @param searchTerm the search term
         * @param pageable   pagination parameters
         * @return paginated search results
         */
        Page<AppUserProfileResponseDto> searchUsersByName(String searchTerm, Pageable pageable);

        /**
         * Filters users by status.
         *
         * @param status   the status to filter by
         * @param pageable pagination parameters
         * @return paginated filtered results
         */
        Page<AppUserProfileResponseDto> filterUsersByStatus(AppUserStatus status, Pageable pageable);

        /**
         * Searches users with combined filters.
         *
         * @param searchTerm the name search term
         * @param status     the status filter
         * @param pageable   pagination parameters
         * @return paginated filtered search results
         */
        Page<AppUserProfileResponseDto> searchUsersWithFilters(String searchTerm, AppUserStatus status,
                        Pageable pageable);

        // ========================================
        // Account Security & Verification
        // ========================================

        /**
         * Changes a user's password.
         *
         * @param id  the user ID
         * @param dto the password change data
         */
        void changePassword(UUID id, AppUserChangePasswordDto dto);

        /**
         * Initiates password reset by sending a verification code.
         *
         * @param dto the password reset request data
         */
        void requestPasswordReset(AppUserPasswordResetRequestDto dto);

        /**
         * Completes password reset after verifying the code.
         *
         * @param dto the password reset completion data
         */
        void completePasswordReset(AppUserPasswordResetCompleteDto dto);

        /**
         * Generates and sends email verification code.
         *
         * @param id the user ID
         * @return the generated verification code (for testing purposes)
         */
        String generateEmailVerificationCode(UUID id);

        /**
         * Generates and sends phone verification code.
         *
         * @param id the user ID
         * @return the generated verification code (for testing purposes)
         */
        String generatePhoneVerificationCode(UUID id);

        /**
         * Verifies user's email with the provided code.
         *
         * @param id               the user ID
         * @param verificationCode the verification code
         */
        void verifyEmail(UUID id, String verificationCode);

        /**
         * Verifies user's phone with the provided code.
         *
         * @param id               the user ID
         * @param verificationCode the verification code
         */
        void verifyPhone(UUID id, String verificationCode);

        // ========================================
        // Profile & Preferences
        // ========================================

        /**
         * Updates user's profile picture.
         *
         * @param id        the user ID
         * @param avatarUrl the new avatar URL
         */
        void updateProfilePicture(UUID id, String avatarUrl);

        /**
         * Updates the profile picture by uploading a new image file.
         *
         * @param id         the user ID
         * @param avatarFile multipart file containing the new avatar image
         * @return the path/URL of the uploaded avatar image
         * @throws AppUserNotFoundException if user not found
         * @throws InvalidRequestException  if file is invalid
         */
        String updateProfilePicture(UUID id, org.springframework.web.multipart.MultipartFile avatarFile);

        /**
         * Retrieves the user's avatar image file.
         *
         * @param id the user ID
         * @return the avatar image resource
         * @throws ResourceNotFoundException if avatar not found
         */
        org.springframework.core.io.Resource getAvatarFile(UUID id);

        /**
         * Updates user's marketing and privacy preferences.
         *
         * @param id              the user ID
         * @param marketingOptIn  marketing preference
         * @param privacySettings privacy settings JSON
         */
        void updatePreferences(UUID id, Boolean marketingOptIn, String privacySettings);

        /**
         * Exports all user data for GDPR compliance.
         *
         * @param id the user ID
         * @return structured data containing all personal information
         */
        AppUserDataExportDto exportUserData(UUID id);

        // ========================================
        // Account Lifecycle
        // ========================================

        /**
         * Requests account deletion for GDPR compliance.
         *
         * @param id        the user ID
         * @param deletedBy the admin ID requesting deletion
         */
        void requestAccountDeletion(UUID id, UUID deletedBy);

        /**
         * Permanently deletes a user profile.
         *
         * @param id the user ID
         */
        void deleteUser(UUID id);

        /**
         * Deactivates a user account without deleting it.
         *
         * @param id the user ID
         */
        void deactivateUser(UUID id);

        /**
         * Activates a previously deactivated user account.
         *
         * @param id the user ID
         */
        void activateUser(UUID id);

        /**
         * Self-service account reactivation.
         * Allows users to reactivate their deactivated account using email/phone and
         * password.
         *
         * @param dto the reactivation data
         * @return the reactivated user profile
         */
        AppUserProfileResponseDto reactivateAccount(AppUserReactivateAccountDto dto);

        /**
         * Records a successful login and updates last login timestamp.
         *
         * @param id the user ID
         * @return the updated user profile
         */
        AppUserProfileResponseDto recordLogin(UUID id);

        // ========================================
        // Audit & Activity
        // ========================================

        /**
         * Fetches user activity log.
         *
         * @param id the user ID
         * @return the activity log data
         */
        String getUserActivityLog(UUID id);

        // ========================================
        // Session Management
        // ========================================

        /**
         * Creates a new session for a user.
         *
         * @param userId     the user ID
         * @param deviceInfo device information
         * @param ipAddress  client IP address
         * @param userAgent  user agent string
         * @param location   geographic location
         * @return the created session
         */
        AppUserSessionDto createSession(UUID userId, String deviceInfo, String ipAddress, String userAgent,
                        String location);

        /**
         * Gets all active sessions for a user.
         *
         * @param userId the user ID
         * @return list of active sessions
         */
        List<AppUserSessionDto> getActiveSessions(UUID userId);

        /**
         * Invalidates a specific session.
         *
         * @param userId    the user ID
         * @param sessionId the session ID to invalidate
         */
        void invalidateSession(UUID userId, UUID sessionId);

        /**
         * Invalidates all sessions for a user (logout everywhere).
         *
         * @param userId the user ID
         */
        void invalidateAllSessions(UUID userId);

        // ========================================
        // OAuth/Social Login
        // ========================================

        /**
         * Login or register via OAuth provider (Google, Facebook, etc.).
         *
         * @param dto the OAuth login data
         * @return the user profile
         */
        AppUserProfileResponseDto oauthLogin(AppUserOAuthLoginDto dto);

        // ========================================
        // Account Lockout
        // ========================================

        /**
         * Unlocks a locked user account.
         *
         * @param userId the user ID
         */
        void unlockAccount(UUID userId);

        /**
         * Checks if an account is locked.
         *
         * @param userId the user ID
         * @return true if account is locked, false otherwise
         */
        boolean isAccountLocked(UUID userId);

        // ========================================
        // Bulk Operations (Admin)
        // ========================================

        /**
         * Bulk activates multiple user accounts.
         *
         * @param userIds list of user IDs to activate
         * @return operation result with success/failure counts
         */
        BulkOperationResultDto bulkActivateUsers(List<UUID> userIds);

        /**
         * Bulk deactivates multiple user accounts.
         *
         * @param userIds list of user IDs to deactivate
         * @return operation result with success/failure counts
         */
        BulkOperationResultDto bulkDeactivateUsers(List<UUID> userIds);

        /**
         * Bulk deletes multiple user accounts.
         *
         * @param userIds list of user IDs to delete
         * @return operation result with success/failure counts
         */
        BulkOperationResultDto bulkDeleteUsers(List<UUID> userIds);

        /**
         * Bulk unlocks multiple user accounts.
         *
         * @param userIds list of user IDs to unlock
         * @return operation result with success/failure counts
         */
        BulkOperationResultDto bulkUnlockAccounts(List<UUID> userIds);

        // ========================================
        // Audit Logging
        // ========================================

        /**
         * Creates an audit log entry for a user profile action.
         *
         * @param userProfileId the user profile ID being audited
         * @param action        the action performed
         * @param details       details about the action
         * @param actorId       the user who performed the action
         */
        void logAction(UUID userProfileId, String action, String details, UUID actorId);
}
