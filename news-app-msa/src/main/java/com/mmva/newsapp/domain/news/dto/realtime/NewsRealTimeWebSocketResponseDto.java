package com.mmva.newsapp.domain.news.dto.realtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for WebSocket-based real-time news updates.
 *
 * <p>
 * Contains real-time notification data sent to connected clients
 * including breaking news, updates, comments, and analytics.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsRealTimeWebSocketResponseDto {

        /**
         * Message type identifier.
         * Values: breaking_news, news_published, news_updated, new_comment,
         * user_notification, news_recommendation, analytics_update,
         * system_status, maintenance
         */
        private String type;

        /**
         * Unique message identifier for tracking.
         */
        private UUID messageId;

        /**
         * Message title for display.
         */
        private String title;

        /**
         * Main message content.
         */
        private String message;

        /**
         * Additional message data as key-value pairs.
         * Contains type-specific information (newsId, userId, etc.)
         */
        private Map<String, Object> data;

        /**
         * Message priority level.
         * Values: low, normal, high, urgent
         */
        @Builder.Default
        private String priority = "normal";

        /**
         * Target user ID for personal messages.
         * Null for broadcast messages.
         */
        private UUID userId;

        /**
         * Related news article ID.
         */
        private UUID newsId;

        /**
         * Message timestamp.
         */
        private LocalDateTime timestamp;

        /**
         * Message expiration time (optional).
         */
        private LocalDateTime expiresAt;

        /**
         * Client action required.
         * Values: none, refresh, redirect, acknowledge
         */
        @Builder.Default
        private String actionRequired = "none";

        /**
         * Action URL for redirects or additional actions.
         */
        private String actionUrl;

        /**
         * Message category for filtering.
         * Values: news, system, user, analytics
         */
        private String category;

        /**
         * Message source identifier.
         */
        private String source;

        // ========================================
        // Convenience Factory Methods
        // ========================================

        /**
         * Create breaking news response.
         */
        public static NewsRealTimeWebSocketResponseDto breakingNews(UUID newsId, String title, String alertMessage) {
                return NewsRealTimeWebSocketResponseDto.builder()
                                .type("breaking_news")
                                .messageId(UUID.randomUUID())
                                .title(title)
                                .message(alertMessage)
                                .newsId(newsId)
                                .priority("high")
                                .category("news")
                                .timestamp(LocalDateTime.now())
                                .build();
        }

        /**
         * Create news publication response.
         */
        public static NewsRealTimeWebSocketResponseDto newsPublished(UUID newsId, String title) {
                return NewsRealTimeWebSocketResponseDto.builder()
                                .type("news_published")
                                .messageId(UUID.randomUUID())
                                .title("New Article Published")
                                .message(title)
                                .newsId(newsId)
                                .priority("normal")
                                .category("news")
                                .timestamp(LocalDateTime.now())
                                .build();
        }

        /**
         * Create user notification response.
         */
        public static NewsRealTimeWebSocketResponseDto userNotification(UUID userId, String title, String message,
                        String notificationType) {
                return NewsRealTimeWebSocketResponseDto.builder()
                                .type("user_notification")
                                .messageId(UUID.randomUUID())
                                .userId(userId)
                                .title(title)
                                .message(message)
                                .priority("normal")
                                .category("user")
                                .timestamp(LocalDateTime.now())
                                .data(Map.of("notificationType", notificationType))
                                .build();
        }

        /**
         * Create analytics update response.
         */
        public static NewsRealTimeWebSocketResponseDto analyticsUpdate(UUID newsId, long views, long likes,
                        long comments) {
                return NewsRealTimeWebSocketResponseDto.builder()
                                .type("analytics_update")
                                .messageId(UUID.randomUUID())
                                .newsId(newsId)
                                .title("Analytics Update")
                                .message(String.format("Views: %d, Likes: %d, Comments: %d", views, likes, comments))
                                .priority("low")
                                .category("analytics")
                                .timestamp(LocalDateTime.now())
                                .data(Map.of(
                                                "viewCount", views,
                                                "likeCount", likes,
                                                "commentCount", comments))
                                .build();
        }

        /**
         * Create system status response.
         */
        public static NewsRealTimeWebSocketResponseDto systemStatus(String status, Object statusData) {
                return NewsRealTimeWebSocketResponseDto.builder()
                                .type("system_status")
                                .messageId(UUID.randomUUID())
                                .title("System Status")
                                .message(status)
                                .priority("low")
                                .category("system")
                                .timestamp(LocalDateTime.now())
                                .data(Map.of("statusData", statusData))
                                .build();
        }

        /**
         * Create maintenance notification response.
         */
        public static NewsRealTimeWebSocketResponseDto maintenance(String message, String scheduledTime) {
                return NewsRealTimeWebSocketResponseDto.builder()
                                .type("maintenance")
                                .messageId(UUID.randomUUID())
                                .title("Maintenance Notice")
                                .message(message)
                                .priority("high")
                                .category("system")
                                .timestamp(LocalDateTime.now())
                                .data(Map.of("scheduledTime", scheduledTime))
                                .actionRequired("acknowledge")
                                .build();
        }
}