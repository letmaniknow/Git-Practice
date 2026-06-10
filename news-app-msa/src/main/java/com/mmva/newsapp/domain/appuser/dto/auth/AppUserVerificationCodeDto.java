package com.mmva.newsapp.domain.appuser.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for email/phone verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserVerificationCodeDto {

    @Schema(description = "6-digit verification code sent to user", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Verification code is required")
    @Size(min = 6, max = 6, message = "Verification code must be 6 digits")
    private String appUsersVerificationCode;
}
