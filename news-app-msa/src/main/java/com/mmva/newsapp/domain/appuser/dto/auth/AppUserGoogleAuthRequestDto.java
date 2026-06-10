package com.mmva.newsapp.domain.appuser.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Google OAuth login request.
 * 
 * <p>
 * Contains the Google ID token received from the frontend after
 * the user authenticates with Google Sign-In.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Google OAuth login request")
public class AppUserGoogleAuthRequestDto {

    @Schema(description = "Google ID token received from Google Sign-In on the frontend", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOTczZWUwZTE2...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Google ID token is required")
    private String appUsersIdToken;
}
