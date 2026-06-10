package com.mmva.newsapp.domain.newsletter.service.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mmva.newsapp.domain.newsletter.dto.analytics.NewsletterDeliveryAnalyticsDto;
import com.mmva.newsapp.domain.newsletter.enums.core.NewsletterDeliveryStatus;
import com.mmva.newsapp.domain.newsletter.exception.core.NewsletterCampaignNotFoundException;
import com.mmva.newsapp.domain.newsletter.exception.core.NewsletterSubscriberNotFoundException;
import com.mmva.newsapp.domain.newsletter.mapper.core.NewsletterMapper;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterCampaign;
import com.mmva.newsapp.domain.newsletter.model.audit.NewsletterDeliveryLog;
import com.mmva.newsapp.domain.newsletter.model.core.NewsletterSubscriber;
import com.mmva.newsapp.domain.newsletter.repository.core.NewsletterCampaignRepository;
import com.mmva.newsapp.domain.newsletter.repository.audit.NewsletterDeliveryLogRepository;
import com.mmva.newsapp.domain.newsletter.repository.core.NewsletterSubscriberRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of NewsletterDeliveryService.
 *
 * <p>
 * Provides business logic for newsletter delivery tracking
 * with transaction support and validation.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterDeliveryServiceImpl implements NewsletterDeliveryService {

    private final NewsletterDeliveryLogRepository deliveryLogRepository;
    private final NewsletterCampaignRepository campaignRepository;
    private final NewsletterSubscriberRepository subscriberRepository;
    private final NewsletterMapper mapper;

    // =========================
    // Delivery Operations
    // =========================

    @Override
    @Transactional
    public void recordDelivery(Long campaignId, Long subscriberId, Instant deliveryTimestamp) {
        log.debug("Recording delivery for campaign {} to subscriber {} at {}", campaignId, subscriberId,
                deliveryTimestamp);

        NewsletterCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NewsletterCampaignNotFoundException(
                        "Campaign not found with ID: " + campaignId));

        NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NewsletterSubscriberNotFoundException(
                        "Subscriber not found with ID: " + subscriberId));

        NewsletterDeliveryLog deliveryLog = new NewsletterDeliveryLog();
        deliveryLog.setNewsletterCampaign(campaign);
        deliveryLog.setNewsletterSubscriber(subscriber);
        deliveryLog.setNewsletterDeliveryLogStatus(NewsletterDeliveryStatus.DELIVERED);
        deliveryLog.setNewsletterDeliveryLogDeliveredAt(deliveryTimestamp);

        deliveryLogRepository.save(deliveryLog);
        log.info("Successfully recorded delivery for campaign {} to subscriber {}", campaignId, subscriberId);
    }

    @Override
    @Transactional
    public void recordOpen(Long campaignId, Long subscriberId, Instant openTimestamp, String userAgent,
            String ipAddress) {
        log.debug("Recording open for campaign {} by subscriber {} at {}", campaignId, subscriberId, openTimestamp);

        NewsletterCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NewsletterCampaignNotFoundException(
                        "Campaign not found with ID: " + campaignId));

        NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NewsletterSubscriberNotFoundException(
                        "Subscriber not found with ID: " + subscriberId));

        // Find existing delivery log
        NewsletterDeliveryLog deliveryLog = deliveryLogRepository
                .findByNewsletterCampaign_NewsletterCampaignIdAndNewsletterSubscriber_NewsletterSubscriberId(
                        campaignId, subscriberId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No delivery log found for campaign " + campaignId + " and subscriber " + subscriberId));

        // Update status if not already opened/clicked
        if (deliveryLog.getNewsletterDeliveryLogStatus() == NewsletterDeliveryStatus.DELIVERED ||
                deliveryLog.getNewsletterDeliveryLogStatus() == NewsletterDeliveryStatus.SENT) {
            deliveryLog.setNewsletterDeliveryLogStatus(NewsletterDeliveryStatus.OPENED);
        }

        deliveryLog.setNewsletterDeliveryLogOpenedAt(openTimestamp);
        deliveryLog.setNewsletterDeliveryLogUserAgent(userAgent);
        deliveryLog.setNewsletterDeliveryLogIpAddress(ipAddress);

        deliveryLogRepository.save(deliveryLog);
        log.info("Successfully recorded open for campaign {} by subscriber {}", campaignId, subscriberId);
    }

    @Override
    @Transactional
    public void recordClick(Long campaignId, Long subscriberId, Instant clickTimestamp, String clickedUrl,
            String userAgent, String ipAddress) {
        log.debug("Recording click for campaign {} by subscriber {} at {}", campaignId, subscriberId, clickTimestamp);

        NewsletterCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NewsletterCampaignNotFoundException(
                        "Campaign not found with ID: " + campaignId));

        NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NewsletterSubscriberNotFoundException(
                        "Subscriber not found with ID: " + subscriberId));

        // Find existing delivery log
        NewsletterDeliveryLog deliveryLog = deliveryLogRepository
                .findByNewsletterCampaign_NewsletterCampaignIdAndNewsletterSubscriber_NewsletterSubscriberId(
                        campaignId, subscriberId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No delivery log found for campaign " + campaignId + " and subscriber " + subscriberId));

        deliveryLog.setNewsletterDeliveryLogStatus(NewsletterDeliveryStatus.CLICKED);
        deliveryLog.setNewsletterDeliveryLogClickedAt(clickTimestamp);
        deliveryLog.setNewsletterDeliveryLogClickedUrl(clickedUrl);
        deliveryLog.setNewsletterDeliveryLogUserAgent(userAgent);
        deliveryLog.setNewsletterDeliveryLogIpAddress(ipAddress);

        deliveryLogRepository.save(deliveryLog);
        log.info("Successfully recorded click for campaign {} by subscriber {}", campaignId, subscriberId);
    }

    @Override
    @Transactional
    public void recordBounce(Long campaignId, Long subscriberId, Instant bounceTimestamp, String bounceReason) {
        log.debug("Recording bounce for campaign {} subscriber {} at {}", campaignId, subscriberId, bounceTimestamp);

        NewsletterCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NewsletterCampaignNotFoundException(
                        "Campaign not found with ID: " + campaignId));

        NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NewsletterSubscriberNotFoundException(
                        "Subscriber not found with ID: " + subscriberId));

        NewsletterDeliveryLog deliveryLog = new NewsletterDeliveryLog();
        deliveryLog.setNewsletterCampaign(campaign);
        deliveryLog.setNewsletterSubscriber(subscriber);
        deliveryLog.setNewsletterDeliveryLogStatus(NewsletterDeliveryStatus.BOUNCED);
        deliveryLog.setNewsletterDeliveryLogBouncedAt(bounceTimestamp);
        deliveryLog.setNewsletterDeliveryLogBounceReason(bounceReason);

        deliveryLogRepository.save(deliveryLog);
        log.warn("Recorded bounce for campaign {} subscriber {}: {}", campaignId, subscriberId, bounceReason);
    }

    @Override
    @Transactional
    public void recordFailure(Long campaignId, Long subscriberId, Instant failureTimestamp, String failureReason) {
        log.debug("Recording failure for campaign {} subscriber {} at {}", campaignId, subscriberId, failureTimestamp);

        NewsletterCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NewsletterCampaignNotFoundException(
                        "Campaign not found with ID: " + campaignId));

        NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new NewsletterSubscriberNotFoundException(
                        "Subscriber not found with ID: " + subscriberId));

        NewsletterDeliveryLog deliveryLog = new NewsletterDeliveryLog();
        deliveryLog.setNewsletterCampaign(campaign);
        deliveryLog.setNewsletterSubscriber(subscriber);
        deliveryLog.setNewsletterDeliveryLogStatus(NewsletterDeliveryStatus.FAILED);
        deliveryLog.setNewsletterDeliveryLogFailedAt(failureTimestamp);
        deliveryLog.setNewsletterDeliveryLogFailureReason(failureReason);

        deliveryLogRepository.save(deliveryLog);
        log.error("Recorded delivery failure for campaign {} subscriber {}: {}", campaignId, subscriberId,
                failureReason);
    }

    // =========================
    // Query Operations
    // =========================

    @Override
    public Page<NewsletterDeliveryAnalyticsDto> getDeliveryLogsByCampaign(Long campaignId, Pageable pageable) {
        log.debug("Fetching delivery logs for campaign: {}", campaignId);

        Page<NewsletterDeliveryLog> deliveryLogs = deliveryLogRepository
                .findByNewsletterCampaign_NewsletterCampaignId(campaignId, pageable);
        return deliveryLogs.map(mapper::toAnalyticsDto);
    }

    @Override
    public Page<NewsletterDeliveryAnalyticsDto> getDeliveryLogsBySubscriber(Long subscriberId, Pageable pageable) {
        log.debug("Fetching delivery logs for subscriber: {}", subscriberId);

        Page<NewsletterDeliveryLog> deliveryLogs = deliveryLogRepository
                .findByNewsletterSubscriber_NewsletterSubscriberId(subscriberId, pageable);
        return deliveryLogs.map(mapper::toAnalyticsDto);
    }

    @Override
    public Page<NewsletterDeliveryAnalyticsDto> getDeliveryLogsByStatus(NewsletterDeliveryStatus status,
            Pageable pageable) {
        log.debug("Fetching delivery logs by status: {}", status);

        Page<NewsletterDeliveryLog> deliveryLogs = deliveryLogRepository.findByNewsletterDeliveryLogStatus(status,
                pageable);
        return deliveryLogs.map(mapper::toAnalyticsDto);
    }

    @Override
    public List<NewsletterDeliveryAnalyticsDto> getBouncedDeliveriesByCampaign(Long campaignId) {
        log.debug("Fetching bounced deliveries for campaign: {}", campaignId);

        List<NewsletterDeliveryLog> bouncedLogs = deliveryLogRepository.findBouncedDeliveriesByCampaignId(campaignId);
        return bouncedLogs.stream()
                .map(mapper::toAnalyticsDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NewsletterDeliveryAnalyticsDto> getFailedDeliveriesInTimeRange(Instant startTime, Instant endTime) {
        log.debug("Fetching failed deliveries between {} and {}", startTime, endTime);

        List<NewsletterDeliveryLog> failedLogs = deliveryLogRepository
                .findByNewsletterDeliveryLogStatusAndCreatedAtBetween(
                        NewsletterDeliveryStatus.FAILED, startTime, endTime);
        return failedLogs.stream()
                .map(mapper::toAnalyticsDto)
                .collect(Collectors.toList());
    }

    // =========================
    // Analytics & Statistics
    // =========================

    @Override
    public DeliveryAnalytics getDeliveryAnalytics(Long campaignId) {
        log.debug("Calculating delivery analytics for campaign: {}", campaignId);

        // Get statistics from repository
        Object[] stats = deliveryLogRepository.getDeliveryStatisticsByCampaignId(campaignId);
        long total = ((Number) stats[0]).longValue();
        long sent = ((Number) stats[1]).longValue();
        long delivered = ((Number) stats[2]).longValue();
        long opened = ((Number) stats[3]).longValue();
        long clicked = ((Number) stats[4]).longValue();
        long bounced = ((Number) stats[5]).longValue();
        long failed = ((Number) stats[6]).longValue();

        double deliveryRate = sent > 0 ? (double) delivered / sent * 100 : 0;
        double openRate = total > 0 ? (double) opened / total * 100 : 0;
        double clickRate = total > 0 ? (double) clicked / total * 100 : 0;
        double bounceRate = sent > 0 ? (double) bounced / sent * 100 : 0;

        // Get time range
        Instant firstDelivery = deliveryLogRepository
                .findByNewsletterCampaign_NewsletterCampaignId(campaignId, Pageable.ofSize(1))
                .stream()
                .findFirst()
                .map(NewsletterDeliveryLog::getCreatedAt)
                .orElse(null);

        Instant lastDelivery = deliveryLogRepository.findByNewsletterCampaign_NewsletterCampaignId(
                campaignId, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .findFirst()
                .map(NewsletterDeliveryLog::getCreatedAt)
                .orElse(null);

        return new DeliveryAnalytics() {
            @Override
            public long getTotalSent() {
                return sent;
            }

            @Override
            public long getTotalDelivered() {
                return delivered;
            }

            @Override
            public long getTotalOpened() {
                return opened;
            }

            @Override
            public long getTotalClicked() {
                return clicked;
            }

            @Override
            public long getTotalBounced() {
                return bounced;
            }

            @Override
            public long getTotalFailed() {
                return failed;
            }

            @Override
            public double getDeliveryRate() {
                return deliveryRate;
            }

            @Override
            public double getOpenRate() {
                return openRate;
            }

            @Override
            public double getClickRate() {
                return clickRate;
            }

            @Override
            public double getBounceRate() {
                return bounceRate;
            }

            @Override
            public Instant getFirstDelivery() {
                return firstDelivery;
            }

            @Override
            public Instant getLastDelivery() {
                return lastDelivery;
            }
        };
    }

    @Override
    public OverallDeliveryStatistics getOverallDeliveryStatistics() {
        log.debug("Calculating overall delivery statistics");

        // This would require aggregating across all campaigns
        // For now, return placeholder values
        return new OverallDeliveryStatistics() {
            @Override
            public long getTotalDeliveries() {
                return 0;
            }

            @Override
            public long getTotalDelivered() {
                return 0;
            }

            @Override
            public long getTotalOpened() {
                return 0;
            }

            @Override
            public long getTotalClicked() {
                return 0;
            }

            @Override
            public long getTotalBounced() {
                return 0;
            }

            @Override
            public long getTotalFailed() {
                return 0;
            }

            @Override
            public double getAverageDeliveryRate() {
                return 0;
            }

            @Override
            public double getAverageOpenRate() {
                return 0;
            }

            @Override
            public double getAverageClickRate() {
                return 0;
            }

            @Override
            public double getAverageBounceRate() {
                return 0;
            }
        };
    }

    @Override
    public List<DeliveryPerformanceData> getDeliveryPerformanceTrends(int days) {
        log.debug("Calculating delivery performance trends for last {} days", days);

        // This would require custom queries to aggregate by date
        return List.of();
    }

    @Override
    public double getDeliverySuccessRate(Long campaignId) {
        log.debug("Calculating delivery success rate for campaign: {}", campaignId);

        Object[] stats = deliveryLogRepository.getDeliveryStatisticsByCampaignId(campaignId);
        long sent = ((Number) stats[1]).longValue();
        long delivered = ((Number) stats[2]).longValue();

        return sent > 0 ? (double) delivered / sent * 100 : 0;
    }

    @Override
    public List<UrlClickData> getTopClickedUrls(Long campaignId, int limit) {
        log.debug("Getting top {} clicked URLs for campaign: {}", limit, campaignId);

        // This would require a custom query to aggregate clicks by URL
        return List.of();
    }

    // =========================
    // Administrative Operations
    // =========================

    @Override
    @Transactional
    public int cleanupOldDeliveryLogs(Instant beforeDate) {
        log.info("Cleaning up delivery logs before: {}", beforeDate);

        // This would require a custom delete query
        // For now, return 0
        return 0;
    }

    @Override
    @Transactional
    public void updateDeliveryStatus(Long deliveryLogId, NewsletterDeliveryStatus newStatus, String reason) {
        log.info("Updating delivery status for log {} to {}: {}", deliveryLogId, newStatus, reason);

        NewsletterDeliveryLog deliveryLog = deliveryLogRepository.findById(deliveryLogId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery log not found with ID: " + deliveryLogId));

        deliveryLog.setNewsletterDeliveryLogStatus(newStatus);

        // Update reason fields based on status
        Instant now = Instant.now();
        switch (newStatus) {
            case BOUNCED:
                deliveryLog.setNewsletterDeliveryLogBouncedAt(now);
                deliveryLog.setNewsletterDeliveryLogBounceReason(reason);
                break;
            case FAILED:
                deliveryLog.setNewsletterDeliveryLogFailedAt(now);
                deliveryLog.setNewsletterDeliveryLogFailureReason(reason);
                break;
            default:
                break;
        }

        deliveryLogRepository.save(deliveryLog);
        log.info("Successfully updated delivery status for log {}", deliveryLogId);
    }
}