package com.mmva.newsapp.controller.admin.auth;

import com.mmva.newsapp.domain.adminuser.dto.auth.AdminUserLoginRequestDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserResponseDto;
import com.mmva.newsapp.infrastructure.security.dto.AuthResponseDto;
import com.mmva.newsapp.infrastructure.security.dto.RefreshTokenRequestDto;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleResponseDto;
import com.mmva.newsapp.domain.adminuser.service.refreshtokenvalidation.AdminRefreshTokenValidationService;

// ===============================
// Security Imports
// ===============================
import com.mmva.newsapp.infrastructure.security.jwt.JwtTokenProvider;

// ===============================
// Service Imports
// ===============================
import com.mmva.newsapp.domain.adminuser.service.core.AdminUserService;

// ===============================
// OpenAPI/Swagger Imports
// ===============================
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Admin Authentication Controller.
 * 
 * <p>
 * Exposes public authentication endpoints for admin users (login, refresh
 * token).
 * Path: /api/v1/admin/auth
 * </p>
 * 
 * <p>
 * This controller is public (no authentication required) for login and token
 * refresh.
 * </p>
 * 
 * <table border="1">
 * <caption>Admin Auth Endpoints</caption>
 * <tr>
 * <th>#</th>
 * <th>Method</th>
 * <th>Endpoint</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>POST</td>
 * <td>/api/v1/admin/auth/login</td>
 * <td>Admin user login</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>POST</td>
 * <td>/api/v1/admin/auth/refresh-token</td>
 * <td>Refresh access token</td>
 * </tr>
 * </table>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin - Authentication", description = "Admin authentication endpoints (login, refresh token)")
public class AdminAuthController {
        // ===============================
        // Endpoint 3: Logout (Revoke Refresh Token)
        // ===============================

        @PostMapping("/logout")
        @Operation(summary = "3. Admin logout", description = "Revokes the refresh token for the admin user.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Logout successful"),
                        @ApiResponse(responseCode = "400", description = "Invalid request")
        })
        public ResponseEntity<ApiResponseDto<String>> logout(
                        @Valid @RequestBody RefreshTokenRequestDto refreshRequest) {
                String refreshToken = refreshRequest.getRefreshToken();
                var tokenOpt = adminRefreshTokenService.findByToken(refreshToken);
                if (tokenOpt.isEmpty()) {
                        log.warn("Admin: Logout failed, token not found");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(ApiResponseDto.error("Refresh token not found"));
                }
                adminRefreshTokenService.revokeToken(refreshToken);
                log.info("Admin: Refresh token revoked for logout");
                return ResponseEntity.ok(ApiResponseDto.success("Logout successful", "Refresh token revoked"));
        }

        private final AdminUserService adminUserService;
        private final JwtTokenProvider jwtTokenProvider;
        private final AdminRefreshTokenValidationService adminRefreshTokenService;

        // ===============================
        // Endpoint 1: Admin Login
        // ===============================

        @PostMapping("/login")
        @Operation(summary = "1. Admin login", description = "Authenticates an admindashboard user by username/email and password. Returns JWT tokens with role and permissions.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Login successful"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "403", description = "Account locked or inactive")
        })
        public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(
                        @Valid @RequestBody AdminUserLoginRequestDto loginRequest) {

                log.info("Admin: Login request received for: {}", loginRequest.getAdminUsersUsernameOrEmail());

                // Authenticate admindashboard via service (validates credentials)
                AdminUserResponseDto adminProfile = adminUserService.login(loginRequest);

                // Get roles and permissions for the admindashboard
                List<RoleResponseDto> roles = adminUserService.getRolesForAdmin(adminProfile.getAdminUsersId());
                List<PermissionResponseDto> permissions = adminUserService
                                .getPermissionsForAdmin(adminProfile.getAdminUsersId());

                // Extract role names and permission names
                List<String> roleNames = roles.stream()
                                .map(RoleResponseDto::getRoleName)
                                .toList();

                List<String> permissionNames = permissions.stream()
                                .map(PermissionResponseDto::getPermissionName)
                                .toList();

                // Generate JWT tokens with roles and permissions
                String accessToken = jwtTokenProvider.generateAdminAccessToken(
                                adminProfile.getAdminUsersId(),
                                adminProfile.getAdminUsersEmail() != null ? adminProfile.getAdminUsersEmail()
                                                : adminProfile.getAdminUsersUsername(),
                                roleNames,
                                permissionNames);

                String refreshToken = jwtTokenProvider.generateAdminRefreshToken(
                                adminProfile.getAdminUsersId(),
                                adminProfile.getAdminUsersEmail() != null ? adminProfile.getAdminUsersEmail()
                                                : adminProfile.getAdminUsersUsername());

                // Save refresh token in DB
                com.mmva.newsapp.domain.adminuser.model.refreshtokenvalidation.AdminRefreshTokenValidation refreshTokenEntity = new com.mmva.newsapp.domain.adminuser.model.refreshtokenvalidation.AdminRefreshTokenValidation();
                refreshTokenEntity.setAdminId(adminProfile.getAdminUsersId());
                refreshTokenEntity.setToken(refreshToken);
                refreshTokenEntity.setCreatedAt(java.time.Instant.now());
                refreshTokenEntity.setExpiresAt(java.time.Instant.now()
                                .plusSeconds(jwtTokenProvider.getRefreshTokenExpirationSeconds()));
                refreshTokenEntity.setRevoked(false);
                adminRefreshTokenService.save(refreshTokenEntity);

                // Build auth response
                AuthResponseDto authResponse = AuthResponseDto.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                                .userId(adminProfile.getAdminUsersId().toString())
                                .username(adminProfile.getAdminUsersEmail() != null ? adminProfile.getAdminUsersEmail()
                                                : adminProfile.getAdminUsersUsername())
                                .userType(JwtTokenProvider.USER_TYPE_ADMIN)
                                .roles(roleNames)
                                .permissions(permissionNames)
                                .build();

                log.info("Admin: Login successful for: {}", loginRequest.getAdminUsersUsernameOrEmail());
                return ResponseEntity.ok(ApiResponseDto.success("Login successful", authResponse));
        }

        // ===============================
        // Endpoint 2: Refresh Token
        // ===============================

        @PostMapping("/refresh-token")
        @Operation(summary = "2. Refresh admindashboard access token", description = "Obtains a new access token using a valid refresh token. Reloads current roles and permissions.")
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
                        log.warn("Admin: Invalid refresh token attempt");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponseDto.error("Invalid or expired refresh token"));
                }

                // Check DB for token existence and revocation
                var tokenOpt = adminRefreshTokenService.findByToken(refreshToken);
                if (tokenOpt.isEmpty() || tokenOpt.get().isRevoked()
                                || java.time.Instant.now().isAfter(tokenOpt.get().getExpiresAt())) {
                        log.warn("Admin: Refresh token revoked or expired in DB");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponseDto.error("Refresh token revoked or expired"));
                }

                // Extract admindashboard info from refresh token
                UUID adminId = jwtTokenProvider.getIdFromToken(refreshToken);
                String userType = jwtTokenProvider.getUserTypeFromToken(refreshToken);
                String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

                // Ensure it's an ADMIN token
                if (!JwtTokenProvider.USER_TYPE_ADMIN.equals(userType)) {
                        log.warn("Admin: Attempt to refresh non-admindashboard token");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponseDto.error("Invalid token type"));
                }

                // Re-fetch current roles and permissions (they may have changed)
                List<RoleResponseDto> roles = adminUserService.getRolesForAdmin(adminId);
                List<PermissionResponseDto> permissions = adminUserService.getPermissionsForAdmin(adminId);

                List<String> roleNames = roles.stream()
                                .map(RoleResponseDto::getRoleName)
                                .toList();

                List<String> permissionNames = permissions.stream()
                                .map(PermissionResponseDto::getPermissionName)
                                .toList();

                // Generate new access token with current roles/permissions
                String newAccessToken = jwtTokenProvider.generateAdminAccessToken(
                                adminId,
                                username,
                                roleNames,
                                permissionNames);

                AuthResponseDto authResponse = AuthResponseDto.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(refreshToken) // Return same refresh token
                                .tokenType("Bearer")
                                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                                .userId(adminId.toString())
                                .username(username)
                                .userType(JwtTokenProvider.USER_TYPE_ADMIN)
                                .roles(roleNames)
                                .permissions(permissionNames)
                                .build();

                log.info("Admin: Token refreshed for admindashboard: {}", adminId);
                return ResponseEntity.ok(ApiResponseDto.success("Token refreshed successfully", authResponse));
        }
}
