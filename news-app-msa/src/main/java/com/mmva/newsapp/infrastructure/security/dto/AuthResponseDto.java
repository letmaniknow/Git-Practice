package com.mmva.newsapp.infrastructure.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Security Authentication Response DTO.
 * 
 * <p>
 * Contains JWT tokens and user information returned after successful
 * authentication.
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing JWT tokens and user info")
public class AuthResponseDto {

    @Schema(description = "JWT access token for API authentication", example = "eyJhbGciOiJIUzI1NiIs...")
    private String accessToken;

    @Schema(description = "JWT refresh token for obtaining new access tokens", example = "eyJhbGciOiJIUzI1NiIs...")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiration time in seconds", example = "900")
    private Long expiresIn;

    @Schema(description = "User ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
    private String userId;

    @Schema(description = "Username or email", example = "john@example.com")
    private String username;

    @Schema(description = "User type: USER or ADMIN", example = "USER")
    private String userType;

    @Schema(description = "List of roles assigned to user", example = "[\"USER\"]")
    private List<String> roles;

    @Schema(description = "List of permissions assigned to user (admindashboard only)", example = "[\"CREATE_NEWS\", \"DELETE_NEWS\"]")
    private List<String> permissions;
}
