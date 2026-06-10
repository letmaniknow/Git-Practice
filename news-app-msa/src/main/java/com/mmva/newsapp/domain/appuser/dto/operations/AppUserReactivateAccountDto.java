package com.mmva.newsapp.domain.appuser.dto.operations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for self-service account reactivation.
 * Users can reactivate their deactivated account using email/phone and
 * password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserReactivateAccountDto {

    @Schema(description = "User's email address or phone number", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email or phone number is required")
    private String appUsersEmailOrPhone;

    @Schema(description = "User's password", example = "P@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String appUsersPassword;
}
