package com.mmva.newsapp.infrastructure.push.model;

import com.mmva.newsapp.infrastructure.push.enums.PushNotificationDeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Individual notification delivery record.
 * 
 * <h3>Purpose:</h3>
 * <p>
 * Tracks delivery status for each device when sending notifications
 * to multiple devices. Allows granular tracking and retry for
 * failed individual deliveries.
 * </p>
 * 
 * <h3>Use Cases:</h3>
 * <ul>
 * <li>Track delivery to each device in a batch send</li>
 * <li>Retry failed deliveries individually</li>
 * <li>Analytics on delivery success rate</li>
 * <li>Debug delivery issues for specific devices</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "push_notification_deliveries", indexes = {
        @Index(name = "idx_push_delivery_notification_id", columnList = "notification_id"),
        @Index(name = "idx_push_delivery_device_id", columnList = "device_id"),
        @Index(name = "idx_push_delivery_status", columnList = "status"),
        @Index(name = "idx_push_delivery_sent_at", columnList = "sent_at")
})
public class PushNotificationDelivery {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    // ========================================
    // References
    // ========================================

    /**
     * Parent notification this delivery belongs to.
     */
    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    /**
     * Target device for this delivery.
     */
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    /**
     * FCM token used for delivery.
     * Stored separately as token may change.
     */
    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;

    // ========================================
    // Delivery Status
    // ========================================

    /**
     * Delivery status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PushNotificationDeliveryStatus status = PushNotificationDeliveryStatus.PENDING;

    /**
     * When delivery was attempted.
     */
    @Column(name = "sent_at")
    private Instant sentAt;

    /**
     * When delivery was confirmed by FCM.
     */
    @Column(name = "delivered_at")
    private Instant deliveredAt;

    /**
     * When user opened the notification.
     */
    @Column(name = "opened_at")
    private Instant openedAt;

    /**
     * FCM message ID for this delivery.
     */
    @Column(name = "fcm_message_id", length = 255)
    private String fcmMessageId;

    /**
     * Error code from FCM if failed.
     */
    @Column(name = "error_code", length = 100)
    private String errorCode;

    /**
     * Error message if delivery failed.
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /**
     * Number of retry attempts.
     */
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    // ========================================
    // Timestamps
    // ========================================

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
