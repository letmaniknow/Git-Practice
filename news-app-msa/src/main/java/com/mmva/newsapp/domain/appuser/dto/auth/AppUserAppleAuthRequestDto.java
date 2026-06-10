package com.mmva.newsapp.domain.appuser.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Apple Sign In request.
 * 
 * <p>
 * Contains the Apple identity token received from the frontend after
 * the user authenticates with Apple Sign In.
 * </p>
 * 
 * <p>
 * Note: Apple may only provide user's name on first login. Store it
 * immediately.
 * On subsequent logins, only the identity token is provided.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Apple Sign In request")
public class AppUserAppleAuthRequestDto {

    @Schema(description = "Apple identity token (JWT) received from Apple Sign In on the frontend", example = "eyJraWQiOiJXNldjT0tCIiwiYWxnIjoiUlMyNTYifQ...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Apple identity token is required")
    private String identityToken;

    @Schema(description = "User's full name (only provided on first Apple Sign In)", example = "John Doe")
    private String fullName;

    @Schema(description = "Authorization code for server-to-server validation (optional)", example = "c1234567890abcdef...")
    private String authorizationCode;
}
