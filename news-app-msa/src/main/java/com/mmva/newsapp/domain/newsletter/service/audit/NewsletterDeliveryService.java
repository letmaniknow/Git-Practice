package com.mmva.newsapp.domain.newsletter.service.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mmva.newsapp.domain.newsletter.dto.analytics.NewsletterDeliveryAnalyticsDto;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterDeliveryStatus;
import com.mmva.newsapp.domain.newsletter.exception.core.NewsletterCampaignNotFoundException;
import com.mmva.newsapp.domain.newsletter.exception.core.NewsletterSubscriberNotFoundException;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for newsletter delivery management.
 *
 * <p>
 * Provides business logic for delivery tracking, email sending,
 * bounce handling, and delivery analytics.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsletterDeliveryService {

    // =========================
    // Delivery Operations
    // =========================

    /**
     * Records a successful delivery.
     *
     * @param campaignId        the campaign ID
     * @param subscriberId      the subscriber ID
     * @param deliveryTimestamp the delivery timestamp
     * @throws NewsletterCampaignNotFoundException   if campaign not found
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    void recordDelivery(Long campaignId, Long subscriberId, Instant deliveryTimestamp);

    /**
     * Records an email open event.
     *
     * @param campaignId    the campaign ID
     * @param subscriberId  the subscriber ID
     * @param openTimestamp the open timestamp
     * @param userAgent     the user agent string
     * @param ipAddress     the IP address
     * @throws NewsletterCampaignNotFoundException   if campaign not found
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    void recordOpen(Long campaignId, Long subscriberId, Instant openTimestamp, String userAgent, String ipAddress);

    /**
     * Records a click event.
     *
     * @param campaignId     the campaign ID
     * @param subscriberId   the subscriber ID
     * @param clickTimestamp the click timestamp
     * @param clickedUrl     the URL that was clicked
     * @param userAgent      the user agent string
     * @param ipAddress      the IP address
     * @throws NewsletterCampaignNotFoundException   if campaign not found
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    void recordClick(Long campaignId, Long subscriberId, Instant clickTimestamp, String clickedUrl, String userAgent,
            String ipAddress);

    /**
     * Records a bounce event.
     *
     * @param campaignId      the campaign ID
     * @param subscriberId    the subscriber ID
     * @param bounceTimestamp the bounce timestamp
     * @param bounceReason    the bounce reason
     * @throws NewsletterCampaignNotFoundException   if campaign not found
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    void recordBounce(Long campaignId, Long subscriberId, Instant bounceTimestamp, String bounceReason);

    /**
     * Records a delivery failure.
     *
     * @param campaignId       the campaign ID
     * @param subscriberId     the subscriber ID
     * @param failureTimestamp the failure timestamp
     * @param failureReason    the failure reason
     * @throws NewsletterCampaignNotFoundException   if campaign not found
     * @throws NewsletterSubscriberNotFoundException if subscriber not found
     */
    void recordFailure(Long campaignId, Long subscriberId, Instant failureTimestamp, String failureReason);

    // =========================
    // Query Operations
    // =========================

    /**
     * Gets delivery logs for a campaign.
     *
     * @param campaignId the campaign ID
     * @param pageable   pagination information
     * @return page of delivery analytics
     */
    Page<NewsletterDeliveryAnalyticsDto> getDeliveryLogsByCampaign(Long campaignId, Pageable pageable);

    /**
     * Gets delivery logs for a subscriber.
     *
     * @param subscriberId the subscriber ID
     * @param pageable     pagination information
     * @return page of delivery analytics
     */
    Page<NewsletterDeliveryAnalyticsDto> getDeliveryLogsBySubscriber(Long subscriberId, Pageable pageable);

    /**
     * Gets delivery logs by status.
     *
     * @param status   the delivery status
     * @param pageable pagination information
     * @return page of delivery analytics
     */
    Page<NewsletterDeliveryAnalyticsDto> getDeliveryLogsByStatus(NewsletterDeliveryStatus status, Pageable pageable);

    /**
     * Gets bounced deliveries for a campaign.
     *
     * @param campaignId the campaign ID
     * @return list of bounced delivery analytics
     */
    List<NewsletterDeliveryAnalyticsDto> getBouncedDeliveriesByCampaign(Long campaignId);

    /**
     * Gets failed deliveries within a time range.
     *
     * @param startTime the start timestamp
     * @param endTime   the end timestamp
     * @return list of failed delivery analytics
     */
    List<NewsletterDeliveryAnalyticsDto> getFailedDeliveriesInTimeRange(Instant startTime, Instant endTime);

    // =========================
    // Analytics & Statistics
    // =========================

    /**
     * Gets delivery analytics for a campaign.
     *
     * @param campaignId the campaign ID
     * @return delivery analytics
     */
    DeliveryAnalytics getDeliveryAnalytics(Long campaignId);

    /**
     * Gets overall delivery statistics.
     *
     * @return overall delivery statistics
     */
    OverallDeliveryStatistics getOverallDeliveryStatistics();

    /**
     * Gets delivery performance trends.
     *
     * @param days number of days to look back
     * @return list of performance data points
     */
    List<DeliveryPerformanceData> getDeliveryPerformanceTrends(int days);

    /**
     * Calculates delivery success rate for a campaign.
     *
     * @param campaignId the campaign ID
     * @return success rate as percentage (0-100)
     */
    double getDeliverySuccessRate(Long campaignId);

    /**
     * Gets top clicked URLs for a campaign.
     *
     * @param campaignId the campaign ID
     * @param limit      maximum number of URLs to return
     * @return list of URL click data
     */
    List<UrlClickData> getTopClickedUrls(Long campaignId, int limit);

    // =========================
    // Administrative Operations
    // =========================

    /**
     * Cleans up old delivery logs.
     *
     * @param beforeDate delete logs before this date
     * @return number of logs deleted
     */
    int cleanupOldDeliveryLogs(Instant beforeDate);

    /**
     * Updates delivery status.
     *
     * @param deliveryLogId the delivery log ID
     * @param newStatus     the new status
     * @param reason        optional reason for status change
     */
    void updateDeliveryStatus(Long deliveryLogId, NewsletterDeliveryStatus newStatus, String reason);

    // =========================
    // Data Transfer Objects
    // =========================

    /**
     * Delivery analytics data.
     */
    interface DeliveryAnalytics {
        long getTotalSent();

        long getTotalDelivered();

        long getTotalOpened();

        long getTotalClicked();

        long getTotalBounced();

        long getTotalFailed();

        double getDeliveryRate();

        double getOpenRate();

        double getClickRate();

        double getBounceRate();

        Instant getFirstDelivery();

        Instant getLastDelivery();
    }

    /**
     * Overall delivery statistics.
     */
    interface OverallDeliveryStatistics {
        long getTotalDeliveries();

        long getTotalDelivered();

        long getTotalOpened();

        long getTotalClicked();

        long getTotalBounced();

        long getTotalFailed();

        double getAverageDeliveryRate();

        double getAverageOpenRate();

        double getAverageClickRate();

        double getAverageBounceRate();
    }

    /**
     * Delivery performance data point.
     */
    interface DeliveryPerformanceData {
        String getDate();

        long getDeliveriesSent();

        long getDeliveriesDelivered();

        long getOpens();

        long getClicks();

        long getBounces();

        double getDeliveryRate();

        double getOpenRate();

        double getClickRate();
    }

    /**
     * URL click data.
     */
    interface UrlClickData {
        String getUrl();

        long getClickCount();

        double getClickRate();
    }
}