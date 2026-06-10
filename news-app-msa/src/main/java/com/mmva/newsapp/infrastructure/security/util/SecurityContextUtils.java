package com.mmva.newsapp.infrastructure.security.util;

import com.mmva.newsapp.infrastructure.security.userdetails.AdminUserDetails;
import com.mmva.newsapp.infrastructure.security.userdetails.AppUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Security Utility Class.
 * 
 * <p>
 * Provides static helper methods to access the current authenticated user
 * from the SecurityContext. This is useful in controllers and services
 * where you need to get the current user without injecting dependencies.
 * </p>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * // In a controller or service
 * UUID adminId = SecurityUtils.getCurrentAdminId()
 *         .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
 * 
 * // Check if user is admindashboard
 * boolean isAdmin = SecurityUtils.isCurrentUserAdmin();
 * 
 * // Get permissions
 * List&lt;String&gt; permissions = SecurityUtils.getCurrentPermissions();
 * </pre>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
public final class SecurityContextUtils {

    private SecurityContextUtils() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // Authentication Status
    // ========================================

    /**
     * Checks if the current request is authenticated.
     *
     * @return true if authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Checks if the current authenticated user is an admindashboard.
     *
     * @return true if admindashboard user
     */
    public static boolean isCurrentUserAdmin() {
        return getCurrentAdminDetails().isPresent();
    }

    /**
     * Checks if the current authenticated user is a regular app user.
     *
     * @return true if app user
     */
    public static boolean isCurrentUserAppUser() {
        return getCurrentAppUserDetails().isPresent();
    }

    // ========================================
    // Admin User Access
    // ========================================

    /**
     * Gets the current admindashboard ID from SecurityContext.
     *
     * @return Optional containing admindashboard UUID if authenticated as
     *         admindashboard
     */
    public static Optional<UUID> getCurrentAdminId() {
        return getCurrentAdminDetails()
                .map(AdminUserDetails::getAdminId);
    }

    /**
     * Gets the current admindashboard ID, throwing exception if not authenticated.
     *
     * @return Admin UUID
     * @throws IllegalStateException if not authenticated as admindashboard
     */
    public static UUID requireCurrentAdminId() {
        return getCurrentAdminId()
                .orElseThrow(() -> new IllegalStateException("Not authenticated as admindashboard"));
    }

    /**
     * Gets the current admindashboard username from SecurityContext.
     *
     * @return Optional containing admindashboard username if authenticated as
     *         admindashboard
     */
    public static Optional<String> getCurrentAdminUsername() {
        return getCurrentAdminDetails()
                .map(AdminUserDetails::getUsername);
    }

    /**
     * Gets the AdminUserDetails from SecurityContext.
     *
     * @return Optional containing AdminUserDetails if authenticated as
     *         admindashboard
     */
    public static Optional<AdminUserDetails> getCurrentAdminDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminUserDetails adminDetails) {
            return Optional.of(adminDetails);
        }
        return Optional.empty();
    }

    // ========================================
    // App User Access
    // ========================================

    /**
     * Gets the current app user ID from SecurityContext.
     *
     * @return Optional containing user UUID if authenticated as app user
     */
    public static Optional<UUID> getCurrentUserId() {
        return getCurrentAppUserDetails()
                .map(AppUserDetails::getUserId);
    }

    /**
     * Gets the current user ID, throwing exception if not authenticated.
     *
     * @return User UUID
     * @throws IllegalStateException if not authenticated as app user
     */
    public static UUID requireCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Not authenticated as app user"));
    }

    /**
     * Gets the current app user's username from SecurityContext.
     *
     * @return Optional containing username if authenticated as app user
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentAppUserDetails()
                .map(AppUserDetails::getUsername);
    }

    /**
     * Gets the AppUserDetails from SecurityContext.
     *
     * @return Optional containing AppUserDetails if authenticated as app user
     */
    public static Optional<AppUserDetails> getCurrentAppUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUserDetails userDetails) {
            return Optional.of(userDetails);
        }
        return Optional.empty();
    }

    // ========================================
    // Generic User Access (Admin or App User)
    // ========================================

    /**
     * Gets the current user ID regardless of type (admindashboard or app user).
     *
     * @return Optional containing UUID
     */
    public static Optional<UUID> getCurrentAnyUserId() {
        Optional<UUID> adminId = getCurrentAdminId();
        if (adminId.isPresent()) {
            return adminId;
        }
        return getCurrentUserId();
    }

    /**
     * Gets the current user ID, throwing exception if not authenticated.
     *
     * @return UUID of current user (admindashboard or app user)
     * @throws IllegalStateException if not authenticated
     */
    public static UUID requireCurrentAnyUserId() {
        return getCurrentAnyUserId()
                .orElseThrow(() -> new IllegalStateException("Not authenticated"));
    }

    // ========================================
    // Roles and Permissions
    // ========================================

    /**
     * Gets the current admindashboard's roles.
     *
     * @return List of role names, empty list if not admindashboard
     */
    public static List<String> getCurrentRoles() {
        return getCurrentAdminDetails()
                .map(AdminUserDetails::getRoles)
                .orElse(Collections.emptyList());
    }

    /**
     * Gets the current admindashboard's permissions.
     *
     * @return List of permission names, empty list if not admindashboard
     */
    public static List<String> getCurrentPermissions() {
        return getCurrentAdminDetails()
                .map(AdminUserDetails::getPermissions)
                .orElse(Collections.emptyList());
    }

    /**
     * Checks if current admindashboard has a specific role.
     *
     * @param roleName Role name to check
     * @return true if has role
     */
    public static boolean hasRole(String roleName) {
        return getCurrentRoles().contains(roleName);
    }

    /**
     * Checks if current admindashboard has a specific permission.
     *
     * @param permissionName Permission name to check
     * @return true if has permission
     */
    public static boolean hasPermission(String permissionName) {
        return getCurrentPermissions().contains(permissionName);
    }

    /**
     * Checks if current admindashboard has any of the specified permissions.
     *
     * @param permissionNames Permissions to check
     * @return true if has any permission
     */
    public static boolean hasAnyPermission(String... permissionNames) {
        List<String> currentPermissions = getCurrentPermissions();
        for (String permission : permissionNames) {
            if (currentPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if current admindashboard has all of the specified permissions.
     *
     * @param permissionNames Permissions to check
     * @return true if has all permissions
     */
    public static boolean hasAllPermissions(String... permissionNames) {
        List<String> currentPermissions = getCurrentPermissions();
        for (String permission : permissionNames) {
            if (!currentPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }
}
