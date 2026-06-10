package com.mmva.newsapp.infrastructure.push.service;

import com.mmva.newsapp.infrastructure.push.dto.*;

import java.util.List;
import java.util.UUID;

/**
 * Push device management service interface.
 * 
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Device registration (no authentication required)</li>
 * <li>Device settings management</li>
 * <li>Topic subscription management</li>
 * <li>Device lifecycle management</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface PushDeviceService {

    // ========================================
    // Device Registration
    // ========================================

    /**
     * Register a device for push notifications.
     * Creates new device or updates existing by FCM token.
     * No authentication required - supports anonymous users.
     * 
     * @param request registration request
     * @return registration response with device ID
     */
    PushDeviceRegistrationResponseDto registerDevice(PushDeviceRegistrationRequestDto request);

    /**
     * Update device FCM token (token refresh).
     * 
     * @param deviceId device ID
     * @param newToken new FCM token
     * @return updated device info
     */
    PushDeviceRegistrationResponseDto refreshToken(UUID deviceId, String newToken);

    /**
     * Unregister device (app uninstalled or user requested).
     * 
     * @param deviceId device ID
     */
    void unregisterDevice(UUID deviceId);

    /**
     * Unregister device by FCM token.
     * 
     * @param fcmToken FCM token
     */
    void unregisterByToken(String fcmToken);

    // ========================================
    // User Association
    // ========================================

    /**
     * Link device to authenticated user.
     * Called on user login.
     * 
     * @param deviceId device ID
     * @param userId   user ID
     */
    void linkToUser(UUID deviceId, UUID userId);

    /**
     * Unlink device from user.
     * Called on user logout.
     * 
     * @param deviceId device ID
     */
    void unlinkFromUser(UUID deviceId);

    // ========================================
    // Device Settings
    // ========================================

    /**
     * Update device notification settings.
     * 
     * @param deviceId device ID
     * @param request  settings update request
     * @return updated settings
     */
    PushDeviceRegistrationResponseDto updateSettings(UUID deviceId, PushDeviceSettingsUpdateRequestDto request);

    /**
     * Get device information.
     * 
     * @param deviceId device ID
     * @return device info
     */
    PushDeviceRegistrationResponseDto getDevice(UUID deviceId);

    /**
     * Get device by FCM token.
     * 
     * @param fcmToken FCM token
     * @return device info
     */
    PushDeviceRegistrationResponseDto getDeviceByToken(String fcmToken);

    // ========================================
    // Topic Subscriptions
    // ========================================

    /**
     * Update topic subscriptions for device.
     * 
     * @param deviceId device ID
     * @param request  subscription request
     * @return subscription result
     */
    PushTopicSubscriptionResponseDto updateSubscriptions(UUID deviceId, PushTopicSubscriptionRequestDto request);

    /**
     * Get current subscriptions for device.
     * 
     * @param deviceId device ID
     * @return list of subscribed topics
     */
    List<String> getSubscriptions(UUID deviceId);

    /**
     * Get all available topics for subscription.
     * 
     * @return list of available topics
     */
    List<PushAvailableTopicDto> getAvailableTopics();

    // ========================================
    // Activity Tracking
    // ========================================

    /**
     * Record device activity (called when app opens or makes API calls).
     * 
     * @param deviceId device ID
     */
    void recordActivity(UUID deviceId);

    /**
     * Record device activity by token.
     * 
     * @param fcmToken FCM token
     */
    void recordActivityByToken(String fcmToken);

    // ========================================
    // Admin Operations
    // ========================================

    /**
     * Get all devices for a user.
     * 
     * @param userId user ID
     * @return list of devices
     */
    List<PushDeviceRegistrationResponseDto> getDevicesForUser(UUID userId);

    /**
     * Get device statistics.
     * 
     * @return device stats
     */
    DeviceStatistics getDeviceStatistics();

    /**
     * Clean up stale devices.
     * 
     * @param daysInactive number of days of inactivity
     * @return count of devices cleaned up
     */
    int cleanupStaleDevices(int daysInactive);

    /**
     * Device statistics DTO.
     */
    record DeviceStatistics(
            long totalDevices,
            long activeDevices,
            long androidDevices,
            long iosDevices,
            long webDevices,
            java.util.Map<String, Long> devicesByLanguage) {
    }
}
