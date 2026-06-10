package com.mmva.newsapp.domain.adminuser.dto.core;

import com.mmva.newsapp.domain.adminuser.enums.core.AdminStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponseDto {
    // Identity
    private UUID adminUsersId;
    private String adminUsersUsername;

    // Contact
    private String adminUsersEmail;
    private String adminUsersPhoneNumber;
    private Boolean adminUsersPhoneVerified;

    // Profile
    private String adminUsersFirstName;
    private String adminUsersLastName;
    private String adminUsersFullName;
    private String adminUsersAvatarUrl;

    // Status & Role
    private AdminStatus adminUsersStatus;
    private UUID adminUsersRoleId;
    private String adminUsersRoleName;
    private Boolean adminUsersAccountLocked;

    // Verification
    private Boolean adminUsersEmailVerified;
    private String adminUsersEmailVerificationCode;
    private String adminUsersEmailVerificationExpiresAt;

    // Security (non-sensitive only)
    private Integer adminUsersFailedLoginAttempts;
    private String adminUsersAccountLockExpiresAt;
    private Boolean adminUsersMfaEnabled;

    // Activity & Audit
    private String adminUsersLastLogin;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private UUID deletedBy;
    private UUID createdBy;
    private UUID updatedBy;

    // Provider & Permissions
    private String adminUsersAuthProvider;

    // Notes or Description
    private String adminUsersNotes;
}
