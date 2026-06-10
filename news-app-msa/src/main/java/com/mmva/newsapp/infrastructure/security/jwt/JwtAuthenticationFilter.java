package com.mmva.newsapp.infrastructure.security.jwt;

import com.mmva.newsapp.infrastructure.security.userdetails.AdminUserDetails;
import com.mmva.newsapp.infrastructure.security.userdetails.AdminUserDetailsService;
import com.mmva.newsapp.infrastructure.security.userdetails.AppUserDetails;
import com.mmva.newsapp.infrastructure.security.userdetails.AppUserDetailsService;
import com.mmva.newsapp.infrastructure.rbac.config.PermissionConfigService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT Authentication Filter.
 * 
 * <p>
 * Intercepts incoming requests and validates JWT tokens.
 * If valid, sets the authentication in SecurityContext.
 * </p>
 * 
 * <h2>Token Extraction:</h2>
 * <ul>
 * <li>Extracts token from Authorization header: Bearer {token}</li>
 * <li>Validates token signature and expiration</li>
 * <li>Loads user details and sets authentication</li>
 * </ul>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AdminUserDetailsService adminUserDetailsService;
    private final AppUserDetailsService appUserDetailsService;
    private final PermissionConfigService permissionConfigService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);
            log.debug("JWT extracted: {}",
                    jwt != null ? "present (" + jwt.substring(0, Math.min(20, jwt.length())) + "...)" : "null");

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                log.debug("JWT validation passed");

                // Ensure it's an access token (not refresh token)
                if (!jwtTokenProvider.isAccessToken(jwt)) {
                    log.warn("Attempt to use non-access token for API access");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Extract user info from token
                UUID userId = jwtTokenProvider.getIdFromToken(jwt);
                String userType = jwtTokenProvider.getUserTypeFromToken(jwt);
                log.debug("Extracted from JWT - userId: {}, userType: {}", userId, userType);

                UserDetails userDetails = loadUserDetails(userId, userType);
                log.debug("UserDetails loaded: {}", userDetails != null ? "yes" : "no");

                if (userDetails != null) {
                    // Load permissions from cache for admin users
                    java.util.Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

                    if (userDetails instanceof AdminUserDetails) {
                        AdminUserDetails adminDetails = (AdminUserDetails) userDetails;

                        // Load permissions from PermissionConfigService cache
                        try {
                            java.util.Set<GrantedAuthority> cachedPermissions = permissionConfigService
                                    .getUserPermissions(adminDetails);
                            log.info("User {} authenticated with {} roles and {} permissions loaded from CACHE",
                                    userId,
                                    adminDetails.getRoles().size(),
                                    cachedPermissions.size());

                            // Combine role authorities with permission authorities from cache
                            java.util.Set<GrantedAuthority> combinedAuthorities = new java.util.HashSet<>(
                                    adminDetails.getAuthorities());
                            combinedAuthorities.addAll(cachedPermissions);
                            authorities = combinedAuthorities;

                        } catch (Exception e) {
                            log.warn("Failed to load permissions from cache for user {}: {}", userId, e.getMessage());
                            log.info(
                                    "User {} authenticated with {} roles and 0 permissions (cache failed, using local)",
                                    userId,
                                    adminDetails.getRoles().size());
                        }
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities);

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Set authentication for {} user: {} with {} authorities",
                            userType, userId, authorities.size());
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from Authorization header.
     *
     * @param request HTTP request
     * @return JWT token or null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * Loads user details based on user type.
     *
     * @param userId   User UUID
     * @param userType ADMIN or USER
     * @return UserDetails or null
     */
    private UserDetails loadUserDetails(UUID userId, String userType) {
        try {
            if (JwtTokenProvider.USER_TYPE_ADMIN.equals(userType)) {
                AdminUserDetails adminDetails = adminUserDetailsService.loadUserById(userId);
                if (adminDetails.isEnabled() && adminDetails.isAccountNonLocked()) {
                    return adminDetails;
                }
                log.warn("Admin account disabled or locked: {}", userId);
            } else if (JwtTokenProvider.USER_TYPE_USER.equals(userType)) {
                AppUserDetails appDetails = appUserDetailsService.loadUserById(userId);
                if (appDetails.isEnabled()) {
                    return appDetails;
                }
                log.warn("User account disabled: {}", userId);
            }
        } catch (Exception ex) {
            log.error("Error loading user details for {} {}: {}", userType, userId, ex.getMessage());
        }

        return null;
    }

    /**
     * Skip filter for certain paths (optimization).
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();

        // Skip JWT filter for public endpoints, swagger, and actuator
        return path.startsWith("/api/v1/public/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator/health");
    }
}
