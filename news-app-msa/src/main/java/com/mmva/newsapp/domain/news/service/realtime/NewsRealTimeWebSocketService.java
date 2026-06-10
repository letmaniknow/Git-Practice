package com.mmva.newsapp.domain.news.service.realtime;

import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;

import java.util.UUID;

/**
 * Service interface for real-time news updates via WebSocket.
 *
 * <p>
 * Provides functionality to broadcast breaking news, live updates,
 * and real-time notifications to connected WebSocket clients.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Broadcast breaking news alerts to all connected clients</li>
 * <li>Send live news updates as they are published</li>
 * <li>Notify users of new comments on articles they follow</li>
 * <li>Real-time analytics and engagement metrics</li>
 * <li>User-specific notifications for personalized content</li>
 * </ul>
 *
 * <h3>WebSocket Destinations:</h3>
 * <ul>
 * <li><b>/topic/breaking-news</b> - Breaking news alerts for all users</li>
 * <li><b>/topic/news-updates</b> - Live news publication updates</li>
 * <li><b>/topic/comments/{newsId}</b> - New comments on specific articles</li>
 * <li><b>/user/queue/notifications</b> - Personal notifications</li>
 * <li><b>/topic/analytics</b> - Real-time analytics updates</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsRealTimeWebSocketService {

    // ========================================
    // Breaking News Broadcasting
    // ========================================

    /**
     * Broadcast breaking news alert to all connected clients.
     *
     * <p>
     * Sends breaking news notification to /topic/breaking-news destination
     * with high priority and immediate delivery.
     * </p>
     *
     * @param news The breaking news article to broadcast
     */
    void broadcastBreakingNews(NewsMasterEntity news);

    /**
     * Broadcast breaking news alert with custom message.
     *
     * <p>
     * Sends breaking news notification with custom alert message
     * to /topic/breaking-news destination.
     * </p>
     *
     * @param news         The breaking news article
     * @param alertMessage Custom alert message to display
     */
    void broadcastBreakingNews(NewsMasterEntity news, String alertMessage);

    // ========================================
    // Live News Updates
    // ========================================

    /**
     * Broadcast news publication update to all connected clients.
     *
     * <p>
     * Notifies all clients when a new news article is published
     * to /topic/news-updates destination.
     * </p>
     *
     * @param news The newly published news article
     */
    void broadcastNewsPublished(NewsMasterEntity news);

    /**
     * Broadcast news update to all connected clients.
     *
     * <p>
     * Notifies all clients when an existing news article is updated
     * to /topic/news-updates destination.
     * </p>
     *
     * @param news The updated news article
     */
    void broadcastNewsUpdated(NewsMasterEntity news);

    // ========================================
    // Comment Notifications
    // ========================================

    /**
     * Broadcast new comment notification for specific news article.
     *
     * <p>
     * Notifies clients following the article about new comments
     * to /topic/comments/{newsId} destination.
     * </p>
     *
     * @param newsId         The news article ID
     * @param commentId      The new comment ID
     * @param authorName     The comment author name
     * @param commentPreview Preview text of the comment
     */
    void broadcastNewComment(UUID newsId, UUID commentId, String authorName, String commentPreview);

    // ========================================
    // User-Specific Notifications
    // ========================================

    /**
     * Send personal notification to specific user.
     *
     * <p>
     * Sends personalized notification to individual user
     * via /user/{userId}/queue/notifications destination.
     * </p>
     *
     * @param userId  The target user ID
     * @param title   Notification title
     * @param message Notification message
     * @param type    Notification type (info, success, warning, error)
     */
    void sendUserNotification(UUID userId, String title, String message, String type);

    /**
     * Send personalized news recommendation to user.
     *
     * <p>
     * Sends news recommendation based on user preferences
     * via /user/{userId}/queue/recommendations destination.
     * </p>
     *
     * @param userId The target user ID
     * @param newsId The recommended news article ID
     * @param reason Reason for the recommendation
     */
    void sendNewsRecommendation(UUID userId, UUID newsId, String reason);

    // ========================================
    // Analytics Broadcasting
    // ========================================

    /**
     * Broadcast real-time analytics update.
     *
     * <p>
     * Sends live analytics data (views, engagement, etc.)
     * to /topic/analytics destination for dashboard updates.
     * </p>
     *
     * @param newsId       The news article ID
     * @param viewCount    Current view count
     * @param likeCount    Current like count
     * @param commentCount Current comment count
     */
    void broadcastAnalyticsUpdate(UUID newsId, long viewCount, long likeCount, long commentCount);

    // ========================================
    // System Status Broadcasting
    // ========================================

    /**
     * Broadcast system status update.
     *
     * <p>
     * Sends system status information (server load, uptime, etc.)
     * to /topic/system-status destination.
     * </p>
     *
     * @param status System status information
     */
    void broadcastSystemStatus(Object status);

    /**
     * Broadcast maintenance notification.
     *
     * <p>
     * Notifies all clients about upcoming maintenance
     * to /topic/maintenance destination.
     * </p>
     *
     * @param message       Maintenance notification message
     * @param scheduledTime Scheduled maintenance time (ISO format)
     */
    void broadcastMaintenanceNotification(String message, String scheduledTime);
}