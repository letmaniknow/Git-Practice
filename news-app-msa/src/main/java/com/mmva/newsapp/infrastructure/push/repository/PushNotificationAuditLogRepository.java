package com.mmva.newsapp.infrastructure.push.repository;

import com.mmva.newsapp.infrastructure.push.model.PushNotificationAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for push notification audit logs.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface PushNotificationAuditLogRepository extends JpaRepository<PushNotificationAuditLog, Long> {

    /**
     * Find audit logs for a specific notification.
     */
    List<PushNotificationAuditLog> findByNotificationIdOrderByCreatedAtDesc(UUID notificationId);

    /**
     * Find audit logs by actor (admindashboard who performed actions).
     */
    Page<PushNotificationAuditLog> findByActorIdOrderByCreatedAtDesc(UUID actorId, Pageable pageable);

    /**
     * Find audit logs by action type.
     */
    Page<PushNotificationAuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    /**
     * Find audit logs within a date range.
     */
    @Query("SELECT a FROM PushNotificationAuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<PushNotificationAuditLog> findByDateRange(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);

    /**
     * Find audit logs with filters.
     */
    @Query("SELECT a FROM PushNotificationAuditLog a WHERE " +
            "(:actorId IS NULL OR a.actorId = :actorId) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:targetType IS NULL OR a.targetType = :targetType) AND " +
            "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR a.createdAt <= :endDate) " +
            "ORDER BY a.createdAt DESC")
    Page<PushNotificationAuditLog> findWithFilters(
            @Param("actorId") UUID actorId,
            @Param("action") String action,
            @Param("targetType") String targetType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);

    /**
     * Count actions by actor within a time period.
     * Useful for rate limiting or activity reports.
     */
    @Query("SELECT COUNT(a) FROM PushNotificationAuditLog a WHERE a.actorId = :actorId AND a.createdAt >= :since")
    long countByActorSince(@Param("actorId") UUID actorId, @Param("since") Instant since);

    /**
     * Count failed operations.
     */
    @Query("SELECT COUNT(a) FROM PushNotificationAuditLog a WHERE a.success = false AND a.createdAt >= :since")
    long countFailedSince(@Param("since") Instant since);
}
