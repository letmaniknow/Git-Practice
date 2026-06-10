package com.mmva.newsapp.infrastructure.rbac.role.core.repository;

import com.mmva.newsapp.infrastructure.rbac.role.core.model.RbacRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RbacRole, UUID>, JpaSpecificationExecutor<RbacRole> {
    /**
     * Check if role name exists (includes soft-deleted).
     */
    boolean existsByRoleName(String roleName);

    /**
     * Check if active (non-deleted) role exists with given name.
     * Use for uniqueness validation.
     */
    @Query("SELECT COUNT(r) > 0 FROM RbacRole r WHERE r.deletedAt IS NULL AND r.roleName = :roleName")
    boolean existsActiveByRoleName(@Param("roleName") String roleName);

    /**
     * Find role by name (may include soft-deleted for admindashboard restore).
     */
    Optional<RbacRole> findByRoleName(String roleName);

    /**
     * Find active (non-deleted) role by name.
     */
    @Query("SELECT r FROM RbacRole r WHERE r.deletedAt IS NULL AND r.roleName = :roleName")
    Optional<RbacRole> findActiveByRoleName(@Param("roleName") String roleName);

    /**
     * Find role by ID with permissions eagerly fetched.
     * Use this when you need to access permissions to avoid
     * LazyInitializationException.
     */
    @Query("SELECT r FROM RbacRole r LEFT JOIN FETCH r.permissions WHERE r.deletedAt IS NULL AND r.roleId = :roleId")
    Optional<RbacRole> findByIdWithPermissions(@Param("roleId") UUID roleId);

    /**
     * Find role by name with permissions eagerly fetched.
     */
    @Query("SELECT r FROM RbacRole r LEFT JOIN FETCH r.permissions WHERE r.deletedAt IS NULL AND r.roleName = :roleName")
    Optional<RbacRole> findByRoleNameWithPermissions(@Param("roleName") String roleName);

    /**
     * Find all roles with permissions eagerly fetched.
     */
    @Query("SELECT DISTINCT r FROM RbacRole r LEFT JOIN FETCH r.permissions WHERE r.deletedAt IS NULL")
    List<RbacRole> findAllWithPermissions();

    @Query("SELECT r FROM RbacRole r WHERE r.deletedAt IS NULL AND (LOWER(r.roleName) LIKE LOWER(CONCAT('%', :query, '%')) "
            +
            "OR LOWER(r.roleDescription) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<RbacRole> searchByNameOrDescription(@Param("query") String query);

    @Query("SELECT DISTINCT r FROM RbacRole r JOIN r.permissions p WHERE r.deletedAt IS NULL AND p.permissionId = :permissionId")
    List<RbacRole> findByPermissionId(@Param("permissionId") UUID permissionId);
}
