package com.mmva.newsapp.infrastructure.rbac.permission.core.repository;

import com.mmva.newsapp.infrastructure.rbac.permission.core.model.RbacPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionRepository
        extends JpaRepository<RbacPermission, UUID>, JpaSpecificationExecutor<RbacPermission> {
    /**
     * Check if permission name exists (includes soft-deleted).
     * Use existsByPermissionNameActive for uniqueness checks.
     */
    boolean existsByPermissionName(String permissionName);

    /**
     * Check if active (non-deleted) permission exists with given name.
     * Use this for uniqueness validation when creating/updating permissions.
     */
    @Query("SELECT COUNT(p) > 0 FROM RbacPermission p WHERE p.deletedAt IS NULL AND p.permissionName = :permissionName")
    boolean existsByPermissionNameActive(@Param("permissionName") String permissionName);

    /**
     * Find all soft-deleted permissions.
     * Works when soft-delete filter is disabled (admindashboard endpoints).
     */
    List<RbacPermission> findByDeletedAtIsNotNull();
}
