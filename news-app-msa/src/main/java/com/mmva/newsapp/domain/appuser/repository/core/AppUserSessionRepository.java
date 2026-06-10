package com.mmva.newsapp.domain.appuser.repository.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mmva.newsapp.domain.appuser.model.core.AppUserSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AppUserSession entity.
 * 
 * <p>
 * Table: app_users_sessions
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface AppUserSessionRepository extends JpaRepository<AppUserSession, UUID> {

    /**
     * Find all active sessions for a user.
     */
    List<AppUserSession> findByAppUsersSessionsUserIdAndAppUsersSessionsIsActiveTrue(UUID userId);

    /**
     * Find session by token.
     */
    Optional<AppUserSession> findByAppUsersSessionsTokenAndAppUsersSessionsIsActiveTrue(String sessionToken);

    /**
     * Invalidate all sessions for a user.
     */
    @Modifying
    @Query("UPDATE AppUserSession s SET s.appUsersSessionsIsActive = false WHERE s.appUsersSessionsUserId = :userId")
    int invalidateAllUserSessions(@Param("userId") UUID userId);

    /**
     * Invalidate a specific session.
     */
    @Modifying
    @Query("UPDATE AppUserSession s SET s.appUsersSessionsIsActive = false WHERE s.appUsersSessionsId = :sessionId")
    int invalidateSession(@Param("sessionId") UUID sessionId);

    /**
     * Count active sessions for a user.
     */
    long countByAppUsersSessionsUserIdAndAppUsersSessionsIsActiveTrue(UUID userId);

    /**
     * Delete expired sessions.
     */
    @Modifying
    @Query("DELETE FROM AppUserSession s WHERE s.appUsersSessionsExpiresAt < :currentTime")
    int deleteExpiredSessions(@Param("currentTime") String currentTime);
}
