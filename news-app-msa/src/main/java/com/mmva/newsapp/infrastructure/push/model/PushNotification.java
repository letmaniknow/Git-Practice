package com.mmva.newsapp.infrastructure.push.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.infrastructure.push.enums.PushNotificationPriority;
import com.mmva.newsapp.infrastructure.push.enums.PushNotificationStatus;
import com.mmva.newsapp.infrastructure.push.enums.PushNotificationTargetType;
import com.mmva.newsapp.infrastructure.push.enums.PushNotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Push notification log entity.
 * 
 * <h3>Purpose:</h3>
 * <ul>
 * <li>Track all notifications sent from the system</li>
 * <li>Audit trail for compliance and debugging</li>
 * <li>Analytics for notification effectiveness</li>
 * <li>Retry queue for failed notifications</li>
 * </ul>
 * 
 * <h3>Industry Standards:</h3>
 * <ul>
 * <li>Idempotency key to prevent duplicate sends</li>
 * <li>Status tracking for delivery confirmation</li>
 * <li>Payload stored for debugging</li>
 * <li>Metrics for open/click tracking</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "push_notifications", indexes = {
        @Index(name = "idx_push_notifications_status", columnList = "status"),
        @Index(name = "idx_push_notifications_type", columnList = "notification_type"),
        @Index(name = "idx_push_notifications_target_type", columnList = "target_type"),
        @Index(name = "idx_push_notifications_news_id", columnList = "news_id"),
        @Index(name = "idx_push_notifications_scheduled_at", columnList = "scheduled_at"),
        @Index(name = "idx_push_notifications_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_push_notifications_deleted_at", columnList = "deleted_at")
})
public class PushNotification extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    // ========================================
    // Idempotency
    // ========================================

    /**
     * Unique key to prevent duplicate notifications.
     * Format: {type}_{targetType}_{targetValue}_{timestamp}
     * Example: "BREAKING_NEWS_TOPIC_breaking_news_2024010112"
     */
    @Column(name = "idempotency_key", unique = true, length = 255)
    private String idempotencyKey;

    // ========================================
    // Notification Content
    // ========================================

    /**
     * Notification type for categorization.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private PushNotificationType notificationType;

    /**
     * Notification title (shown in system tray).
     * Max 65 chars for iOS compatibility.
     */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * Notification body text.
     * Max 240 chars for best cross-infrastructure display.
     */
    @Column(name = "body", nullable = false, length = 1000)
    private String body;

    /**
     * Image URL for rich notifications.
     * Must be HTTPS for FCM.
     */
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    /**
     * Deep link URL for click action.
     * Opens specific screen/content in app.
     */
    @Column(name = "click_action_url", length = 1000)
    private String clickActionUrl;

    /**
     * Custom data payload (JSON).
     * Contains additional data for app processing.
     */
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    // ========================================
    // Targeting
    // ========================================

    /**
     * Target audience type.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private PushNotificationTargetType targetType;

    /**
     * Target value based on targetType.
     * - TOPIC: topic name (e.g., "breaking_news", "category_sports")
     * - DEVICE: device ID
     * - USER: user ID
     * - ALL: null or "all"
     * - SEGMENT: segment identifier
     */
    @Column(name = "target_value", length = 255)
    private String targetValue;

    // ========================================
    // Scheduling
    // ========================================

    /**
     * When the notification should be sent.
     * Null for immediate send.
     */
    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    /**
     * Timezone for scheduled notifications.
     * Used for "send at 9 AM local time" scenarios.
     */
    @Column(name = "scheduled_timezone", length = 100)
    private String scheduledTimezone;

    /**
     * TTL (Time-To-Live) in seconds.
     * How long FCM should try to deliver if device is offline.
     * Default: 86400 (24 hours), Max: 2419200 (28 days)
     */
    @Column(name = "ttl_seconds")
    @Builder.Default
    private Integer ttlSeconds = 86400;

    // ========================================
    // Status & Delivery
    // ========================================

    /**
     * Current notification status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PushNotificationStatus status = PushNotificationStatus.PENDING;

    /**
     * When the notification was sent to FCM.
     */
    @Column(name = "sent_at")
    private Instant sentAt;

    /**
     * FCM message ID returned after successful send.
     */
    @Column(name = "fcm_message_id", length = 255)
    private String fcmMessageId;

    /**
     * Error message if delivery failed.
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * Number of retry attempts.
     */
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * Next retry time (for exponential backoff).
     */
    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    // ========================================
    // Analytics
    // ========================================

    /**
     * Number of devices notification was sent to.
     */
    @Column(name = "sent_count")
    @Builder.Default
    private Integer sentCount = 0;

    /**
     * Number of successful deliveries (FCM acknowledged).
     */
    @Column(name = "delivered_count")
    @Builder.Default
    private Integer deliveredCount = 0;

    /**
     * Number of failures.
     */
    @Column(name = "failed_count")
    @Builder.Default
    private Integer failedCount = 0;

    /**
     * Number of opens (user clicked notification).
     * Tracked via app analytics callback.
     */
    @Column(name = "opened_count")
    @Builder.Default
    private Integer openedCount = 0;

    // ========================================
    // Related Entities
    // ========================================

    /**
     * Associated news ID (for news-related notifications).
     */
    @Column(name = "news_id")
    private UUID newsId;

    /**
     * Associated category ID (for category updates).
     */
    @Column(name = "category_id")
    private UUID categoryId;

    // ========================================
    // Priority & Behavior
    // ========================================

    /**
     * Notification priority.
     * HIGH: Immediately visible, may wake device
     * NORMAL: Delivered when convenient
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    @Builder.Default
    private PushNotificationPriority priority = PushNotificationPriority.NORMAL;

    /**
     * Android notification channel ID.
     * Allows users to customize notification behavior per category.
     */
    @Column(name = "android_channel_id", length = 100)
    private String androidChannelId;

    /**
     * iOS notification sound name.
     */
    @Column(name = "sound", length = 100)
    private String sound;

    /**
     * Badge count to display on app icon.
     */
    @Column(name = "badge_count")
    private Integer badgeCount;
}
