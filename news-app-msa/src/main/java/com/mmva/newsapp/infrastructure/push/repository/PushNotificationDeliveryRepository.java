package com.mmva.newsapp.infrastructure.push.repository;

import com.mmva.newsapp.infrastructure.push.enums.PushNotificationDeliveryStatus;
import com.mmva.newsapp.infrastructure.push.model.PushNotificationDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for push notification delivery tracking.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface PushNotificationDeliveryRepository
                extends JpaRepository<PushNotificationDelivery, UUID> {

        // ========================================
        // Find by Notification
        // ========================================

        /**
         * Find all deliveries for a notification.
         */
        List<PushNotificationDelivery> findByNotificationId(UUID notificationId);

        /**
         * Find deliveries for notification with pagination.
         */
        Page<PushNotificationDelivery> findByNotificationId(UUID notificationId, Pageable pageable);

        /**
         * Find deliveries by status for notification.
         */
        List<PushNotificationDelivery> findByNotificationIdAndStatus(UUID notificationId,
                        PushNotificationDeliveryStatus status);

        // ========================================
        // Find by Device
        // ========================================

        /**
         * Find deliveries for a device.
         */
        List<PushNotificationDelivery> findByDeviceId(UUID deviceId);

        /**
         * Find recent deliveries for device.
         */
        Page<PushNotificationDelivery> findByDeviceIdOrderBySentAtDesc(UUID deviceId, Pageable pageable);

        // ========================================
        // Find by Status
        // ========================================

        /**
         * Find pending deliveries for retry.
         */
        @Query("SELECT d FROM PushNotificationDelivery d WHERE d.status = 'FAILED' AND d.retryCount < :maxRetries")
        List<PushNotificationDelivery> findFailedForRetry(@Param("maxRetries") int maxRetries);

        /**
         * Find invalid token deliveries (for device cleanup).
         */
        List<PushNotificationDelivery> findByStatus(PushNotificationDeliveryStatus status);

        // ========================================
        // Find Specific Delivery
        // ========================================

        /**
         * Find delivery for notification and device.
         */
        Optional<PushNotificationDelivery> findByNotificationIdAndDeviceId(UUID notificationId, UUID deviceId);

        /**
         * Find by FCM message ID.
         */
        Optional<PushNotificationDelivery> findByFcmMessageId(String fcmMessageId);

        // ========================================
        // Update Operations
        // ========================================

        /**
         * Update delivery status.
         */
        @Modifying
        @Query("UPDATE PushNotificationDelivery d SET d.status = :status, d.deliveredAt = :time WHERE d.deliveryId = :id")
        void updateStatus(@Param("id") UUID id, @Param("status") PushNotificationDeliveryStatus status,
                        @Param("time") Instant time);

        /**
         * Mark as opened.
         */
        @Modifying
        @Query("UPDATE PushNotificationDelivery d SET d.status = 'OPENED', d.openedAt = :time WHERE d.deliveryId = :id")
        void markAsOpened(@Param("id") UUID id, @Param("time") Instant time);

        /**
         * Update as sent.
         */
        @Modifying
        @Query("UPDATE PushNotificationDelivery d SET d.status = 'SENT', d.sentAt = :time, d.fcmMessageId = :messageId WHERE d.deliveryId = :id")
        void updateAsSent(@Param("id") UUID id, @Param("time") Instant time, @Param("messageId") String messageId);

        /**
         * Update as failed.
         */
        @Modifying
        @Query("UPDATE PushNotificationDelivery d SET d.status = :status, d.errorCode = :code, d.errorMessage = :message, d.retryCount = d.retryCount + 1 WHERE d.deliveryId = :id")
        void updateAsFailed(@Param("id") UUID id, @Param("status") PushNotificationDeliveryStatus status,
                        @Param("code") String code,
                        @Param("message") String message);

        // ========================================
        // Statistics
        // ========================================

        /**
         * Count deliveries by status for notification.
         */
        @Query("SELECT d.status, COUNT(d) FROM PushNotificationDelivery d WHERE d.notificationId = :notificationId GROUP BY d.status")
        List<Object[]> countByStatusForNotification(@Param("notificationId") UUID notificationId);

        /**
         * Count total deliveries.
         */
        long countByNotificationId(UUID notificationId);

        /**
         * Count successful deliveries.
         */
        long countByNotificationIdAndStatus(UUID notificationId, PushNotificationDeliveryStatus status);

        // ========================================
        // Cleanup
        // ========================================

        /**
         * Delete old delivery records (for data retention).
         */
        @Modifying
        @Query("DELETE FROM PushNotificationDelivery d WHERE d.createdAt < :cutoff")
        int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
