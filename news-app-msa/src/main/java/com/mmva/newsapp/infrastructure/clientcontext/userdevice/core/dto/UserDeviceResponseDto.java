package com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for user device response in "Manage My Devices" feature.
 * 
 * <p>
 * Following industry standards (Google, Apple, Microsoft):
 * </p>
 * <ul>
 * <li>Shows device identification info</li>
 * <li>Displays trust/security status</li>
 * <li>Includes last activity information</li>
 * <li>Provides location context for security</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceResponseDto {

    // ========================================
    // Device Identification
    // ========================================

    /** Unique device ID */
    private UUID deviceId;

    /** User-friendly display name */
    private String displayName;

    /** Custom device name set by user */
    private String deviceName;

    /** Device type: MOBILE, TABLET, DESKTOP */
    private String deviceType;

    /** Access channel: WEB, IOS_APP, ANDROID_APP */
    private String channel;

    // ========================================
    // Device Details
    // ========================================

    /** Operating system (e.g., "iOS 17", "Windows 11") */
    private String operatingSystem;

    /** Browser name (for web) */
    private String browserName;

    /** Device brand (e.g., "Apple", "Samsung") */
    private String deviceBrand;

    /** Device model (e.g., "iPhone 15 Pro") */
    private String deviceModel;

    /** App version (for mobile apps) */
    private String appVersion;

    // ========================================
    // Location & Activity
    // ========================================

    /** Last known country */
    private String lastCountryCode;

    /** Last known city */
    private String lastCity;

    /** Masked IP address for security display */
    private String lastIpAddress;

    /** When device was first seen */
    private Instant firstSeenAt;

    /** When device was last active */
    private Instant lastUsedAt;

    /** Total number of logins from this device */
    private Integer loginCount;

    // ========================================
    // Trust & Security Status
    // ========================================

    /** Whether device is trusted by user */
    private Boolean isTrusted;

    /** Trust level 0-100 */
    private Integer trustLevel;

    /** Whether device is blocked */
    private Boolean isBlocked;

    /** Reason for blocking (if blocked) */
    private String blockedReason;

    /** When device was blocked */
    private Instant blockedAt;

    /** Whether this is the current session device */
    private Boolean isCurrentDevice;

    /** Risk score 0-100 */
    private Integer riskScore;

    /** Whether device requires re-verification */
    private Boolean requiresVerification;
}
