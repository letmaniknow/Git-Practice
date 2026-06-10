package com.mmva.newsapp.domain.adminuser.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin user login request.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserLoginRequestDto {

    @NotBlank(message = "Admin username or email is required")
    private String adminUsersUsernameOrEmail;

    @NotBlank(message = "Password is required")
    private String adminUsersPassword;
}
