/**
 * Security module providing authentication and authorization infrastructure.
 *
 * <p>
 * This infrastructure module provides comprehensive security functionality including:
 * </p>
 * <ul>
 * <li><b>JWT Authentication:</b> Token generation, validation, and refresh</li>
 * <li><b>OAuth Integration:</b> Google and Apple Sign-In verification</li>
 * <li><b>UserDetails Adapters:</b> Spring Security integration for Admin and
 * App users</li>
 * <li><b>Security Handlers:</b> Custom error responses for auth failures</li>
 * <li><b>Security Utilities:</b> Helper methods for accessing current user
 * context</li>
 * </ul>
 *
 * <h2>Package Structure</h2>
 * 
 * <pre>
 * security/
 * ├── config/              - Security configuration
 * │   ├── AuthenticationSecurityConfig   - Main Spring Security config
 * │   └── SecurityAlertNotificationProperties - Alert email settings
 * ├── dto/                 - Data Transfer Objects
 * │   ├── AuthenticationResponseDto - Authentication response
 * │   ├── RefreshTokenRequestDto - Refresh token request
 * │   └── SecurityAlertDto - Security alert email DTO
 * ├── exception/           - Security-specific exceptions
 * ├── handler/             - Spring Security handlers
 * │   ├── JwtAuthenticationEntryPoint - 401 handler
 * │   └── SecurityAccessDeniedHandler - 403 handler
 * ├── jwt/                 - JWT infrastructure
 * │   ├── JwtAuthenticationFilter - Request filter
 * │   ├── JwtProperties    - JWT configuration
 * │   └── JwtTokenProvider - Token operations
 * ├── oauth/               - OAuth token verifiers
 * │   ├── AppleTokenVerifierService
 * │   └── GoogleTokenVerifierService
 * ├── userdetails/         - Spring Security adapters
 * │   ├── AdminUserDetails/Service
 * │   └── AppUserDetails/Service
 * └── util/                - Security utilities
 *     └── SecurityContextUtils    - Current user helpers
 * </pre>
 *
 * <h2>Key Components</h2>
 * <ul>
 * <li>{@link com.mmva.newsapp.infrastructure.security.jwt.JwtTokenProvider} -
 * JWT
 * operations</li>
 * <li>{@link com.mmva.newsapp.infrastructure.security.config.AuthenticationSecurityConfig} -
 * Security configuration</li>
 * <li>{@link com.mmva.newsapp.infrastructure.security.util.SecurityContextUtils} - Current
 * user utilities</li>
 * </ul>
 *
 * <h2>Portability</h2>
 * <p>
 * This is a <b>infrastructure module</b> designed to be portable across products.
 * It depends only on Spring Security and other infrastructure modules.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
package com.mmva.newsapp.infrastructure.security;
