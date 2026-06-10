package com.mmva.newsapp.domain.adminuser.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for exporting admindashboard user data for compliance purposes.
 * Excludes sensitive data like password hash.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserExportDto {
    private UUID adminUsersId;
    private String adminUsersUsername;
    private String adminUsersEmail;
    private String adminUsersFirstName;
    private String adminUsersLastName;
    private String adminUsersPhoneNumber;
    private String adminUsersRoleName;
    private String adminUsersStatus;
    private String adminUsersAvatarUrl;
    private boolean adminUsersEmailVerified;
    private boolean adminUsersPhoneVerified;
    private boolean adminUsersTwoFactorEnabled;
    private String createdAt;
    private String updatedAt;
    private String adminUsersLastLoginAt;
    private String exportedAt;
}
