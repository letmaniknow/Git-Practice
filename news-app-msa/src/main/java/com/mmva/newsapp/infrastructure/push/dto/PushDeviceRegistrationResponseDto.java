package com.mmva.newsapp.infrastructure.push.dto;

import com.mmva.newsapp.infrastructure.push.enums.PushDevicePlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for device registration.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushDeviceRegistrationResponseDto {

    /**
     * Unique device ID assigned by the system.
     */
    private UUID deviceId;

    /**
     * Device infrastructure.
     */
    private PushDevicePlatform platform;

    /**
     * Whether device is active for notifications.
     */
    private Boolean isActive;

    /**
     * Current notification settings.
     */
    private DeviceNotificationSettingsDto settings;

    /**
     * List of topics device is subscribed to.
     */
    private List<String> subscribedTopics;

    /**
     * When device was registered.
     */
    private Instant registeredAt;

    /**
     * Server message (e.g., "Device registered successfully").
     */
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceNotificationSettingsDto {
        private Boolean notificationsEnabled;
        private Boolean breakingNewsEnabled;
        private Boolean dailyDigestEnabled;
        private Boolean promotionalEnabled;
    }
}
