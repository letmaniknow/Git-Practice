package com.mmva.newsapp.infrastructure.security.userdetails;

import com.mmva.newsapp.domain.adminuser.enums.core.AdminStatus;
import com.mmva.newsapp.domain.adminuser.model.core.AdminUser;
import com.mmva.newsapp.domain.adminuser.repository.core.AdminUserRepository;
import com.mmva.newsapp.infrastructure.rbac.config.PermissionConfigService;
import com.mmva.newsapp.infrastructure.rbac.permission.core.model.RbacPermission;
import com.mmva.newsapp.infrastructure.rbac.role.core.model.RbacRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UserDetailsService implementation for Admin users.
 * 
 * <p>
 * Loads admindashboard user details from database for authentication.
 * Supports loading by username or email.
 * </p>
 * 
 * <h2>Architectural Note:</h2>
 * <p>
 * This class lives in infrastructure/security/ (not in adminuser/) because it
 * implements
 * Spring Security's UserDetailsService interface - making it a framework
 * adapter.
 * It is allowed to import from domain modules (adminuser/) as infrastructure
 * adapters
 * bridge framework requirements to domain models. The dependency direction is:
 * infrastructure/security → adminuser/model (allowed).
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 * @see AdminUserDetails
 */
@Slf4j
@Service("adminUserDetailsService")
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;
    private final PermissionConfigService permissionConfigService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading admindashboard user by username/email: {}", usernameOrEmail);

        // Try to find by username first, then by email
        AdminUser adminUser = adminUserRepository.findByAdminUsersUsername(usernameOrEmail)
                .or(() -> adminUserRepository.findByAdminUsersEmail(usernameOrEmail))
                .orElseThrow(() -> {
                    log.warn("Admin user not found: {}", usernameOrEmail);
                    return new UsernameNotFoundException("Admin user not found: " + usernameOrEmail);
                });

        return buildUserDetails(adminUser);
    }

    /**
     * Loads admindashboard user details by admindashboard ID.
     *
     * @param adminId Admin UUID
     * @return AdminUserDetails
     * @throws UsernameNotFoundException if admindashboard not found
     */
    @Transactional(readOnly = true)
    public AdminUserDetails loadUserById(java.util.UUID adminId) throws UsernameNotFoundException {
        log.debug("Loading admindashboard user by ID: {}", adminId);

        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> {
                    log.warn("Admin user not found by ID: {}", adminId);
                    return new UsernameNotFoundException("Admin user not found with ID: " + adminId);
                });

        return buildUserDetails(adminUser);
    }

    /**
     * Builds AdminUserDetails from AdminUser entity.
     * Loads role name directly from role relationship.
     * Permissions will be loaded from cache via
     * PermissionConfigService.getUserPermissions()
     * when needed during request processing.
     */
    private AdminUserDetails buildUserDetails(AdminUser adminUser) {
        // Get role name
        List<String> roles = new ArrayList<>();
        if (adminUser.getRole() != null) {
            roles.add(adminUser.getRole().getRoleName());
        }

        // Permissions will be loaded from cache by PermissionConfigService
        // when called from JwtAuthenticationFilter or request processing
        List<String> permissions = new ArrayList<>();

        // Determine if account is active based on status field
        // ACTIVE status means the account is enabled
        boolean isEnabled = AdminStatus.ACTIVE.equals(adminUser.getAdminUsersStatus()) &&
                adminUser.getDeletedAt() == null;

        boolean isLocked = Boolean.TRUE.equals(adminUser.getAdminUsersAccountLocked());

        log.debug("Built AdminUserDetails for: {} with roles: {} and permissions will be loaded on demand from cache",
                adminUser.getAdminUsersUsername(), roles);

        return new AdminUserDetails(
                adminUser.getAdminUsersId(),
                adminUser.getAdminUsersUsername(),
                adminUser.getAdminUsersEmail(),
                adminUser.getAdminUsersPasswordHash(),
                isEnabled,
                isLocked,
                roles,
                permissions);
    }
}
