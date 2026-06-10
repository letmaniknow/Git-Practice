package com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.model;

import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.core.enums.Channel;
import com.mmva.newsapp.infrastructure.clientcontext.core.enums.DeviceType;
import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a user's device for multi-device tracking and security.
 * 
 * <p>
 * This entity enables:
 * </p>
 * <ul>
 * <li>Multi-device session management</li>
 * <li>Device trust scoring for security</li>
 * <li>Anomaly detection (login from new device/location)</li>
 * <li>"Manage your devices" user feature</li>
 * <li>Push notification targeting per device</li>
 * </ul>
 * 
 * <h3>Industry Standard Implementation:</h3>
 * <ul>
 * <li>Device fingerprinting for unique identification</li>
 * <li>Location tracking for security alerts</li>
 * <li>Trust level progression over time</li>
 * <li>Automatic expiration of unused devices</li>
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
@Table(name = "user_devices", indexes = {
        @Index(name = "idx_user_devices_user_id", columnList = "user_id"),
        @Index(name = "idx_user_devices_fingerprint", columnList = "device_fingerprint"),
        @Index(name = "idx_user_devices_last_used", columnList = "last_used_at"),
        @Index(name = "idx_user_devices_trusted", columnList = "user_id, is_trusted")
})
public class UserDevice extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "device_id", nullable = false, updatable = false)
    private UUID deviceId;

    // ========================================
    // Foreign Key - User
    // ========================================

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // ========================================
    // Device Identification
    // ========================================

    /** Unique fingerprint hash for this device */
    @Column(name = "device_fingerprint", length = 64, nullable = false)
    private String deviceFingerprint;

    /** User-friendly device name (e.g., "John's iPhone") */
    @Column(name = "device_name", length = 100)
    private String deviceName;

    /** Device type: MOBILE, TABLET, DESKTOP, etc. */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType;

    /** Access channel: WEB, MOBILE_APP, etc. */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20)
    private Channel channel;

    // ========================================
    // Device Details
    // ========================================

    /** Operating system name */
    @Column(name = "os_name", length = 50)
    private String osName;

    /** Operating system version */
    @Column(name = "os_version", length = 30)
    private String osVersion;

    /** Browser name */
    @Column(name = "browser_name", length = 50)
    private String browserName;

    /** Browser version */
    @Column(name = "browser_version", length = 30)
    private String browserVersion;

    /** Device brand (Apple, Samsung, etc.) */
    @Column(name = "device_brand", length = 50)
    private String deviceBrand;

    /** Device model (iPhone 15, Galaxy S24, etc.) */
    @Column(name = "device_model", length = 100)
    private String deviceModel;

    /** Full User-Agent string (for reference) */
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    // ========================================
    // App Information (for mobile apps)
    // ========================================

    /** App version if from mobile app */
    @Column(name = "app_version", length = 30)
    private String appVersion;

    /** App build number */
    @Column(name = "app_build", length = 30)
    private String appBuild;

    /** Push notification token */
    @Column(name = "push_token", length = 512)
    private String pushToken;

    /** Push token type: FCM, APNS, etc. */
    @Column(name = "push_token_type", length = 20)
    private String pushTokenType;

    // ========================================
    // Location Information
    // ========================================

    /** Last known country code */
    @Column(name = "last_country_code", length = 3)
    private String lastCountryCode;

    /** Last known city */
    @Column(name = "last_city", length = 100)
    private String lastCity;

    /** Last known timezone */
    @Column(name = "last_timezone", length = 50)
    private String lastTimezone;

    /** Last known IP address */
    @Column(name = "last_ip_address", length = 45)
    private String lastIpAddress;

    // ========================================
    // Trust & Security
    // ========================================

    /** Whether this device is trusted by user */
    @Builder.Default
    @Column(name = "is_trusted")
    private Boolean isTrusted = false;

    /** Trust level: 0 (new) to 100 (fully trusted) */
    @Builder.Default
    @Column(name = "trust_level")
    private Integer trustLevel = 0;

    /** Whether this device is blocked */
    @Builder.Default
    @Column(name = "is_blocked")
    private Boolean isBlocked = false;

    /** Reason for blocking */
    @Column(name = "blocked_reason", length = 255)
    private String blockedReason;

    /** When device was blocked */
    @Column(name = "blocked_at")
    private Instant blockedAt;

    /** Risk score for this device */
    @Builder.Default
    @Column(name = "risk_score")
    private Integer riskScore = 0;

    /** Detected risk factors */
    @Column(name = "risk_factors", length = 512)
    private String riskFactors;

    // ========================================
    // Activity Tracking
    // ========================================

    /** When device was first seen */
    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    /** When device was last used */
    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    /** Total number of logins from this device */
    @Builder.Default
    @Column(name = "login_count")
    private Integer loginCount = 0;

    /** Number of failed login attempts */
    @Builder.Default
    @Column(name = "failed_login_count")
    private Integer failedLoginCount = 0;

    /** Whether device requires re-verification */
    @Builder.Default
    @Column(name = "requires_verification")
    private Boolean requiresVerification = false;

    // ========================================
    // Lifecycle Methods
    // ========================================

    @PrePersist
    protected void onPrePersist() {
        if (firstSeenAt == null) {
            firstSeenAt = Instant.now();
        }
        if (lastUsedAt == null) {
            lastUsedAt = Instant.now();
        }
    }

    // ========================================
    // Convenience Methods
    // ========================================

    /**
     * Updates the device activity timestamp.
     */
    public void recordActivity() {
        this.lastUsedAt = Instant.now();
        this.loginCount = (this.loginCount != null ? this.loginCount : 0) + 1;
    }

    /**
     * Records a failed login attempt.
     */
    public void recordFailedLogin() {
        this.failedLoginCount = (this.failedLoginCount != null ? this.failedLoginCount : 0) + 1;
        // Auto-block after too many failures
        if (this.failedLoginCount >= 10) {
            this.isBlocked = true;
            this.blockedReason = "Too many failed login attempts";
            this.blockedAt = Instant.now();
        }
    }

    /**
     * Increases trust level based on successful usage.
     */
    public void increaseTrust(int amount) {
        this.trustLevel = Math.min(100, (this.trustLevel != null ? this.trustLevel : 0) + amount);
    }

    /**
     * Checks if this device should be considered active.
     * 
     * @param inactiveDays number of days of inactivity threshold
     * @return true if device is still active
     */
    public boolean isActive(int inactiveDays) {
        if (lastUsedAt == null) {
            return false;
        }
        Instant threshold = Instant.now().minusSeconds(inactiveDays * 24L * 60 * 60);
        return lastUsedAt.isAfter(threshold);
    }

    /**
     * Gets a user-friendly display name for this device.
     */
    public String getDisplayName() {
        if (deviceName != null && !deviceName.isEmpty()) {
            return deviceName;
        }
        StringBuilder sb = new StringBuilder();
        if (deviceBrand != null) {
            sb.append(deviceBrand).append(" ");
        }
        if (deviceModel != null) {
            sb.append(deviceModel);
        } else if (browserName != null && osName != null) {
            sb.append(browserName).append(" on ").append(osName);
        } else if (deviceType != null) {
            sb.append(deviceType.name());
        } else {
            sb.append("Unknown Device");
        }
        return sb.toString().trim();
    }

    /**
     * Creates a new UserDevice from ClientContextDto.
     */
    public static UserDevice fromContext(UUID userId, ClientContextDto context) {
        return UserDevice.builder()
                .userId(userId)
                .deviceFingerprint(context.deviceFingerprint())
                .deviceType(context.deviceType())
                .channel(context.channel())
                .osName(context.osName())
                .osVersion(context.osVersion())
                .browserName(context.browserName())
                .browserVersion(context.browserVersion())
                .deviceBrand(context.deviceBrand())
                .deviceModel(context.deviceModel())
                .userAgent(truncate(context.userAgent(), 512))
                .appVersion(context.appVersion())
                .appBuild(context.appBuild())
                .lastCountryCode(context.countryCode())
                .lastCity(context.city())
                .lastTimezone(context.timezone())
                .lastIpAddress(context.ipAddress())
                .riskScore(context.riskScore())
                .riskFactors(context.riskFactors())
                .firstSeenAt(Instant.now())
                .lastUsedAt(Instant.now())
                .loginCount(1)
                .trustLevel(0)
                .isTrusted(false)
                .isBlocked(false)
                .build();
    }

    /**
     * Updates this device with new context information.
     */
    public void updateFromContext(ClientContextDto context) {
        // Update location
        if (context.countryCode() != null) {
            this.lastCountryCode = context.countryCode();
        }
        if (context.city() != null) {
            this.lastCity = context.city();
        }
        if (context.timezone() != null) {
            this.lastTimezone = context.timezone();
        }
        if (context.ipAddress() != null) {
            this.lastIpAddress = context.ipAddress();
        }

        // Update app info
        if (context.appVersion() != null) {
            this.appVersion = context.appVersion();
        }
        if (context.appBuild() != null) {
            this.appBuild = context.appBuild();
        }

        // Update browser/OS versions (they can change with updates)
        if (context.browserVersion() != null) {
            this.browserVersion = context.browserVersion();
        }
        if (context.osVersion() != null) {
            this.osVersion = context.osVersion();
        }

        // Record activity
        recordActivity();

        // Update trust
        increaseTrust(1);
    }

    @SuppressWarnings("unused") // Used by fromContext factory method
    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
