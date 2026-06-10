package com.mmva.newsapp.domain.adminuser.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for changing admin user password.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserChangePasswordDto {

    @NotBlank(message = "Current password is required")
    private String adminUsersCurrentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    private String adminUsersNewPassword;

    @NotBlank(message = "Confirm password is required")
    private String adminUsersConfirmPassword;
}
