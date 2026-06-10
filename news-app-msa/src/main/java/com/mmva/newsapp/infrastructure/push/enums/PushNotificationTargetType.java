package com.mmva.newsapp.infrastructure.push.enums;

/**
 * Enum representing the target type for a push notification.
 * 
 * <p>
 * Defines how to select recipients for a notification:
 * all devices, topic subscribers, specific devices, etc.
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
public enum PushNotificationTargetType {

    /**
     * Send to all registered devices.
     */
    ALL,

    /**
     * Send to FCM topic subscribers.
     * Target value is the topic name.
     */
    TOPIC,

    /**
     * Send to specific device(s) by ID.
     * Target value is device ID(s).
     */
    DEVICE,

    /**
     * Send to specific user(s) by ID.
     * Target value is user ID(s).
     */
    USER,

    /**
     * Send to a user segment.
     * Target value is segment name (e.g., "inactive_7_days").
     */
    SEGMENT
}
