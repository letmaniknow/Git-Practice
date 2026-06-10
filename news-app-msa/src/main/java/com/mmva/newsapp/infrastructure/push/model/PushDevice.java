package com.mmva.newsapp.infrastructure.push.model;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import com.mmva.newsapp.infrastructure.push.enums.PushDevicePlatform;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Push notification device registration entity.
 * 
 * <h3>Key Design Principle: Device-Centric (not User-Centric)</h3>
 * <p>
 * The primary entity is the DEVICE, not the user. This allows:
 * </p>
 * <ul>
 * <li>Anonymous users to receive push notifications</li>
 * <li>Device can be optionally linked to a user when logged in</li>
 * <li>Same device can switch between users</li>
 * <li>Same user can have multiple devices</li>
 * </ul>
 * 
 * <h3>Industry Standards Applied:</h3>
 * <ul>
 * <li>FCM token as primary identifier (unique per device)</li>
 * <li>Device fingerprint for tracking across token refreshes</li>
 * <li>Platform-specific metadata for targeted notifications</li>
 * <li>Last active tracking for device health monitoring</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "push_devices", indexes = {
        @Index(name = "idx_push_devices_fcm_token", columnList = "fcm_token", unique = true),
        @Index(name = "idx_push_devices_user_id", columnList = "user_id"),
        @Index(name = "idx_push_devices_device_fingerprint", columnList = "device_fingerprint"),
        @Index(name = "idx_push_devices_platform", columnList = "platform"),
        @Index(name = "idx_push_devices_is_active", columnList = "is_active"),
        @Index(name = "idx_push_devices_deleted_at", columnList = "deleted_at")
})
public class PushDevice extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    // ========================================
    // Device Identification
    // ========================================

    /**
     * Firebase Cloud Messaging registration token.
     * Unique per device installation. Changes when:
     * - App is reinstalled
     * - User clears app data
     * - Token is refreshed by FCM
     */
    @Column(name = "fcm_token", nullable = false, unique = true, length = 512)
    private String fcmToken;

    /**
     * Unique device fingerprint for tracking across token refreshes.
     * Generated client-side using device-specific identifiers.
     * Helps correlate old and new tokens for the same device.
     */
    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;

    // ========================================
    // Platform Information
    // ========================================

    /**
     * Device platform/OS type.
     * Values: ANDROID, IOS, WEB
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private PushDevicePlatform platform;

    /**
     * Application version currently installed.
     * Used for targeted notifications and compatibility checks.
     */
    @Column(name = "app_version", length = 50)
    private String appVersion;

    /**
     * Operating system version.
     * Example: "14.0" for iOS 14, "13" for Android 13
     */
    @Column(name = "os_version", length = 50)
    private String osVersion;

    /**
     * Device model identifier.
     * Example: "iPhone 15 Pro", "Samsung Galaxy S24"
     */
    @Column(name = "device_model", length = 100)
    private String deviceModel;

    /**
     * Device manufacturer.
     * Example: "Apple", "Samsung", "Google"
     */
    @Column(name = "device_manufacturer", length = 100)
    private String deviceManufacturer;

    // ========================================
    // User Association (Optional)
    // ========================================

    /**
     * Associated user ID (nullable for anonymous users).
     * Links device to authenticated user when logged in.
     * Set to null when user logs out.
     */
    @Column(name = "user_id")
    private UUID userId;

    // ========================================
    // Localization
    // ========================================

    /**
     * Preferred language for notifications.
     * ISO 639-1 code (e.g., "en", "es")
     */
    @Column(name = "language", length = 10)
    private String language;

    /**
     * Device timezone for scheduling notifications.
     * IANA timezone ID (e.g., "America/New_York", "Europe/London")
     */
    @Column(name = "timezone", length = 100)
    private String timezone;

    /**
     * Country code from device locale.
     * ISO 3166-1 alpha-2 (e.g., "US", "MX", "ES")
     */
    @Column(name = "country_code", length = 10)
    private String countryCode;

    // ========================================
    // Status & Health
    // ========================================

    /**
     * Whether the device is active and can receive notifications.
     * Set to false when FCM reports token as invalid.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * Last time the device was seen active.
     * Updated when app opens or makes API calls.
     */
    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    /**
     * Number of consecutive failed delivery attempts.
     * Reset to 0 on successful delivery.
     * Device marked inactive after threshold exceeded.
     */
    @Column(name = "failed_delivery_count")
    private Integer failedDeliveryCount;

    /**
     * Last time a notification was successfully delivered.
     */
    @Column(name = "last_notification_at")
    private Instant lastNotificationAt;

    // ========================================
    // Push Notification Settings (Device-Level)
    // ========================================

    /**
     * Whether the user has enabled push notifications on this device.
     * Reflects system-level notification permission.
     */
    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled;

    /**
     * Whether breaking news alerts are enabled.
     */
    @Column(name = "breaking_news_enabled", nullable = false)
    private Boolean breakingNewsEnabled;

    /**
     * Whether daily digest notifications are enabled.
     */
    @Column(name = "daily_digest_enabled", nullable = false)
    private Boolean dailyDigestEnabled;

    /**
     * Whether promotional notifications are enabled.
     */
    @Column(name = "promotional_enabled", nullable = false)
    private Boolean promotionalEnabled;
}
