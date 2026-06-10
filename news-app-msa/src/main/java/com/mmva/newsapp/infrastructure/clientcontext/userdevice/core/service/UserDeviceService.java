package com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.service;

import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.model.UserDevice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing user devices with comprehensive security and
 * analytics features.
 * 
 * <p>
 * Provides:
 * </p>
 * <ul>
 * <li>Device registration and tracking</li>
 * <li>Multi-device session management</li>
 * <li>Anomaly detection (new device, new location)</li>
 * <li>Device trust management</li>
 * <li>Push notification token management</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface UserDeviceService {

    // ========================================
    // Device Registration & Tracking
    // ========================================

    /**
     * Registers or updates a device for a user based on current request context.
     * This should be called on login and significant user actions.
     * 
     * @param userId the user ID
     * @return the registered or updated device, and whether it's new
     */
    DeviceRegistrationResult registerOrUpdateDevice(UUID userId);

    /**
     * Registers or updates a device for a user with provided context.
     * 
     * @param userId  the user ID
     * @param context the client context
     * @return the registered or updated device, and whether it's new
     */
    DeviceRegistrationResult registerOrUpdateDevice(UUID userId, ClientContextDto context);

    /**
     * Gets a device by fingerprint for a user.
     */
    Optional<UserDevice> getDevice(UUID userId, String fingerprint);

    /**
     * Gets all devices for a user.
     */
    List<UserDevice> getUserDevices(UUID userId);

    /**
     * Gets active (non-blocked) devices for a user.
     */
    List<UserDevice> getActiveDevices(UUID userId);

    /**
     * Gets trusted devices for a user.
     */
    List<UserDevice> getTrustedDevices(UUID userId);

    // ========================================
    // Device Trust Management
    // ========================================

    /**
     * Marks a device as trusted by the user.
     */
    UserDevice trustDevice(UUID userId, UUID deviceId);

    /**
     * Removes trust from a device.
     */
    UserDevice untrustDevice(UUID userId, UUID deviceId);

    /**
     * Increases trust level for a device (called on successful actions).
     */
    void increaseTrust(UUID deviceId, int amount);

    // ========================================
    // Device Security
    // ========================================

    /**
     * Blocks a device.
     */
    UserDevice blockDevice(UUID userId, UUID deviceId, String reason);

    /**
     * Unblocks a device.
     */
    UserDevice unblockDevice(UUID userId, UUID deviceId);

    /**
     * Records a failed login attempt for a device.
     */
    void recordFailedLogin(UUID userId, String fingerprint);

    /**
     * Gets high-risk devices for a user.
     */
    List<UserDevice> getHighRiskDevices(UUID userId);

    /**
     * Checks if this device fingerprint is used by multiple users (suspicious).
     */
    boolean isSharedDevice(String fingerprint);

    // ========================================
    // Anomaly Detection
    // ========================================

    /**
     * Checks if this is a new device for the user.
     */
    boolean isNewDevice(UUID userId, String fingerprint);

    /**
     * Checks if this is a new location for the user.
     */
    boolean isNewLocation(UUID userId, String countryCode);

    /**
     * Checks if the current request represents an anomaly.
     */
    AnomalyCheckResult checkForAnomalies(UUID userId, ClientContextDto context);

    // ========================================
    // Push Notification Management
    // ========================================

    /**
     * Updates push notification token for a device.
     */
    void updatePushToken(UUID deviceId, String token, String tokenType);

    /**
     * Gets devices with push tokens for a user.
     */
    List<UserDevice> getDevicesWithPushToken(UUID userId);

    /**
     * Gets FCM-enabled devices for a user.
     */
    List<UserDevice> getFcmDevices(UUID userId);

    /**
     * Gets APNS-enabled devices for a user.
     */
    List<UserDevice> getApnsDevices(UUID userId);

    // ========================================
    // Device Removal
    // ========================================

    /**
     * Removes a device (soft delete). Throws exception if device not found.
     */
    void removeDevice(UUID userId, UUID deviceId);

    /**
     * Removes all devices for a user (on account deletion).
     */
    int removeAllDevices(UUID userId);

    /**
     * Logs out from all devices except current.
     */
    int logoutOtherDevices(UUID userId, String currentFingerprint);

    /**
     * Gets a device by ID, ensuring it belongs to the user.
     */
    UserDevice getDeviceById(UUID deviceId, UUID userId);

    /**
     * Gets blocked devices for a user.
     */
    List<UserDevice> getBlockedDevices(UUID userId);

    /**
     * Renames a device with a custom name.
     */
    UserDevice renameDevice(UUID userId, UUID deviceId, String newName);

    // ========================================
    // Result Records
    // ========================================

    /**
     * Result of device registration.
     */
    record DeviceRegistrationResult(
            UserDevice device,
            boolean isNewDevice,
            boolean locationChanged,
            String message) {
    }

    /**
     * Result of anomaly check.
     */
    record AnomalyCheckResult(
            boolean isNewDevice,
            boolean isNewLocation,
            boolean isSharedDevice,
            boolean isHighRiskContext,
            int anomalyScore,
            String anomalyFactors) {
        public boolean hasAnomalies() {
            return anomalyScore > 0;
        }

        public boolean requiresVerification() {
            return anomalyScore >= 50;
        }
    }
}
