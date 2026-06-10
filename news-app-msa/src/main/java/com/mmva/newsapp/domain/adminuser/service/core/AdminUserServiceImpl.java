package com.mmva.newsapp.domain.adminuser.service.core;

import com.mmva.newsapp.domain.adminuser.config.core.AdminUserCacheConstants;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserExportDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserRequestDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserResponseDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserUpdateDto;
import com.mmva.newsapp.domain.adminuser.dto.auth.AdminUserLoginRequestDto;
import com.mmva.newsapp.domain.adminuser.dto.auth.AdminUserChangePasswordDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleResponseDto;
import com.mmva.newsapp.domain.adminuser.exception.core.AdminNotFoundException;
import com.mmva.newsapp.infrastructure.common.exception.DuplicateResourceException;
import com.mmva.newsapp.infrastructure.security.exception.InvalidCredentialsException;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.domain.adminuser.mapper.AdminUserMapper;
import com.mmva.newsapp.infrastructure.rbac.permission.core.mapper.PermissionMapper;
import com.mmva.newsapp.infrastructure.rbac.role.core.mapper.RoleMapper;
import com.mmva.newsapp.domain.adminuser.model.core.AdminUser;
import com.mmva.newsapp.domain.adminuser.enums.core.AdminStatus;
import com.mmva.newsapp.domain.adminuser.enums.core.AuditAction;
import com.mmva.newsapp.infrastructure.rbac.role.core.model.RbacRole;
import com.mmva.newsapp.domain.adminuser.repository.core.AdminUserRepository;
import com.mmva.newsapp.infrastructure.common.audit.repository.SoftDeleteSpec;
import com.mmva.newsapp.infrastructure.rbac.role.core.repository.RoleRepository;
import com.mmva.newsapp.infrastructure.email.service.EmailService;
import com.mmva.newsapp.domain.adminuser.service.audit.AdminUserAuditLogService;
import com.mmva.newsapp.domain.adminuser.model.audit.AdminUserAuditLog;
import com.mmva.newsapp.domain.adminuser.repository.audit.AdminUserAuditLogRepository;
import com.mmva.newsapp.domain.adminuser.audit.constants.AdminUserAuditActions;
import com.mmva.newsapp.infrastructure.common.audit.service.AuditingUtility;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestInfoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of {@link AdminUserService} for managing admindashboard/staff
 * user
 * accounts.
 * 
 * <p>
 * Provides comprehensive operations for staff lifecycle management including
 * authentication, role management, profile updates, and audit logging.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {

    // ========================================
    // Dependencies
    // ========================================

    private final AdminUserRepository adminUserRepository;
    private final AdminUserAuditLogService adminUserAuditLogService;
    private final AdminUserAuditLogRepository adminUserAuditLogRepository;
    private final AdminUserMapper adminUserMapper;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final EmailService emailService;
    private final AuditingUtility auditingUtility;
    private final RequestInfoService requestInfoService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ========================================
    // Configuration
    // ========================================

    @Value("${media.entities.admin.avatars:${media.root-path}/processed/admin/avatars}")
    private String avatarFolder;

    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 15;

    // ========================================
    // Authentication
    // ========================================

    @Override
    public AdminUserResponseDto login(AdminUserLoginRequestDto loginRequest) {
        log.info("Authenticating admindashboard with login: {}",
                loginRequest != null ? loginRequest.getAdminUsersUsernameOrEmail() : "null");

        validateLoginRequest(loginRequest);

        // After validation, loginRequest is guaranteed non-null
        Objects.requireNonNull(loginRequest, "Login request cannot be null after validation");

        AdminUser adminUser = findAdminByLoginId(loginRequest.getAdminUsersUsernameOrEmail());

        // CHECK ACCOUNT STATUS BEFORE PASSWORD VALIDATION
        if (adminUser.getAdminUsersStatus() == AdminStatus.INACTIVE) {
            log.warn("Login attempt for deactivated admin account: {}", loginRequest.getAdminUsersUsernameOrEmail());
            throw new InvalidCredentialsException("Account is deactivated. Please contact system administrator.");
        }

        if (adminUser.getAdminUsersStatus() == AdminStatus.SUSPENDED) {
            log.warn("Login attempt for suspended admin account: {}", loginRequest.getAdminUsersUsernameOrEmail());
            throw new InvalidCredentialsException("Account is temporarily suspended. Please try again later.");
        }

        if (adminUser.getAdminUsersStatus() == AdminStatus.BANNED) {
            log.warn("Login attempt for banned admin account: {}", loginRequest.getAdminUsersUsernameOrEmail());
            throw new InvalidCredentialsException("Account has been permanently banned.");
        }

        if (adminUser.getAdminUsersStatus() == AdminStatus.DELETED) {
            log.warn("Login attempt for deleted admin account: {}", loginRequest.getAdminUsersUsernameOrEmail());
            throw new InvalidCredentialsException("Account not found.");
        }

        // NOW validate password (only for ACTIVE accounts)
        validatePassword(loginRequest.getAdminUsersPassword(), adminUser.getAdminUsersPasswordHash());

        // Update last login timestamp
        adminUser.setAdminUsersLastLogin(Instant.now());
        adminUserRepository.save(adminUser);

        createAuditLog(adminUser.getAdminUsersId(), AuditAction.LOGIN, adminUser.getAdminUsersId());

        log.info("Admin authenticated successfully: {}", loginRequest.getAdminUsersUsernameOrEmail());
        return adminUserMapper.toResponseDto(adminUser);
    }

    // ========================================
    // Role & Permission Queries
    // ========================================

    @Override
    public List<RoleResponseDto> getRolesForAdmin(UUID adminId) {
        log.debug("Fetching roles for admindashboard ID: {}", adminId);
        validateNotNull(adminId, "Admin ID");

        AdminUser adminUser = findAdminById(adminId);

        if (adminUser.getRole() == null) {
            log.debug("No role assigned to admindashboard ID: {}", adminId);
            return Collections.emptyList();
        }

        log.debug("Roles fetched successfully for admindashboard ID: {}", adminId);
        return Collections.singletonList(roleMapper.toResponseDto(adminUser.getRole()));
    }

    @Override
    public List<PermissionResponseDto> getPermissionsForAdmin(UUID adminId) {
        log.debug("Fetching permissions for admindashboard ID: {}", adminId);
        validateNotNull(adminId, "Admin ID");

        AdminUser adminUser = findAdminById(adminId);
        RbacRole role = adminUser.getRole();

        if (role == null) {
            log.debug("No role assigned to admindashboard ID: {}. Returning empty permission list.", adminId);
            return Collections.emptyList();
        }

        if (role.getPermissions() == null) {
            log.debug("No permissions found for admindashboard ID: {}", adminId);
            return Collections.emptyList();
        }

        List<PermissionResponseDto> permissions = role.getPermissions().stream()
                .map(permissionMapper::toResponseDto)
                .toList();

        log.debug("Fetched {} permissions for admindashboard ID: {}", permissions.size(), adminId);
        return permissions;
    }

    @Override
    public boolean checkAdminPermission(UUID adminId, String permissionName) {
        log.debug("Checking permission '{}' for admindashboard ID: {}", permissionName, adminId);
        validateNotNull(adminId, "Admin ID");
        validateNotBlank(permissionName, "Permission name");

        AdminUser adminUser = findAdminById(adminId);
        RbacRole role = adminUser.getRole();

        if (role == null) {
            log.debug("No role assigned to admindashboard ID: {}. Permission '{}' denied.", adminId, permissionName);
            return false;
        }

        if (role.getPermissions() == null) {
            log.debug("No permissions found for admindashboard ID: {}. Permission '{}' denied.", adminId,
                    permissionName);
            return false;
        }

        boolean hasPermission = role.getPermissions().stream()
                .anyMatch(p -> permissionName.equalsIgnoreCase(p.getPermissionName()));

        log.debug("Permission '{}' for admindashboard ID {}: {}", permissionName, adminId,
                hasPermission ? "GRANTED" : "DENIED");
        return hasPermission;
    }

    // ========================================
    // Account Lifecycle
    // ========================================

    @Override
    @Transactional
    @CacheEvict(value = AdminUserCacheConstants.ADMIN_LIST_CACHE, allEntries = true)
    public AdminUserResponseDto createAdmin(AdminUserRequestDto dto) {
        log.info("Creating new admindashboard user with username: {}", dto.getAdminUsersUsername());

        validateUniqueUsername(dto.getAdminUsersUsername());
        validateUniqueEmail(dto.getAdminUsersEmail());

        AdminUser adminUser = adminUserMapper.toEntity(dto);

        if (dto.getAdminUsersRoleId() != null) {
            RbacRole role = findRoleById(dto.getAdminUsersRoleId());
            adminUser.setRole(role);
        }

        adminUser.setAdminUsersPasswordHash(hashPassword(dto.getAdminUsersPassword()));
        adminUser = adminUserRepository.save(adminUser);

        createAuditLog(adminUser.getAdminUsersId(), AuditAction.CREATE, adminUser.getAdminUsersId());

        log.info("Admin user created successfully with ID: {}", adminUser.getAdminUsersId());
        return adminUserMapper.toResponseDto(adminUser);
    }

    @Override
    @Cacheable(value = AdminUserCacheConstants.ADMIN_CACHE, key = "#adminId")
    public AdminUserResponseDto getAdminById(UUID adminId) {
        log.debug("Fetching admindashboard user by ID: {}", adminId);
        validateNotNull(adminId, "Admin ID");

        AdminUser adminUser = findAdminById(adminId);

        log.debug("Admin user fetched successfully: {}", adminId);
        return adminUserMapper.toResponseDto(adminUser);
    }

    @Override
    public AdminUserResponseDto getAdminByUsername(String adminUsername) {
        log.debug("Fetching admindashboard user by username: {}", adminUsername);

        AdminUser adminUser = adminUserRepository.findByAdminUsersUsername(adminUsername)
                .orElseThrow(() -> {
                    log.warn("Admin user not found with username: {}", adminUsername);
                    return new AdminNotFoundException("username", adminUsername);
                });

        log.debug("Admin user fetched successfully by username: {}", adminUsername);
        return adminUserMapper.toResponseDto(adminUser);
    }

    @Override
    public AdminUserResponseDto getAdminByEmail(String adminEmail) {
        log.debug("Fetching admindashboard user by email: {}", adminEmail);

        AdminUser adminUser = adminUserRepository.findByAdminUsersEmail(adminEmail)
                .orElseThrow(() -> {
                    log.warn("Admin user not found with email: {}", adminEmail);
                    return new AdminNotFoundException("email", adminEmail);
                });

        log.debug("Admin user fetched successfully by email: {}", adminEmail);
        return adminUserMapper.toResponseDto(adminUser);
    }

    @Override
    @Cacheable(value = AdminUserCacheConstants.ADMIN_LIST_CACHE, key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<AdminUserResponseDto> getAllAdmins(Pageable pageable) {
        log.debug("Fetching admindashboard users (including deleted) - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Admin API: Use SoftDeleteSpec.includeDeleted() to see all records
        Page<AdminUser> page = adminUserRepository.findAll(SoftDeleteSpec.includeDeleted(), pageable);

        log.debug("Fetched {} admindashboard users on page {}", page.getNumberOfElements(), pageable.getPageNumber());
        return page.map(adminUserMapper::toResponseDto);
    }

    /**
     * Get only active (non-deleted) admindashboard users.
     * Use for dropdown selections or public views.
     *
     * @param pageable pagination info
     * @return page of active admindashboard users
     */
    @Override
    public Page<AdminUserResponseDto> getActiveAdmins(Pageable pageable) {
        log.debug("Fetching active admindashboard users only - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Use SoftDeleteSpec.notDeleted() to exclude soft-deleted
        Page<AdminUser> page = adminUserRepository.findAll(SoftDeleteSpec.notDeleted(), pageable);

        log.debug("Fetched {} active admindashboard users on page {}", page.getNumberOfElements(),
                pageable.getPageNumber());
        return page.map(adminUserMapper::toResponseDto);
    }

    /**
     * Get only soft-deleted admindashboard users for restore listing.
     *
     * @param pageable pagination info
     * @return page of deleted admindashboard users
     */
    @Override
    public Page<AdminUserResponseDto> getDeletedAdmins(Pageable pageable) {
        log.debug("Fetching deleted admindashboard users only - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Admin restore listing: Use SoftDeleteSpec.onlyDeleted()
        Page<AdminUser> page = adminUserRepository.findAll(SoftDeleteSpec.onlyDeleted(), pageable);

        log.debug("Fetched {} deleted admindashboard users on page {}", page.getNumberOfElements(),
                pageable.getPageNumber());
        return page.map(adminUserMapper::toResponseDto);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_CACHE, key = "#id"),
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_LIST_CACHE, allEntries = true),
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_BY_ROLE_CACHE, allEntries = true)
    })
    public AdminUserResponseDto updateAdmin(UUID id, AdminUserUpdateDto dto) {
        log.info("Updating admindashboard user with ID: {}", id);
        validateNotNull(id, "Admin ID");

        AdminUser adminUser = findAdminById(id);

        validateUniqueUsernameForUpdate(dto.getAdminUsersUsername(), adminUser);
        validateUniqueEmailForUpdate(dto.getAdminUsersEmail(), adminUser);

        adminUserMapper.updateEntityFromDto(dto, adminUser);

        // Update role if provided and different
        UUID currentRoleId = adminUser.getRole() != null ? adminUser.getRole().getRoleId() : null;
        if (dto.getAdminUsersRoleId() != null && !dto.getAdminUsersRoleId().equals(currentRoleId)) {
            RbacRole role = findRoleById(dto.getAdminUsersRoleId());
            adminUser.setRole(role);
        }

        adminUser = adminUserRepository.save(adminUser);
        createAuditLog(id, AuditAction.UPDATE, id);

        log.info("Admin user updated successfully: {}", id);
        return adminUserMapper.toResponseDto(adminUser);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_CACHE, key = "#id"),
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_LIST_CACHE, allEntries = true),
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_BY_ROLE_CACHE, allEntries = true)
    })
    public void deleteAdmin(UUID id, UUID deletedBy) {
        log.info("Soft deleting admindashboard user ID: {} by admindashboard: {}", id, deletedBy);
        validateNotNull(id, "Admin ID");

        AdminUser adminUser = findAdminById(id);

        adminUser.setAdminUsersStatus(AdminStatus.DELETED);
        adminUser.setDeletedAt(Instant.now());
        adminUser.setDeletedBy(deletedBy);
        adminUserRepository.save(adminUser);

        createAuditLog(id, AuditAction.DELETE, deletedBy);

        log.info("Admin user soft deleted successfully: {} by {}", id, deletedBy);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_CACHE, key = "#id"),
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_LIST_CACHE, allEntries = true)
    })
    public void restoreAdmin(UUID id) {
        log.info("Restoring soft-deleted admindashboard user: {}", id);
        validateNotNull(id, "Admin ID");

        // Use Specification to find deleted admindashboard
        AdminUser adminUser = adminUserRepository.findOne(
                SoftDeleteSpec.<AdminUser>onlyDeleted()
                        .and((root, query, cb) -> cb.equal(root.get("adminUsersId"), id)))
                .orElseThrow(() -> {
                    log.warn("Deleted admindashboard user not found with ID: {}", id);
                    return new AdminNotFoundException("id", id.toString());
                });

        adminUser.setAdminUsersStatus(AdminStatus.ACTIVE);
        adminUser.setDeletedAt(null);
        adminUser.setDeletedBy(null);
        adminUserRepository.save(adminUser);

        createAuditLog(id, AuditAction.RESTORE, id);

        log.info("Admin user restored successfully: {}", id);
    }

    // ========================================
    // Account Status
    // ========================================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_CACHE, key = "#id"),
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_LIST_CACHE, allEntries = true)
    })
    public void activateAdmin(UUID id) {
        log.info("Activating admindashboard user ID: {}", id);
        validateNotNull(id, "Admin ID");

        AdminUser adminUser = findAdminById(id);

        adminUser.setAdminUsersStatus(AdminStatus.ACTIVE);
        adminUser.setUpdatedAt(Instant.now());
        adminUserRepository.save(adminUser);

        createAuditLog(id, AuditAction.ACTIVATE, id);

        log.info("Admin user activated successfully: {}", id);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_CACHE, key = "#id"),
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_LIST_CACHE, allEntries = true)
    })
    public void deactivateAdmin(UUID id) {
        log.info("Deactivating admindashboard user ID: {}", id);
        validateNotNull(id, "Admin ID");

        AdminUser adminUser = findAdminById(id);

        adminUser.setAdminUsersStatus(AdminStatus.INACTIVE);
        adminUser.setUpdatedAt(Instant.now());
        adminUserRepository.save(adminUser);

        createAuditLog(id, AuditAction.DEACTIVATE, id);

        log.info("Admin user deactivated successfully: {}", id);
    }

    // ========================================
    // Authentication & Security
    // ========================================

    @Override
    @Transactional
    public AdminUserResponseDto recordLogin(UUID id) {
        log.info("Recording login for admindashboard user ID: {}", id);
        validateNotNull(id, "Admin ID");

        AdminUser adminUser = findAdminById(id);

        adminUser.setAdminUsersLastLogin(Instant.now());
        adminUser = adminUserRepository.save(adminUser);

        createAuditLog(id, AuditAction.LOGIN, id);

        log.info("Admin login recorded successfully: {}", id);
        return adminUserMapper.toResponseDto(adminUser);
    }

    @Override
    @Transactional
    public void changePassword(UUID id, AdminUserChangePasswordDto changePasswordDto) {
        log.info("Changing password for admindashboard ID: {}", id);
        validateNotNull(id, "Admin ID");

        // Validate password confirmation
        if (!changePasswordDto.getAdminUsersNewPassword().equals(changePasswordDto.getAdminUsersConfirmPassword())) {
            throw new InvalidRequestException("password", "New password and confirm password do not match");
        }

        AdminUser adminUser = findAdminById(id);

        // Verify current password using BCrypt
        if (!passwordEncoder.matches(changePasswordDto.getAdminUsersCurrentPassword(),
                adminUser.getAdminUsersPasswordHash())) {
            log.warn("Password change failed - current password incorrect for admin ID: {}", id);
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        adminUser.setAdminUsersPasswordHash(hashPassword(changePasswordDto.getAdminUsersNewPassword()));
        adminUserRepository.save(adminUser);

        createAuditLog(id, AuditAction.CHANGE_PASSWORD, id);

        log.info("Password changed successfully for admindashboard ID: {}", id);
    }

    @Override
    public void requestPasswordReset(String loginId) {
        log.info("Requesting password reset for login ID: {}", loginId);
        // TODO: Implement sending email/SMS with reset link or OTP
        log.info("Password reset request processed for: {} (stub implementation)", loginId);
    }

    @Override
    @Transactional
    @CacheEvict(value = AdminUserCacheConstants.ADMIN_CACHE, key = "#id")
    public String generateEmailVerificationCode(UUID id) {
        log.info("Generating email verification code for admindashboard ID: {}", id);
        validateNotNull(id, "Admin ID");

        AdminUser adminUser = findAdminById(id);

        // Check if already verified
        if (Boolean.TRUE.equals(adminUser.getAdminUsersEmailVerified())) {
            throw new InvalidRequestException("email", "Email is already verified");
        }

        // Generate a 6-digit verification code using SecureRandom
        String verificationCode = generateSecureVerificationCode();
        Instant expiresAt = Instant.now().plusSeconds(VERIFICATION_CODE_EXPIRY_MINUTES * 60L);

        adminUser.setAdminUsersEmailVerificationCode(verificationCode);
        adminUser.setAdminUsersEmailVerificationExpiresAt(expiresAt);
        adminUserRepository.save(adminUser);

        // Send verification email
        emailService.sendVerificationCode(adminUser.getAdminUsersEmail(), verificationCode);

        createAuditLog(id, AuditAction.EMAIL_VERIFICATION_SENT, id);

        log.info("Email verification code sent to admindashboard ID: {} (expires at: {})", id, expiresAt);
        return "Verification code sent to your email";
    }

    @Override
    @Transactional
    public void verifyEmail(UUID id, String verificationCode) {
        log.info("Verifying email for admindashboard ID: {}", id);
        validateNotNull(id, "Admin ID");
        validateNotBlank(verificationCode, "Verification code");

        AdminUser adminUser = findAdminById(id);

        // Validate verification code
        if (adminUser.getAdminUsersEmailVerificationCode() == null) {
            throw new InvalidRequestException("verification",
                    "No verification code was generated for this admindashboard");
        }
        if (!verificationCode.equals(adminUser.getAdminUsersEmailVerificationCode())) {
            throw new InvalidRequestException("verification", "Invalid verification code");
        }

        // Check if code has expired
        if (adminUser.getAdminUsersEmailVerificationExpiresAt() != null) {
            Instant expiresAt = adminUser.getAdminUsersEmailVerificationExpiresAt();
            if (Instant.now().isAfter(expiresAt)) {
                throw new InvalidRequestException("verification", "Verification code has expired");
            }
        }

        adminUser.setAdminUsersEmailVerified(true);
        adminUser.setAdminUsersEmailVerificationCode(null);
        adminUser.setAdminUsersEmailVerificationExpiresAt(null);
        adminUserRepository.save(adminUser);

        createAuditLog(id, AuditAction.VERIFY_EMAIL, id);

        log.info("Email verified successfully for admindashboard ID: {}", id);
    }

    // ========================================
    // Role Management
    // ========================================

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_CACHE, key = "#id"),
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_BY_ROLE_CACHE, allEntries = true),
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_LIST_CACHE, allEntries = true)
    })
    public void assignRole(UUID id, UUID roleId) {
        log.info("Assigning role ID {} to admin ID: {}", roleId, id);
        validateNotNull(id, "Admin ID");
        validateNotNull(roleId, "Role ID");

        AdminUser adminUser = findAdminById(id);
        log.debug("Admin found. Current role: {}",
                adminUser.getRole() != null ? adminUser.getRole().getRoleName() : "NONE");

        RbacRole role = findRoleById(roleId);
        log.debug("Role found: {} (ID: {})", role.getRoleName(), role.getRoleId());

        adminUser.setRole(role);
        AdminUser savedAdmin = adminUserRepository.save(adminUser);
        log.debug("Admin saved successfully. New role_id: {}",
                savedAdmin.getRole() != null ? savedAdmin.getRole().getRoleId() : "NULL");

        createAuditLogWithDetails(id, AuditAction.ASSIGN_ROLE,
                AuditAction.ASSIGN_ROLE.getDescription() + ": " + role.getRoleName(), id);

        log.info("Role {} ({}) assigned successfully to admin ID: {}", role.getRoleName(), roleId, id);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_CACHE, key = "#id"),
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_BY_ROLE_CACHE, allEntries = true),
            @CacheEvict(value = AdminUserCacheConstants.ADMIN_LIST_CACHE, allEntries = true)
    })
    public void revokeRole(UUID id, UUID roleId) {
        log.info("Revoking role ID {} from admin ID: {}", roleId, id);
        validateNotNull(id, "Admin ID");
        validateNotNull(roleId, "Role ID");

        AdminUser adminUser = findAdminById(id);
        RbacRole currentRole = adminUser.getRole();

        log.debug("Admin found. Current role: {}", currentRole != null ? currentRole.getRoleName() : "NONE");

        if (currentRole != null && roleId.equals(currentRole.getRoleId())) {
            log.debug("Role match confirmed. Revoking role: {}", currentRole.getRoleName());
            adminUser.setRole(null);
            AdminUser savedAdmin = adminUserRepository.save(adminUser);
            log.debug("Admin saved successfully. Role_id cleared: {}", savedAdmin.getRole() == null ? "YES" : "NO");

            createAuditLogWithDetails(id, AuditAction.REVOKE_ROLE,
                    AuditAction.REVOKE_ROLE.getDescription() + ": " + currentRole.getRoleName(), id);

            log.info("Role {} ({}) revoked successfully from admin ID: {}", currentRole.getRoleName(), roleId, id);
        } else {
            log.warn("Admin ID {} does not have role ID {} to revoke. Current role: {}",
                    id, roleId, currentRole != null ? currentRole.getRoleId() : "NONE");
        }
    }

    @Override
    @Cacheable(value = AdminUserCacheConstants.ADMIN_BY_ROLE_CACHE, key = "#roleName + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<AdminUserResponseDto> getAdminsByRole(String roleName, Pageable pageable) {
        log.debug("Fetching admins by role '{}' - page: {}, size: {}", roleName,
                pageable.getPageNumber(), pageable.getPageSize());

        Page<AdminUserResponseDto> result = adminUserRepository.findByRoleName(roleName, pageable)
                .map(adminUserMapper::toResponseDto);

        log.debug("Fetched {} admins with role '{}'", result.getNumberOfElements(), roleName);
        return result;
    }

    // ========================================
    // Profile & Data
    // ========================================

    @Override
    @Transactional
    public void updateProfilePicture(UUID id, String avatarUrl) {
        log.info("Updating profile picture URL for admindashboard ID: {}", id);
        validateNotNull(id, "Admin ID");

        AdminUser adminUser = findAdminById(id);

        adminUser.setAdminUsersAvatarUrl(avatarUrl);
        adminUserRepository.save(adminUser);

        createAuditLog(id, AuditAction.UPDATE_PROFILE_PICTURE, id);

        log.info("Profile picture URL updated successfully for admindashboard ID: {}", id);
    }

    @Override
    @Transactional
    public String updateProfilePicture(UUID id, MultipartFile avatarFile) {
        log.info("Uploading new avatar for admindashboard ID: {}", id);
        validateNotNull(id, "Admin ID");

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new InvalidRequestException("avatarFile", "Avatar file is required");
        }

        AdminUser adminUser = findAdminById(id);

        String newFileName = UUID.randomUUID() + "_" + avatarFile.getOriginalFilename();
        String filePath = resolveAvatarFilePath(newFileName);

        ensureAvatarFolderExists();

        File target = new File(filePath);
        try {
            avatarFile.transferTo(target);
            adminUser.setAdminUsersAvatarUrl(filePath);
            adminUserRepository.save(adminUser);

            createAuditLog(id, AuditAction.UPDATE_PROFILE_PICTURE, id);

            log.info("Avatar uploaded and profile updated for admindashboard ID: {}", id);
            return filePath;
        } catch (Exception e) {
            if (target.exists()) {
                target.delete();
            }
            log.error("Failed to upload avatar for admindashboard ID: {}. Error: {}", id, e.getMessage());
            throw new InvalidRequestException("avatarFile", "Failed to upload avatar: " + e.getMessage());
        }
    }

    @Override
    public Resource getAvatarFile(UUID id) {
        log.debug("Fetching avatar file for admindashboard ID: {}", id);
        validateNotNull(id, "Admin ID");

        AdminUser adminUser = findAdminById(id);

        String avatarPath = adminUser.getAdminUsersAvatarUrl();
        if (avatarPath == null || avatarPath.isBlank()) {
            log.warn("No avatar set for admindashboard user ID: {}", id);
            throw new InvalidRequestException("avatar", "No avatar set for this admindashboard user");
        }

        File file = new File(avatarPath);
        if (!file.exists()) {
            log.warn("Avatar file not found: {}", avatarPath);
            throw new ResourceNotFoundException("Avatar file", "path", avatarPath);
        }

        try {
            Resource resource = new UrlResource(file.toURI());
            log.debug("Avatar file fetched successfully: {}", avatarPath);
            return resource;
        } catch (Exception e) {
            log.error("Cannot read avatar file: {}. Error: {}", avatarPath, e.getMessage());
            throw new InvalidRequestException("avatar", "Cannot read avatar file: " + avatarPath);
        }
    }

    @Override
    public AdminUserExportDto exportAdminData(UUID id) {
        log.debug("Exporting admindashboard data for ID: {}", id);
        validateNotNull(id, "Admin ID");

        AdminUser adminUser = findAdminById(id);

        AdminUserExportDto exportDto = AdminUserExportDto.builder()
                .adminUsersId(adminUser.getAdminUsersId())
                .adminUsersUsername(adminUser.getAdminUsersUsername())
                .adminUsersEmail(adminUser.getAdminUsersEmail())
                .adminUsersFirstName(adminUser.getAdminUsersFirstName())
                .adminUsersLastName(adminUser.getAdminUsersLastName())
                .adminUsersPhoneNumber(null)
                .adminUsersRoleName(adminUser.getRole() != null ? adminUser.getRole().getRoleName() : null)
                .adminUsersStatus(
                        adminUser.getAdminUsersStatus() != null ? adminUser.getAdminUsersStatus().name() : "UNKNOWN")
                .adminUsersAvatarUrl(adminUser.getAdminUsersAvatarUrl())
                .adminUsersEmailVerified(Boolean.TRUE.equals(adminUser.getAdminUsersEmailVerified()))
                .adminUsersPhoneVerified(false)
                .adminUsersTwoFactorEnabled(false)
                .createdAt(adminUser.getCreatedAt() != null ? adminUser.getCreatedAt().toString() : null)
                .updatedAt(adminUser.getUpdatedAt() != null ? adminUser.getUpdatedAt().toString() : null)
                .adminUsersLastLoginAt(
                        adminUser.getAdminUsersLastLogin() != null ? adminUser.getAdminUsersLastLogin().toString()
                                : null)
                .exportedAt(Instant.now().toString())
                .build();

        log.debug("Admin data exported for ID: {}", id);
        return exportDto;
    }

    @Override
    public Page<AdminUserResponseDto> searchActiveAdminsByNameOrUsernameOrEmail(String query, Pageable pageable) {
        String effectiveQuery = (query == null) ? "" : query.trim();
        Page<AdminUser> page = adminUserRepository.searchActiveByNameOrUsernameOrEmail(effectiveQuery, pageable);
        return page.map(adminUserMapper::toResponseDto);
    }

    // ========================================
    // Account Deletion & Audit
    // ========================================

    @Override
    @Transactional
    public void requestAccountDeletion(UUID id) {
        log.info("Requesting account deletion for admindashboard ID: {}", id);
        validateNotNull(id, "Admin ID");

        AdminUser adminUser = findAdminById(id);

        adminUser.setDeletedAt(Instant.now());
        adminUser.setAdminUsersStatus(AdminStatus.DELETED);
        adminUserRepository.save(adminUser);

        createAuditLog(id, AuditAction.REQUEST_ACCOUNT_DELETION, id);

        log.info("Account deletion requested successfully for admindashboard ID: {}", id);
    }

    @Override
    public String getAuditLog(UUID id) {
        log.debug("Fetching audit log summary for admindashboard ID: {}", id);
        validateNotNull(id, "Admin ID");

        AdminUser adminUser = findAdminById(id);

        log.debug("Audit log summary fetched for admindashboard ID: {}", id);
        return "Last login: " + adminUser.getAdminUsersLastLogin();
    }

    @Override
    public void logAction(UUID adminUserId, String action, String details, UUID actorId) {
        log.info("Creating audit log for admindashboard ID: {} action: {}", adminUserId, action);
        adminUserAuditLogService.logAction(adminUserId, action, details, actorId);
        log.info("Audit log created successfully for admindashboard ID: {}", adminUserId);
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * Finds an admindashboard user by ID or throws AdminNotFoundException.
     */
    private AdminUser findAdminById(UUID adminId) {
        return adminUserRepository.findById(adminId)
                .orElseThrow(() -> {
                    log.warn("Admin user not found with ID: {}", adminId);
                    return new AdminNotFoundException("id", adminId.toString());
                });
    }

    /**
     * Finds an admindashboard user by login ID (email or username).
     */
    private AdminUser findAdminByLoginId(String loginId) {
        if (loginId.contains("@")) {
            return adminUserRepository.findByAdminUsersEmail(loginId)
                    .orElseThrow(InvalidCredentialsException::new);
        }
        return adminUserRepository.findByAdminUsersUsername(loginId)
                .orElseThrow(InvalidCredentialsException::new);
    }

    /**
     * Finds a role by ID or throws ResourceNotFoundException.
     */
    private RbacRole findRoleById(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId.toString()));
    }

    /**
     * Finds a role by name or throws ResourceNotFoundException.
     * Uses findActiveByRoleName to exclude soft-deleted roles.
     */
    private RbacRole findRoleByName(String roleName) {
        log.debug("Searching for active role with name: {}", roleName);
        return roleRepository.findActiveByRoleName(roleName)
                .orElseThrow(() -> {
                    log.error("Role not found or is deleted with name: {}", roleName);
                    return new ResourceNotFoundException("Role", "name", roleName);
                });
    }

    /**
     * Validates that the login request contains required fields.
     */
    private void validateLoginRequest(AdminUserLoginRequestDto loginRequest) {
        if (loginRequest == null || loginRequest.getAdminUsersUsernameOrEmail() == null
                || loginRequest.getAdminUsersPassword() == null) {
            throw new IllegalArgumentException("Admin username/email and password are required");
        }
    }

    /**
     * Validates that a value is not null.
     */
    private void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Validates that a string is not null or blank.
     */
    private void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    /**
     * Validates that username is unique.
     */
    private void validateUniqueUsername(String username) {
        if (adminUserRepository.existsByAdminUsersUsername(username)) {
            throw new DuplicateResourceException("Admin", "username", username);
        }
    }

    /**
     * Validates that email is unique.
     */
    private void validateUniqueEmail(String email) {
        if (adminUserRepository.existsByAdminUsersEmail(email)) {
            throw new DuplicateResourceException("Admin", "email", email);
        }
    }

    /**
     * Validates that username is unique for update (excluding current user).
     */
    private void validateUniqueUsernameForUpdate(String newUsername, AdminUser currentUser) {
        if (newUsername != null && !newUsername.equals(currentUser.getAdminUsersUsername())) {
            validateUniqueUsername(newUsername);
        }
    }

    /**
     * Validates that email is unique for update (excluding current user).
     */
    private void validateUniqueEmailForUpdate(String newEmail, AdminUser currentUser) {
        if (newEmail != null && !newEmail.equals(currentUser.getAdminUsersEmail())) {
            validateUniqueEmail(newEmail);
        }
    }

    /**
     * Validates password against BCrypt stored hash.
     */
    private void validatePassword(String rawPassword, String storedHash) {
        log.info("===============================================");
        log.info("PASSWORD VALIDATION DEBUG:");
        log.info("Raw Password from Input: {}", rawPassword);
        log.info("Stored Hash in DB: {}", storedHash);

        boolean matches = passwordEncoder.matches(rawPassword, storedHash);
        log.info("BCrypt Matches Result: {}", matches);
        log.info("===============================================");

        if (!matches) {
            log.warn("Password validation failed - credentials mismatch");
            throw new InvalidCredentialsException();
        }
    }

    /**
     * Creates an audit log entry using unified audit utility.
     * Maps legacy AuditAction enum to AdminUserAuditActions constants.
     */
    private void createAuditLog(UUID adminUserId, AuditAction action, UUID actorId) {
        createAuditLogWithDetails(adminUserId, action, action.getDescription(), actorId);
    }

    /**
     * Creates an audit log entry with custom details using unified audit utility.
     */
    private void createAuditLogWithDetails(UUID adminUserId, AuditAction action, String details, UUID actorId) {
        try {
            RequestClientInfoDto clientInfo = getClientInfo();
            String mappedAction = mapActionToConstant(action);
            String severity = resolveSeverity(action);

            auditingUtility.audit(
                    "ADMIN",
                    actorId,
                    mappedAction,
                    adminUserId,
                    "",
                    details,
                    clientInfo,
                    severity,
                    AdminUserAuditLog.class,
                    adminUserAuditLogRepository);

            log.debug("Audit log created: action={}, adminUserId={}, actor={}", mappedAction, adminUserId, actorId);
        } catch (Exception e) {
            log.warn("Failed to create audit log for admin {}: {}", adminUserId, e.getMessage());
        }
    }

    /**
     * Gets client info from request context or returns default empty info.
     */
    private RequestClientInfoDto getClientInfo() {
        try {
            return requestInfoService.getClientInfo(null);
        } catch (Exception e) {
            log.debug("Could not extract client info from context: {}", e.getMessage());
            return new RequestClientInfoDto(
                    "UNKNOWN",
                    "UNKNOWN",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "");
        }
    }

    /**
     * Maps legacy AuditAction enum to new AdminUserAuditActions constants.
     */
    private String mapActionToConstant(AuditAction action) {
        return switch (action) {
            case CREATE -> AdminUserAuditActions.ADMIN_CREATED;
            case UPDATE -> AdminUserAuditActions.ADMIN_UPDATED;
            case DELETE, SOFT_DELETE -> AdminUserAuditActions.ADMIN_DELETED;
            case RESTORE -> AdminUserAuditActions.ADMIN_RESTORED;
            case ACTIVATE -> AdminUserAuditActions.ADMIN_ACTIVATED;
            case DEACTIVATE -> AdminUserAuditActions.ADMIN_DEACTIVATED;
            case LOGIN -> AdminUserAuditActions.ADMIN_LOGIN;
            case CHANGE_PASSWORD -> AdminUserAuditActions.ADMIN_PASSWORD_CHANGED;
            case PASSWORD_RESET -> AdminUserAuditActions.ADMIN_PASSWORD_RESET;
            case EMAIL_VERIFICATION_SENT -> AdminUserAuditActions.ADMIN_EMAIL_VERIFICATION_CODE_SENT;
            case VERIFY_EMAIL -> AdminUserAuditActions.ADMIN_EMAIL_VERIFIED;
            case ASSIGN_ROLE -> AdminUserAuditActions.ADMIN_ROLE_ASSIGNED;
            case REVOKE_ROLE -> AdminUserAuditActions.ADMIN_ROLE_REVOKED;
            case UPDATE_PROFILE_PICTURE -> AdminUserAuditActions.ADMIN_AVATAR_UPDATED;
            case DATA_EXPORTED -> AdminUserAuditActions.ADMIN_DATA_EXPORTED;
            default -> action.name();
        };
    }

    /**
     * Resolves severity level based on action type.
     */
    private String resolveSeverity(AuditAction action) {
        return switch (action) {
            case CREATE, DELETE, SOFT_DELETE, RESTORE -> "CRITICAL";
            case ACTIVATE, DEACTIVATE, ASSIGN_ROLE, REVOKE_ROLE -> "CRITICAL";
            case CHANGE_PASSWORD, PASSWORD_RESET, UPDATE -> "HIGH";
            default -> "MEDIUM";
        };
    }

    /**
     * Generates a secure random verification code.
     */
    private String generateSecureVerificationCode() {
        return String.format("%0" + VERIFICATION_CODE_LENGTH + "d",
                new SecureRandom().nextInt((int) Math.pow(10, VERIFICATION_CODE_LENGTH)));
    }

    /**
     * Ensures the avatar folder exists.
     */
    private void ensureAvatarFolderExists() {
        File dir = new File(avatarFolder);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new InvalidRequestException("avatarFolder", "Avatar directory cannot be created: " + avatarFolder);
        }
    }

    /**
     * Resolves the full path for an avatar file.
     */
    private String resolveAvatarFilePath(String filename) {
        return Path.of(avatarFolder, filename).toString();
    }

    /**
     * Hashes a password using BCryptPasswordEncoder.
     * Uses strength 10 (default) for security vs performance balance.
     */
    private String hashPassword(String password) {
        String hash = passwordEncoder.encode(password);
        log.info("===============================================");
        log.info("PASSWORD HASHING DEBUG:");
        log.info("Plain Password: {}", password);
        log.info("Generated BCrypt Hash: {}", hash);
        log.info("===============================================");
        return hash;
    }
}
