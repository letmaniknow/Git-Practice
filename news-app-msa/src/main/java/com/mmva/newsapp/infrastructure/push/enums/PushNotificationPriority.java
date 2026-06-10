package com.mmva.newsapp.infrastructure.push.enums;

/**
 * Enum representing the priority level of a push notification.
 * 
 * <p>
 * Controls how FCM delivers the notification to devices.
 * High priority wakes the device immediately; normal priority
 * batches for better battery life.
 * </p>
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
public enum PushNotificationPriority {

    /**
     * High priority - wake device, show immediately.
     * Use for breaking news and time-sensitive content.
     * May affect battery life.
     */
    HIGH,

    /**
     * Normal priority - delivered when convenient.
     * May be batched with other notifications for battery optimization.
     */
    NORMAL
}
