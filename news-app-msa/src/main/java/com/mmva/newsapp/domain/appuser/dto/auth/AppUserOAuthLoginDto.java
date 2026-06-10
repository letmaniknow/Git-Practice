package com.mmva.newsapp.domain.appuser.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for OAuth/Social login request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserOAuthLoginDto {
    @Schema(description = "OAuth provider name", example = "google", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "OAuth provider is required")
    private String appUsersProvider; // google, facebook, apple, github, etc.

    @Schema(description = "OAuth access token", example = "ya29.a0AfH6SMB...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "OAuth access token is required")
    private String appUsersAccessToken;

    @Schema(description = "OAuth refresh token", example = "1//0gL...", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String appUsersRefreshToken;

    @Schema(description = "OAuth ID token (for providers like Google)", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
    private String appUsersIdToken; // For providers like Google that return an ID token

    @Schema(description = "User's email address", example = "user@example.com")
    private String appUsersEmail;

    @Schema(description = "User's display name", example = "John Doe")
    private String appUsersName;

    @Schema(description = "URL to user's avatar image", example = "https://example.com/avatar.jpg")
    private String appUsersAvatarUrl;

    @Schema(description = "User's ID from the OAuth provider", example = "1234567890")
    private String appUsersProviderId; // The user's ID from the OAuth provider
}
