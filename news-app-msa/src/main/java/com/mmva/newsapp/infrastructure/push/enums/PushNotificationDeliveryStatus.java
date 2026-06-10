package com.mmva.newsapp.infrastructure.push.enums;

/**
 * Enum representing the delivery status of an individual notification delivery.
 * 
 * <p>
 * Tracks delivery to a specific device. Different from notification status
 * which tracks the overall notification; this tracks per-device delivery.
 * </p>
 * 
 * <h3>Naming Convention:</h3>
 * <p>
 * Prefixed with {@code PushNotificationDelivery} per PROJECT_PRINCIPLES.md §6.1
 * Feature-Contextual Naming to clearly indicate entity ownership.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum PushNotificationDeliveryStatus {

    /**
     * Not yet sent to FCM.
     */
    PENDING,

    /**
     * Sent to FCM, awaiting confirmation.
     */
    SENT,

    /**
     * FCM confirmed delivery to device.
     */
    DELIVERED,

    /**
     * User clicked/opened the notification.
     */
    OPENED,

    /**
     * Delivery failed (network error, FCM error).
     */
    FAILED,

    /**
     * FCM token is invalid (app uninstalled).
     */
    INVALID_TOKEN,

    /**
     * Device has opted out of notifications.
     */
    UNSUBSCRIBED
}
