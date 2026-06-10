package com.mmva.newsapp.domain.appuser.repository.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mmva.newsapp.domain.appuser.model.core.AppUsers;
import com.mmva.newsapp.domain.appuser.enums.core.AppUserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AppUsers entity.
 * 
 * <p>
 * Provides data access operations for app user management.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface AppUserRepository
                extends JpaRepository<AppUsers, UUID>, JpaSpecificationExecutor<AppUsers> {

        // ========================================
        // Login Methods (may find deleted for error message)
        // ========================================

        Optional<AppUsers> findByAppUsersEmail(String appUsersEmail);

        Optional<AppUsers> findByAppUsersDeviceId(String appUsersDeviceId);

        Optional<AppUsers> findByAppUsersPhoneNumber(String appUsersPhoneNumber);

        // ========================================
        // Active User Lookups (excludes soft-deleted)
        // ========================================

        @Query("SELECT u FROM AppUsers u WHERE u.deletedAt IS NULL AND u.appUsersEmail = :email")
        Optional<AppUsers> findActiveByEmail(@Param("email") String email);

        @Query("SELECT u FROM AppUsers u WHERE u.deletedAt IS NULL AND u.appUsersPhoneNumber = :phoneNumber")
        Optional<AppUsers> findActiveByPhoneNumber(@Param("phoneNumber") String phoneNumber);

        @Query("SELECT u FROM AppUsers u WHERE u.deletedAt IS NULL AND u.appUsersOauthProvider = :provider AND u.appUsersOauthProviderId = :providerId")
        Optional<AppUsers> findActiveByOauthProvider(@Param("provider") String oauthProvider,
                        @Param("providerId") String oauthProviderId);

        // ========================================
        // Uniqueness Checks (excludes soft-deleted)
        // ========================================

        /**
         * Check if email exists (includes soft-deleted).
         */
        boolean existsByAppUsersEmail(String appUsersEmail);

        /**
         * Check if phone exists (includes soft-deleted).
         */
        boolean existsByAppUsersPhoneNumber(String appUsersPhoneNumber);

        /**
         * Check if active (non-deleted) user exists with given email.
         * Use for registration validation.
         */
        @Query("SELECT COUNT(u) > 0 FROM AppUsers u WHERE u.deletedAt IS NULL AND u.appUsersEmail = :email")
        boolean existsActiveByEmail(@Param("email") String email);

        /**
         * Check if active (non-deleted) user exists with given phone.
         * Use for registration validation.
         */
        @Query("SELECT COUNT(u) > 0 FROM AppUsers u WHERE u.deletedAt IS NULL AND u.appUsersPhoneNumber = :phoneNumber")
        boolean existsActiveByPhoneNumber(@Param("phoneNumber") String phoneNumber);

        // ========================================
        // Search & Filter Methods (excludes soft-deleted)
        // ========================================

        /**
         * Search active users by name with partial match.
         */
        @Query("SELECT u FROM AppUsers u WHERE u.deletedAt IS NULL AND (" +
                        "LOWER(u.appUsersFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(u.appUsersLastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(u.appUsersFullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(u.appUsersUsername) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
        Page<AppUsers> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

        /**
         * Filter active users by status.
         */
        @Query("SELECT u FROM AppUsers u WHERE u.deletedAt IS NULL AND u.appUsersStatus = :status")
        Page<AppUsers> findActiveByStatus(@Param("status") AppUserStatus status, Pageable pageable);

        /**
         * Filter users by status (includes soft-deleted for admindashboard).
         */
        Page<AppUsers> findByAppUsersStatus(AppUserStatus status, Pageable pageable);

        /**
         * Search active users by name and filter by status.
         */
        @Query("SELECT u FROM AppUsers u WHERE u.deletedAt IS NULL AND " +
                        "(LOWER(u.appUsersFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(u.appUsersLastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(u.appUsersFullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(u.appUsersUsername) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                        "AND u.appUsersStatus = :status")
        Page<AppUsers> searchByNameAndStatus(@Param("searchTerm") String searchTerm,
                        @Param("status") AppUserStatus status,
                        Pageable pageable);

        // ========================================
        // OAuth/Social Login Methods
        // ========================================

        /**
         * Find user by OAuth provider (may include deleted for login handling).
         */
        Optional<AppUsers> findByAppUsersOauthProviderAndAppUsersOauthProviderId(String oauthProvider,
                        String oauthProviderId);

        // ========================================
        // Segment Methods (for push notifications)
        // ========================================

        /**
         * Find active user IDs by segment.
         * Used for segment-targeted push notifications.
         * 
         * @param segment the segment identifier (e.g., "inactive_7_days",
         *                "power_users")
         * @return list of user IDs in the segment
         */
        @Query("SELECT u.appUsersId FROM AppUsers u WHERE u.deletedAt IS NULL AND u.appUsersSegment = :segment AND u.appUsersStatus = 'ACTIVE'")
        List<UUID> findActiveUserIdsBySegment(@Param("segment") String segment);

        /**
         * Count active users in a segment.
         * 
         * @param segment the segment identifier
         * @return count of users in segment
         */
        @Query("SELECT COUNT(u) FROM AppUsers u WHERE u.deletedAt IS NULL AND u.appUsersSegment = :segment AND u.appUsersStatus = 'ACTIVE'")
        long countActiveUsersBySegment(@Param("segment") String segment);
}
