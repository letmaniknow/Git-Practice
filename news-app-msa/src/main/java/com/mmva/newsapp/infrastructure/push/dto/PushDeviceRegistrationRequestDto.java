package com.mmva.newsapp.infrastructure.push.dto;

import com.mmva.newsapp.infrastructure.push.enums.PushDevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for registering a device for push notifications.
 * 
 * <h3>Usage:</h3>
 * <p>
 * Called by mobile/web apps when:
 * </p>
 * <ul>
 * <li>App is first installed</li>
 * <li>FCM token is refreshed</li>
 * <li>User logs in (to link device to user)</li>
 * <li>User logs out (to unlink device from user)</li>
 * </ul>
 * 
 * <h3>No Authentication Required:</h3>
 * <p>
 * This endpoint is public to allow anonymous users to register.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushDeviceRegistrationRequestDto {

    /**
     * Firebase Cloud Messaging registration token.
     * Required for all registrations.
     */
    @NotBlank(message = "FCM token is required")
    @Size(max = 512, message = "FCM token must not exceed 512 characters")
    private String fcmToken;

    /**
     * Device infrastructure/OS type.
     */
    @NotNull(message = "Platform is required")
    private PushDevicePlatform platform;

    /**
     * Unique device fingerprint for cross-token tracking.
     * Generated client-side using device-specific identifiers.
     */
    @Size(max = 255, message = "Device fingerprint must not exceed 255 characters")
    private String deviceFingerprint;

    /**
     * Application version currently installed.
     */
    @Size(max = 50, message = "App version must not exceed 50 characters")
    private String appVersion;

    /**
     * Operating system version.
     */
    @Size(max = 50, message = "OS version must not exceed 50 characters")
    private String osVersion;

    /**
     * Device model identifier.
     */
    @Size(max = 100, message = "Device model must not exceed 100 characters")
    private String deviceModel;

    /**
     * Device manufacturer.
     */
    @Size(max = 100, message = "Device manufacturer must not exceed 100 characters")
    private String deviceManufacturer;

    /**
     * Preferred language for notifications (ISO 639-1).
     */
    @Size(max = 10, message = "Language must not exceed 10 characters")
    @Builder.Default
    private String language = "en";

    /**
     * Device timezone (IANA timezone ID).
     */
    @Size(max = 100, message = "Timezone must not exceed 100 characters")
    private String timezone;

    /**
     * Country code (ISO 3166-1 alpha-2).
     */
    @Size(max = 10, message = "Country code must not exceed 10 characters")
    private String countryCode;

    /**
     * Whether user has enabled notifications on device.
     */
    @Builder.Default
    private Boolean notificationsEnabled = true;
}
