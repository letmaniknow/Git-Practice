package com.mmva.newsapp.domain.adminuser.dto.core;

import com.mmva.newsapp.domain.adminuser.enums.core.AdminStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserRequestDto {

    @NotBlank(message = "Admin username is required")
    @Size(min = 3, max = 255, message = "Admin username must be between 3 and 255 characters")
    private String adminUsersUsername;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Admin email must be valid")
    @Size(max = 255, message = "Admin email must not exceed 255 characters")
    private String adminUsersEmail;

    @Size(max = 30, message = "Phone number must be at most 30 characters")
    private String adminUsersPhoneNumber;

    private Boolean adminUsersPhoneVerified;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String adminUsersPassword;

    private java.util.UUID adminUsersRoleId;

    @Size(max = 100, message = "Role name must be at most 100 characters")
    private String adminUsersRoleName;

    @Builder.Default
    private AdminStatus adminUsersStatus = AdminStatus.ACTIVE;

    @Size(max = 100, message = "Auth provider must not exceed 100 characters")
    private String adminUsersAuthProvider;

    private Boolean adminUsersAccountLocked;

    @Size(max = 100, message = "First name must be at most 100 characters")
    private String adminUsersFirstName;

    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String adminUsersLastName;

    @Size(max = 255, message = "Full name must be at most 255 characters")
    private String adminUsersFullName;

    @Size(max = 255, message = "Avatar URL must be at most 255 characters")
    private String adminUsersAvatarUrl;

    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    private String adminUsersNotes;
}
