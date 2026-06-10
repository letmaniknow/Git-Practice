package com.mmva.newsapp.domain.news.mapper.realtime;

import com.mmva.newsapp.domain.news.dto.realtime.NewsRealTimeWebSocketResponseDto;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * MapStruct mapper for WebSocket real-time news update DTOs.
 *
 * <p>
 * Provides bidirectional mapping between WebSocket message DTOs
 * and internal data structures for real-time news communication.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsRealTimeWebSocketMapper {

    /**
     * Convert NewsMasterEntity to breaking news response.
     *
     * @param news The news entity to convert
     * @return Breaking news WebSocket response
     */
    @Mapping(target = "type", constant = "breaking_news")
    @Mapping(target = "messageId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "title", source = "newsTitleEn")
    @Mapping(target = "message", expression = "java(\"BREAKING NEWS: \" + news.getNewsTitleEn())")
    @Mapping(target = "newsId", source = "newsNewsId")
    @Mapping(target = "priority", constant = "high")
    @Mapping(target = "category", constant = "news")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "data", expression = "java(createBreakingNewsData(news))")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "actionRequired", ignore = true)
    @Mapping(target = "actionUrl", ignore = true)
    @Mapping(target = "source", ignore = true)
    NewsRealTimeWebSocketResponseDto toBreakingNewsResponse(NewsMasterEntity news);

    /**
     * Convert NewsMasterEntity to news publication response.
     *
     * @param news The news entity to convert
     * @return News publication WebSocket response
     */
    @Mapping(target = "type", constant = "news_published")
    @Mapping(target = "messageId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "title", constant = "New Article Published")
    @Mapping(target = "message", source = "newsTitleEn")
    @Mapping(target = "newsId", source = "newsNewsId")
    @Mapping(target = "priority", constant = "normal")
    @Mapping(target = "category", constant = "news")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "data", expression = "java(createNewsPublicationData(news))")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "actionRequired", ignore = true)
    @Mapping(target = "actionUrl", ignore = true)
    @Mapping(target = "source", ignore = true)
    NewsRealTimeWebSocketResponseDto toNewsPublicationResponse(NewsMasterEntity news);

    /**
     * Convert NewsMasterEntity to news update response.
     *
     * @param news The news entity to convert
     * @return News update WebSocket response
     */
    @Mapping(target = "type", constant = "news_updated")
    @Mapping(target = "messageId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "title", constant = "Article Updated")
    @Mapping(target = "message", source = "newsTitleEn")
    @Mapping(target = "newsId", source = "newsNewsId")
    @Mapping(target = "priority", constant = "normal")
    @Mapping(target = "category", constant = "news")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "data", expression = "java(createNewsUpdateData(news))")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "actionRequired", ignore = true)
    @Mapping(target = "actionUrl", ignore = true)
    @Mapping(target = "source", ignore = true)
    NewsRealTimeWebSocketResponseDto toNewsUpdateResponse(NewsMasterEntity news);

    /**
     * Create user notification response.
     *
     * @param userId           Target user ID
     * @param title            Notification title
     * @param message          Notification message
     * @param notificationType Type of notification
     * @return User notification WebSocket response
     */
    @Mapping(target = "type", constant = "user_notification")
    @Mapping(target = "messageId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "priority", constant = "normal")
    @Mapping(target = "category", constant = "user")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "actionRequired", constant = "none")
    @Mapping(target = "data", expression = "java(createUserNotificationData(notificationType))")
    @Mapping(target = "newsId", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "actionUrl", ignore = true)
    @Mapping(target = "source", ignore = true)
    NewsRealTimeWebSocketResponseDto toUserNotificationResponse(
            UUID userId, String title, String message, String notificationType);

    /**
     * Create analytics update response.
     *
     * @param newsId       News article ID
     * @param viewCount    Current view count
     * @param likeCount    Current like count
     * @param commentCount Current comment count
     * @return Analytics update WebSocket response
     */
    @Mapping(target = "type", constant = "analytics_update")
    @Mapping(target = "messageId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "newsId", source = "newsId")
    @Mapping(target = "title", constant = "Analytics Update")
    @Mapping(target = "message", expression = "java(createAnalyticsMessage(viewCount, likeCount, commentCount))")
    @Mapping(target = "priority", constant = "low")
    @Mapping(target = "category", constant = "analytics")
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "actionRequired", constant = "none")
    @Mapping(target = "data", expression = "java(createAnalyticsData(newsId, viewCount, likeCount, commentCount))")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "actionUrl", ignore = true)
    @Mapping(target = "source", ignore = true)
    NewsRealTimeWebSocketResponseDto toAnalyticsUpdateResponse(
            UUID newsId, long viewCount, long likeCount, long commentCount);

    // ========================================
    // Helper Methods for Complex Mappings
    // ========================================

    /**
     * Create breaking news data map.
     */
    default Map<String, Object> createBreakingNewsData(NewsMasterEntity news) {
        return Map.of(
                "newsId", news.getNewsNewsId(),
                "title", news.getNewsTitleEn(),
                "categoryId", news.getNewsNewsCategoryId(),
                "publishedAt", news.getNewsPublishedAt(),
                "isBreaking", news.getNewsIsBreaking(),
                "priority", "high");
    }

    /**
     * Create news publication data map.
     */
    default Map<String, Object> createNewsPublicationData(NewsMasterEntity news) {
        return Map.of(
                "newsId", news.getNewsNewsId(),
                "title", news.getNewsTitleEn(),
                "categoryId", news.getNewsNewsCategoryId(),
                "publishedAt", news.getNewsPublishedAt(),
                "author", news.getNewsSourceAuthorName(),
                "action", "published");
    }

    /**
     * Create news update data map.
     */
    default Map<String, Object> createNewsUpdateData(NewsMasterEntity news) {
        return Map.of(
                "newsId", news.getNewsNewsId(),
                "title", news.getNewsTitleEn(),
                "categoryId", news.getNewsNewsCategoryId(),
                "updatedAt", LocalDateTime.now(),
                "author", news.getNewsSourceAuthorName(),
                "action", "updated");
    }

    /**
     * Create user notification data map.
     */
    default Map<String, Object> createUserNotificationData(String notificationType) {
        return Map.of(
                "notificationType", notificationType,
                "timestamp", LocalDateTime.now());
    }

    /**
     * Create analytics data map.
     */
    default Map<String, Object> createAnalyticsData(UUID newsId, long viewCount, long likeCount, long commentCount) {
        return Map.of(
                "newsId", newsId,
                "viewCount", viewCount,
                "likeCount", likeCount,
                "commentCount", commentCount,
                "timestamp", LocalDateTime.now());
    }

    /**
     * Create analytics message string.
     */
    default String createAnalyticsMessage(long viewCount, long likeCount, long commentCount) {
        return String.format("Views: %d, Likes: %d, Comments: %d", viewCount, likeCount, commentCount);
    }
}