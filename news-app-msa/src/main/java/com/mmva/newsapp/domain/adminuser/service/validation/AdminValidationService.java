package com.mmva.newsapp.domain.adminuser.service.validation;

import com.mmva.newsapp.infrastructure.rbac.RbacPermissionConstants;
import com.mmva.newsapp.infrastructure.common.exception.UnauthorizedAccessException;

import java.util.UUID;

/**
 * Service for validating admindashboard users across all admindashboard
 * controllers.
 * Provides centralized admindashboard authentication validation.
 * 
 * <p>
 * This service supports two phases of authentication:
 * </p>
 * <ul>
 * <li><b>Phase 1 (Current):</b> Parameter-based validation using adminId</li>
 * <li><b>Phase 2 (JWT):</b> Token-based validation using SecurityContext</li>
 * </ul>
 * 
 * @see JWT_MIGRATION_PLAN.md for migration details
 */
public interface AdminValidationService {

    // =========================
    // Phase 1: Parameter-based validation (Current)
    // =========================

    /**
     * Validates that the given adminId belongs to an existing admindashboard user.
     * Throws UnauthorizedAccessException if validation fails.
     *
     * @param adminId The UUID of the admindashboard user to validate.
     * @throws UnauthorizedAccessException if adminId is
     *                                     null or not
     *                                     found.
     */
    void validateAdmin(UUID adminId);

    /**
     * Validates the current admindashboard from SecurityContext and returns the
     * admindashboard ID.
     * Combines SecurityUtils.requireCurrentAdminId() + validateAdmin() in one call.
     * Use this in controllers instead of duplicating the validation logic.
     *
     * @return The UUID of the validated admindashboard.
     * @throws UnauthorizedAccessException if validation
     *                                     fails.
     */
    UUID validateAndGetAdminId();

    /**
     * Checks if an admindashboard user exists without throwing an exception.
     *
     * @param adminId The UUID of the admindashboard user to check.
     * @return true if the admindashboard exists, false otherwise.
     */
    boolean isValidAdmin(UUID adminId);

    // =========================
    // Phase 1.5: RBAC Permission-based validation
    // =========================

    /**
     * Validates that the admindashboard exists AND has the required permission.
     * Throws UnauthorizedAccessException if validation fails.
     *
     * @param adminId    The UUID of the admindashboard user to validate.
     * @param permission The permission name to check (e.g., "staff.create").
     * @throws UnauthorizedAccessException if adminId is
     *                                     null, not found,
     *                                     or lacks
     *                                     permission.
     * @see RbacPermissionConstants for permission constants
     */
    void validateAdminWithPermission(UUID adminId, String permission);

    /**
     * Checks if an admindashboard has a specific permission without throwing an
     * exception.
     *
     * @param adminId    The UUID of the admindashboard user.
     * @param permission The permission name to check.
     * @return true if the admindashboard exists and has the permission, false
     *         otherwise.
     */
    boolean hasPermission(UUID adminId, String permission);

    // =========================
    // Phase 2: JWT-based validation (Future - Production)
    // =========================

    /**
     * Gets the current admindashboard's UUID from the SecurityContext (JWT token).
     * Use this after JWT migration is complete.
     *
     * @return The UUID of the currently authenticated admindashboard.
     * @throws UnauthorizedAccessException if no admindashboard is
     *                                     authenticated.
     */
    default UUID getCurrentAdminId() {
        throw new UnsupportedOperationException("JWT authentication not yet implemented");
    }

    /**
     * Validates the current admindashboard from the JWT token in SecurityContext.
     * Call this instead of validateAdmin(adminId) after JWT migration.
     *
     * @throws UnauthorizedAccessException if validation
     *                                     fails.
     */
    default void validateCurrentAdmin() {
        throw new UnsupportedOperationException("JWT authentication not yet implemented");
    }
}
