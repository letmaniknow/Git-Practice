/**
 * Push notification feature enumerations.
 * 
 * <p>
 * Contains all enums used by the push notification module.
 * All enums follow PROJECT_PRINCIPLES.md §6.1 Feature-Contextual Naming
 * with entity-specific prefixes for clarity.
 * </p>
 * 
 * <h2>Enums:</h2>
 * <ul>
 * <li>{@link com.mmva.newsapp.infrastructure.push.enums.PushDevicePlatform}
 * - Device infrastructure (ANDROID, IOS, WEB)</li>
 * <li>{@link com.mmva.newsapp.infrastructure.push.enums.PushNotificationType}
 * - Notification category (BREAKING_NEWS, NEWS_UPDATE, etc.)</li>
 * <li>{@link com.mmva.newsapp.infrastructure.push.enums.PushNotificationStatus}
 * - Delivery lifecycle (PENDING, SENT, DELIVERED, etc.)</li>
 * <li>{@link com.mmva.newsapp.infrastructure.push.enums.PushNotificationPriority}
 * - FCM priority (HIGH, NORMAL)</li>
 * <li>{@link com.mmva.newsapp.infrastructure.push.enums.PushNotificationTargetType}
 * - Target selection (ALL, TOPIC, DEVICE, USER, SEGMENT)</li>
 * <li>{@link com.mmva.newsapp.infrastructure.push.enums.PushNotificationDeliveryStatus}
 * - Per-device delivery status</li>
 * <li>{@link com.mmva.newsapp.infrastructure.push.enums.PushTopicCategory}
 * - Topic grouping (GLOBAL, CATEGORY, LANGUAGE, etc.)</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
package com.mmva.newsapp.infrastructure.push.enums;
