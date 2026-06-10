package com.mmva.newsapp.infrastructure.rbac.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import com.mmva.newsapp.infrastructure.rbac.permission.core.model.RbacPermission;
import com.mmva.newsapp.infrastructure.rbac.permission.core.repository.PermissionRepository;
import com.mmva.newsapp.infrastructure.rbac.role.core.model.RbacRole;
import com.mmva.newsapp.infrastructure.rbac.role.core.repository.RoleRepository;
import com.mmva.newsapp.infrastructure.rbac.config.model.RbacConfigCache;
import com.mmva.newsapp.infrastructure.rbac.config.repository.RbacConfigCacheRepository;
import com.mmva.newsapp.infrastructure.common.audit.repository.SoftDeleteSpec;
import com.mmva.newsapp.infrastructure.security.userdetails.AdminUserDetails;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PermissionConfigService: In-memory permission cache with automatic refresh
 * 
 * Purpose:
 * - Load all permissions and roles at startup into memory cache
 * - Eliminate N+1 database queries for permission checks
 * - Provide getUserPermissions(user) for fast permission lookups
 * - Implement background listener thread for permission changes
 * - Support multi-server sync without redeployment
 * 
 * Performance:
 * - First-call: Load all permissions (1 DB query)
 * - Subsequent calls: Return from cache (~1ms per request)
 * - Background refresh: Check DB every 30 seconds for changes
 * - Memory footprint: ~500KB for 50 permissions + roles
 * 
 * Architecture:
 * - Cache maps: permissionCache (UUID→Permission), roleCache (UUID→Role)
 * - Background thread: Periodically checks for permission changes
 * - Version tracking: Compares counts to detect changes
 * - Auto-reload: If permission count changed, reload all
 */
@Slf4j
@Service
public class PermissionConfigService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RbacConfigCacheRepository rbacConfigCacheRepository;

    // Caching data structures - volatile for thread safety
    private volatile Map<UUID, RbacPermission> permissionCache = new ConcurrentHashMap<>();
    private volatile Map<UUID, RbacRole> roleCache = new ConcurrentHashMap<>();
    private volatile Map<UUID, Set<GrantedAuthority>> userPermissionCache = new ConcurrentHashMap<>();
    private volatile Map<String, Set<String>> roleNameToPermissionNamesCache = new ConcurrentHashMap<>(); // NEW: role
                                                                                                          // name →
                                                                                                          // permission
                                                                                                          // names

    // Version tracking for multi-server sync
    private volatile int lastPermissionCount = 0;
    private volatile int lastRoleCount = 0;
    private volatile int rbacConfigVersion = 0;
    private volatile long lastLoadTime = 0L;

    // Background listener thread
    private Thread backgroundRefreshThread;
    private volatile boolean shouldRun = true;

    // Constants
    private static final long REFRESH_CHECK_INTERVAL_MS = 30_000; // 30 seconds
    private static final String CACHE_LOADED_MSG = "Configuration loaded successfully. Permissions: {}, Roles: {}";
    private static final String BACKGROUND_REFRESH_MSG = "Background refresh check started";
    private static final String PERMISSION_CHANGE_DETECTED_MSG = "Permission change detected. Reloading configuration. Old count: {}, New count: {}";
    private static final String BACKGROUND_REFRESH_COMPLETE_MSG = "Background refresh completed successfully";
    private static final String RBAC_CONFIG_VERSION_KEY = "RBAC_CONFIG_VERSION";

    public PermissionConfigService(PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            RbacConfigCacheRepository rbacConfigCacheRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.rbacConfigCacheRepository = rbacConfigCacheRepository;
    }

    /**
     * Initialize cache on application startup
     * Loads all active permissions and roles into memory
     */
    @PostConstruct
    public void initializeConfiguration() {
        log.info("Initializing PermissionConfigService cache...");
        reloadConfiguration();
        startBackgroundRefreshThread();
        log.info(CACHE_LOADED_MSG, permissionCache.size(), roleCache.size());
    }

    /**
     * Load all permissions and roles from database
     * Called at startup and whenever changes are detected
     * 
     * Strategy:
     * 1. Query all active permissions from DB (single query)
     * 2. Query all active roles with permissions eagerly fetched (single query with
     * JOIN FETCH)
     * 3. Build roleNameToPermissionNames cache while in transactional context
     * 4. Store in volatile ConcurrentHashMaps for thread-safe access
     * 5. Update version counters for change detection
     */
    @Transactional(readOnly = true)
    public synchronized void reloadConfiguration() {
        try {
            log.debug("Starting configuration reload...");

            // Load all active permissions
            List<RbacPermission> permissions = permissionRepository.findAll(SoftDeleteSpec.notDeleted());
            Map<UUID, RbacPermission> newPermissionCache = new ConcurrentHashMap<>();
            permissions.forEach(p -> newPermissionCache.put(p.getPermissionId(), p));

            // Load all active ROLES WITH PERMISSIONS EAGERLY FETCHED via JOIN FETCH
            // This prevents LazyInitializationException when accessing
            // role.getPermissions()
            List<RbacRole> roles = roleRepository.findAllWithPermissions();
            Map<UUID, RbacRole> newRoleCache = new ConcurrentHashMap<>();
            Map<String, Set<String>> newRoleNameToPermissionNamesCache = new ConcurrentHashMap<>();

            // Build role name → permission names cache
            for (RbacRole role : roles) {
                newRoleCache.put(role.getRoleId(), role);

                // Permissions are already eagerly loaded, no lazy loading needed
                Set<String> permissionNames = new HashSet<>();
                if (role.getPermissions() != null) {
                    role.getPermissions().stream()
                            .filter(p -> Boolean.TRUE.equals(p.getIsActive()) && p.getDeletedAt() == null)
                            .forEach(p -> permissionNames.add(p.getPermissionName()));
                }
                newRoleNameToPermissionNamesCache.put(role.getRoleName(), permissionNames);

                log.debug("Loaded role '{}' with {} permissions", role.getRoleName(), permissionNames.size());
            }

            // Update caches atomically
            this.permissionCache = newPermissionCache;
            this.roleCache = newRoleCache;
            this.roleNameToPermissionNamesCache = newRoleNameToPermissionNamesCache;
            this.lastPermissionCount = permissions.size();
            this.lastRoleCount = roles.size();
            this.lastLoadTime = System.currentTimeMillis();

            // Clear user permission cache since roles/permissions changed
            this.userPermissionCache.clear();

            log.info(CACHE_LOADED_MSG, permissionCache.size(), roleCache.size());

        } catch (Exception e) {
            log.error("Error reloading configuration", e);
            // Don't throw - keep existing cache if reload fails
        }
    }

    /**
     * Get all permissions for a user based on their assigned roles
     * 
     * Process:
     * 1. Check if user is null
     * 2. Get user's roles from JWT/SecurityContext
     * 3. For each role name, lookup pre-loaded permission names from cache
     * 4. Return Set<GrantedAuthority> with all permissions
     * 
     * @param user AdminUserDetails with assigned roles
     * @return Set of GrantedAuthority for all user permissions
     */
    public Set<GrantedAuthority> getUserPermissions(AdminUserDetails user) {
        if (user == null) {
            log.warn("getUserPermissions called with null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        UUID userId = user.getAdminId();

        // Check cache first
        if (userPermissionCache.containsKey(userId)) {
            log.debug("User permissions retrieved from cache for user: {}", userId);
            return userPermissionCache.get(userId);
        }

        log.debug("Computing permissions for user: {}", userId);

        // Get user's roles
        List<String> userRoles = user.getRoles();
        if (userRoles == null || userRoles.isEmpty()) {
            log.debug("User {} has no roles assigned", userId);
            Set<GrantedAuthority> emptySet = Collections.emptySet();
            userPermissionCache.put(userId, emptySet);
            return emptySet;
        }

        // Collect all permissions from pre-built role→permission map
        // This avoids accessing lazy-loaded Hibernate collections
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (String roleName : userRoles) {
            Set<String> permissionNames = roleNameToPermissionNamesCache.get(roleName);

            if (permissionNames == null) {
                log.warn("Role '{}' not found in permission cache for user {}", roleName, userId);
                continue;
            }

            // Convert permission names to GrantedAuthority
            permissionNames.forEach(pName -> authorities.add(new SimpleGrantedAuthority(pName)));
        }

        log.debug("User {} has {} permissions from roles {}", userId, authorities.size(), userRoles);

        // Cache result
        userPermissionCache.put(userId, authorities);

        return authorities;
    }

    /**
     * Check if user has specific permission
     * 
     * @param user           AdminUserDetails
     * @param permissionName Permission name to check
     * @return true if user has permission
     */
    public boolean hasPermission(AdminUserDetails user, String permissionName) {
        if (user == null || permissionName == null) {
            return false;
        }

        Set<GrantedAuthority> permissions = getUserPermissions(user);
        return permissions.stream()
                .anyMatch(auth -> auth.getAuthority().equals(permissionName));
    }

    /**
     * Get permission by ID
     * 
     * @param permissionId UUID of permission
     * @return Optional containing Permission if found
     */
    public Optional<RbacPermission> getPermissionById(UUID permissionId) {
        return Optional.ofNullable(permissionCache.get(permissionId));
    }

    /**
     * Get permission by name
     * 
     * @param permissionName Name of permission
     * @return Optional containing Permission if found
     */
    public Optional<RbacPermission> getPermissionByName(String permissionName) {
        return permissionCache.values().stream()
                .filter(p -> p.getPermissionName().equals(permissionName))
                .findFirst();
    }

    /**
     * Get role by ID
     * 
     * @param roleId UUID of role
     * @return Optional containing Role if found
     */
    public Optional<RbacRole> getRoleById(UUID roleId) {
        return Optional.ofNullable(roleCache.get(roleId));
    }

    /**
     * Get role by name
     * 
     * @param roleName Name of role
     * @return Optional containing Role if found
     */
    public Optional<RbacRole> getRoleByName(String roleName) {
        return roleCache.values().stream()
                .filter(r -> r.getRoleName().equals(roleName))
                .findFirst();
    }

    /**
     * Get all permissions from cache
     * 
     * @return Map of all permissions
     */
    public Map<UUID, RbacPermission> getAllPermissions() {
        return new ConcurrentHashMap<>(permissionCache);
    }

    /**
     * Get all roles from cache
     * 
     * @return Map of all roles
     */
    public Map<UUID, RbacRole> getAllRoles() {
        return new ConcurrentHashMap<>(roleCache);
    }

    /**
     * Get cache statistics
     * 
     * @return String with cache info
     */
    public String getCacheStats() {
        return String.format(
                "Cache Stats - Permissions: %d, Roles: %d, Cached Users: %d, Last Load: %tT",
                permissionCache.size(),
                roleCache.size(),
                userPermissionCache.size(),
                new Date(lastLoadTime));
    }

    /**
     * Start background refresh thread
     * Runs every 30 seconds to check for permission changes
     * Uses version tracking to detect changes without full comparison
     */
    private void startBackgroundRefreshThread() {
        backgroundRefreshThread = new Thread(() -> {
            log.info(BACKGROUND_REFRESH_MSG);

            while (shouldRun) {
                try {
                    Thread.sleep(REFRESH_CHECK_INTERVAL_MS);

                    // Check if RBAC config version changed (multi-server sync)
                    if (hasRbacConfigVersionChanged()) {
                        log.info("RBAC config version changed - reloading all permissions and roles");
                        reloadConfiguration();
                        continue;
                    }

                    // Check if permission count changed (single-server detection)
                    int currentPermissionCount = permissionRepository.findAll(SoftDeleteSpec.notDeleted()).size();
                    int currentRoleCount = roleRepository.findAll(SoftDeleteSpec.notDeleted()).size();

                    if (currentPermissionCount != lastPermissionCount ||
                            currentRoleCount != lastRoleCount) {

                        log.info(
                                PERMISSION_CHANGE_DETECTED_MSG,
                                lastPermissionCount,
                                currentPermissionCount);
                        reloadConfiguration();
                    }

                    log.debug(BACKGROUND_REFRESH_COMPLETE_MSG);

                } catch (InterruptedException e) {
                    if (shouldRun) {
                        log.debug("Background refresh thread interrupted", e);
                    }
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("Error in background refresh thread", e);
                }
            }
        });

        backgroundRefreshThread.setName("PermissionConfigService-RefreshThread");
        backgroundRefreshThread.setDaemon(true);
        backgroundRefreshThread.start();
    }

    /**
     * Shutdown gracefully
     * Stops background thread
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down PermissionConfigService");
        shouldRun = false;

        if (backgroundRefreshThread != null && backgroundRefreshThread.isAlive()) {
            backgroundRefreshThread.interrupt();
            try {
                backgroundRefreshThread.join(5000);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for background thread to stop", e);
                Thread.currentThread().interrupt();
            }
        }

        permissionCache.clear();
        roleCache.clear();
        userPermissionCache.clear();

        log.info("PermissionConfigService shutdown complete");
    }

    /**
     * Manual invalidation of user permission cache
     * Call when user's role assignments change
     * 
     * @param userId UUID of user whose cache should be invalidated
     */
    public void invalidateUserPermissionCache(UUID userId) {
        userPermissionCache.remove(userId);
        log.debug("Invalidated permission cache for user: {}", userId);
    }

    /**
     * Force immediate reload (useful for testing or manual refresh)
     */
    public void forceReload() {
        log.info("Force reload requested");
        reloadConfiguration();
    }

    // ======================================
    // Configuration Retrieval Methods
    // ======================================

    /**
     * Get configuration value as String
     * Returns null if config key not found or inactive
     * 
     * @param configKey Configuration key (e.g., "RBAC_CONFIG_VERSION")
     * @return Configuration value or null
     */
    public String getConfig(String configKey) {
        try {
            return rbacConfigCacheRepository.findByConfigKey(configKey)
                    .filter(RbacConfigCache::getConfigIsActive)
                    .map(RbacConfigCache::getConfigValue)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error retrieving config: {}", configKey, e);
            return null;
        }
    }

    /**
     * Get configuration value as Integer
     * 
     * @param configKey Configuration key
     * @return Configuration value as Integer or null if not found/invalid
     */
    public Integer getConfigAsInteger(String configKey) {
        try {
            String value = getConfig(configKey);
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            log.warn("Invalid integer config value for key: {}", configKey, e);
            return null;
        }
    }

    /**
     * Get configuration value as Boolean
     * 
     * @param configKey Configuration key
     * @return Configuration value as Boolean or null if not found
     */
    public Boolean getConfigAsBoolean(String configKey) {
        try {
            String value = getConfig(configKey);
            return value != null ? Boolean.parseBoolean(value) : null;
        } catch (Exception e) {
            log.warn("Invalid boolean config value for key: {}", configKey, e);
            return null;
        }
    }

    /**
     * Get current RBAC config version
     * Used to detect if permissions have changed on other servers
     * 
     * @return Current version number
     */
    public int getRbacConfigVersion() {
        Integer version = getConfigAsInteger(RBAC_CONFIG_VERSION_KEY);
        return version != null ? version : 1;
    }

    /**
     * Check if RBAC version has changed (for multi-server sync)
     * 
     * @return true if version changed since last check
     */
    public boolean hasRbacConfigVersionChanged() {
        int currentVersion = getRbacConfigVersion();
        if (currentVersion != rbacConfigVersion) {
            log.info("RBAC config version changed: {} → {}", rbacConfigVersion, currentVersion);
            rbacConfigVersion = currentVersion;
            return true;
        }
        return false;
    }
}
