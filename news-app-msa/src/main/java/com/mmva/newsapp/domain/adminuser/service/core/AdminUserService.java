package com.mmva.newsapp.domain.adminuser.service.core;

import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserExportDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserRequestDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserResponseDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserUpdateDto;
import com.mmva.newsapp.domain.adminuser.dto.auth.AdminUserLoginRequestDto;
import com.mmva.newsapp.domain.adminuser.dto.auth.AdminUserChangePasswordDto;
import com.mmva.newsapp.domain.adminuser.exception.core.AdminNotFoundException;
import com.mmva.newsapp.infrastructure.common.exception.*;
import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface AdminUserService {
    /**
     * Autocomplete search for active admin users by partial name, username, or
     * email.
     *
     * @param query    partial string to match
     * @param pageable pagination info
     * @return page of matching admin user response DTOs
     */
    org.springframework.data.domain.Page<AdminUserResponseDto> searchActiveAdminsByNameOrUsernameOrEmail(String query,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Autocomplete search for active admin users by partial name, username, or
     * email.
     *
     * @param query    partial string to match
     * @param pageable pagination info
     * @return page of matching admin user response DTOs
     */

    // ...existing code...

    // ========================================
    // Authentication
    // ========================================

    /**
     * Authenticates an admindashboard user by username/email and password.
     *
     * @param loginRequest login credentials containing username/email and password
     * @return authenticated admindashboard user response with profile details
     * @throws InvalidCredentialsException if credentials
     *                                     are invalid
     */
    AdminUserResponseDto login(AdminUserLoginRequestDto loginRequest);

    // ========================================
    // Role & Permission Queries
    // ========================================

    /**
     * Returns all roles assigned to the admindashboard user.
     *
     * @param adminId admindashboard user UUID
     * @return list of role DTOs (currently supports single role, returned as list
     *         for extensibility)
     * @throws AdminNotFoundException if admindashboard not found
     */
    List<RoleResponseDto> getRolesForAdmin(UUID adminId);

    /**
     * Returns all permissions assigned to the admindashboard user via their role.
     *
     * @param adminId admindashboard user UUID
     * @return list of permission DTOs
     * @throws AdminNotFoundException if admindashboard not found
     */
    List<PermissionResponseDto> getPermissionsForAdmin(UUID adminId);

    /**
     * Checks if the admindashboard user has a specific permission.
     *
     * @param adminId        admindashboard user UUID
     * @param permissionName name of the permission to check
     * @return true if permission is granted, false otherwise
     * @throws AdminNotFoundException if admindashboard not found
     */
    boolean checkAdminPermission(UUID adminId, String permissionName);

    // ========================================
    // Account Lifecycle
    // ========================================

    /**
     * Creates a new admindashboard user.
     *
     * @param dto admindashboard user creation request data
     * @return created admindashboard user response
     * @throws DuplicateResourceException if username or
     *                                    email exists
     */
    AdminUserResponseDto createAdmin(AdminUserRequestDto dto);

    /**
     * Retrieves an admindashboard user by their unique ID.
     *
     * @param id admindashboard user UUID
     * @return admindashboard user response
     * @throws AdminNotFoundException if admindashboard not found
     */
    AdminUserResponseDto getAdminById(UUID id);

    /**
     * Retrieves an admindashboard user by their username.
     *
     * @param username admindashboard username
     * @return admindashboard user response
     * @throws AdminNotFoundException if admindashboard not found
     */
    AdminUserResponseDto getAdminByUsername(String username);

    /**
     * Retrieves an admindashboard user by their email address.
     *
     * @param email admindashboard email
     * @return admindashboard user response
     * @throws AdminNotFoundException if admindashboard not found
     */
    AdminUserResponseDto getAdminByEmail(String email);

    /**
     * Retrieves a paginated list of all admindashboard users.
     *
     * @param pageable pagination information
     * @return page of admindashboard user responses
     */
    Page<AdminUserResponseDto> getAllAdmins(Pageable pageable);

    /**
     * Retrieves only active (non-deleted) admindashboard users with pagination.
     * Use for dropdown selections or assignments.
     *
     * @param pageable pagination information
     * @return page of active admindashboard user responses
     */
    Page<AdminUserResponseDto> getActiveAdmins(Pageable pageable);

    /**
     * Retrieves only soft-deleted admindashboard users with pagination.
     * Use for admindashboard restore listing.
     *
     * @param pageable pagination information
     * @return page of deleted admindashboard user responses
     */
    Page<AdminUserResponseDto> getDeletedAdmins(Pageable pageable);

    /**
     * Updates an existing admindashboard user's details.
     *
     * @param id  admindashboard user UUID
     * @param dto update data
     * @return updated admindashboard user response
     * @throws AdminNotFoundException     if admindashboard not found
     * @throws DuplicateResourceException if new
     *                                    username/email
     *                                    exists
     */
    AdminUserResponseDto updateAdmin(UUID id, AdminUserUpdateDto dto);

    /**
     * Soft deletes an admindashboard user, recording who performed the deletion.
     *
     * @param id        admindashboard user UUID
     * @param deletedBy UUID of the admindashboard performing the deletion
     * @throws AdminNotFoundException if admindashboard not found
     */
    void deleteAdmin(UUID id, UUID deletedBy);

    /**
     * Restores a previously soft-deleted admindashboard user.
     *
     * @param id admindashboard user UUID
     * @throws AdminNotFoundException if admindashboard not found
     */
    void restoreAdmin(UUID id);

    // ========================================
    // Account Status
    // ========================================

    /**
     * Activates an admindashboard user account.
     *
     * @param id admindashboard user UUID
     * @throws AdminNotFoundException if admindashboard not found
     */
    void activateAdmin(UUID id);

    /**
     * Deactivates an admindashboard user account.
     *
     * @param id admindashboard user UUID
     * @throws AdminNotFoundException if admindashboard not found
     */
    void deactivateAdmin(UUID id);

    // ========================================
    // Authentication & Security
    // ========================================

    /**
     * Records a login event for an admindashboard user and updates last login
     * timestamp.
     *
     * @param id admindashboard user UUID
     * @return updated admindashboard user response
     * @throws AdminNotFoundException if admindashboard not found
     */
    AdminUserResponseDto recordLogin(UUID id);

    /**
     * Changes the password for an admindashboard user.
     *
     * @param id                admindashboard user UUID
     * @param changePasswordDto DTO containing current password, new password, and
     *                          confirmation
     * @throws AdminNotFoundException      if admindashboard not
     *                                     found
     * @throws InvalidCredentialsException if current
     *                                     password is wrong
     * @throws InvalidRequestException     if passwords
     *                                     don't match
     */
    void changePassword(UUID id, AdminUserChangePasswordDto changePasswordDto);

    /**
     * Initiates a password reset for an admindashboard user.
     *
     * @param loginId admindashboard login identifier (username or email)
     */
    void requestPasswordReset(String loginId);

    /**
     * Generates and sends an email verification code to the admindashboard user.
     *
     * @param id admindashboard user UUID
     * @return success message (code is sent via email, not returned for security)
     * @throws AdminNotFoundException  if admindashboard not found
     * @throws InvalidRequestException if email already
     *                                 verified
     */
    String generateEmailVerificationCode(UUID id);

    /**
     * Verifies the email address of an admindashboard user using a verification
     * code.
     *
     * @param id               admindashboard user UUID
     * @param verificationCode verification code to validate
     * @throws AdminNotFoundException  if admindashboard not found
     * @throws InvalidRequestException if code is
     *                                 invalid/expired
     */
    void verifyEmail(UUID id, String verificationCode);

    // ========================================
    // Role Management
    // ========================================

    /**
     * Assigns a role to an admin user.
     *
     * @param id     admin user UUID
     * @param roleId role UUID to assign
     * @throws AdminNotFoundException    if admin not found
     * @throws ResourceNotFoundException if role not found
     */
    void assignRole(UUID id, UUID roleId);

    /**
     * Revokes a role from an admin user.
     *
     * @param id     admin user UUID
     * @param roleId role UUID to revoke
     * @throws AdminNotFoundException if admin not found
     */
    void revokeRole(UUID id, UUID roleId);

    /**
     * Retrieves a paginated list of admindashboard users by role.
     *
     * @param roleName role name to filter by
     * @param pageable pagination information
     * @return page of admindashboard user responses
     */
    Page<AdminUserResponseDto> getAdminsByRole(String roleName, Pageable pageable);

    // ========================================
    // Profile & Data
    // ========================================

    /**
     * Updates the profile picture URL of an admindashboard user.
     *
     * @param id        admindashboard user UUID
     * @param avatarUrl URL of the new profile picture
     * @throws AdminNotFoundException if admindashboard not found
     */
    void updateProfilePicture(UUID id, String avatarUrl);

    /**
     * Updates the profile picture by uploading a new image file.
     *
     * @param id         admindashboard user UUID
     * @param avatarFile multipart file containing the new avatar image
     * @return the path/URL of the uploaded avatar image
     * @throws AdminNotFoundException  if admindashboard not found
     * @throws InvalidRequestException if file is invalid
     */
    String updateProfilePicture(UUID id, MultipartFile avatarFile);

    /**
     * Retrieves the avatar image file for the given admindashboard user.
     *
     * @param id admindashboard user UUID
     * @return resource representing the avatar image
     * @throws AdminNotFoundException    if admindashboard not found
     * @throws InvalidRequestException   if no avatar set
     * @throws ResourceNotFoundException if avatar file not
     *                                   found
     */
    Resource getAvatarFile(UUID id);

    /**
     * Exports the data of an admindashboard user for compliance purposes.
     *
     * @param id admindashboard user UUID
     * @return exported data as AdminUserExportDto (excludes sensitive data like
     *         passwords)
     * @throws AdminNotFoundException if admindashboard not found
     */
    AdminUserExportDto exportAdminData(UUID id);

    // ========================================
    // Account Deletion & Audit
    // ========================================

    /**
     * Requests account deletion for an admindashboard user (soft delete with
     * deletion
     * request flag).
     *
     * @param id admindashboard user UUID
     * @throws AdminNotFoundException if admindashboard not found
     */
    void requestAccountDeletion(UUID id);

    /**
     * Retrieves a summary of the audit log for an admindashboard user.
     *
     * @param id admindashboard user UUID
     * @return audit log summary as string
     * @throws AdminNotFoundException if admindashboard not found
     */
    String getAuditLog(UUID id);

    /**
     * Creates an audit log entry for an admindashboard user action.
     *
     * @param adminUserId the admindashboard user ID being audited
     * @param action      the action performed
     * @param details     details about the action
     * @param actorId     the user who performed the action
     */
    void logAction(UUID adminUserId, String action, String details, UUID actorId);
}
