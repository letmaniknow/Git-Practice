package com.mmva.newsapp.domain.appuser.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for changing user password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserChangePasswordDto {

    @Schema(description = "Current password of the user", example = "OldP@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Current password is required")
    private String appUsersCurrentPassword;

    @Schema(description = "New password to set", example = "NewP@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    private String appUsersNewPassword;

    @Schema(description = "Confirmation of the new password", example = "NewP@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Confirm password is required")
    private String appUsersConfirmPassword;
}
