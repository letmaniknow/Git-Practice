package com.mmva.newsapp.infrastructure.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Security Token Refresh Request DTO.
 * 
 * <p>
 * Used to request new access token using a valid refresh token.
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to refresh access token using refresh token")
public class RefreshTokenRequestDto {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "Valid refresh token", example = "eyJhbGciOiJIUzI1NiIs...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
