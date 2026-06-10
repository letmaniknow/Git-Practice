package com.mmva.newsapp.domain.adminuser.model.core;

import com.mmva.newsapp.domain.adminuser.enums.core.AdminStatus;
import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.infrastructure.rbac.role.core.model.RbacRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Admin/Staff user entity for system administration.
 * 
 * <p>
 * Represents internal staff users with roles and permissions for managing the
 * newsapp infrastructure.
 * </p>
 * 
 * <p>
 * Soft-delete filtering is handled via {@code SoftDeleteSpec} in repository
 * queries.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Entity
@Table(name = "admin_users", indexes = {
        // Soft-delete filter (used in almost all queries)
        @Index(name = "idx_admin_users_deleted_at", columnList = "deleted_at"),

        // Login/Authentication lookups
        @Index(name = "idx_admin_users_email", columnList = "admin_users_email"),
        @Index(name = "idx_admin_users_username", columnList = "admin_users_username"),

        // Role-based queries (FK column)
        @Index(name = "idx_admin_users_role_id", columnList = "admin_users_role_id"),

        // Status filtering
        @Index(name = "idx_admin_users_status", columnList = "admin_users_status"),

        // Composite: Most common query pattern (active + status filtering)
        @Index(name = "idx_admin_users_deleted_status", columnList = "deleted_at, admin_users_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminUser extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "admin_users_id", updatable = false, nullable = false)
    private UUID adminUsersId;

    // ========================================
    // Identity Fields
    // ========================================

    @Column(name = "admin_users_username", unique = true, nullable = false)
    private String adminUsersUsername;

    @Column(name = "admin_users_email", unique = true, nullable = false)
    private String adminUsersEmail;

    // ========================================
    // Name Fields
    // ========================================

    @Column(name = "admin_users_first_name", length = 100)
    private String adminUsersFirstName;

    @Column(name = "admin_users_last_name", length = 100)
    private String adminUsersLastName;

    @Column(name = "admin_users_full_name", length = 255)
    private String adminUsersFullName;

    // ========================================
    // Contact Information
    // ========================================

    @Column(name = "admin_users_phone_number", length = 30)
    private String adminUsersPhoneNumber;

    @Column(name = "admin_users_phone_verified")
    private Boolean adminUsersPhoneVerified;

    @Column(name = "admin_users_avatar_url", length = 255)
    private String adminUsersAvatarUrl;

    // ========================================
    // Credentials & Security
    // ========================================

    @Column(name = "admin_users_password_hash", nullable = false)
    private String adminUsersPasswordHash;

    @Column(name = "admin_users_password_salt", length = 255)
    private String adminUsersPasswordSalt;

    @Column(name = "admin_users_last_password_change_at")
    private Instant adminUsersLastPasswordChangeAt;

    @Column(name = "admin_users_reset_password_token", length = 255)
    private String adminUsersResetPasswordToken;

    @Column(name = "admin_users_reset_password_expires_at")
    private Instant adminUsersResetPasswordExpiresAt;

    // ========================================
    // Account Lockout
    // ========================================

    @Column(name = "admin_users_failed_login_attempts")
    private Integer adminUsersFailedLoginAttempts;

    @Column(name = "admin_users_account_locked", nullable = false)
    private Boolean adminUsersAccountLocked;

    @Column(name = "admin_users_account_lock_expires_at")
    private Instant adminUsersAccountLockExpiresAt;

    // ========================================
    // Multi-Factor Authentication
    // ========================================

    @Column(name = "admin_users_mfa_enabled")
    private Boolean adminUsersMfaEnabled;

    @Column(name = "admin_users_mfa_secret", length = 255)
    private String adminUsersMfaSecret;

    // ========================================
    // Status & Role
    // ========================================

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_users_status", length = 50, nullable = false)
    private AdminStatus adminUsersStatus = AdminStatus.ACTIVE;

    /**
     * Role relationship - establishes FK to roles table.
     * Use role.getRoleId() for ID, role.getRoleName() for name.
     * 
     * NOT NULL: Every admin user must have a role assigned.
     */
    @NotNull(message = "Admin user must have a role assigned")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_users_role_id", referencedColumnName = "role_id", nullable = false)
    private RbacRole role;

    // ========================================
    // Email Verification
    // ========================================

    @Column(name = "admin_users_email_verified")
    private Boolean adminUsersEmailVerified = false;

    @Column(name = "admin_users_email_verification_code", length = 100)
    private String adminUsersEmailVerificationCode;

    @Column(name = "admin_users_email_verification_expires_at")
    private Instant adminUsersEmailVerificationExpiresAt;

    // ========================================
    // Activity Tracking
    // ========================================

    @Column(name = "admin_users_last_login")
    private Instant adminUsersLastLogin;

    // ========================================
    // Auth Provider & Notes
    // ========================================

    @Column(name = "admin_users_auth_provider", length = 100)
    private String adminUsersAuthProvider;

    @Column(name = "admin_users_notes", length = 1000)
    private String adminUsersNotes;
}
