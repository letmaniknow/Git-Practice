package com.mmva.newsapp.infrastructure.push.enums;

/**
 * Enum representing the lifecycle status of a push notification.
 * 
 * <p>
 * Tracks the notification through its complete delivery lifecycle
 * from pending to final state (delivered, failed, or cancelled).
 * </p>
 * 
 * <h3>Status Flow:</h3>
 * 
 * <pre>
 * PENDING ──► PROCESSING ──► SENT ──► DELIVERED
 *    │             │           │
 *    ▼             ▼           ▼
 * SCHEDULED     FAILED    PARTIALLY_SENT
 *    │                         │
 *    ▼                         ▼
 * CANCELLED                  FAILED
 *    │
 *    ▼
 * EXPIRED
 * </pre>
 * 
 * <h3>Naming Convention:</h3>
 * <p>
 * Prefixed with {@code PushNotification} per PROJECT_PRINCIPLES.md §6.1
 * Feature-Contextual Naming to clearly indicate entity ownership.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum PushNotificationStatus {

    /**
     * Waiting to be sent.
     */
    PENDING,

    /**
     * Scheduled for future delivery.
     */
    SCHEDULED,

    /**
     * Currently being sent to FCM.
     */
    PROCESSING,

    /**
     * Successfully sent to FCM.
     */
    SENT,

    /**
     * FCM confirmed delivery to device.
     */
    DELIVERED,

    /**
     * Some devices succeeded, some failed.
     */
    PARTIALLY_SENT,

    /**
     * All delivery attempts failed.
     */
    FAILED,

    /**
     * Manually cancelled before sending.
     */
    CANCELLED,

    /**
     * TTL expired before delivery.
     */
    EXPIRED
}
