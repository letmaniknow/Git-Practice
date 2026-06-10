package com.mmva.newsapp.infrastructure.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for security alert notifications.
 * 
 * <p>
 * Controls when and how security alerts are sent to users:
 * </p>
 * <ul>
 * <li>New device login notifications</li>
 * <li>New location login notifications</li>
 * <li>Suspicious activity alerts</li>
 * </ul>
 * 
 * <h3>Configuration Example:</h3>
 * 
 * <pre>
 * security:
 *   alerts:
 *     enabled: true
 *     new-device-alert:
 *       enabled: true
 *       include-ip: false
 *     new-location-alert:
 *       enabled: true
 *     secure-account-url: https://yourapp.com/account/security
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security.alerts")
public class SecurityAlertNotificationProperties {

    /**
     * Master switch to enable/disable all security emails
     */
    private boolean enabled = true;

    /**
     * Configuration for new device login alerts
     */
    private NewDeviceAlertConfig newDeviceAlert = new NewDeviceAlertConfig();

    /**
     * Configuration for new location login alerts
     */
    private NewLocationAlertConfig newLocationAlert = new NewLocationAlertConfig();

    /**
     * Configuration for suspicious activity alerts
     */
    private SuspiciousActivityConfig suspiciousActivity = new SuspiciousActivityConfig();

    /**
     * URL links for email actions
     */
    private EmailLinks links = new EmailLinks();

    /**
     * Email appearance settings
     */
    private EmailAppearance appearance = new EmailAppearance();

    // ========================================
    // Nested Configuration Classes
    // ========================================

    /**
     * New device login alert configuration
     */
    @Data
    public static class NewDeviceAlertConfig {
        /**
         * Enable new device login alerts
         */
        private boolean enabled = true;

        /**
         * Include IP address in email (privacy consideration)
         */
        private boolean includeIpAddress = false;

        /**
         * Include location in email
         */
        private boolean includeLocation = true;

        /**
         * Include device details (browser, OS)
         */
        private boolean includeDeviceDetails = true;

        /**
         * Skip alerts for trusted devices
         */
        private boolean skipTrustedDevices = true;

        /**
         * Minimum trust level to skip alerts (0-100)
         */
        private int trustThreshold = 80;
    }

    /**
     * New location login alert configuration
     */
    @Data
    public static class NewLocationAlertConfig {
        /**
         * Enable new location login alerts
         */
        private boolean enabled = true;

        /**
         * Include IP address in email
         */
        private boolean includeIpAddress = false;

        /**
         * Include previous location for comparison
         */
        private boolean includePreviousLocation = true;

        /**
         * Also send if device is trusted but location is new
         */
        private boolean alertEvenIfDeviceTrusted = true;

        /**
         * Only alert for country changes (not city changes)
         */
        private boolean countryLevelOnly = false;
    }

    /**
     * Suspicious activity alert configuration
     */
    @Data
    public static class SuspiciousActivityConfig {
        /**
         * Enable suspicious activity alerts
         */
        private boolean enabled = true;

        /**
         * Risk score threshold to trigger alert (0-100)
         */
        private int riskThreshold = 70;

        /**
         * Number of failed attempts before alert
         */
        private int failedAttemptsThreshold = 3;

        /**
         * Alert on VPN/Proxy detection
         */
        private boolean alertOnProxy = false;

        /**
         * Alert when device is shared across users
         */
        private boolean alertOnSharedDevice = true;
    }

    /**
     * Email action links configuration
     */
    @Data
    public static class EmailLinks {
        /**
         * Base URL for the application
         */
        private String baseUrl = "https://thenews.app";

        /**
         * URL for "Secure My Account" action
         */
        private String secureAccountUrl = "/account/security";

        /**
         * URL for "Change Password" action
         */
        private String changePasswordUrl = "/account/change-password";

        /**
         * URL for "View Login History" action
         */
        private String loginHistoryUrl = "/account/login-history";

        /**
         * URL for "Report Unauthorized Access" action
         */
        private String reportUnauthorizedUrl = "/support/report-unauthorized";

        /**
         * URL for "Trust This Device" action
         */
        private String trustDeviceUrl = "/account/devices/trust";

        /**
         * Gets the full URL for an action
         */
        public String getFullUrl(String path) {
            if (path.startsWith("http")) {
                return path;
            }
            return baseUrl + path;
        }
    }

    /**
     * Email appearance configuration
     */
    @Data
    public static class EmailAppearance {
        /**
         * Primary brand color (hex)
         */
        private String primaryColor = "#007bff";

        /**
         * Warning color for alerts (hex)
         */
        private String warningColor = "#dc3545";

        /**
         * Success color (hex)
         */
        private String successColor = "#28a745";

        /**
         * Logo URL (optional)
         */
        private String logoUrl;

        /**
         * Company/App name for footer
         */
        private String companyName = "TheNews";

        /**
         * Support email for questions
         */
        private String supportEmail = "support@thenews.app";
    }

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Checks if any security email is enabled
     */
    public boolean isAnyAlertEnabled() {
        return enabled && (newDeviceAlert.isEnabled() ||
                newLocationAlert.isEnabled() ||
                suspiciousActivity.isEnabled());
    }

    /**
     * Should send new device alert based on trust level?
     */
    public boolean shouldAlertNewDevice(int trustLevel) {
        if (!enabled || !newDeviceAlert.isEnabled()) {
            return false;
        }
        if (newDeviceAlert.isSkipTrustedDevices() &&
                trustLevel >= newDeviceAlert.getTrustThreshold()) {
            return false;
        }
        return true;
    }

    /**
     * Should send suspicious activity alert based on risk score?
     */
    public boolean shouldAlertSuspiciousActivity(int riskScore) {
        return enabled &&
                suspiciousActivity.isEnabled() &&
                riskScore >= suspiciousActivity.getRiskThreshold();
    }
}
