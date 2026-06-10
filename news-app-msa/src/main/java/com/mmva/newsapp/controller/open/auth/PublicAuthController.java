package com.mmva.newsapp.controller.open.auth;

import com.mmva.newsapp.domain.appuser.dto.auth.AppUserAppleAuthRequestDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserGoogleAuthRequestDto;
import com.mmva.newsapp.infrastructure.security.dto.AuthResponseDto;
import com.mmva.newsapp.infrastructure.security.dto.RefreshTokenRequestDto;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserLoginRequestDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserOAuthLoginDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserPasswordResetCompleteDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserPasswordResetRequestDto;
import com.mmva.newsapp.domain.appuser.dto.operations.AppUserReactivateAccountDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileRequestDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileResponseDto;

// ===============================
// Security Imports
// ===============================
import com.mmva.newsapp.infrastructure.security.jwt.JwtTokenProvider;
import com.mmva.newsapp.infrastructure.security.oauth.AppleTokenVerifierService;
import com.mmva.newsapp.infrastructure.security.oauth.GoogleTokenVerifierService;
import com.mmva.newsapp.domain.appuser.service.core.AppUserService;

// ===============================
// OpenAPI/Swagger Imports
// ===============================
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// ===============================
// Validation Imports
// ===============================
import jakarta.validation.Valid;

// ===============================
// Lombok Imports
// ===============================
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ===============================
// Spring Framework Imports
// ===============================
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public Authentication Controller
 * 
 * Exposes public authentication endpoints (login, register, etc.)
 * Path: /api/v1/public/auth
 *
 * Follows industry best practices: dedicated controller for authentication.
 * 
 * Endpoints:
 * 1. POST /login - Public user login
 * 2. POST /register - Register new public user
 * 3. GET /check-email - Check if email exists
 * 4. GET /check-phone - Check if phone exists
 * 5. POST /request-password-reset - Request password reset
 * 6. POST /complete-password-reset - Complete password reset
 * 7. POST /reactivate - Self-service account reactivation
 * 8. POST /oauth/login - OAuth login
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/public/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Public Authentication", description = "Public authentication endpoints (login, register, etc.)")
public class PublicAuthController {

        private final AppUserService userService;
        private final JwtTokenProvider jwtTokenProvider;
        private final GoogleTokenVerifierService googleTokenVerifierService;
        private final AppleTokenVerifierService appleTokenVerifierService;

        // ===============================
        // Endpoint 1: Login
        // ===============================

        @PostMapping("/login")
        @Operation(summary = "1. Public user login", description = "Authenticates a public user by email/phone and password. Returns JWT tokens and user profile.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Login successful"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                        @ApiResponse(responseCode = "400", description = "Invalid request")
        })
        public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(
                        @Valid @RequestBody AppUserLoginRequestDto loginRequest) {

                log.info("Public: Login request received for: {}", loginRequest.getAppUsersEmailOrPhone());

                // Authenticate user via service (validates credentials)
                AppUserProfileResponseDto userProfile = userService.login(loginRequest);

                // Generate JWT tokens
                String accessToken = jwtTokenProvider.generateUserAccessToken(
                                userProfile.getAppUsersId(),
                                userProfile.getAppUsersEmail() != null ? userProfile.getAppUsersEmail()
                                                : userProfile.getAppUsersPhoneNumber());

                String refreshToken = jwtTokenProvider.generateUserRefreshToken(
                                userProfile.getAppUsersId(),
                                userProfile.getAppUsersEmail() != null ? userProfile.getAppUsersEmail()
                                                : userProfile.getAppUsersPhoneNumber());

                // Build auth response
                AuthResponseDto authResponse = AuthResponseDto.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                                .userId(userProfile.getAppUsersId().toString())
                                .username(userProfile.getAppUsersEmail() != null ? userProfile.getAppUsersEmail()
                                                : userProfile.getAppUsersPhoneNumber())
                                .userType(JwtTokenProvider.USER_TYPE_USER)
                                .roles(List.of("USER"))
                                .build();

                log.info("Public: Login successful for: {}", loginRequest.getAppUsersEmailOrPhone());
                return ResponseEntity.ok(ApiResponseDto.success("Login successful", authResponse));
        }

        // ===============================
        // Endpoint 1b: Refresh Token
        // ===============================

        @PostMapping("/refresh-token")
        @Operation(summary = "1b. Refresh access token", description = "Obtains a new access token using a valid refresh token")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
                        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
                        @ApiResponse(responseCode = "400", description = "Invalid request")
        })
        public ResponseEntity<ApiResponseDto<AuthResponseDto>> refreshToken(
                        @Valid @RequestBody RefreshTokenRequestDto refreshRequest) {

                String refreshToken = refreshRequest.getRefreshToken();

                // Validate refresh token
                if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
                        log.warn("Public: Invalid refresh token attempt");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponseDto.error("Invalid or expired refresh token"));
                }

                // Extract user info from refresh token
                java.util.UUID userId = jwtTokenProvider.getIdFromToken(refreshToken);
                String userType = jwtTokenProvider.getUserTypeFromToken(refreshToken);
                String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

                // Ensure it's a USER token (not ADMIN)
                if (!JwtTokenProvider.USER_TYPE_USER.equals(userType)) {
                        log.warn("Public: Attempt to refresh non-user token");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponseDto.error("Invalid token type"));
                }

                // Generate new access token
                String newAccessToken = jwtTokenProvider.generateUserAccessToken(userId, username);

                AuthResponseDto authResponse = AuthResponseDto.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(refreshToken) // Return same refresh token
                                .tokenType("Bearer")
                                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                                .userId(userId.toString())
                                .username(username)
                                .userType(JwtTokenProvider.USER_TYPE_USER)
                                .roles(List.of("USER"))
                                .build();

                log.info("Public: Token refreshed for user: {}", userId);
                return ResponseEntity.ok(ApiResponseDto.success("Token refreshed successfully", authResponse));
        }

        // ===============================
        // Endpoint 2: Register
        // ===============================

        @PostMapping("/create-app-user")
        @Operation(summary = "2. Register new app user", description = "Creates a new app user profile with the provided details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "User registered successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request or validation error"),
                        @ApiResponse(responseCode = "409", description = "Email or phone already exists")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> createAppUser(
                        @Valid @RequestBody AppUserProfileRequestDto dto) {

                log.info("Public: Register request received for email: {}", dto.getAppUsersEmail());

                AppUserProfileResponseDto response = userService.createAppUser(dto);

                log.info("Public: User registered successfully with ID: {}", response.getAppUsersId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("User profile created successfully", response));
        }

        // ===============================
        // Endpoint 3: Check Email Exists
        // ===============================

        @GetMapping("/check-email")
        @Operation(summary = "3. Check if email exists", description = "Validates if an email is already registered (for pre-registration check)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Email check completed"),
                        @ApiResponse(responseCode = "400", description = "Invalid email format")
        })
        public ResponseEntity<ApiResponseDto<Boolean>> checkEmailExists(
                        @Parameter(description = "Email address to check", required = true, example = "user@example.com") @RequestParam String email) {

                log.debug("Public: Checking if email exists: {}", email);

                boolean exists = userService.emailExists(email);
                boolean isAvailable = !exists; // Invert: true = available, false = taken
                String message = exists ? "Email is already registered" : "Email is available";

                log.debug("Public: Email check result for {}: {}", email, exists ? "exists" : "available");
                return ResponseEntity.ok(ApiResponseDto.success(message, isAvailable));
        }

        // ===============================
        // Endpoint 4: Check Phone Exists
        // ===============================

        @GetMapping("/check-phone")
        @Operation(summary = "4. Check if phone exists", description = "Validates if a phone number is already registered (for pre-registration check)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Phone check completed"),
                        @ApiResponse(responseCode = "400", description = "Invalid phone format")
        })
        public ResponseEntity<ApiResponseDto<Boolean>> checkPhoneExists(
                        @Parameter(description = "Phone number to check", required = true, example = "+1234567890") @RequestParam String phone) {

                log.debug("Public: Checking if phone exists: {}", phone);

                boolean exists = userService.phoneExists(phone);
                boolean isAvailable = !exists; // Invert: true = available, false = taken
                String message = exists ? "Phone number is already registered" : "Phone number is available";

                log.debug("Public: Phone check result for {}: {}", phone, exists ? "exists" : "available");
                return ResponseEntity.ok(ApiResponseDto.success(message, isAvailable));
        }

        // ===============================
        // Endpoint 5: Request Password Reset
        // ===============================

        @PostMapping("/request-password-reset")
        @Operation(summary = "5. Request password reset", description = "Initiates password reset process by sending a reset code to email")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Password reset code sent"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> requestPasswordReset(
                        @Valid @RequestBody AppUserPasswordResetRequestDto dto) {

                log.info("Public: Password reset request received for: {}", dto.getAppUsersEmailOrPhone());

                userService.requestPasswordReset(dto);

                log.info("Public: Password reset code sent to: {}", dto.getAppUsersEmailOrPhone());
                return ResponseEntity.ok(ApiResponseDto.success("Password reset code sent to your email", null));
        }

        // ===============================
        // Endpoint 6: Complete Password Reset
        // ===============================

        @PostMapping("/complete-password-reset")
        @Operation(summary = "6. Complete password reset", description = "Completes password reset with verification code")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Password reset completed"),
                        @ApiResponse(responseCode = "400", description = "Invalid or expired reset code"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> completePasswordReset(
                        @Valid @RequestBody AppUserPasswordResetCompleteDto dto) {

                log.info("Public: Complete password reset request for: {}", dto.getAppUsersEmailOrPhone());

                userService.completePasswordReset(dto);

                log.info("Public: Password reset completed for: {}", dto.getAppUsersEmailOrPhone());
                return ResponseEntity.ok(ApiResponseDto.success("Password reset completed successfully", null));
        }

        // ===============================
        // Endpoint 7: Reactivate Account
        // ===============================

        @PostMapping("/reactivate")
        @Operation(summary = "7. Self-service account reactivation", description = "Allows public users to reactivate their deactivated account using email/phone and password")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Account reactivated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<ApiResponseDto<AppUserProfileResponseDto>> reactivateAccount(
                        @Valid @RequestBody AppUserReactivateAccountDto dto) {

                log.info("Public: Account reactivation request for: {}", dto.getAppUsersEmailOrPhone());

                AppUserProfileResponseDto response = userService.reactivateAccount(dto);

                log.info("Public: Account reactivated successfully for: {}", dto.getAppUsersEmailOrPhone());
                return ResponseEntity.ok(ApiResponseDto.success("Account reactivated successfully", response));
        }

        // ===============================
        // Endpoint 8: Google OAuth Login
        // ===============================

        @PostMapping("/google")
        @Operation(summary = "8. Google OAuth login", description = "Login or register via Google. Frontend sends Google ID token, backend verifies and returns JWT tokens.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Google login successful"),
                        @ApiResponse(responseCode = "400", description = "Invalid Google ID token"),
                        @ApiResponse(responseCode = "401", description = "Google authentication failed"),
                        @ApiResponse(responseCode = "503", description = "Google OAuth not configured")
        })
        public ResponseEntity<ApiResponseDto<AuthResponseDto>> googleLogin(
                        @Valid @RequestBody AppUserGoogleAuthRequestDto dto) {

                log.info("Public: Google login request received");

                // Check if Google OAuth is configured
                if (!googleTokenVerifierService.isConfigured()) {
                        log.error("Public: Google OAuth is not configured");
                        return ResponseEntity.status(503)
                                        .body(ApiResponseDto.error("Google OAuth is not configured"));
                }

                // Verify Google ID token and extract user info
                AppUserOAuthLoginDto oauthDto = googleTokenVerifierService.verifyIdToken(dto.getAppUsersIdToken());

                // Login or create user via OAuth service
                AppUserProfileResponseDto userProfile = userService.oauthLogin(oauthDto);

                // Generate JWT tokens
                String accessToken = jwtTokenProvider.generateUserAccessToken(
                                userProfile.getAppUsersId(),
                                userProfile.getAppUsersEmail());

                String refreshToken = jwtTokenProvider.generateUserRefreshToken(
                                userProfile.getAppUsersId(),
                                userProfile.getAppUsersEmail());

                // Build auth response
                AuthResponseDto authResponse = AuthResponseDto.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                                .userId(userProfile.getAppUsersId().toString())
                                .username(userProfile.getAppUsersEmail())
                                .userType(JwtTokenProvider.USER_TYPE_USER)
                                .roles(List.of("USER"))
                                .build();

                log.info("Public: Google login successful for: {}", userProfile.getAppUsersEmail());
                return ResponseEntity.ok(ApiResponseDto.success("Google login successful", authResponse));
        }

        // ===============================
        // Endpoint 9: Apple Sign In
        // ===============================

        @PostMapping("/apple")
        @Operation(summary = "9. Apple Sign In", description = "Login or register via Apple Sign In. Frontend sends Apple identity token, backend verifies and returns JWT tokens.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Apple login successful"),
                        @ApiResponse(responseCode = "400", description = "Invalid Apple identity token"),
                        @ApiResponse(responseCode = "401", description = "Apple authentication failed"),
                        @ApiResponse(responseCode = "503", description = "Apple Sign In not configured")
        })
        public ResponseEntity<ApiResponseDto<AuthResponseDto>> appleLogin(
                        @Valid @RequestBody AppUserAppleAuthRequestDto dto) {

                log.info("Public: Apple Sign In request received");

                // Check if Apple Sign In is configured
                if (!appleTokenVerifierService.isConfigured()) {
                        log.error("Public: Apple Sign In is not configured");
                        return ResponseEntity.status(503)
                                        .body(ApiResponseDto.error("Apple Sign In is not configured"));
                }

                // Verify Apple identity token and extract user info
                AppUserOAuthLoginDto oauthDto = appleTokenVerifierService.verifyIdentityToken(
                                dto.getIdentityToken(), dto.getFullName());

                // Login or create user via OAuth service
                AppUserProfileResponseDto userProfile = userService.oauthLogin(oauthDto);

                // Generate JWT tokens
                String accessToken = jwtTokenProvider.generateUserAccessToken(
                                userProfile.getAppUsersId(),
                                userProfile.getAppUsersEmail());

                String refreshToken = jwtTokenProvider.generateUserRefreshToken(
                                userProfile.getAppUsersId(),
                                userProfile.getAppUsersEmail());

                // Build auth response
                AuthResponseDto authResponse = AuthResponseDto.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                                .userId(userProfile.getAppUsersId().toString())
                                .username(userProfile.getAppUsersEmail())
                                .userType(JwtTokenProvider.USER_TYPE_USER)
                                .roles(List.of("USER"))
                                .build();

                log.info("Public: Apple login successful for: {}",
                                userProfile.getAppUsersEmail() != null
                                                ? userProfile.getAppUsersEmail()
                                                : "Apple User (private email)");
                return ResponseEntity.ok(ApiResponseDto.success("Apple login successful", authResponse));
        }
}
