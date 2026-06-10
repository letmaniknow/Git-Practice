package com.mmva.newsapp.domain.appuser.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for password reset request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserPasswordResetRequestDto {

    @Schema(description = "User's email address or phone number", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email or phone number is required")
    private String appUsersEmailOrPhone;
}
