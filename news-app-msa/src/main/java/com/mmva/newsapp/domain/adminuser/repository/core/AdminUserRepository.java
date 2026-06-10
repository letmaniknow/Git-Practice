package com.mmva.newsapp.domain.adminuser.repository.core;

import com.mmva.newsapp.domain.adminuser.model.core.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, UUID>, JpaSpecificationExecutor<AdminUser> {

        // ========================================
        // Login Methods (may find deleted for error message)
        // ========================================

        Optional<AdminUser> findByAdminUsersUsername(String adminUsersUsername);

        Optional<AdminUser> findByAdminUsersEmail(String adminUsersEmail);

        // ========================================
        // Active User Lookups (excludes soft-deleted)
        // ========================================

        @Query("SELECT a FROM AdminUser a WHERE a.deletedAt IS NULL AND a.adminUsersUsername = :username")
        Optional<AdminUser> findActiveByUsername(@Param("username") String username);

        @Query("SELECT a FROM AdminUser a WHERE a.deletedAt IS NULL AND a.adminUsersEmail = :email")
        Optional<AdminUser> findActiveByEmail(@Param("email") String email);

        // ========================================
        // Uniqueness Checks (excludes soft-deleted)
        // ========================================

        /**
         * Check if username exists (includes soft-deleted).
         */
        boolean existsByAdminUsersUsername(String adminUsersUsername);

        /**
         * Check if email exists (includes soft-deleted).
         */
        boolean existsByAdminUsersEmail(String adminUsersEmail);

        /**
         * Check if active (non-deleted) admindashboard exists with given username.
         * Use for uniqueness validation.
         */
        @Query("SELECT COUNT(a) > 0 FROM AdminUser a WHERE a.deletedAt IS NULL AND a.adminUsersUsername = :username")
        boolean existsActiveByUsername(@Param("username") String username);

        /**
         * Check if active (non-deleted) admindashboard exists with given email.
         * Use for uniqueness validation.
         */
        @Query("SELECT COUNT(a) > 0 FROM AdminUser a WHERE a.deletedAt IS NULL AND a.adminUsersEmail = :email")
        boolean existsActiveByEmail(@Param("email") String email);

        // ========================================
        // Admin Listing by Role (excludes soft-deleted)
        // ========================================

        @Query("SELECT a FROM AdminUser a WHERE a.deletedAt IS NULL AND a.role.roleName = :roleName")
        org.springframework.data.domain.Page<AdminUser> findActiveByRoleName(@Param("roleName") String roleName,
                        org.springframework.data.domain.Pageable pageable);

        @Query("SELECT a FROM AdminUser a WHERE a.deletedAt IS NULL AND a.role.roleId = :roleId")
        org.springframework.data.domain.Page<AdminUser> findActiveByRoleId(@Param("roleId") UUID roleId,
                        org.springframework.data.domain.Pageable pageable);

        @Query("SELECT a FROM AdminUser a WHERE a.role.roleName = :roleName")
        org.springframework.data.domain.Page<AdminUser> findByRoleName(@Param("roleName") String roleName,
                        org.springframework.data.domain.Pageable pageable);

        @Query("SELECT a FROM AdminUser a WHERE a.role.roleId = :roleId")
        org.springframework.data.domain.Page<AdminUser> findByRoleId(@Param("roleId") UUID roleId,
                        org.springframework.data.domain.Pageable pageable);

        /**
         * Search active (non-deleted) admin users by partial name, username, or email.
         * Uses PostgreSQL ILIKE for native case-insensitive pattern matching.
         * A GIN index on pg_trgm can be added to these columns for large datasets.
         *
         * @param query    partial string to match against username, email, or full name
         * @param pageable pagination info
         * @return page of matching admin users
         */
        @Query(value = "SELECT * FROM admin_users WHERE deleted_at IS NULL AND ("
                        + "admin_users_username ILIKE CONCAT('%', :query, '%') OR "
                        + "admin_users_email ILIKE CONCAT('%', :query, '%') OR "
                        + "admin_users_full_name ILIKE CONCAT('%', :query, '%'))", countQuery = "SELECT COUNT(*) FROM admin_users WHERE deleted_at IS NULL AND ("
                                        + "admin_users_username ILIKE CONCAT('%', :query, '%') OR "
                                        + "admin_users_email ILIKE CONCAT('%', :query, '%') OR "
                                        + "admin_users_full_name ILIKE CONCAT('%', :query, '%'))", nativeQuery = true)
        org.springframework.data.domain.Page<AdminUser> searchActiveByNameOrUsernameOrEmail(
                        @Param("query") String query, org.springframework.data.domain.Pageable pageable);

        /**
         * Find all soft-deleted admindashboard users.
         * Works when soft-delete filter is disabled (admindashboard endpoints).
         */
        java.util.List<AdminUser> findByDeletedAtIsNotNull();
}
