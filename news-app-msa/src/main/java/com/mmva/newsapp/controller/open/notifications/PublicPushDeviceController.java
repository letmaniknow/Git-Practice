package com.mmva.newsapp.controller.open.notifications;

import com.mmva.newsapp.infrastructure.push.dto.*;
import com.mmva.newsapp.infrastructure.push.service.PushDeviceService;
import com.mmva.newsapp.infrastructure.push.service.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Public push notification controller for device management.
 * 
 * <h3>No Authentication Required:</h3>
 * <p>
 * These endpoints are public to support anonymous users who haven't logged in.
 * Device registration is by FCM token, not user identity.
 * </p>
 * 
 * <h3>Security Considerations:</h3>
 * <ul>
 * <li>Rate limiting applied to prevent abuse</li>
 * <li>Device fingerprint for fraud detection</li>
 * <li>Token validation via FCM</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/public/push")
@RequiredArgsConstructor
@Tag(name = "Push Notifications (Public)", description = "Device registration and subscription management for push notifications")
@Slf4j
public class PublicPushDeviceController {

        private final PushDeviceService deviceService;
        private final PushNotificationService notificationService;

        // ========================================
        // Device Registration
        // ========================================

        @Operation(summary = "Register device for push notifications", description = """
                        Registers a device to receive push notifications.
                        - No authentication required (supports anonymous users)
                        - Creates new device or updates existing by FCM token
                        - Automatically subscribes to default topics (all_news, breaking_news)
                        - Call this on app start and when FCM token refreshes
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Device registered successfully"),
                        @ApiResponse(responseCode = "200", description = "Existing device updated"),
                        @ApiResponse(responseCode = "400", description = "Invalid request")
        })
        @PostMapping("/devices/register")
        public ResponseEntity<ApiResponseDto<PushDeviceRegistrationResponseDto>> registerDevice(
                        @Valid @RequestBody PushDeviceRegistrationRequestDto request) {

                log.info("PublicPushController: Register device - infrastructure={}", request.getPlatform());

                PushDeviceRegistrationResponseDto response = deviceService.registerDevice(request);

                HttpStatus status = response.getMessage().contains("registered")
                                ? HttpStatus.CREATED
                                : HttpStatus.OK;
                String message = response.getMessage().contains("registered") ? "Device registered" : "Device updated";

                return ResponseEntity.status(status).body(ApiResponseDto.success(message, response));
        }

        @Operation(summary = "Refresh FCM token", description = """
                        Updates the FCM token for an existing device.
                        Called when FCM refreshes the token (app reinstall, data clear, etc.).
                        Re-subscribes to all topics with new token.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        @PutMapping("/devices/{deviceId}/token")
        public ResponseEntity<ApiResponseDto<PushDeviceRegistrationResponseDto>> refreshToken(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId,
                        @RequestBody Map<String, String> body) {

                String newToken = body.get("fcmToken");
                if (newToken == null || newToken.isBlank()) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponseDto.error("FCM token is required"));
                }

                log.info("PublicPushController: Refresh token - deviceId={}", deviceId);

                PushDeviceRegistrationResponseDto response = deviceService.refreshToken(deviceId, newToken);
                return ResponseEntity.ok(ApiResponseDto.success("Token refreshed", response));
        }

        @Operation(summary = "Unregister device", description = """
                        Unregisters a device from push notifications.
                        Unsubscribes from all topics and marks device as inactive.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Device unregistered"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        @DeleteMapping("/devices/{deviceId}")
        public ResponseEntity<ApiResponseDto<Void>> unregisterDevice(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId) {

                log.info("PublicPushController: Unregister device - deviceId={}", deviceId);

                deviceService.unregisterDevice(deviceId);
                return ResponseEntity.ok(ApiResponseDto.success("Device unregistered", null));
        }

        // ========================================
        // Device Settings
        // ========================================

        @Operation(summary = "Get device information", description = "Retrieves device registration details and current settings.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device information retrieved"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        @GetMapping("/devices/{deviceId}")
        public ResponseEntity<ApiResponseDto<PushDeviceRegistrationResponseDto>> getDevice(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId) {

                PushDeviceRegistrationResponseDto response = deviceService.getDevice(deviceId);
                return ResponseEntity.ok(ApiResponseDto.success("Device retrieved", response));
        }

        @Operation(summary = "Update device settings", description = """
                        Updates notification preferences for a device.
                        - Toggle notification types (breaking news, daily digest, etc.)
                        - Update language preference
                        - Update timezone
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Settings updated"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        @PutMapping("/devices/{deviceId}/settings")
        public ResponseEntity<ApiResponseDto<PushDeviceRegistrationResponseDto>> updateSettings(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId,
                        @Valid @RequestBody PushDeviceSettingsUpdateRequestDto request) {

                log.info("PublicPushController: Update settings - deviceId={}", deviceId);

                PushDeviceRegistrationResponseDto response = deviceService.updateSettings(deviceId, request);
                return ResponseEntity.ok(ApiResponseDto.success("Settings updated", response));
        }

        // ========================================
        // Topic Subscriptions
        // ========================================

        @Operation(summary = "Get available topics", description = "Returns list of all topics available for subscription.")
        @GetMapping("/topics")
        public ResponseEntity<ApiResponseDto<List<PushAvailableTopicDto>>> getAvailableTopics() {
                List<PushAvailableTopicDto> topics = deviceService.getAvailableTopics();
                return ResponseEntity.ok(ApiResponseDto.success("Topics retrieved", topics));
        }

        @Operation(summary = "Get device subscriptions", description = "Returns list of topics the device is subscribed to.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscriptions retrieved"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        @GetMapping("/devices/{deviceId}/subscriptions")
        public ResponseEntity<ApiResponseDto<List<String>>> getSubscriptions(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId) {

                List<String> subscriptions = deviceService.getSubscriptions(deviceId);
                return ResponseEntity.ok(ApiResponseDto.success("Subscriptions retrieved", subscriptions));
        }

        @Operation(summary = "Update topic subscriptions", description = """
                        Subscribe or unsubscribe from topics.
                        - `subscribe`: List of topics to subscribe to
                        - `unsubscribe`: List of topics to unsubscribe from
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscriptions updated"),
                        @ApiResponse(responseCode = "404", description = "Device not found")
        })
        @PostMapping("/devices/{deviceId}/subscriptions")
        public ResponseEntity<ApiResponseDto<PushTopicSubscriptionResponseDto>> updateSubscriptions(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId,
                        @Valid @RequestBody PushTopicSubscriptionRequestDto request) {

                log.info("PublicPushController: Update subscriptions - deviceId={}", deviceId);

                PushTopicSubscriptionResponseDto response = deviceService.updateSubscriptions(deviceId, request);
                return ResponseEntity.ok(ApiResponseDto.success("Subscriptions updated", response));
        }

        // ========================================
        // Activity Tracking
        // ========================================

        @Operation(summary = "Record device activity", description = """
                        Records device activity (heartbeat).
                        Call periodically when app is active to keep device from being marked stale.
                        """)
        @PostMapping("/devices/{deviceId}/activity")
        public ResponseEntity<ApiResponseDto<Void>> recordActivity(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId) {

                deviceService.recordActivity(deviceId);
                return ResponseEntity.ok(ApiResponseDto.success("Activity recorded", null));
        }

        // ========================================
        // Notification Tracking
        // ========================================

        @Operation(summary = "Record notification opened", description = """
                        Records that a user opened/clicked a notification.
                        Call from app when notification is tapped.
                        """)
        @PostMapping("/notifications/{notificationId}/opened")
        public ResponseEntity<ApiResponseDto<Void>> recordNotificationOpened(
                        @Parameter(description = "Notification ID") @PathVariable UUID notificationId,
                        @RequestBody Map<String, String> body) {

                String deviceIdStr = body.get("deviceId");
                if (deviceIdStr != null) {
                        UUID deviceId = UUID.fromString(deviceIdStr);
                        notificationService.recordNotificationOpened(notificationId, deviceId);
                }

                return ResponseEntity.ok(ApiResponseDto.success("Notification opened recorded", null));
        }
}
