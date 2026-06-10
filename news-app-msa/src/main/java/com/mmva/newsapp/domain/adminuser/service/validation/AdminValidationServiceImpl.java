package com.mmva.newsapp.domain.adminuser.service.validation;

// ==================== Exception ====================
import com.mmva.newsapp.infrastructure.common.exception.UnauthorizedAccessException;

// ==================== Security ====================
import com.mmva.newsapp.infrastructure.security.util.SecurityContextUtils;

// ==================== Repository ====================
import com.mmva.newsapp.domain.adminuser.repository.core.AdminUserRepository;
import com.mmva.newsapp.domain.adminuser.service.core.AdminUserService;

// ==================== Lombok ====================
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ==================== Spring ====================
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ==================== Java ====================
import java.util.UUID;

/**
 * Implementation of AdminValidationService.
 * <p>
 * Provides centralized admindashboard user validation for all admindashboard
 * controllers.
 * Supports both existence checks and RBAC permission validation.
 * </p>
 *
 * <h2>Validation Methods:</h2>
 * <ol>
 * <li>{@link #validateAdmin(UUID)} - Validate admindashboard exists (throws
 * exception)</li>
 * <li>{@link #isValidAdmin(UUID)} - Check admindashboard exists (returns
 * boolean)</li>
 * <li>{@link #validateAdminWithPermission(UUID, String)} - Validate
 * admindashboard with
 * permission (throws exception)</li>
 * <li>{@link #hasPermission(UUID, String)} - Check admindashboard has
 * permission
 * (returns boolean)</li>
 * </ol>
 *
 * @author MMVA
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminValidationServiceImpl implements AdminValidationService {

    private final AdminUserRepository adminUserRepository;
    private final AdminUserService adminUserService;

    // ==================== 1. Validate Admin (Throws Exception)
    // ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateAdmin(UUID adminId) {
        log.debug("AdminValidationService: Validating admindashboard user with ID: {}", adminId);

        if (adminId == null) {
            log.warn("AdminValidationService: Validation failed - adminId is null");
            throw new UnauthorizedAccessException(null, "access admindashboard resources");
        }

        if (!adminUserRepository.existsById(adminId)) {
            log.warn("AdminValidationService: Validation failed - admindashboard user {} not found", adminId);
            throw new UnauthorizedAccessException(adminId, "access admindashboard resources");
        }

        log.debug("AdminValidationService: Admin user {} validated successfully", adminId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID validateAndGetAdminId() {
        UUID adminId = SecurityContextUtils.requireCurrentAdminId();
        validateAdmin(adminId);
        return adminId;
    }

    // ==================== 2. Check Admin Valid (Returns Boolean)
    // ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidAdmin(UUID adminId) {
        log.debug("AdminValidationService: Checking if admindashboard user {} is valid", adminId);

        if (adminId == null) {
            log.debug("AdminValidationService: Admin validation check - adminId is null, returning false");
            return false;
        }

        boolean exists = adminUserRepository.existsById(adminId);
        log.debug("AdminValidationService: Admin user {} exists: {}", adminId, exists);

        return exists;
    }

    // ==================== 3. Validate Admin With Permission (Throws Exception)
    // ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateAdminWithPermission(UUID adminId, String permission) {
        log.debug("AdminValidationService: Validating admindashboard {} with permission '{}'", adminId, permission);

        // First validate the admindashboard exists
        validateAdmin(adminId);

        // Then check for the required permission
        if (permission == null || permission.isBlank()) {
            log.warn("AdminValidationService: Validation failed - permission name is null or empty");
            throw new IllegalArgumentException("Permission name cannot be null or empty");
        }

        if (!adminUserService.checkAdminPermission(adminId, permission)) {
            log.warn("AdminValidationService: Validation failed - admindashboard {} lacks permission '{}'", adminId,
                    permission);
            throw new UnauthorizedAccessException(adminId, "perform action requiring '" + permission + "' permission");
        }

        log.info("AdminValidationService: Admin user {} validated with permission '{}' successfully", adminId,
                permission);
    }

    // ==================== 4. Check Admin Has Permission (Returns Boolean)
    // ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPermission(UUID adminId, String permission) {
        log.debug("AdminValidationService: Checking if admindashboard {} has permission '{}'", adminId, permission);

        if (!isValidAdmin(adminId)) {
            log.debug("AdminValidationService: Permission check - admindashboard {} is not valid", adminId);
            return false;
        }

        if (permission == null || permission.isBlank()) {
            log.debug("AdminValidationService: Permission check - permission is null or empty");
            return false;
        }

        boolean hasPermission = adminUserService.checkAdminPermission(adminId, permission);
        log.debug("AdminValidationService: Admin {} has permission '{}': {}", adminId, permission, hasPermission);

        return hasPermission;
    }
}
