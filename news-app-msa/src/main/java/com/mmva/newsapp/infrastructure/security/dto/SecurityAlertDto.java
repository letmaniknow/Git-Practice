package com.mmva.newsapp.infrastructure.security.dto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * DTO for security alert emails containing device and location information.
 * 
 * <p>
 * Used for sending security notifications like:
 * </p>
 * <ul>
 * <li>New device login alerts</li>
 * <li>New location login alerts</li>
 * <li>Suspicious activity alerts</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public record SecurityAlertDto(
        // ========================================
        // User Information
        // ========================================

        /** User's display name or username */
        String userName,

        /** User's email address (recipient) */
        String userEmail,

        // ========================================
        // Device Information
        // ========================================

        /** Device type: MOBILE, TABLET, DESKTOP, etc. */
        String deviceType,

        /** Browser name: Chrome, Safari, Firefox, etc. */
        String browserName,

        /** Browser version */
        String browserVersion,

        /** Operating system: Windows, macOS, iOS, Android, etc. */
        String osName,

        /** OS version */
        String osVersion,

        /** Device brand if available: Apple, Samsung, etc. */
        String deviceBrand,

        /** Device model if available: iPhone 15, Galaxy S24, etc. */
        String deviceModel,

        // ========================================
        // Location Information
        // ========================================

        /** City name */
        String city,

        /** Region/State name */
        String region,

        /** Country name */
        String country,

        /** Country code (ISO 3166-1 alpha-2) */
        String countryCode,

        /** Timezone */
        String timezone,

        /** Previous city (for location change alerts) */
        String previousCity,

        /** Previous country (for location change alerts) */
        String previousCountry,

        // ========================================
        // Network Information
        // ========================================

        /** Client IP address (partially masked for security) */
        String ipAddress,

        /** Whether connection is through VPN/Proxy */
        Boolean isProxy,

        /** ISP name if available */
        String isp,

        // ========================================
        // Event Information
        // ========================================

        /** When the login occurred */
        Instant loginTime,

        /** Alert type: NEW_DEVICE, NEW_LOCATION, SUSPICIOUS_ACTIVITY */
        AlertType alertType,

        /** Risk score (0-100) */
        Integer riskScore,

        /** Additional notes or warnings */
        String additionalInfo) {

    // ========================================
    // Alert Types
    // ========================================

    public enum AlertType {
        NEW_DEVICE("New Device Login"),
        NEW_LOCATION("Login from New Location"),
        SUSPICIOUS_ACTIVITY("Suspicious Activity Detected"),
        MULTIPLE_FAILED_ATTEMPTS("Multiple Failed Login Attempts"),
        ACCOUNT_LOCKED("Account Locked"),
        PASSWORD_CHANGED("Password Changed"),
        EMAIL_CHANGED("Email Address Changed");

        private final String displayName;

        AlertType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========================================
    // Factory Methods
    // ========================================

    /**
     * Creates a SecurityAlertDto for new device login.
     */
    public static SecurityAlertDto forNewDevice(
            String userName,
            String userEmail,
            String deviceType,
            String browserName,
            String browserVersion,
            String osName,
            String osVersion,
            String city,
            String country,
            String countryCode,
            String ipAddress,
            Instant loginTime) {
        return new SecurityAlertDto(
                userName, userEmail,
                deviceType, browserName, browserVersion, osName, osVersion,
                null, null, // deviceBrand, deviceModel
                city, null, country, countryCode, null, // location
                null, null, // previous location
                ipAddress, null, null, // network
                loginTime, AlertType.NEW_DEVICE, null, null);
    }

    /**
     * Creates a SecurityAlertDto for new location login.
     */
    public static SecurityAlertDto forNewLocation(
            String userName,
            String userEmail,
            String deviceType,
            String browserName,
            String osName,
            String city,
            String country,
            String countryCode,
            String previousCity,
            String previousCountry,
            String ipAddress,
            Instant loginTime) {
        return new SecurityAlertDto(
                userName, userEmail,
                deviceType, browserName, null, osName, null,
                null, null,
                city, null, country, countryCode, null,
                previousCity, previousCountry,
                ipAddress, null, null,
                loginTime, AlertType.NEW_LOCATION, null, null);
    }

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Gets a display-friendly device description.
     * Example: "Chrome on Windows 10" or "Safari on iPhone"
     */
    public String getDeviceDescription() {
        StringBuilder sb = new StringBuilder();

        if (browserName != null && !browserName.isEmpty()) {
            sb.append(browserName);
            if (browserVersion != null && !browserVersion.isEmpty()) {
                sb.append(" ").append(browserVersion);
            }
        }

        if (osName != null && !osName.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(" on ");
            }
            sb.append(osName);
            if (osVersion != null && !osVersion.isEmpty()) {
                sb.append(" ").append(osVersion);
            }
        }

        if (deviceModel != null && !deviceModel.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(" (").append(deviceModel).append(")");
            } else {
                sb.append(deviceModel);
            }
        }

        return sb.isEmpty() ? "Unknown device" : sb.toString();
    }

    /**
     * Gets a display-friendly location description.
     * Example: "New York, United States"
     */
    public String getLocationDescription() {
        StringBuilder sb = new StringBuilder();

        if (city != null && !city.isEmpty()) {
            sb.append(city);
        }

        if (region != null && !region.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(region);
        }

        if (country != null && !country.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(country);
        }

        return sb.isEmpty() ? "Unknown location" : sb.toString();
    }

    /**
     * Gets formatted login time in user-friendly format.
     */
    public String getFormattedLoginTime() {
        if (loginTime == null) {
            return "Unknown time";
        }

        ZoneId zone = timezone != null ? ZoneId.of(timezone) : ZoneId.systemDefault();

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("MMM dd, yyyy 'at' hh:mm a z")
                .withZone(zone);

        return formatter.format(loginTime);
    }

    /**
     * Gets masked IP address for display (e.g., "203.xxx.xxx.123").
     */
    public String getMaskedIpAddress() {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return "Unknown";
        }

        // For IPv4, mask middle octets
        String[] parts = ipAddress.split("\\.");
        if (parts.length == 4) {
            return parts[0] + ".xxx.xxx." + parts[3];
        }

        // For IPv6 or other formats, show first and last parts
        if (ipAddress.length() > 10) {
            return ipAddress.substring(0, 4) + "...:" +
                    ipAddress.substring(ipAddress.length() - 4);
        }

        return ipAddress;
    }

    /**
     * Checks if this is a high-risk alert.
     */
    public boolean isHighRisk() {
        return riskScore != null && riskScore >= 70;
    }

    /**
     * Gets the alert severity level.
     */
    public String getSeverity() {
        if (alertType == AlertType.SUSPICIOUS_ACTIVITY || isHighRisk()) {
            return "HIGH";
        }
        if (alertType == AlertType.NEW_LOCATION) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
