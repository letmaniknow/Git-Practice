package com.mmva.newsapp.infrastructure.push.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmva.newsapp.infrastructure.push.dto.DigestNewsItemDto;
import com.mmva.newsapp.infrastructure.push.dto.PushNotificationResponseDto;
import com.mmva.newsapp.infrastructure.push.dto.PushNotificationSendRequestDto;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.infrastructure.push.enums.*;
import com.mmva.newsapp.infrastructure.push.mapper.PushNotificationMapper;
import com.mmva.newsapp.infrastructure.push.model.PushDevice;
import com.mmva.newsapp.infrastructure.push.model.PushNotificationDelivery;
import com.mmva.newsapp.infrastructure.push.model.PushNotification;
import com.mmva.newsapp.infrastructure.push.repository.PushDeviceRepository;
import com.mmva.newsapp.infrastructure.push.repository.PushNotificationDeliveryRepository;
import com.mmva.newsapp.infrastructure.push.repository.PushNotificationRepository;
import com.mmva.newsapp.infrastructure.push.repository.PushTopicSubscriptionRepository;
import com.mmva.newsapp.domain.appuser.repository.core.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Push notification service implementation.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationServiceImpl implements PushNotificationService {

    private static final int BATCH_SIZE = 500;
    private static final int MAX_RETRIES = 3;

    private final PushNotificationRepository notificationRepository;
    private final PushNotificationDeliveryRepository deliveryRepository;
    private final PushDeviceRepository deviceRepository;
    private final PushTopicSubscriptionRepository subscriptionRepository;
    private final AppUserRepository userRepository;
    private final PushFcmService fcmService;
    private final ObjectMapper objectMapper;
    private final PushNotificationMapper notificationMapper;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ========================================
    // Send Notifications
    // ========================================

    @Override
    @Transactional
    public PushNotificationResponseDto sendNotification(PushNotificationSendRequestDto request) {
        log.info("PushNotificationService: Sending notification - type={}, targetType={}",
                request.getNotificationType(), request.getTargetType());

        // Check idempotency
        if (request.getIdempotencyKey() != null) {
            Optional<PushNotification> existing = notificationRepository
                    .findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("PushNotificationService: Duplicate notification skipped - idempotencyKey={}",
                        request.getIdempotencyKey());
                return notificationMapper.toResponse(existing.get());
            }
        }

        // Build notification entity
        PushNotification notification = buildNotificationEntity(request);

        // Handle scheduled vs immediate
        if (request.getScheduledAt() != null && request.getScheduledAt().isAfter(Instant.now())) {
            notification.setStatus(PushNotificationStatus.SCHEDULED);
            notification = notificationRepository.save(notification);
            log.info("PushNotificationService: Notification scheduled - notificationId={}, scheduledAt={}",
                    notification.getNotificationId(), notification.getScheduledAt());
            return notificationMapper.toResponse(notification);
        }

        notification = notificationRepository.save(notification);

        // Send asynchronously for large audiences
        sendNotificationAsync(notification, request);

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public PushNotificationResponseDto sendBreakingNews(UUID newsId, String title, String body, String thumbnailUrl) {
        log.info("PushNotificationService: Sending breaking newsapp - newsId={}, hasThumbnail={}",
                newsId, thumbnailUrl != null);

        PushNotificationSendRequestDto request = PushNotificationSendRequestDto.builder()
                .notificationType(PushNotificationType.BREAKING_NEWS)
                .title(title)
                .body(body)
                .imageUrl(thumbnailUrl) // Maps to FCM imageUrl - from news entity newsThumbnailUrl
                .targetType(PushNotificationTargetType.TOPIC)
                .targetValues(List.of("breaking_news"))
                .newsId(newsId)
                .priority(PushNotificationPriority.HIGH)
                .clickActionUrl(baseUrl + "/newsapp/" + newsId)
                .androidChannelId("breaking_news")
                .sound("breaking_news.mp3")
                .idempotencyKey("breaking_news_" + newsId)
                .build();

        return sendNotification(request);
    }

    @Override
    @Transactional
    public PushNotificationResponseDto sendCategoryUpdate(UUID categoryId, String categorySlug, UUID newsId,
            String title, String body, String thumbnailUrl) {
        log.info(
                "PushNotificationService: Sending newscategory update - categoryId={}, categorySlug={}, hasThumbnail={}",
                categoryId, categorySlug, thumbnailUrl != null);

        PushNotificationSendRequestDto request = PushNotificationSendRequestDto.builder()
                .notificationType(PushNotificationType.CATEGORY_UPDATE)
                .title(title)
                .body(body)
                .imageUrl(thumbnailUrl) // Maps to FCM imageUrl - from news entity newsThumbnailUrl
                .targetType(PushNotificationTargetType.TOPIC)
                .targetValues(List.of("category_" + categorySlug))
                .newsId(newsId)
                .categoryId(categoryId)
                .priority(PushNotificationPriority.NORMAL)
                .clickActionUrl(baseUrl + "/newsapp/" + newsId)
                .idempotencyKey("category_update_" + newsId)
                .build();

        return sendNotification(request);
    }

    @Override
    @Transactional
    public PushNotificationResponseDto sendToUser(UUID userId, String title, String body, String thumbnailUrl) {
        log.info("PushNotificationService: Sending personalized notification to user - userId={}, hasImage={}",
                userId, thumbnailUrl != null);

        PushNotificationSendRequestDto request = PushNotificationSendRequestDto.builder()
                .notificationType(PushNotificationType.PERSONALIZED)
                .title(title)
                .body(body)
                .imageUrl(thumbnailUrl) // Maps to FCM imageUrl - personalized content with image
                .targetType(PushNotificationTargetType.USER)
                .targetValues(List.of(userId.toString()))
                .priority(PushNotificationPriority.NORMAL)
                .idempotencyKey("personalized_" + userId + "_" + System.currentTimeMillis())
                .build();

        return sendNotification(request);
    }

    @Override
    @Transactional
    public PushNotificationResponseDto sendNewsUpdate(UUID newsId, String categorySlug, String title, String body,
            String thumbnailUrl) {
        log.info("PushNotificationService: Sending news update - newsId={}, categorySlug={}, hasImage={}",
                newsId, categorySlug, thumbnailUrl != null);

        PushNotificationSendRequestDto request = PushNotificationSendRequestDto.builder()
                .notificationType(PushNotificationType.NEWS_UPDATE)
                .title(title)
                .body(body)
                .imageUrl(thumbnailUrl) // Maps to FCM imageUrl - from news entity newsThumbnailUrl
                .targetType(PushNotificationTargetType.TOPIC)
                .targetValues(List.of("category_" + categorySlug, "all_news"))
                .newsId(newsId)
                .priority(PushNotificationPriority.NORMAL)
                .clickActionUrl(baseUrl + "/newsapp/" + newsId)
                .idempotencyKey("news_update_" + newsId)
                .build();

        return sendNotification(request);
    }

    @Override
    @Transactional
    public PushNotificationResponseDto sendDailyDigest(String title, String body, String featuredImageUrl,
            List<DigestNewsItemDto> newsItems) {
        log.info("PushNotificationService: Sending daily digest - newsCount={}, hasImage={}",
                newsItems != null ? newsItems.size() : 0, featuredImageUrl != null);

        // First news item used for primary deep link when notification banner is tapped
        UUID primaryNewsId = (newsItems != null && !newsItems.isEmpty()) ? newsItems.get(0).getNewsId() : null;

        // Build click URL - goes to digest page or specific news
        String clickUrl = primaryNewsId != null
                ? baseUrl + "/newsapp/" + primaryNewsId
                : baseUrl + "/digest/daily";

        // Generate unique key for today's digest
        String today = java.time.LocalDate.now().toString();

        // Serialize news items to JSON for client-side digest rendering
        // Each item: {newsId, title, thumbnailUrl, category, summary}
        Map<String, String> customData = new HashMap<>();
        if (newsItems != null && !newsItems.isEmpty()) {
            try {
                // Convert to JSON array for mobile app to parse
                StringBuilder itemsJson = new StringBuilder("[");
                for (int i = 0; i < newsItems.size(); i++) {
                    DigestNewsItemDto item = newsItems.get(i);
                    if (i > 0)
                        itemsJson.append(",");
                    itemsJson.append("{")
                            .append("\"newsId\":\"").append(item.getNewsId()).append("\",")
                            .append("\"title\":\"").append(escapeJson(item.getTitle())).append("\",")
                            .append("\"thumbnailUrl\":\"")
                            .append(item.getThumbnailUrl() != null ? item.getThumbnailUrl() : "").append("\",")
                            .append("\"category\":\"").append(item.getCategory() != null ? item.getCategory() : "")
                            .append("\",")
                            .append("\"clickUrl\":\"").append(baseUrl).append("/newsapp/").append(item.getNewsId())
                            .append("\"")
                            .append("}");
                }
                itemsJson.append("]");
                customData.put("newsItems", itemsJson.toString());
            } catch (Exception e) {
                log.warn("Failed to serialize news items for digest", e);
            }
            customData.put("newsCount", String.valueOf(newsItems.size()));
        }
        customData.put("digestType", "daily");

        PushNotificationSendRequestDto request = PushNotificationSendRequestDto.builder()
                .notificationType(PushNotificationType.DAILY_DIGEST)
                .title(title)
                .body(body)
                .imageUrl(featuredImageUrl) // Featured story image for notification banner
                .targetType(PushNotificationTargetType.TOPIC)
                .targetValues(List.of("daily_digest", "all_news"))
                .newsId(primaryNewsId)
                .data(customData)
                .priority(PushNotificationPriority.NORMAL)
                .clickActionUrl(clickUrl)
                .androidChannelId("digest")
                .idempotencyKey("daily_digest_" + today)
                .build();

        return sendNotification(request);
    }

    /**
     * Escape special characters for JSON string values.
     */
    private String escapeJson(String value) {
        if (value == null)
            return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    @Transactional
    public PushNotificationResponseDto sendWeeklyDigest(String title, String body, String featuredImageUrl,
            List<DigestNewsItemDto> newsItems) {
        log.info("PushNotificationService: Sending weekly digest - newsCount={}, hasImage={}",
                newsItems != null ? newsItems.size() : 0, featuredImageUrl != null);

        // First news item used for primary deep link when notification banner is tapped
        UUID primaryNewsId = (newsItems != null && !newsItems.isEmpty()) ? newsItems.get(0).getNewsId() : null;

        // Build click URL - goes to digest page or specific news
        String clickUrl = primaryNewsId != null
                ? baseUrl + "/newsapp/" + primaryNewsId
                : baseUrl + "/digest/weekly";

        // Generate unique key for this week's digest
        String week = java.time.LocalDate.now().toString();

        // Serialize news items to JSON for client-side digest rendering
        // Each item: {newsId, title, thumbnailUrl, category, clickUrl}
        Map<String, String> customData = new HashMap<>();
        if (newsItems != null && !newsItems.isEmpty()) {
            try {
                // Convert to JSON array for mobile app to parse
                StringBuilder itemsJson = new StringBuilder("[");
                for (int i = 0; i < newsItems.size(); i++) {
                    DigestNewsItemDto item = newsItems.get(i);
                    if (i > 0)
                        itemsJson.append(",");
                    itemsJson.append("{")
                            .append("\"newsId\":\"").append(item.getNewsId()).append("\",")
                            .append("\"title\":\"").append(escapeJson(item.getTitle())).append("\",")
                            .append("\"thumbnailUrl\":\"")
                            .append(item.getThumbnailUrl() != null ? item.getThumbnailUrl() : "").append("\",")
                            .append("\"category\":\"").append(item.getCategory() != null ? item.getCategory() : "")
                            .append("\",")
                            .append("\"clickUrl\":\"").append(baseUrl).append("/newsapp/").append(item.getNewsId())
                            .append("\"")
                            .append("}");
                }
                itemsJson.append("]");
                customData.put("newsItems", itemsJson.toString());
            } catch (Exception e) {
                log.warn("Failed to serialize news items for digest", e);
            }
            customData.put("newsCount", String.valueOf(newsItems.size()));
        }
        customData.put("digestType", "weekly");

        PushNotificationSendRequestDto request = PushNotificationSendRequestDto.builder()
                .notificationType(PushNotificationType.WEEKLY_DIGEST)
                .title(title)
                .body(body)
                .imageUrl(featuredImageUrl) // Week's highlight image for notification banner
                .targetType(PushNotificationTargetType.TOPIC)
                .targetValues(List.of("weekly_digest", "all_news"))
                .newsId(primaryNewsId)
                .data(customData)
                .priority(PushNotificationPriority.NORMAL)
                .clickActionUrl(clickUrl)
                .androidChannelId("digest")
                .idempotencyKey("weekly_digest_" + week)
                .build();

        return sendNotification(request);
    }

    @Override
    @Transactional
    public PushNotificationResponseDto sendPromotional(String title, String body, String thumbnailUrl,
            String clickActionUrl, String segment, String campaignId) {
        log.info("PushNotificationService: Sending promotional notification - segment={}, campaignId={}, hasImage={}",
                segment, campaignId, thumbnailUrl != null);

        // Build custom data for campaign tracking
        Map<String, String> customData = new HashMap<>();
        if (campaignId != null) {
            customData.put("campaignId", campaignId);
        }
        customData.put("type", "promotional");

        PushNotificationSendRequestDto.PushNotificationSendRequestDtoBuilder builder = PushNotificationSendRequestDto
                .builder()
                .notificationType(PushNotificationType.PROMOTIONAL)
                .title(title)
                .body(body)
                .imageUrl(thumbnailUrl) // Promotional banner image
                .priority(PushNotificationPriority.NORMAL)
                .clickActionUrl(clickActionUrl != null ? clickActionUrl : baseUrl + "/promotions")
                .androidChannelId("promotions")
                .data(customData)
                .idempotencyKey("promotional_" + (campaignId != null ? campaignId : System.currentTimeMillis()));

        // Target by segment or all users
        if (segment != null && !segment.isEmpty()) {
            builder.targetType(PushNotificationTargetType.SEGMENT)
                    .targetValues(List.of(segment));
        } else {
            builder.targetType(PushNotificationTargetType.TOPIC)
                    .targetValues(List.of("promotions", "all_news"));
        }

        return sendNotification(builder.build());
    }

    @Override
    @Transactional
    public PushNotificationResponseDto sendTrending(UUID newsId, String title, String body, String thumbnailUrl) {
        log.info("PushNotificationService: Sending trending notification - newsId={}, hasImage={}",
                newsId, thumbnailUrl != null);

        PushNotificationSendRequestDto request = PushNotificationSendRequestDto.builder()
                .notificationType(PushNotificationType.TRENDING)
                .title(title)
                .body(body)
                .imageUrl(thumbnailUrl) // Maps to FCM imageUrl - from news entity newsThumbnailUrl
                .targetType(PushNotificationTargetType.TOPIC)
                .targetValues(List.of("trending", "all_news"))
                .newsId(newsId)
                .priority(PushNotificationPriority.HIGH) // Trending = time-sensitive
                .clickActionUrl(baseUrl + "/newsapp/" + newsId)
                .androidChannelId("trending")
                .idempotencyKey("trending_" + newsId)
                .build();

        return sendNotification(request);
    }

    @Override
    @Transactional
    public PushNotificationResponseDto sendCommentReply(UUID userId, UUID newsId, String replierName,
            String commentSnippet) {
        log.info("PushNotificationService: Sending comment reply notification - userId={}, newsId={}",
                userId, newsId);

        String title = replierName + " replied to your comment";
        String body = commentSnippet.length() > 100
                ? commentSnippet.substring(0, 97) + "..."
                : commentSnippet;

        PushNotificationSendRequestDto request = PushNotificationSendRequestDto.builder()
                .notificationType(PushNotificationType.COMMENT_REPLY)
                .title(title)
                .body(body)
                .targetType(PushNotificationTargetType.USER)
                .targetValues(List.of(userId.toString()))
                .newsId(newsId)
                .priority(PushNotificationPriority.NORMAL)
                .clickActionUrl(baseUrl + "/newsapp/" + newsId + "/comments")
                .androidChannelId("comments")
                .idempotencyKey("comment_reply_" + userId + "_" + newsId + "_" + System.currentTimeMillis())
                .build();

        return sendNotification(request);
    }

    @Override
    @Transactional
    public PushNotificationResponseDto sendSystemAnnouncement(String title, String body, String clickActionUrl) {
        log.info("PushNotificationService: Sending system announcement - title='{}'", title);

        PushNotificationSendRequestDto request = PushNotificationSendRequestDto.builder()
                .notificationType(PushNotificationType.SYSTEM)
                .title(title)
                .body(body)
                .targetType(PushNotificationTargetType.TOPIC)
                .targetValues(List.of("all_news"))
                .priority(PushNotificationPriority.NORMAL)
                .clickActionUrl(clickActionUrl != null ? clickActionUrl : baseUrl + "/announcements")
                .androidChannelId("system")
                .idempotencyKey("system_" + System.currentTimeMillis())
                .build();

        return sendNotification(request);
    }

    // ========================================
    // Scheduled Notifications
    // ========================================

    @Override
    @Transactional
    public PushNotificationResponseDto scheduleNotification(PushNotificationSendRequestDto request) {
        if (request.getScheduledAt() == null || request.getScheduledAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("scheduledAt must be in the future");
        }
        return sendNotification(request);
    }

    @Override
    @Transactional
    public boolean cancelScheduledNotification(UUID notificationId) {
        Optional<PushNotification> notification = notificationRepository.findById(notificationId);
        if (notification.isEmpty()) {
            return false;
        }

        PushNotification entity = notification.get();
        if (entity.getStatus() != PushNotificationStatus.SCHEDULED) {
            log.warn(
                    "PushNotificationService: Cannot cancel non-scheduled notification - notificationId={}, status={}",
                    notificationId, entity.getStatus());
            return false;
        }

        entity.setStatus(PushNotificationStatus.CANCELLED);
        notificationRepository.save(entity);
        log.info("PushNotificationService: Scheduled notification cancelled - notificationId={}", notificationId);
        return true;
    }

    @Override
    @Transactional
    public void processScheduledNotifications() {
        List<PushNotification> scheduled = notificationRepository.findScheduledNotificationsReady(Instant.now());

        log.info("PushNotificationService: Processing scheduled notifications - count={}", scheduled.size());

        for (PushNotification notification : scheduled) {
            try {
                notification.setStatus(PushNotificationStatus.PROCESSING);
                notificationRepository.save(notification);

                PushNotificationSendRequestDto request = rebuildRequest(notification);
                sendNotificationAsync(notification, request);
            } catch (Exception e) {
                log.error(
                        "PushNotificationService: Failed to process scheduled notification - notificationId={}, error={}",
                        notification.getNotificationId(), e.getMessage());
                notification.setStatus(PushNotificationStatus.FAILED);
                notification.setErrorMessage(e.getMessage());
                notificationRepository.save(notification);
            }
        }
    }

    // ========================================
    // Analytics & Tracking
    // ========================================

    @Override
    @Transactional
    public void recordNotificationOpened(UUID notificationId, UUID deviceId) {
        deliveryRepository.findByNotificationIdAndDeviceId(notificationId, deviceId)
                .ifPresent(delivery -> {
                    deliveryRepository.markAsOpened(delivery.getDeliveryId(), Instant.now());
                    notificationRepository.incrementOpenCount(notificationId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public PushNotificationResponseDto getNotification(UUID notificationId) {
        PushNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "notificationId",
                        notificationId.toString()));
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PushNotificationResponseDto> getRecentNotifications(Pageable pageable) {
        Page<PushNotification> page = notificationRepository.findRecent(pageable);
        List<PushNotificationResponseDto> responses = page.getContent().stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PushNotificationResponseDto> getNotificationsForNews(UUID newsId, Pageable pageable) {
        List<PushNotification> notifications = notificationRepository.findByNewsId(newsId);
        List<PushNotificationResponseDto> responses = notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responses.size());

        return new PageImpl<>(
                start < responses.size() ? responses.subList(start, end) : List.of(),
                pageable,
                responses.size());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatistics getStatistics(Instant startDate, Instant endDate) {
        Object[] stats = notificationRepository.getDeliveryStatsForPeriod(startDate, endDate);

        long totalSent = 0;
        long totalDelivered = 0;
        long totalFailed = 0;
        long totalOpened = 0;

        if (stats != null && stats.length >= 4) {
            totalSent = stats[0] != null ? ((Number) stats[0]).longValue() : 0;
            totalDelivered = stats[1] != null ? ((Number) stats[1]).longValue() : 0;
            totalFailed = stats[2] != null ? ((Number) stats[2]).longValue() : 0;
            totalOpened = stats[3] != null ? ((Number) stats[3]).longValue() : 0;
        }

        double deliveryRate = totalSent > 0 ? (double) totalDelivered / totalSent * 100 : 0;
        double openRate = totalDelivered > 0 ? (double) totalOpened / totalDelivered * 100 : 0;

        Map<String, Long> byType = notificationRepository.countByType().stream()
                .collect(Collectors.toMap(
                        arr -> ((PushNotificationType) arr[0]).name(),
                        arr -> (Long) arr[1]));

        Map<String, Long> byStatus = notificationRepository.countByStatus().stream()
                .collect(Collectors.toMap(
                        arr -> ((PushNotificationStatus) arr[0]).name(),
                        arr -> (Long) arr[1]));

        return new NotificationStatistics(
                totalSent, totalDelivered, totalFailed, totalOpened,
                deliveryRate, openRate, byType, byStatus);
    }

    // ========================================
    // Retry & Maintenance
    // ========================================

    @Override
    @Transactional
    public void retryFailedNotifications() {
        List<PushNotification> failed = notificationRepository.findFailedForRetry(MAX_RETRIES, Instant.now());

        log.info("PushNotificationService: Retrying failed notifications - count={}", failed.size());

        for (PushNotification notification : failed) {
            try {
                PushNotificationSendRequestDto request = rebuildRequest(notification);
                sendNotificationAsync(notification, request);
            } catch (Exception e) {
                log.error("PushNotificationService: Retry failed - notificationId={}, error={}",
                        notification.getNotificationId(), e.getMessage());

                // Calculate next retry with exponential backoff
                int retryCount = notification.getRetryCount() + 1;
                Instant nextRetry = Instant.now().plus((long) Math.pow(2, retryCount) * 5, ChronoUnit.MINUTES);
                notificationRepository.setRetryInfo(notification.getNotificationId(), nextRetry, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public int cleanupOldNotifications(int daysToKeep) {
        Instant cutoff = Instant.now().minus(daysToKeep, ChronoUnit.DAYS);
        int deleted = deliveryRepository.deleteOlderThan(cutoff);
        log.info("PushNotificationService: Cleaned up old delivery records - count={}", deleted);
        return deleted;
    }

    // ========================================
    // Internal Send Logic
    // ========================================

    @Async
    protected void sendNotificationAsync(PushNotification notification, PushNotificationSendRequestDto request) {
        try {
            notification.setStatus(PushNotificationStatus.PROCESSING);
            notificationRepository.save(notification);

            PushFcmService.FcmMessage fcmMessage = buildFcmMessage(request);

            switch (request.getTargetType()) {
                case TOPIC -> sendToTopics(notification, request.getTargetValues(), fcmMessage);
                case ALL -> sendToAllDevices(notification, fcmMessage);
                case DEVICE -> sendToDevices(notification, request.getTargetValues(), fcmMessage);
                case USER -> sendToUsers(notification, request.getTargetValues(), fcmMessage);
                case SEGMENT -> sendToSegment(notification, request.getTargetValues().get(0), fcmMessage);
            }

            // Update final status
            if (notification.getFailedCount() == 0) {
                notification.setStatus(PushNotificationStatus.SENT);
            } else if (notification.getSentCount() > 0) {
                notification.setStatus(PushNotificationStatus.PARTIALLY_SENT);
            } else {
                notification.setStatus(PushNotificationStatus.FAILED);
            }
            notification.setSentAt(Instant.now());
            notificationRepository.save(notification);

            log.info("PushNotificationService: Notification sent - notificationId={}, sent={}, failed={}",
                    notification.getNotificationId(), notification.getSentCount(), notification.getFailedCount());

        } catch (Exception e) {
            log.error("PushNotificationService: Failed to send notification - notificationId={}, error={}",
                    notification.getNotificationId(), e.getMessage(), e);
            notification.setStatus(PushNotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    private void sendToTopics(PushNotification notification, List<String> topics,
            PushFcmService.FcmMessage message) {
        for (String topic : topics) {
            PushFcmService.FcmSendResult result = fcmService.sendToTopic(topic, message);

            if (result.isSuccess()) {
                notification.setFcmMessageId(result.getMessageId());
                // Topic sends don't have exact sent count, estimate from subscriptions
                long subscriberCount = subscriptionRepository.countByTopicAndIsActiveTrue(topic);
                notification.setSentCount(notification.getSentCount() + (int) subscriberCount);
            } else {
                notification.setFailedCount(notification.getFailedCount() + 1);
                notification.setErrorMessage(result.getErrorMessage());
            }
        }
    }

    private void sendToAllDevices(PushNotification notification, PushFcmService.FcmMessage message) {
        // Send to "all_news" topic for efficiency
        PushFcmService.FcmSendResult result = fcmService.sendToTopic("all_news", message);

        if (result.isSuccess()) {
            notification.setFcmMessageId(result.getMessageId());
            long deviceCount = deviceRepository.countActiveDevices();
            notification.setSentCount((int) deviceCount);
        } else {
            notification.setFailedCount(1);
            notification.setErrorMessage(result.getErrorMessage());
        }
    }

    private void sendToDevices(PushNotification notification, List<String> deviceIds,
            PushFcmService.FcmMessage message) {
        List<String> tokens = new ArrayList<>();
        Map<String, UUID> tokenToDeviceId = new HashMap<>();

        for (String deviceIdStr : deviceIds) {
            UUID deviceId = UUID.fromString(deviceIdStr);
            deviceRepository.findById(deviceId).ifPresent(device -> {
                if (device.getIsActive() && device.getNotificationsEnabled()) {
                    tokens.add(device.getFcmToken());
                    tokenToDeviceId.put(device.getFcmToken(), deviceId);
                }
            });
        }

        sendToTokens(notification, tokens, tokenToDeviceId, message);
    }

    private void sendToUsers(PushNotification notification, List<String> userIds,
            PushFcmService.FcmMessage message) {
        List<String> tokens = new ArrayList<>();
        Map<String, UUID> tokenToDeviceId = new HashMap<>();

        for (String userIdStr : userIds) {
            UUID userId = UUID.fromString(userIdStr);
            List<PushDevice> devices = deviceRepository.findActiveDevicesByUserId(userId);
            for (PushDevice device : devices) {
                tokens.add(device.getFcmToken());
                tokenToDeviceId.put(device.getFcmToken(), device.getDeviceId());
            }
        }

        sendToTokens(notification, tokens, tokenToDeviceId, message);
    }

    private void sendToSegment(PushNotification notification, String segment,
            PushFcmService.FcmMessage message) {
        log.info("PushNotificationService: Sending to segment - segment={}, notificationId={}",
                segment, notification.getNotificationId());

        // Find all active users in the segment
        List<UUID> userIds = userRepository.findActiveUserIdsBySegment(segment);

        if (userIds.isEmpty()) {
            log.warn("PushNotificationService: No users found in segment - segment={}", segment);
            notification.setStatus(PushNotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setSentCount(0);
            return;
        }

        log.info("PushNotificationService: Found {} users in segment - segment={}",
                userIds.size(), segment);

        // Find active devices for those users
        List<PushDevice> devices = deviceRepository.findActiveDevicesByUserIdIn(userIds);

        if (devices.isEmpty()) {
            log.warn("PushNotificationService: No active devices found for segment users - segment={}", segment);
            notification.setStatus(PushNotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setSentCount(0);
            return;
        }

        log.info("PushNotificationService: Sending to {} devices for segment - segment={}",
                devices.size(), segment);

        // Build token list and mapping
        List<String> tokens = new ArrayList<>();
        Map<String, UUID> tokenToDeviceId = new HashMap<>();

        for (PushDevice device : devices) {
            tokens.add(device.getFcmToken());
            tokenToDeviceId.put(device.getFcmToken(), device.getDeviceId());
        }

        // Send to all devices
        sendToTokens(notification, tokens, tokenToDeviceId, message);
    }

    private void sendToTokens(PushNotification notification, List<String> tokens,
            Map<String, UUID> tokenToDeviceId, PushFcmService.FcmMessage message) {
        if (tokens.isEmpty()) {
            return;
        }

        // Create delivery records
        for (Map.Entry<String, UUID> entry : tokenToDeviceId.entrySet()) {
            PushNotificationDelivery delivery = PushNotificationDelivery.builder()
                    .notificationId(notification.getNotificationId())
                    .deviceId(entry.getValue())
                    .fcmToken(entry.getKey())
                    .status(PushNotificationDeliveryStatus.PENDING)
                    .build();
            deliveryRepository.save(delivery);
        }

        // Send in batches
        for (int i = 0; i < tokens.size(); i += BATCH_SIZE) {
            List<String> batch = tokens.subList(i, Math.min(i + BATCH_SIZE, tokens.size()));
            PushFcmService.FcmBatchResult result = fcmService.sendToDevices(batch, message);

            notification.setSentCount(notification.getSentCount() + result.getSuccessCount());
            notification.setFailedCount(notification.getFailedCount() + result.getFailureCount());

            // Update delivery records
            for (int j = 0; j < result.getResults().size(); j++) {
                String token = batch.get(j);
                UUID deviceId = tokenToDeviceId.get(token);
                PushFcmService.FcmSendResult sendResult = result.getResults().get(j);

                if (deviceId != null) {
                    deliveryRepository.findByNotificationIdAndDeviceId(notification.getNotificationId(), deviceId)
                            .ifPresent(delivery -> {
                                if (sendResult.isSuccess()) {
                                    deliveryRepository.updateAsSent(
                                            delivery.getDeliveryId(), Instant.now(), sendResult.getMessageId());
                                    deviceRepository.updateLastNotificationSuccess(deviceId, Instant.now());
                                } else {
                                    PushNotificationDeliveryStatus status = sendResult.isTokenInvalid()
                                            ? PushNotificationDeliveryStatus.INVALID_TOKEN
                                            : PushNotificationDeliveryStatus.FAILED;
                                    deliveryRepository.updateAsFailed(
                                            delivery.getDeliveryId(), status,
                                            sendResult.getErrorCode(), sendResult.getErrorMessage());

                                    if (sendResult.isTokenInvalid()) {
                                        deviceRepository.deactivateDevice(deviceId);
                                    } else {
                                        deviceRepository.incrementFailureCount(deviceId);
                                    }
                                }
                            });
                }
            }

            // Handle invalid tokens
            for (String invalidToken : result.getInvalidTokens()) {
                deviceRepository.deactivateByFcmToken(invalidToken);
            }
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    private PushNotification buildNotificationEntity(PushNotificationSendRequestDto request) {
        String payload = null;
        if (request.getData() != null && !request.getData().isEmpty()) {
            try {
                payload = objectMapper.writeValueAsString(request.getData());
            } catch (JsonProcessingException e) {
                log.warn("PushNotificationService: Failed to serialize payload", e);
            }
        }

        String targetValue = request.getTargetValues() != null && !request.getTargetValues().isEmpty()
                ? String.join(",", request.getTargetValues())
                : null;

        return PushNotification.builder()
                .notificationType(request.getNotificationType())
                .title(request.getTitle())
                .body(request.getBody())
                .imageUrl(request.getImageUrl())
                .clickActionUrl(request.getClickActionUrl())
                .payload(payload)
                .targetType(request.getTargetType())
                .targetValue(targetValue)
                .scheduledAt(request.getScheduledAt())
                .scheduledTimezone(request.getScheduledTimezone())
                .ttlSeconds(request.getTtlSeconds())
                .priority(request.getPriority())
                .androidChannelId(request.getAndroidChannelId())
                .sound(request.getSound())
                .badgeCount(request.getBadgeCount())
                .newsId(request.getNewsId())
                .categoryId(request.getCategoryId())
                .idempotencyKey(request.getIdempotencyKey())
                .status(PushNotificationStatus.PENDING)
                .sentCount(0)
                .deliveredCount(0)
                .failedCount(0)
                .openedCount(0)
                .retryCount(0)
                .build();
    }

    private PushFcmService.FcmMessage buildFcmMessage(PushNotificationSendRequestDto request) {
        PushFcmService.FcmMessage.FcmMessageBuilder builder = PushFcmService.FcmMessage.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .imageUrl(request.getImageUrl())
                .clickAction(request.getClickActionUrl())
                .data(request.getData())
                .ttlSeconds(request.getTtlSeconds() != null ? request.getTtlSeconds() : 86400)
                .priority(request.getPriority() == PushNotificationPriority.HIGH ? "high" : "normal");

        // Android config
        if (request.getAndroidChannelId() != null || request.getSound() != null) {
            builder.android(PushFcmService.FcmMessage.AndroidConfig.builder()
                    .channelId(request.getAndroidChannelId())
                    .sound(request.getSound())
                    .build());
        }

        // iOS config
        if (request.getSound() != null || request.getBadgeCount() != null) {
            builder.apns(PushFcmService.FcmMessage.ApnsConfig.builder()
                    .sound(request.getSound())
                    .badge(request.getBadgeCount())
                    .build());
        }

        return builder.build();
    }

    private PushNotificationSendRequestDto rebuildRequest(PushNotification notification) {
        Map<String, String> data = null;
        if (notification.getPayload() != null) {
            try {
                data = objectMapper.readValue(notification.getPayload(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
            } catch (JsonProcessingException e) {
                log.warn("PushNotificationService: Failed to parse payload", e);
            }
        }

        List<String> targetValues = notification.getTargetValue() != null
                ? Arrays.asList(notification.getTargetValue().split(","))
                : null;

        return PushNotificationSendRequestDto.builder()
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .imageUrl(notification.getImageUrl())
                .clickActionUrl(notification.getClickActionUrl())
                .targetType(notification.getTargetType())
                .targetValues(targetValues)
                .ttlSeconds(notification.getTtlSeconds())
                .priority(notification.getPriority())
                .androidChannelId(notification.getAndroidChannelId())
                .sound(notification.getSound())
                .badgeCount(notification.getBadgeCount())
                .newsId(notification.getNewsId())
                .categoryId(notification.getCategoryId())
                .data(data)
                .build();
    }

    @Override
    public boolean isFcmServiceHealthy() {
        try {
            // Simple health check - could be enhanced with actual FCM ping
            return fcmService != null;
        } catch (Exception e) {
            log.error("FCM service health check failed", e);
            return false;
        }
    }

    @Override
    public boolean isDatabaseHealthy() {
        try {
            // Simple database health check
            notificationRepository.count();
            return true;
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return false;
        }
    }

    @Override
    public boolean isQueueHealthy() {
        try {
            // Simple queue health check - could be enhanced with actual queue status
            return true;
        } catch (Exception e) {
            log.error("Queue health check failed", e);
            return false;
        }
    }
}
