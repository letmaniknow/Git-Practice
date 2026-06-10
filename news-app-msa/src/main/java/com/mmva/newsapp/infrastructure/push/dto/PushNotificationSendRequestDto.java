package com.mmva.newsapp.infrastructure.push.dto;

import com.mmva.newsapp.infrastructure.push.enums.PushNotificationPriority;
import com.mmva.newsapp.infrastructure.push.enums.PushNotificationTargetType;
import com.mmva.newsapp.infrastructure.push.enums.PushNotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for sending push notifications (Admin API).
 * 
 * <h3>Target Types:</h3>
 * <ul>
 * <li>ALL: Send to all registered devices</li>
 * <li>TOPIC: Send to topic subscribers (e.g., "breaking_news")</li>
 * <li>DEVICE: Send to specific device IDs</li>
 * <li>USER: Send to specific user IDs</li>
 * <li>SEGMENT: Send to user segment (e.g., "inactive_7_days")</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationSendRequestDto {

    // ========================================
    // Notification Content
    // ========================================

    /**
     * Notification type for categorization.
     */
    @NotNull(message = "Notification type is required")
    private PushNotificationType notificationType;

    /**
     * Notification title (displayed in notification tray).
     * Max 65 chars recommended for iOS.
     */
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    /**
     * Notification body text.
     */
    @NotBlank(message = "Body is required")
    @Size(max = 1000, message = "Body must not exceed 1000 characters")
    private String body;

    /**
     * Image URL for rich notifications.
     * Must be HTTPS.
     */
    @Size(max = 1000, message = "Image URL must not exceed 1000 characters")
    private String imageUrl;

    /**
     * Deep link URL opened when notification is clicked.
     */
    @Size(max = 1000, message = "Click action URL must not exceed 1000 characters")
    private String clickActionUrl;

    // ========================================
    // Targeting
    // ========================================

    /**
     * Target audience type.
     */
    @NotNull(message = "Target type is required")
    private PushNotificationTargetType targetType;

    /**
     * Target values based on targetType:
     * - TOPIC: ["breaking_news", "category_sports"]
     * - DEVICE: ["device-uuid-1", "device-uuid-2"]
     * - USER: ["user-uuid-1", "user-uuid-2"]
     * - SEGMENT: ["inactive_7_days"]
     * - ALL: not required
     */
    private List<String> targetValues;

    // ========================================
    // Scheduling
    // ========================================

    /**
     * When to send the notification.
     * Null for immediate send.
     */
    private Instant scheduledAt;

    /**
     * Timezone for scheduled notifications.
     */
    private String scheduledTimezone;

    /**
     * Time-to-live in seconds.
     * How long FCM should retry if device is offline.
     * Default: 86400 (24 hours)
     */
    @Builder.Default
    private Integer ttlSeconds = 86400;

    // ========================================
    // Behavior
    // ========================================

    /**
     * Notification priority.
     */
    @Builder.Default
    private PushNotificationPriority priority = PushNotificationPriority.NORMAL;

    /**
     * Android notification channel ID.
     */
    private String androidChannelId;

    /**
     * Notification sound.
     */
    private String sound;

    /**
     * Badge count for app icon.
     */
    private Integer badgeCount;

    // ========================================
    // Custom Data
    // ========================================

    /**
     * Custom key-value data passed to app.
     */
    private Map<String, String> data;

    // ========================================
    // Related Entities
    // ========================================

    /**
     * Associated news ID (for news notifications).
     */
    private UUID newsId;

    /**
     * Associated news category ID.
     */
    private UUID categoryId;

    // ========================================
    // Options
    // ========================================

    /**
     * Whether to send a test notification (to creator only).
     */
    @Builder.Default
    private Boolean isTest = false;

    /**
     * Idempotency key to prevent duplicate sends.
     */
    private String idempotencyKey;
}
