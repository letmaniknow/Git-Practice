package com.mmva.newsapp.controller.admin.notifications;

import com.mmva.newsapp.infrastructure.push.dto.PushDeviceRegistrationResponseDto;
import com.mmva.newsapp.infrastructure.push.dto.DigestNewsItemDto;
import com.mmva.newsapp.infrastructure.push.dto.PushNotificationResponseDto;
import com.mmva.newsapp.infrastructure.push.dto.PushNotificationSendRequestDto;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestInfoService;
import com.mmva.newsapp.infrastructure.security.util.SecurityContextUtils;
import com.mmva.newsapp.infrastructure.push.service.PushDeviceService;
import com.mmva.newsapp.infrastructure.push.service.PushNotificationAuditLogService;
import com.mmva.newsapp.infrastructure.push.service.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin controller for push notification management.
 * 
 * <h3>Authentication Required:</h3>
 * <p>
 * Requires admin authentication and PUSH_MANAGE permission.
 * </p>
 * 
 * <h3>Features:</h3>
 * <ul>
 * <li>Send notifications to topics, devices, users</li>
 * <li>Schedule notifications</li>
 * <li>View notification history and analytics</li>
 * <li>Manage devices and subscriptions</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/push")
@RequiredArgsConstructor
@Tag(name = "Push Notifications (Admin)", description = "Admin operations for push notification management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminPushController {

        private final PushNotificationService notificationService;
        private final PushDeviceService deviceService;
        private final PushNotificationAuditLogService auditLogService;
        private final RequestInfoService requestInfoService;

        // ========================================
        // Send Notifications
        // ========================================

        @Operation(summary = "Send push notification", description = """
                        Sends a push notification based on the request configuration.

                        **Target Types:**
                        - `ALL`: Send to all registered devices
                        - `TOPIC`: Send to topic subscribers (e.g., "breaking_news")
                        - `DEVICE`: Send to specific device IDs
                        - `USER`: Send to specific user IDs
                        - `SEGMENT`: Send to user segment

                        **Notification Types:**
                        - `BREAKING_NEWS`: Urgent breaking newsapp
                        - `NEWS_UPDATE`: Regular newsapp update
                        - `CATEGORY_UPDATE`: New content in category
                        - `DAILY_DIGEST`: Daily summary
                        - `PROMOTIONAL`: Marketing content
                        - `SYSTEM`: System announcements
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Notification sent/scheduled"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "409", description = "Duplicate notification (idempotency)")
        })
        @PostMapping("/notifications/send")
        public ResponseEntity<ApiResponseDto<PushNotificationResponseDto>> sendNotification(
                        @Valid @RequestBody PushNotificationSendRequestDto request,
                        HttpServletRequest httpRequest) {

                log.info("AdminPushController: Send notification - type={}, targetType={}",
                                request.getNotificationType(), request.getTargetType());

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);

                try {
                        PushNotificationResponseDto response = notificationService.sendNotification(request);

                        // Audit log the send action
                        auditLogService.logAction(
                                        null, // notification entity handled separately
                                        PushNotificationAuditLogService.ACTION_SEND,
                                        adminId,
                                        true,
                                        null,
                                        clientInfo);

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponseDto.success("Notification sent", response));
                } catch (Exception e) {
                        auditLogService.logFailure(
                                        PushNotificationAuditLogService.ACTION_SEND,
                                        adminId,
                                        e.getMessage(),
                                        clientInfo);
                        throw e;
                }
        }

        @Operation(summary = "Send breaking newsapp notification", description = """
                        Shortcut to send a breaking newsapp notification for a newsapp newsapp.
                        Automatically targets the "breaking_news" topic with HIGH priority.
                        Include thumbnailUrl for rich notification (1024x512 recommended).
                        This should be the news thumbnail URL from NewsMasterEntity.newsThumbnailUrl.
                        """)
        @PostMapping("/notifications/breaking-newsapp")
        public ResponseEntity<ApiResponseDto<PushNotificationResponseDto>> sendBreakingNews(
                        @RequestBody Map<String, Object> body,
                        HttpServletRequest httpRequest) {

                UUID newsId = UUID.fromString((String) body.get("newsId"));
                String title = (String) body.get("title");
                String bodyText = (String) body.get("body");
                String thumbnailUrl = (String) body.get("thumbnailUrl"); // From news entity newsThumbnailUrl

                log.info("AdminPushController: Send breaking newsapp - newsId={}, hasThumbnail={}",
                                newsId, thumbnailUrl != null);

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);

                try {
                        PushNotificationResponseDto response = notificationService.sendBreakingNews(newsId, title,
                                        bodyText, thumbnailUrl);

                        auditLogService.logAction(
                                        null,
                                        PushNotificationAuditLogService.ACTION_BREAKING_NEWS,
                                        adminId,
                                        true,
                                        null,
                                        clientInfo);

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponseDto.success("Breaking newsapp sent", response));
                } catch (Exception e) {
                        auditLogService.logFailure(
                                        PushNotificationAuditLogService.ACTION_BREAKING_NEWS,
                                        adminId,
                                        e.getMessage(),
                                        clientInfo);
                        throw e;
                }
        }

        // ========================================
        // Scheduled Notifications
        // ========================================

        @Operation(summary = "Schedule notification", description = """
                        Schedules a notification for future delivery.
                        Requires `scheduledAt` to be set in the request.
                        """)
        @PostMapping("/notifications/schedule")
        public ResponseEntity<ApiResponseDto<PushNotificationResponseDto>> scheduleNotification(
                        @Valid @RequestBody PushNotificationSendRequestDto request,
                        HttpServletRequest httpRequest) {

                log.info("AdminPushController: Schedule notification - type={}, scheduledAt={}",
                                request.getNotificationType(), request.getScheduledAt());

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);

                try {
                        PushNotificationResponseDto response = notificationService.scheduleNotification(request);

                        auditLogService.logAction(
                                        null,
                                        PushNotificationAuditLogService.ACTION_SCHEDULE,
                                        adminId,
                                        true,
                                        null,
                                        clientInfo);

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponseDto.success("Notification scheduled", response));
                } catch (Exception e) {
                        auditLogService.logFailure(
                                        PushNotificationAuditLogService.ACTION_SCHEDULE,
                                        adminId,
                                        e.getMessage(),
                                        clientInfo);
                        throw e;
                }
        }

        @Operation(summary = "Cancel scheduled notification", description = "Cancels a scheduled notification before it's sent.")
        @DeleteMapping("/notifications/{notificationId}/cancel")
        public ResponseEntity<ApiResponseDto<Void>> cancelNotification(
                        @Parameter(description = "Notification ID") @PathVariable UUID notificationId,
                        HttpServletRequest httpRequest) {

                log.info("AdminPushController: Cancel notification - notificationId={}", notificationId);

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);

                boolean cancelled = notificationService.cancelScheduledNotification(notificationId);

                // Log the cancel attempt
                auditLogService.logAction(
                                null,
                                PushNotificationAuditLogService.ACTION_CANCEL,
                                adminId,
                                cancelled,
                                cancelled ? null : "Notification not found or already sent",
                                clientInfo);

                if (cancelled) {
                        return ResponseEntity.ok(
                                        ApiResponseDto.success("Notification cancelled successfully", null));
                } else {
                        return ResponseEntity.badRequest().body(
                                        ApiResponseDto.error(
                                                        "Could not cancel notification (already sent or not found)"));
                }
        }

        // ========================================
        // Notification History
        // ========================================

        @Operation(summary = "Get notification by ID", description = "Retrieves details of a specific notification.")
        @GetMapping("/notifications/{notificationId}")
        public ResponseEntity<ApiResponseDto<PushNotificationResponseDto>> getNotification(
                        @Parameter(description = "Notification ID") @PathVariable UUID notificationId) {

                PushNotificationResponseDto response = notificationService.getNotification(notificationId);
                return ResponseEntity.ok(ApiResponseDto.success("Notification retrieved", response));
        }

        @Operation(summary = "Get recent notifications", description = "Returns paginated list of recent notifications.")
        @GetMapping("/notifications")
        public ResponseEntity<ApiResponseDto<Page<PushNotificationResponseDto>>> getRecentNotifications(
                        @PageableDefault(size = 20) Pageable pageable) {

                Page<PushNotificationResponseDto> notifications = notificationService.getRecentNotifications(pageable);
                return ResponseEntity.ok(ApiResponseDto.success("Notifications retrieved", notifications));
        }

        @Operation(summary = "Get notifications for newsapp newsapp", description = "Returns all notifications sent for a specific newsapp newsapp.")
        @GetMapping("/notifications/newsapp/{newsId}")
        public ResponseEntity<ApiResponseDto<Page<PushNotificationResponseDto>>> getNotificationsForNews(
                        @Parameter(description = "News ID") @PathVariable UUID newsId,
                        @PageableDefault(size = 10) Pageable pageable) {

                Page<PushNotificationResponseDto> notifications = notificationService.getNotificationsForNews(newsId,
                                pageable);
                return ResponseEntity.ok(ApiResponseDto.success("Notifications retrieved", notifications));
        }

        // ========================================
        // Analytics & Statistics
        // ========================================

        @Operation(summary = "Get notification statistics", description = """
                        Returns notification delivery statistics for a date range.
                        Includes total sent, delivered, failed, opened, and rates.
                        """)
        @GetMapping("/statistics")
        public ResponseEntity<ApiResponseDto<PushNotificationService.NotificationStatistics>> getStatistics(
                        @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                Instant start = startDate != null
                                ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC)
                                : Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
                Instant end = endDate != null
                                ? endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                                : Instant.now();

                PushNotificationService.NotificationStatistics stats = notificationService.getStatistics(start, end);
                return ResponseEntity.ok(ApiResponseDto.success("Statistics retrieved", stats));
        }

        @Operation(summary = "Get device statistics", description = """
                        Returns device registration statistics.
                        Includes counts by platform, language, and active status.
                        """)
        @GetMapping("/devices/statistics")
        public ResponseEntity<ApiResponseDto<PushDeviceService.DeviceStatistics>> getDeviceStatistics() {
                PushDeviceService.DeviceStatistics stats = deviceService.getDeviceStatistics();
                return ResponseEntity.ok(ApiResponseDto.success("Device statistics retrieved", stats));
        }

        // ========================================
        // Device Management
        // ========================================

        @Operation(summary = "Get devices for user", description = "Returns all devices registered for a specific user.")
        @GetMapping("/users/{userId}/devices")
        public ResponseEntity<ApiResponseDto<List<PushDeviceRegistrationResponseDto>>> getDevicesForUser(
                        @Parameter(description = "User ID") @PathVariable UUID userId) {

                List<PushDeviceRegistrationResponseDto> devices = deviceService.getDevicesForUser(userId);
                return ResponseEntity.ok(ApiResponseDto.success("Devices retrieved", devices));
        }

        @Operation(summary = "Cleanup stale devices", description = """
                        Deactivates devices that haven't been active for specified days.
                        Default: 90 days of inactivity.
                        """)
        @PostMapping("/devices/cleanup")
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> cleanupStaleDevices(
                        @Parameter(description = "Days of inactivity threshold") @RequestParam(defaultValue = "90") int daysInactive) {

                log.info("AdminPushController: Cleanup stale devices - daysInactive={}", daysInactive);

                int count = deviceService.cleanupStaleDevices(daysInactive);

                return ResponseEntity.ok(ApiResponseDto.success("Stale devices cleaned up", Map.of(
                                "devicesDeactivated", count)));
        }

        // ========================================
        // Maintenance
        // ========================================

        @Operation(summary = "Cleanup old notification records", description = "Deletes old notification delivery records for data retention.")
        @PostMapping("/notifications/cleanup")
        public ResponseEntity<ApiResponseDto<Map<String, Object>>> cleanupOldNotifications(
                        @Parameter(description = "Days to keep records") @RequestParam(defaultValue = "90") int daysToKeep) {

                log.info("AdminPushController: Cleanup old notifications - daysToKeep={}", daysToKeep);

                int count = notificationService.cleanupOldNotifications(daysToKeep);

                return ResponseEntity.ok(ApiResponseDto.success("Old notifications cleaned up", Map.of(
                                "recordsDeleted", count)));
        }

        @Operation(summary = "Retry failed notifications", description = "Manually triggers retry for failed notifications.")
        @PostMapping("/notifications/retry")
        public ResponseEntity<ApiResponseDto<Void>> retryFailedNotifications() {

                log.info("AdminPushController: Retry failed notifications");

                notificationService.retryFailedNotifications();

                return ResponseEntity.ok(ApiResponseDto.success("Retry process initiated", null));
        }

        @Operation(summary = "Process scheduled notifications", description = "Manually triggers processing of scheduled notifications.")
        @PostMapping("/notifications/process-scheduled")
        public ResponseEntity<ApiResponseDto<Void>> processScheduledNotifications() {

                log.info("AdminPushController: Process scheduled notifications");

                notificationService.processScheduledNotifications();

                return ResponseEntity.ok(ApiResponseDto.success("Scheduled notification processing initiated", null));
        }

        // ========================================
        // Monetization & Engagement Notifications
        // ========================================

        @Operation(summary = "Send promotional notification", description = """
                        Send promotional notification for monetization campaigns.
                        CRITICAL FOR REVENUE - Use for sponsored content, app promotions, offers.

                        **Parameters:**
                        - `title`: Notification title (max 65 chars)
                        - `body`: Notification body (max 200 chars)
                        - `thumbnailUrl`: Promotional banner image (1024x512 recommended)
                        - `clickActionUrl`: Where to navigate on click
                        - `segment`: Optional user segment to target (null for all)
                        - `campaignId`: Campaign ID for analytics tracking
                        """)
        @PostMapping("/notifications/promotional")
        public ResponseEntity<ApiResponseDto<PushNotificationResponseDto>> sendPromotional(
                        @RequestBody Map<String, Object> body,
                        HttpServletRequest httpRequest) {

                String title = (String) body.get("title");
                String bodyText = (String) body.get("body");
                String thumbnailUrl = (String) body.get("thumbnailUrl");
                String clickActionUrl = (String) body.get("clickActionUrl");
                String segment = (String) body.get("segment");
                String campaignId = (String) body.get("campaignId");

                log.info("AdminPushController: Send promotional - campaignId={}, segment={}, hasThumbnail={}",
                                campaignId, segment, thumbnailUrl != null);

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);

                try {
                        PushNotificationResponseDto response = notificationService.sendPromotional(
                                        title, bodyText, thumbnailUrl, clickActionUrl, segment, campaignId);

                        auditLogService.logAction(null, "PROMOTIONAL", adminId, true, null, clientInfo);

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponseDto.success("Promotional notification sent", response));
                } catch (Exception e) {
                        auditLogService.logFailure("PROMOTIONAL", adminId, e.getMessage(), clientInfo);
                        throw e;
                }
        }

        @Operation(summary = "Send daily digest notification", description = """
                        Send daily digest notification to all subscribed users.
                        Best sent at scheduled times (e.g., 8 AM local time).

                        **Parameters:**
                        - `title`: Digest title (e.g., "Today's Top Stories")
                        - `body`: Summary of today's highlights
                        - `featuredImageUrl`: Main banner image (1024x512)
                        - `newsItems`: Array of news items, each with:
                          - `newsId`: UUID of the news article
                          - `title`: News headline
                          - `thumbnailUrl`: Individual news thumbnail (clickable)
                          - `category`: Optional category name
                        """)
        @PostMapping("/notifications/daily-digest")
        public ResponseEntity<ApiResponseDto<PushNotificationResponseDto>> sendDailyDigest(
                        @RequestBody Map<String, Object> body,
                        HttpServletRequest httpRequest) {

                String title = (String) body.get("title");
                String bodyText = (String) body.get("body");
                String featuredImageUrl = (String) body.get("featuredImageUrl");

                // Parse news items from request (each with newsId, title, thumbnailUrl)
                List<DigestNewsItemDto> newsItems = parseNewsItems(body.get("newsItems"));

                log.info("AdminPushController: Send daily digest - newsCount={}, hasFeaturedImage={}",
                                newsItems.size(), featuredImageUrl != null);

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);

                try {
                        PushNotificationResponseDto response = notificationService.sendDailyDigest(
                                        title, bodyText, featuredImageUrl, newsItems);

                        auditLogService.logAction(null, "DAILY_DIGEST", adminId, true, null, clientInfo);

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponseDto.success("Daily digest sent", response));
                } catch (Exception e) {
                        auditLogService.logFailure("DAILY_DIGEST", adminId, e.getMessage(), clientInfo);
                        throw e;
                }
        }

        @Operation(summary = "Send weekly digest notification", description = """
                        Send weekly digest notification to all subscribed users.
                        Best sent on weekends (e.g., Sunday morning).

                        **Parameters:**
                        - `title`: Digest title (e.g., "This Week's Highlights")
                        - `body`: Summary of week's top stories
                        - `featuredImageUrl`: Main banner image (1024x512)
                        - `newsItems`: Array of news items, each with:
                          - `newsId`: UUID of the news article
                          - `title`: News headline
                          - `thumbnailUrl`: Individual news thumbnail (clickable)
                          - `category`: Optional category name
                        """)
        @PostMapping("/notifications/weekly-digest")
        public ResponseEntity<ApiResponseDto<PushNotificationResponseDto>> sendWeeklyDigest(
                        @RequestBody Map<String, Object> body,
                        HttpServletRequest httpRequest) {

                String title = (String) body.get("title");
                String bodyText = (String) body.get("body");
                String featuredImageUrl = (String) body.get("featuredImageUrl");

                // Parse news items from request (each with newsId, title, thumbnailUrl)
                List<DigestNewsItemDto> newsItems = parseNewsItems(body.get("newsItems"));

                log.info("AdminPushController: Send weekly digest - newsCount={}, hasFeaturedImage={}",
                                newsItems.size(), featuredImageUrl != null);

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);

                try {
                        PushNotificationResponseDto response = notificationService.sendWeeklyDigest(
                                        title, bodyText, featuredImageUrl, newsItems);

                        auditLogService.logAction(null, "WEEKLY_DIGEST", adminId, true, null, clientInfo);

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponseDto.success("Weekly digest sent", response));
                } catch (Exception e) {
                        auditLogService.logFailure("WEEKLY_DIGEST", adminId, e.getMessage(), clientInfo);
                        throw e;
                }
        }

        @Operation(summary = "Send trending notification", description = """
                        Send notification about trending/viral content.
                        Use when a story gains significant traction.

                        **Parameters:**
                        - `newsId`: The trending news article ID
                        - `title`: Notification title (e.g., "🔥 Trending Now")
                        - `body`: Brief description
                        - `thumbnailUrl`: News thumbnail (1024x512)
                        """)
        @PostMapping("/notifications/trending")
        public ResponseEntity<ApiResponseDto<PushNotificationResponseDto>> sendTrending(
                        @RequestBody Map<String, Object> body,
                        HttpServletRequest httpRequest) {

                UUID newsId = UUID.fromString((String) body.get("newsId"));
                String title = (String) body.get("title");
                String bodyText = (String) body.get("body");
                String thumbnailUrl = (String) body.get("thumbnailUrl");

                log.info("AdminPushController: Send trending - newsId={}, hasThumbnail={}",
                                newsId, thumbnailUrl != null);

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);

                try {
                        PushNotificationResponseDto response = notificationService.sendTrending(
                                        newsId, title, bodyText, thumbnailUrl);

                        auditLogService.logAction(null, "TRENDING", adminId, true, null, clientInfo);

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponseDto.success("Trending notification sent", response));
                } catch (Exception e) {
                        auditLogService.logFailure("TRENDING", adminId, e.getMessage(), clientInfo);
                        throw e;
                }
        }

        @Operation(summary = "Send system announcement", description = """
                        Send system-wide announcement to all users.
                        Use for app updates, maintenance notices, policy changes.

                        **Parameters:**
                        - `title`: Announcement title
                        - `body`: Announcement message
                        - `clickActionUrl`: Optional URL to navigate
                        """)
        @PostMapping("/notifications/system-announcement")
        public ResponseEntity<ApiResponseDto<PushNotificationResponseDto>> sendSystemAnnouncement(
                        @RequestBody Map<String, Object> body,
                        HttpServletRequest httpRequest) {

                String title = (String) body.get("title");
                String bodyText = (String) body.get("body");
                String clickActionUrl = (String) body.get("clickActionUrl");

                log.info("AdminPushController: Send system announcement - title='{}'", title);

                UUID adminId = SecurityContextUtils.getCurrentAdminId().orElse(null);
                RequestClientInfoDto clientInfo = requestInfoService.getClientInfo(httpRequest);

                try {
                        PushNotificationResponseDto response = notificationService.sendSystemAnnouncement(
                                        title, bodyText, clickActionUrl);

                        auditLogService.logAction(null, "SYSTEM_ANNOUNCEMENT", adminId, true, null, clientInfo);

                        return ResponseEntity.status(HttpStatus.CREATED)
                                        .body(ApiResponseDto.success("System announcement sent", response));
                } catch (Exception e) {
                        auditLogService.logFailure("SYSTEM_ANNOUNCEMENT", adminId, e.getMessage(), clientInfo);
                        throw e;
                }
        }

        // ========================================
        // Helper Methods
        // ========================================

        /**
         * Parse news items from request body for digest notifications.
         * Each item should have: newsId, title, thumbnailUrl, category (optional)
         * 
         * @param newsItemsObj the raw object from request body
         * @return list of DigestNewsItemDto
         */
        @SuppressWarnings("unchecked")
        private List<DigestNewsItemDto> parseNewsItems(Object newsItemsObj) {
                List<DigestNewsItemDto> newsItems = new ArrayList<>();

                if (newsItemsObj instanceof List<?> rawList) {
                        for (Object item : rawList) {
                                if (item instanceof Map<?, ?> itemMap) {
                                        Map<String, Object> map = (Map<String, Object>) itemMap;

                                        String newsIdStr = (String) map.get("newsId");
                                        String itemTitle = (String) map.get("title");
                                        String itemThumbnailUrl = (String) map.get("thumbnailUrl");
                                        String category = (String) map.get("category");
                                        String summary = (String) map.get("summary");

                                        if (newsIdStr != null && itemTitle != null) {
                                                newsItems.add(DigestNewsItemDto.builder()
                                                                .newsId(UUID.fromString(newsIdStr))
                                                                .title(itemTitle)
                                                                .thumbnailUrl(itemThumbnailUrl)
                                                                .category(category)
                                                                .summary(summary)
                                                                .build());
                                        }
                                }
                        }
                }

                return newsItems;
        }

}
