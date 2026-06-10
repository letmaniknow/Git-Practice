package com.mmva.newsapp.infrastructure.clientcontext.core.dto;

import com.mmva.newsapp.infrastructure.clientcontext.core.enums.Channel;
import com.mmva.newsapp.infrastructure.clientcontext.core.enums.DeviceType;

import java.time.Instant;
import java.util.Map;

/**
 * Comprehensive client context record capturing all relevant information
 * about a client request for analytics, security, and personalization.
 * 
 * <p>
 * This is the core DTO for client information across the application.
 * It contains both raw (extracted from request) and enriched (derived/parsed)
 * data.
 * </p>
 * 
 * <h3>Usage Scenarios:</h3>
 * <ul>
 * <li><b>User Registration:</b> Capture initial context for profile</li>
 * <li><b>Login:</b> Track device for security/anomaly detection</li>
 * <li><b>API Requests:</b> Analytics and rate limiting</li>
 * <li><b>Content Delivery:</b> Personalization and optimization</li>
 * </ul>
 * 
 * <h3>Data Categories (45 fields):</h3>
 * <ul>
 * <li>Network Information (6) - IP, proxy chain</li>
 * <li>Device Information (12) - Platform, browser, device type</li>
 * <li>Location Information (7) - GeoIP derived</li>
 * <li>Request Context (8) - Headers, language, referrer</li>
 * <li>Security Context (6) - Bot detection, risk assessment</li>
 * <li>Channel & Attribution (6) - Marketing, app store</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public record ClientContextDto(
        // ========================================
        // Network Information (6 fields)
        // ========================================

        /** Client IP address (resolved through proxies) */
        String ipAddress,

        /** Original IP before any NAT/proxy (for enterprise networks) */
        String originalIpAddress,

        /** Full X-Forwarded-For chain for audit */
        String forwardedForChain,

        /** Whether request came through known proxy/CDN */
        Boolean isBehindProxy,

        /** CDN/Proxy provider if detected (Cloudflare, AWS, etc.) */
        String proxyProvider,

        /** Connection type: direct, proxy, vpn, tor */
        String connectionType,

        // ========================================
        // Device Information (12 fields)
        // ========================================

        /** Raw User-Agent string (preserved for fingerprinting) */
        String userAgent,

        /** Device type: MOBILE, TABLET, DESKTOP, TV, WEARABLE, BOT, UNKNOWN */
        DeviceType deviceType,

        /** Operating system name: Windows, macOS, iOS, Android, Linux */
        String osName,

        /** Operating system version */
        String osVersion,

        /** Browser name: Chrome, Firefox, Safari, Edge, Opera */
        String browserName,

        /** Browser version */
        String browserVersion,

        /** Device brand: Apple, Samsung, Google, etc. */
        String deviceBrand,

        /** Device model: iPhone 15, Galaxy S24, etc. */
        String deviceModel,

        /** Device memory in GB (from Client Hints) */
        Double deviceMemory,

        /** Screen resolution if available */
        String screenResolution,

        /** Device pixel ratio */
        Double devicePixelRatio,

        /** Unique device fingerprint (hashed) */
        String deviceFingerprint,

        // ========================================
        // Location Information (7 fields)
        // ========================================

        /** ISO 3166-1 alpha-2 country code (from GeoIP) */
        String countryCode,

        /** Country name */
        String countryName,

        /** Region/State/Province code */
        String regionCode,

        /** Region/State/Province name */
        String regionName,

        /** City name */
        String city,

        /** Postal/ZIP code */
        String postalCode,

        /** IANA timezone (e.g., America/New_York) */
        String timezone,

        // ========================================
        // Request Context (8 fields)
        // ========================================

        /** Accept-Language header - user's preferred languages */
        String acceptLanguage,

        /** Primary language extracted (e.g., 'en' from 'en-US,en;q=0.9') */
        String primaryLanguage,

        /** Referer URL - where user came from */
        String referer,

        /** Referer domain only (for analytics) */
        String refererDomain,

        /** Origin header (for CORS) */
        String origin,

        /** Request timestamp */
        Instant requestTime,

        /** Effective connection type: slow-2g, 2g, 3g, 4g */
        String effectiveConnectionType,

        /** Save-Data preference (bandwidth saving) */
        Boolean saveDataPreference,

        // ========================================
        // Security Context (6 fields)
        // ========================================

        /** Whether request appears to be from a bot */
        Boolean isBot,

        /**
         * Bot type if detected: SEARCH_ENGINE, SOCIAL, MONITORING, MALICIOUS, UNKNOWN
         */
        String botType,

        /** Bot name if identified: Googlebot, Bingbot, etc. */
        String botName,

        /** Whether using TOR/VPN (suspicious for some operations) */
        Boolean isAnonymized,

        /** Risk score 0-100 (for fraud prevention) */
        Integer riskScore,

        /** Risk factors if any detected */
        String riskFactors,

        // ========================================
        // Channel & Attribution (6 fields)
        // ========================================

        /** Access channel: WEB, MOBILE_WEB, IOS_APP, ANDROID_APP, API, UNKNOWN */
        Channel channel,

        /** App version if from mobile app */
        String appVersion,

        /** App build number */
        String appBuild,

        /** UTM source for marketing attribution */
        String utmSource,

        /** UTM medium */
        String utmMedium,

        /** UTM campaign */
        String utmCampaign) {

    // ========================================
    // Factory Methods
    // ========================================

    /**
     * Creates a minimal context with only essential fields.
     * Use when full context extraction is not needed.
     */
    public static ClientContextDto minimal(String ipAddress, String userAgent) {
        return new ClientContextDto(
                // Network
                ipAddress, null, null, false, null, "direct",
                // Device
                userAgent, DeviceType.UNKNOWN, null, null, null, null, null, null, null, null, null, null,
                // Location
                null, null, null, null, null, null, null,
                // Request
                null, null, null, null, null, Instant.now(), null, null,
                // Security
                false, null, null, false, 0, null,
                // Channel
                Channel.UNKNOWN, null, null, null, null, null);
    }

    /**
     * Creates an empty context for error/fallback cases.
     */
    public static ClientContextDto empty() {
        return minimal("unknown", "unknown");
    }

    // ========================================
    // Convenience Methods
    // ========================================

    /**
     * Checks if this is a mobile device (phone or tablet).
     */
    public boolean isMobileDevice() {
        return deviceType == DeviceType.MOBILE || deviceType == DeviceType.TABLET;
    }

    /**
     * Checks if this appears to be a legitimate human user.
     */
    public boolean isHumanUser() {
        return !Boolean.TRUE.equals(isBot) && deviceType != DeviceType.BOT;
    }

    /**
     * Checks if location information is available.
     */
    public boolean hasLocation() {
        return countryCode != null && !countryCode.isEmpty();
    }

    /**
     * Gets a safe display string for the device.
     */
    public String getDeviceDisplayName() {
        if (deviceBrand != null && deviceModel != null) {
            return deviceBrand + " " + deviceModel;
        }
        if (osName != null && browserName != null) {
            return browserName + " on " + osName;
        }
        return deviceType != null ? deviceType.name() : "Unknown Device";
    }

    /**
     * Checks if this is a high-risk request.
     */
    public boolean isHighRisk() {
        return riskScore != null && riskScore >= 70;
    }

    /**
     * Gets summary for logging (no sensitive data).
     */
    public String toLogSummary() {
        return String.format(
                "[%s] %s | %s %s | %s/%s | risk=%d",
                channel,
                ipAddress,
                deviceType,
                osName,
                countryCode,
                city,
                riskScore != null ? riskScore : 0);
    }

    /**
     * Creates a map of fields suitable for storing in JSON column.
     */
    public Map<String, Object> toStorageMap() {
        return Map.ofEntries(
                Map.entry("ipAddress", ipAddress != null ? ipAddress : ""),
                Map.entry("deviceType", deviceType != null ? deviceType.name() : "UNKNOWN"),
                Map.entry("osName", osName != null ? osName : ""),
                Map.entry("browserName", browserName != null ? browserName : ""),
                Map.entry("countryCode", countryCode != null ? countryCode : ""),
                Map.entry("city", city != null ? city : ""),
                Map.entry("channel", channel != null ? channel.name() : "UNKNOWN"),
                Map.entry("isBot", Boolean.TRUE.equals(isBot)),
                Map.entry("riskScore", riskScore != null ? riskScore : 0));
    }
}
