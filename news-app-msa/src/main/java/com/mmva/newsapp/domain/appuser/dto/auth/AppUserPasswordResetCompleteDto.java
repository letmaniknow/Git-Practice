package com.mmva.newsapp.domain.appuser.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for completing password reset with verification code.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserPasswordResetCompleteDto {

    @Schema(description = "User's email address or phone number", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email or phone number is required")
    private String appUsersEmailOrPhone;

    @Schema(description = "6-digit verification code sent to user", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Verification code is required")
    @Size(min = 6, max = 6, message = "Verification code must be 6 digits")
    private String appUsersVerificationCode;

    @Schema(description = "New password to set", example = "NewP@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String appUsersNewPassword;

    @Schema(description = "Confirmation of the new password", example = "NewP@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Confirm password is required")
    private String appUsersConfirmPassword;
}
