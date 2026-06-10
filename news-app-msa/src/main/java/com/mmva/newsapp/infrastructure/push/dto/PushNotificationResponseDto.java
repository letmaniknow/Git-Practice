package com.mmva.newsapp.infrastructure.push.dto;

import com.mmva.newsapp.infrastructure.push.enums.PushNotificationPriority;
import com.mmva.newsapp.infrastructure.push.enums.PushNotificationStatus;
import com.mmva.newsapp.infrastructure.push.enums.PushNotificationTargetType;
import com.mmva.newsapp.infrastructure.push.enums.PushNotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for notification operations.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationResponseDto {

    /**
     * Unique notification ID.
     */
    private UUID notificationId;

    /**
     * Notification type.
     */
    private PushNotificationType notificationType;

    /**
     * Notification title.
     */
    private String title;

    /**
     * Notification body.
     */
    private String body;

    /**
     * Target type.
     */
    private PushNotificationTargetType targetType;

    /**
     * Target value.
     */
    private String targetValue;

    /**
     * Current status.
     */
    private PushNotificationStatus status;

    /**
     * Notification priority.
     */
    private PushNotificationPriority priority;

    /**
     * Scheduled send time (null if immediate).
     */
    private Instant scheduledAt;

    /**
     * When notification was sent.
     */
    private Instant sentAt;

    /**
     * Delivery statistics.
     */
    private DeliveryStatsDto stats;

    /**
     * Created timestamp.
     */
    private Instant createdAt;

    /**
     * Created by user.
     */
    private UUID createdBy;

    /**
     * Error message if failed.
     */
    private String errorMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryStatsDto {
        private Integer sentCount;
        private Integer deliveredCount;
        private Integer failedCount;
        private Integer openedCount;
        private Double deliveryRate;
        private Double openRate;
    }
}
