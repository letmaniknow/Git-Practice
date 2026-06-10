package com.mmva.newsapp.domain.news.service.realtime;

import com.mmva.newsapp.domain.news.dto.realtime.NewsRealTimeWebSocketResponseDto;
import com.mmva.newsapp.domain.news.mapper.realtime.NewsRealTimeWebSocketMapper;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of real-time news updates via WebSocket.
 *
 * <p>
 * Uses Spring's SimpMessagingTemplate to broadcast messages to connected
 * WebSocket clients through STOMP over WebSocket protocol.
 * </p>
 *
 * <h3>Implementation Notes:</h3>
 * <ul>
 * <li>Uses SimpMessagingTemplate for message broadcasting</li>
 * <li>Supports both broadcast and user-specific messaging</li>
 * <li>Includes error handling and logging for message delivery</li>
 * <li>Thread-safe for concurrent message sending</li>
 * <li>Configurable message destinations for different event types</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewsRealTimeWebSocketServiceImpl implements NewsRealTimeWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NewsRealTimeWebSocketMapper mapper;

    // ========================================
    // Breaking News Broadcasting
    // ========================================

    @Override
    public void broadcastBreakingNews(NewsMasterEntity news) {
        broadcastBreakingNews(news, "BREAKING NEWS: " + news.getNewsTitleEn());
    }

    @Override
    public void broadcastBreakingNews(NewsMasterEntity news, String alertMessage) {
        log.info("Broadcasting breaking news: {}", news.getNewsNewsId());

        try {
            NewsRealTimeWebSocketResponseDto response = mapper.toBreakingNewsResponse(news);
            response.setMessage(alertMessage); // Override with custom message if provided

            messagingTemplate.convertAndSend("/topic/breaking-news", response);

            log.debug("Successfully broadcast breaking news to all clients: {}", news.getNewsNewsId());
        } catch (Exception e) {
            log.error("Failed to broadcast breaking news: {}", news.getNewsNewsId(), e);
        }
    }

    // ========================================
    // Live News Updates
    // ========================================

    @Override
    public void broadcastNewsPublished(NewsMasterEntity news) {
        log.info("Broadcasting news publication: {}", news.getNewsNewsId());

        try {
            NewsRealTimeWebSocketResponseDto response = mapper.toNewsPublicationResponse(news);
            messagingTemplate.convertAndSend("/topic/news-updates", response);

            log.debug("Successfully broadcast news publication: {}", news.getNewsNewsId());
        } catch (Exception e) {
            log.error("Failed to broadcast news publication: {}", news.getNewsNewsId(), e);
        }
    }

    @Override
    public void broadcastNewsUpdated(NewsMasterEntity news) {
        log.info("Broadcasting news update: {}", news.getNewsNewsId());

        try {
            NewsRealTimeWebSocketResponseDto response = mapper.toNewsUpdateResponse(news);
            messagingTemplate.convertAndSend("/topic/news-updates", response);

            log.debug("Successfully broadcast news update: {}", news.getNewsNewsId());
        } catch (Exception e) {
            log.error("Failed to broadcast news update: {}", news.getNewsNewsId(), e);
        }
    }

    // ========================================
    // Comment Notifications
    // ========================================

    @Override
    public void broadcastNewComment(UUID newsId, UUID commentId, String authorName, String commentPreview) {
        log.debug("Broadcasting new comment for news: {} by: {}", newsId, authorName);

        try {
            NewsRealTimeWebSocketResponseDto response = NewsRealTimeWebSocketResponseDto.builder()
                    .type("new_comment")
                    .messageId(UUID.randomUUID())
                    .newsId(newsId)
                    .title("New Comment")
                    .message(String.format("Comment by %s: %s", authorName, commentPreview))
                    .priority("normal")
                    .category("comments")
                    .timestamp(java.time.LocalDateTime.now())
                    .data(Map.of(
                            "commentId", commentId,
                            "authorName", authorName,
                            "commentPreview", commentPreview))
                    .build();

            messagingTemplate.convertAndSend("/topic/comments/" + newsId, response);

            log.debug("Successfully broadcast new comment for news: {}", newsId);
        } catch (Exception e) {
            log.error("Failed to broadcast new comment for news: {}", newsId, e);
        }
    }

    // ========================================
    // User-Specific Notifications
    // ========================================

    @Override
    public void sendUserNotification(UUID userId, String title, String message, String type) {
        log.debug("Sending user notification to: {} - {}", userId, title);

        try {
            NewsRealTimeWebSocketResponseDto response = mapper.toUserNotificationResponse(userId, title, message, type);
            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", response);

            log.debug("Successfully sent user notification to: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send user notification to: {}", userId, e);
        }
    }

    @Override
    public void sendNewsRecommendation(UUID userId, UUID newsId, String reason) {
        log.debug("Sending news recommendation to user: {} for news: {}", userId, newsId);

        try {
            NewsRealTimeWebSocketResponseDto response = NewsRealTimeWebSocketResponseDto.builder()
                    .type("news_recommendation")
                    .messageId(UUID.randomUUID())
                    .userId(userId)
                    .newsId(newsId)
                    .title("Recommended for You")
                    .message("Based on your interests: " + reason)
                    .priority("normal")
                    .category("user")
                    .timestamp(java.time.LocalDateTime.now())
                    .data(Map.of("reason", reason))
                    .build();

            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/recommendations", response);

            log.debug("Successfully sent news recommendation to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send news recommendation to user: {}", userId, e);
        }
    }

    // ========================================
    // Analytics Broadcasting
    // ========================================

    @Override
    public void broadcastAnalyticsUpdate(UUID newsId, long viewCount, long likeCount, long commentCount) {
        log.debug("Broadcasting analytics update for news: {}", newsId);

        try {
            NewsRealTimeWebSocketResponseDto response = mapper.toAnalyticsUpdateResponse(newsId, viewCount, likeCount,
                    commentCount);
            messagingTemplate.convertAndSend("/topic/analytics", response);

            log.debug("Successfully broadcast analytics update for news: {}", newsId);
        } catch (Exception e) {
            log.error("Failed to broadcast analytics update for news: {}", newsId, e);
        }
    }

    // ========================================
    // System Status Broadcasting
    // ========================================

    @Override
    public void broadcastSystemStatus(Object status) {
        log.debug("Broadcasting system status update");

        try {
            NewsRealTimeWebSocketResponseDto response = NewsRealTimeWebSocketResponseDto.builder()
                    .type("system_status")
                    .messageId(UUID.randomUUID())
                    .title("System Status")
                    .message(status.toString())
                    .priority("low")
                    .category("system")
                    .timestamp(java.time.LocalDateTime.now())
                    .data(Map.of("statusData", status))
                    .build();

            messagingTemplate.convertAndSend("/topic/system-status", response);

            log.debug("Successfully broadcast system status update");
        } catch (Exception e) {
            log.error("Failed to broadcast system status update", e);
        }
    }

    @Override
    public void broadcastMaintenanceNotification(String message, String scheduledTime) {
        log.info("Broadcasting maintenance notification: {}", message);

        try {
            NewsRealTimeWebSocketResponseDto response = NewsRealTimeWebSocketResponseDto.builder()
                    .type("maintenance")
                    .messageId(UUID.randomUUID())
                    .title("Maintenance Notice")
                    .message(message)
                    .priority("high")
                    .category("system")
                    .timestamp(java.time.LocalDateTime.now())
                    .data(Map.of("scheduledTime", scheduledTime))
                    .actionRequired("acknowledge")
                    .build();

            messagingTemplate.convertAndSend("/topic/maintenance", response);

            log.info("Successfully broadcast maintenance notification");
        } catch (Exception e) {
            log.error("Failed to broadcast maintenance notification", e);
        }
    }
}