package com.mmva.newsapp.controller.admin.newsletter;

import com.mmva.newsapp.domain.newsletter.dto.analytics.NewsletterDeliveryAnalyticsDto;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterDeliveryStatus;
import com.mmva.newsapp.domain.newsletter.service.audit.NewsletterDeliveryService;
import static com.mmva.newsapp.domain.newsletter.service.audit.NewsletterDeliveryService.DeliveryAnalytics;
import static com.mmva.newsapp.domain.newsletter.service.audit.NewsletterDeliveryService.UrlClickData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller for newsletter delivery tracking operations.
 *
 * <p>
 * Provides endpoints for delivery tracking, email engagement monitoring,
 * and delivery analytics. Includes tracking pixels and webhook endpoints.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/newsletter/delivery")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Newsletter Delivery API", description = "Newsletter delivery tracking and analytics operations")
public class NewsletterDeliveryController {

    private final NewsletterDeliveryService deliveryService;

    // =========================
    // Tracking Endpoints (Public)
    // =========================

    /**
     * Records an email open event (tracking pixel endpoint).
     * This endpoint is typically called by email clients when loading tracking
     * pixels.
     *
     * @param campaignId   the campaign ID
     * @param subscriberId the subscriber ID
     * @param userAgent    the user agent string
     * @param ipAddress    the IP address
     * @return 1x1 transparent pixel
     */
    @GetMapping("/track/open/{campaignId}/{subscriberId}")
    @Operation(summary = "Track email open", description = "Records an email open event (tracking pixel)")
    public ResponseEntity<byte[]> trackEmailOpen(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Subscriber ID", example = "123") @PathVariable Long subscriberId,
            @Parameter(description = "User agent", example = "Mozilla/5.0...") @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @Parameter(description = "IP address", example = "192.168.1.1") @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {

        log.debug("Email open tracking - campaignId: {}, subscriberId: {}", campaignId, subscriberId);

        try {
            deliveryService.recordOpen(campaignId, subscriberId, Instant.now(), userAgent,
                    ipAddress != null ? ipAddress : "unknown");

            // Return 1x1 transparent GIF pixel
            byte[] pixel = new byte[] {
                    (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x39, (byte) 0x61, // GIF89a
                    (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x80, (byte) 0x00, // Logical screen
                                                                                                  // descriptor
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // Global color table
                    (byte) 0x2C, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, // Image descriptor
                    (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x02, // Image data
                    (byte) 0x44, (byte) 0x01, (byte) 0x00, (byte) 0x3B // GIF trailer
            };

            return ResponseEntity.ok()
                    .header("Content-Type", "image/gif")
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .body(pixel);

        } catch (Exception e) {
            log.warn("Failed to record email open - campaignId: {}, subscriberId: {}, error: {}", campaignId,
                    subscriberId, e.getMessage());
            // Still return pixel even if tracking fails
            return ResponseEntity.ok(new byte[0]);
        }
    }

    /**
     * Records a link click event.
     * This endpoint should be used to wrap newsletter links for click tracking.
     *
     * @param campaignId   the campaign ID
     * @param subscriberId the subscriber ID
     * @param clickedUrl   the URL that was clicked (URL-encoded)
     * @param userAgent    the user agent string
     * @param ipAddress    the IP address
     * @return redirect to the actual URL
     */
    @GetMapping("/track/click/{campaignId}/{subscriberId}")
    @Operation(summary = "Track link click", description = "Records a link click event and redirects to the target URL")
    public ResponseEntity<Void> trackLinkClick(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Subscriber ID", example = "123") @PathVariable Long subscriberId,
            @Parameter(description = "Clicked URL", example = "https://example.com/article") @RequestParam String url,
            @Parameter(description = "User agent", example = "Mozilla/5.0...") @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @Parameter(description = "IP address", example = "192.168.1.1") @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {

        log.debug("Link click tracking - campaignId: {}, subscriberId: {}, url: {}", campaignId, subscriberId, url);

        try {
            deliveryService.recordClick(campaignId, subscriberId, Instant.now(), url, userAgent,
                    ipAddress != null ? ipAddress : "unknown");
        } catch (Exception e) {
            log.warn("Failed to record link click - campaignId: {}, subscriberId: {}, url: {}, error: {}", campaignId,
                    subscriberId, url, e.getMessage());
        }

        // Redirect to the actual URL
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", url)
                .build();
    }

    /**
     * Webhook endpoint for bounce notifications.
     *
     * @param campaignId   the campaign ID
     * @param subscriberId the subscriber ID
     * @param bounceReason the bounce reason
     * @return success response
     */
    @PostMapping("/webhook/bounce/{campaignId}/{subscriberId}")
    @Operation(summary = "Record bounce", description = "Records a bounce event via webhook")
    public ResponseEntity<Void> recordBounce(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Subscriber ID", example = "123") @PathVariable Long subscriberId,
            @Parameter(description = "Bounce reason", example = "Mailbox full") @RequestParam String reason) {

        log.debug("Bounce webhook - campaignId: {}, subscriberId: {}, reason: {}", campaignId, subscriberId, reason);

        deliveryService.recordBounce(campaignId, subscriberId, Instant.now(), reason);
        return ResponseEntity.ok().build();
    }

    /**
     * Webhook endpoint for delivery failure notifications.
     *
     * @param campaignId    the campaign ID
     * @param subscriberId  the subscriber ID
     * @param failureReason the failure reason
     * @return success response
     */
    @PostMapping("/webhook/failure/{campaignId}/{subscriberId}")
    @Operation(summary = "Record delivery failure", description = "Records a delivery failure event via webhook")
    public ResponseEntity<Void> recordDeliveryFailure(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Subscriber ID", example = "123") @PathVariable Long subscriberId,
            @Parameter(description = "Failure reason", example = "Invalid email address") @RequestParam String reason) {

        log.debug("Delivery failure webhook - campaignId: {}, subscriberId: {}, reason: {}", campaignId, subscriberId,
                reason);

        deliveryService.recordFailure(campaignId, subscriberId, Instant.now(), reason);
        return ResponseEntity.ok().build();
    }

    // =========================
    // Analytics Endpoints (Admin)
    // =========================

    /**
     * Gets delivery logs for a specific campaign.
     *
     * @param campaignId the campaign ID
     * @param page       page number
     * @param size       page size
     * @return page of delivery analytics
     */
    @GetMapping("/campaigns/{campaignId}/logs")
    @Operation(summary = "Get campaign delivery logs", description = "Retrieves delivery logs for a specific campaign")
    public ResponseEntity<Page<NewsletterDeliveryAnalyticsDto>> getDeliveryLogsByCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get delivery logs by campaign request - campaignId: {}, page: {}, size: {}", campaignId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterDeliveryAnalyticsDto> logs = deliveryService.getDeliveryLogsByCampaign(campaignId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Gets delivery logs for a specific subscriber.
     *
     * @param subscriberId the subscriber ID
     * @param page         page number
     * @param size         page size
     * @return page of delivery analytics
     */
    @GetMapping("/subscribers/{subscriberId}/logs")
    @Operation(summary = "Get subscriber delivery logs", description = "Retrieves delivery logs for a specific subscriber")
    public ResponseEntity<Page<NewsletterDeliveryAnalyticsDto>> getDeliveryLogsBySubscriber(
            @Parameter(description = "Subscriber ID", example = "123") @PathVariable Long subscriberId,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get delivery logs by subscriber request - subscriberId: {}, page: {}, size: {}", subscriberId, page,
                size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterDeliveryAnalyticsDto> logs = deliveryService.getDeliveryLogsBySubscriber(subscriberId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Gets delivery logs by status.
     *
     * @param status the delivery status
     * @param page   page number
     * @param size   page size
     * @return page of delivery analytics
     */
    @GetMapping("/logs/status/{status}")
    @Operation(summary = "Get delivery logs by status", description = "Retrieves delivery logs filtered by status")
    public ResponseEntity<Page<NewsletterDeliveryAnalyticsDto>> getDeliveryLogsByStatus(
            @Parameter(description = "Delivery status", example = "DELIVERED") @PathVariable NewsletterDeliveryStatus status,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.debug("Get delivery logs by status request - status: {}, page: {}, size: {}", status, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsletterDeliveryAnalyticsDto> logs = deliveryService.getDeliveryLogsByStatus(status, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * Gets bounced deliveries for a campaign.
     *
     * @param campaignId the campaign ID
     * @return list of bounced delivery analytics
     */
    @GetMapping("/campaigns/{campaignId}/bounced")
    @Operation(summary = "Get bounced deliveries", description = "Retrieves bounced delivery logs for a campaign")
    public ResponseEntity<List<NewsletterDeliveryAnalyticsDto>> getBouncedDeliveriesByCampaign(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.debug("Get bounced deliveries by campaign request - campaignId: {}", campaignId);

        List<NewsletterDeliveryAnalyticsDto> bounced = deliveryService.getBouncedDeliveriesByCampaign(campaignId);
        return ResponseEntity.ok(bounced);
    }

    /**
     * Gets failed deliveries within a time range.
     *
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @return list of failed delivery analytics
     */
    @GetMapping("/logs/failed")
    @Operation(summary = "Get failed deliveries", description = "Retrieves failed delivery logs within a time range")
    public ResponseEntity<List<NewsletterDeliveryAnalyticsDto>> getFailedDeliveriesInTimeRange(
            @Parameter(description = "Start time", example = "2024-01-01T00:00:00Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @Parameter(description = "End time", example = "2024-12-31T23:59:59Z") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {

        log.debug("Get failed deliveries in time range request - startTime: {}, endTime: {}", startTime, endTime);

        List<NewsletterDeliveryAnalyticsDto> failed = deliveryService.getFailedDeliveriesInTimeRange(startTime,
                endTime);
        return ResponseEntity.ok(failed);
    }

    /**
     * Gets delivery analytics for a campaign.
     *
     * @param campaignId the campaign ID
     * @return delivery analytics
     */
    @GetMapping("/campaigns/{campaignId}/analytics")
    @Operation(summary = "Get delivery analytics", description = "Retrieves delivery analytics for a specific campaign")
    public ResponseEntity<DeliveryAnalytics> getDeliveryAnalytics(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId) {

        log.debug("Get delivery analytics request for campaignId: {}", campaignId);

        DeliveryAnalytics analytics = deliveryService.getDeliveryAnalytics(campaignId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Gets top clicked URLs for a campaign.
     *
     * @param campaignId the campaign ID
     * @param limit      maximum number of results
     * @return list of URL click data
     */
    @GetMapping("/campaigns/{campaignId}/urls/top-clicked")
    @Operation(summary = "Get top clicked URLs", description = "Retrieves top clicked URLs for a campaign")
    public ResponseEntity<List<UrlClickData>> getTopClickedUrls(
            @Parameter(description = "Campaign ID", example = "1") @PathVariable Long campaignId,
            @Parameter(description = "Maximum results", example = "10") @RequestParam(defaultValue = "10") int limit) {

        log.debug("Get top clicked URLs request - campaignId: {}, limit: {}", campaignId, limit);

        List<UrlClickData> urls = deliveryService.getTopClickedUrls(campaignId, limit);
        return ResponseEntity.ok(urls);
    }
}