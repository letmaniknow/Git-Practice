package com.mmva.newsapp.infrastructure.push.repository;

import com.mmva.newsapp.infrastructure.push.enums.PushNotificationStatus;
import com.mmva.newsapp.infrastructure.push.enums.PushNotificationType;
import com.mmva.newsapp.infrastructure.push.model.PushNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for push notification management.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface PushNotificationRepository
                extends JpaRepository<PushNotification, UUID>, JpaSpecificationExecutor<PushNotification> {

        // ========================================
        // Find by Status
        // ========================================

        /**
         * Find notifications by status.
         */
        List<PushNotification> findByStatus(PushNotificationStatus status);

        /**
         * Find pending notifications ready to send.
         */
        @Query("SELECT n FROM PushNotification n WHERE n.status = 'PENDING' AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now) AND n.deletedAt IS NULL ORDER BY n.createdAt ASC")
        List<PushNotification> findPendingNotificationsToSend(@Param("now") Instant now);

        /**
         * Find scheduled notifications ready to send.
         */
        @Query("SELECT n FROM PushNotification n WHERE n.status = 'SCHEDULED' AND n.scheduledAt <= :now AND n.deletedAt IS NULL")
        List<PushNotification> findScheduledNotificationsReady(@Param("now") Instant now);

        /**
         * Find failed notifications for retry.
         */
        @Query("SELECT n FROM PushNotification n WHERE n.status = 'FAILED' AND n.retryCount < :maxRetries AND n.nextRetryAt <= :now AND n.deletedAt IS NULL")
        List<PushNotification> findFailedForRetry(@Param("maxRetries") int maxRetries, @Param("now") Instant now);

        // ========================================
        // Find by Type
        // ========================================

        /**
         * Find notifications by type with pagination.
         */
        Page<PushNotification> findByNotificationType(PushNotificationType type, Pageable pageable);

        // ========================================
        // Find by Related Entities
        // ========================================

        /**
         * Find notifications for a newsapp newsapp.
         */
        List<PushNotification> findByNewsId(UUID newsId);

        /**
         * Find notifications for a newscategory.
         */
        List<PushNotification> findByCategoryId(UUID categoryId);

        // ========================================
        // Idempotency
        // ========================================

        /**
         * Find by idempotency key (prevent duplicates).
         */
        Optional<PushNotification> findByIdempotencyKey(String idempotencyKey);

        /**
         * Check if idempotency key exists.
         */
        boolean existsByIdempotencyKey(String idempotencyKey);

        // ========================================
        // History & Audit
        // ========================================

        /**
         * Find notifications created by user.
         */
        Page<PushNotification> findByCreatedBy(UUID createdBy, Pageable pageable);

        /**
         * Find recent notifications.
         */
        @Query("SELECT n FROM PushNotification n WHERE n.deletedAt IS NULL ORDER BY n.createdAt DESC")
        Page<PushNotification> findRecent(Pageable pageable);

        /**
         * Find notifications in date range.
         */
        @Query("SELECT n FROM PushNotification n WHERE n.createdAt >= :start AND n.createdAt <= :end AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
        Page<PushNotification> findByDateRange(@Param("start") Instant start, @Param("end") Instant end,
                        Pageable pageable);

        // ========================================
        // Update Operations
        // ========================================

        /**
         * Update notification status.
         */
        @Modifying
        @Query("UPDATE PushNotification n SET n.status = :status, n.sentAt = :sentAt WHERE n.notificationId = :id")
        void updateStatus(@Param("id") UUID id, @Param("status") PushNotificationStatus status,
                        @Param("sentAt") Instant sentAt);

        /**
         * Update delivery counts.
         */
        @Modifying
        @Query("UPDATE PushNotification n SET n.sentCount = :sent, n.deliveredCount = :delivered, n.failedCount = :failed WHERE n.notificationId = :id")
        void updateDeliveryCounts(@Param("id") UUID id, @Param("sent") int sent, @Param("delivered") int delivered,
                        @Param("failed") int failed);

        /**
         * Increment open count.
         */
        @Modifying
        @Query("UPDATE PushNotification n SET n.openedCount = n.openedCount + 1 WHERE n.notificationId = :id")
        void incrementOpenCount(@Param("id") UUID id);

        /**
         * Set retry info for failed notification.
         */
        @Modifying
        @Query("UPDATE PushNotification n SET n.retryCount = n.retryCount + 1, n.nextRetryAt = :nextRetry, n.errorMessage = :error WHERE n.notificationId = :id")
        void setRetryInfo(@Param("id") UUID id, @Param("nextRetry") Instant nextRetry, @Param("error") String error);

        // ========================================
        // Statistics
        // ========================================

        /**
         * Count notifications by status.
         */
        @Query("SELECT n.status, COUNT(n) FROM PushNotification n WHERE n.deletedAt IS NULL GROUP BY n.status")
        List<Object[]> countByStatus();

        /**
         * Count notifications by type.
         */
        @Query("SELECT n.notificationType, COUNT(n) FROM PushNotification n WHERE n.deletedAt IS NULL GROUP BY n.notificationType")
        List<Object[]> countByType();

        /**
         * Get total delivery stats for period.
         */
        @Query("SELECT SUM(n.sentCount), SUM(n.deliveredCount), SUM(n.failedCount), SUM(n.openedCount) FROM PushNotification n WHERE n.sentAt >= :start AND n.sentAt <= :end AND n.deletedAt IS NULL")
        Object[] getDeliveryStatsForPeriod(@Param("start") Instant start, @Param("end") Instant end);
}
