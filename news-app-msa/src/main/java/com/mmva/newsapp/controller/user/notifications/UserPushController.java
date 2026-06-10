package com.mmva.newsapp.controller.user.notifications;

import com.mmva.newsapp.infrastructure.push.dto.*;
import com.mmva.newsapp.infrastructure.security.util.SecurityContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * User controller for push notification management.
 *
 * <h3>Authentication Required:</h3>
 * <p>
 * Requires user authentication. All endpoints are user-scoped.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 * <li>View notification history</li>
 * <li>Manage notification preferences</li>
 * <li>Device management for authenticated user</li>
 * <li>Topic subscription management</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/me/push")
@RequiredArgsConstructor
@Tag(name = "Push Notifications (User)", description = "User notification preferences and history management")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class UserPushController {

        /**
         * Get user's notification history (alias for /history).
         *
         * @param pageable pagination parameters
         * @return paginated list of user's notifications
         */
        @GetMapping
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get notifications", description = "Retrieve paginated list of notifications sent to current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Notification history retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<Page<PushNotificationResponseDto>>> getNotifications(
                        @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

                return getNotificationHistory(pageable);
        }

        /**
         * Get specific notification details.
         *
         * @param notificationId notification ID
         * @return notification details
         */
        @GetMapping("/notifications/{notificationId}")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get notification details", description = "Retrieve details of a specific notification")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Notification retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Notification not found"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<PushNotificationResponseDto>> getNotificationDetails(
                        @Parameter(description = "Notification ID") @PathVariable UUID notificationId) {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would verify notification belongs to user and return details
                return ResponseEntity.ok(ApiResponseDto.success("Notification details retrieved", null));
        }

        // ========================================
        // NOTIFICATION HISTORY
        // ========================================

        /**
         * Get user's notification history.
         *
         * @param pageable pagination parameters
         * @return paginated list of user's notifications
         */
        @GetMapping("/history")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get notification history", description = "Retrieve paginated list of notifications sent to current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Notification history retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<Page<PushNotificationResponseDto>>> getNotificationHistory(
                        @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

                UUID userId = SecurityContextUtils.getCurrentUserId().orElse(null);
                // Note: This would require extending the service to filter by user
                // For now, return empty page as placeholder

                return ResponseEntity.ok(ApiResponseDto.success("Notification history retrieved", Page.empty()));
        }

        /**
         * Mark notification as read.
         *
         * @param notificationId notification ID
         * @return success response
         */
        @PutMapping("/notifications/{notificationId}/read")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Notification marked as read"),
                        @ApiResponse(responseCode = "404", description = "Notification not found"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<Void>> markNotificationAsRead(
                        @Parameter(description = "Notification ID") @PathVariable UUID notificationId) {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would verify notification belongs to user and mark as read
                return ResponseEntity.ok(ApiResponseDto.success("Notification marked as read", null));
        }

        // ========================================
        // DEVICE MANAGEMENT
        // ========================================

        /**
         * Get user's registered devices.
         *
         * @return list of user's devices
         */
        @GetMapping("/devices")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get user devices", description = "Retrieve all push notification devices registered for current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Devices retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<List<PushDeviceRegistrationResponseDto>>> getUserDevices() {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would filter devices by user ID
                return ResponseEntity.ok(ApiResponseDto.success("User devices retrieved", List.of()));
        }

        /**
         * * Get device settings.
         *
         * @param deviceId device ID
         * @return device settings
         */
        @GetMapping("/devices/{deviceId}/settings")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get device settings", description = "Retrieve push notification settings for a specific device")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device settings retrieved"),
                        @ApiResponse(responseCode = "404", description = "Device not found"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<PushDeviceSettingsUpdateRequestDto>> getDeviceSettings(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId) {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would verify device belongs to user and return settings
                return ResponseEntity.ok(ApiResponseDto.success("Device settings retrieved", null));
        }

        /**
         * * Update device settings.
         *
         * @param deviceId device ID
         * @param request  settings update request
         * @return updated device info
         */
        @PutMapping("/devices/{deviceId}/settings")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Update device settings", description = "Update push notification settings for a specific device")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device settings updated"),
                        @ApiResponse(responseCode = "404", description = "Device not found"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<PushDeviceRegistrationResponseDto>> updateDeviceSettings(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId,
                        @Valid @RequestBody PushDeviceSettingsUpdateRequestDto request) {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would verify device belongs to user and update settings
                return ResponseEntity.ok(ApiResponseDto.success("Device settings updated", null));
        }

        /**
         * Unregister device.
         *
         * @param deviceId device ID
         * @return success response
         */
        @DeleteMapping("/devices/{deviceId}")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Unregister device", description = "Remove a device from push notification service")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device unregistered successfully"),
                        @ApiResponse(responseCode = "404", description = "Device not found"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<Void>> unregisterDevice(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId) {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would verify device belongs to user and unregister
                return ResponseEntity.ok(ApiResponseDto.success("Device unregistered successfully", null));
        }

        /**
         * Link device to user account.
         *
         * @param deviceId device ID
         * @param request  linking request
         * @return success response
         */
        @PostMapping("/devices/{deviceId}/link")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Link device", description = "Link a device to the current user's account")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device linked successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid linking request"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<Void>> linkDevice(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId,
                        @RequestBody Map<String, Object> request) {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would link device to user account
                return ResponseEntity.ok(ApiResponseDto.success("Device linked successfully", null));
        }

        // ========================================
        // TOPIC SUBSCRIPTIONS
        // ========================================

        /**
         * Get device-specific topic subscriptions.
         *
         * @param deviceId device ID
         * @return device subscriptions
         */
        @GetMapping("/devices/{deviceId}/subscriptions")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get device subscriptions", description = "Retrieve topic subscriptions for a specific device")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device subscriptions retrieved"),
                        @ApiResponse(responseCode = "404", description = "Device not found"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<List<PushTopicSubscriptionResponseDto>>> getDeviceSubscriptions(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId) {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would return device-specific subscriptions
                return ResponseEntity.ok(ApiResponseDto.success("Device subscriptions retrieved", List.of()));
        }

        /**
         * Update device topic subscriptions.
         *
         * @param deviceId      device ID
         * @param subscriptions subscription updates
         * @return updated subscriptions
         */
        @PutMapping("/devices/{deviceId}/subscriptions")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Update device subscriptions", description = "Update topic subscriptions for a specific device")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Device subscriptions updated"),
                        @ApiResponse(responseCode = "400", description = "Invalid subscription data"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<List<PushTopicSubscriptionResponseDto>>> updateDeviceSubscriptions(
                        @Parameter(description = "Device ID") @PathVariable UUID deviceId,
                        @RequestBody List<Map<String, Object>> subscriptions) {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would update device-specific subscriptions
                return ResponseEntity.ok(ApiResponseDto.success("Device subscriptions updated", List.of()));
        }

        /**
         * Get user's topic subscriptions.
         *
         * @return list of subscribed topics
         */
        @GetMapping("/topics")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get topic subscriptions", description = "Retrieve all topic subscriptions for current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Topic subscriptions retrieved"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<List<PushTopicSubscriptionResponseDto>>> getUserTopicSubscriptions() {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would return user's topic subscriptions
                return ResponseEntity.ok(ApiResponseDto.success("Topic subscriptions retrieved", List.of()));
        }

        /**
         * Subscribe to topic.
         *
         * @param request subscription request
         * @return subscription response
         */
        @PostMapping("/topics")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Subscribe to topic", description = "Subscribe current user to a push notification topic")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Successfully subscribed to topic"),
                        @ApiResponse(responseCode = "400", description = "Invalid subscription request"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<PushTopicSubscriptionResponseDto>> subscribeToTopic(
                        @Valid @RequestBody PushTopicSubscriptionRequestDto request) {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would create topic subscription for user
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Successfully subscribed to topic", null));
        }

        /**
         * Unsubscribe from topic.
         *
         * @param topicCode topic code
         * @return success response
         */
        @DeleteMapping("/topics/{topicCode}")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Unsubscribe from topic", description = "Unsubscribe current user from a push notification topic")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully unsubscribed from topic"),
                        @ApiResponse(responseCode = "404", description = "Topic subscription not found"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<Void>> unsubscribeFromTopic(
                        @Parameter(description = "Topic code") @PathVariable String topicCode) {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would remove topic subscription for user
                return ResponseEntity.ok(ApiResponseDto.success("Successfully unsubscribed from topic", null));
        }

        // ========================================
        // NOTIFICATION PREFERENCES
        // ========================================

        /**
         * Get user's notification preferences.
         *
         * @return user preferences
         */
        @GetMapping("/preferences")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get notification preferences", description = "Retrieve current user's push notification preferences")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> getNotificationPreferences() {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would return user's notification preferences
                return ResponseEntity.ok(ApiResponseDto.success("Preferences retrieved successfully", Map.of()));
        }

        /**
         * Update notification preferences.
         *
         * @param preferences preference settings
         * @return updated preferences
         */
        @PutMapping("/preferences")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Update notification preferences", description = "Update current user's push notification preferences")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid preferences"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateNotificationPreferences(
                        @RequestBody Map<String, Object> preferences) {

                UUID userId = SecurityContextUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("User not authenticated"));
                // Implementation would update and return user's preferences
                return ResponseEntity.ok(ApiResponseDto.success("Preferences updated successfully", preferences));
        }

        // ========================================
        // AVAILABLE TOPICS
        // ========================================

        /**
         * Get available topics for subscription.
         *
         * @return list of available topics
         */
        @GetMapping("/topics/available")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get available topics", description = "Retrieve all available push notification topics for subscription")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Available topics retrieved"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<ApiResponseDto<List<PushAvailableTopicDto>>> getAvailableTopics() {

                // Implementation would return all available topics
                return ResponseEntity.ok(ApiResponseDto.success("Available topics retrieved", List.of()));
        }
}