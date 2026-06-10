package com.mmva.newsapp.infrastructure.push.service;

import com.mmva.newsapp.infrastructure.push.dto.PushNotificationResponseDto;
import com.mmva.newsapp.infrastructure.push.dto.PushNotificationSendRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

/**
 * Push notification sending service interface.
 * 
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Send notifications to devices, users, topics</li>
 * <li>Scheduled notifications</li>
 * <li>Breaking newsapp automation</li>
 * <li>Notification analytics</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface PushNotificationService {

        // ========================================
        // Send Notifications
        // ========================================

        /**
         * Send notification based on request configuration.
         * Handles all target types: ALL, TOPIC, DEVICE, USER, SEGMENT
         * 
         * @param request notification request
         * @return notification response with ID and status
         */
        PushNotificationResponseDto sendNotification(PushNotificationSendRequestDto request);

        /**
         * Send breaking newsapp notification to all subscribed devices.
         * 
         * @param newsId       newsapp newsapp ID
         * @param title        notification title
         * @param body         notification body
         * @param thumbnailUrl news thumbnail URL for rich notification (1024x512
         *                     recommended)
         *                     This comes from NewsMasterEntity.newsThumbnailUrl
         * @return notification response
         */
        PushNotificationResponseDto sendBreakingNews(UUID newsId, String title, String body, String thumbnailUrl);

        /**
         * Send newscategory update notification.
         * 
         * @param categoryId   newscategory ID
         * @param categorySlug newscategory slug for topic
         * @param newsId       related newsapp ID
         * @param title        notification title
         * @param body         notification body
         * @param thumbnailUrl news thumbnail URL for rich notification (1024x512
         *                     recommended)
         *                     This comes from NewsMasterEntity.newsThumbnailUrl
         * @return notification response
         */
        PushNotificationResponseDto sendCategoryUpdate(UUID categoryId, String categorySlug, UUID newsId, String title,
                        String body, String thumbnailUrl);

        /**
         * Send notification to specific user.
         * 
         * @param userId       user ID
         * @param title        notification title
         * @param body         notification body
         * @param thumbnailUrl optional image URL for rich notification
         * @return notification response
         */
        PushNotificationResponseDto sendToUser(UUID userId, String title, String body, String thumbnailUrl);

        /**
         * Send regular news update notification to category subscribers.
         * Lower priority than breaking news.
         * 
         * @param newsId       news article ID
         * @param categorySlug category slug for topic targeting
         * @param title        notification title
         * @param body         notification body
         * @param thumbnailUrl news thumbnail URL (1024x512 recommended)
         * @return notification response
         */
        PushNotificationResponseDto sendNewsUpdate(UUID newsId, String categorySlug, String title, String body,
                        String thumbnailUrl);

        /**
         * Send daily digest notification.
         * Summarizes top stories of the day with individual clickable items.
         * 
         * @param title            digest title (e.g., "Today's Top Stories")
         * @param body             digest body/summary
         * @param featuredImageUrl main featured image for notification banner
         *                         (1024x512)
         * @param newsItems        list of news items, each with: newsId, title,
         *                         thumbnailUrl
         *                         - Each item is clickable → navigates to
         *                         /newsapp/{newsId}
         *                         - Each item has its own thumbnail for display
         * @return notification response
         */
        PushNotificationResponseDto sendDailyDigest(String title, String body, String featuredImageUrl,
                        java.util.List<com.mmva.newsapp.infrastructure.push.dto.DigestNewsItemDto> newsItems);

        /**
         * Send weekly digest notification.
         * Summarizes week's highlights with individual clickable items.
         * 
         * @param title            digest title (e.g., "This Week's Highlights")
         * @param body             digest body/summary
         * @param featuredImageUrl main featured image for notification banner
         *                         (1024x512)
         * @param newsItems        list of news items, each with: newsId, title,
         *                         thumbnailUrl
         *                         - Each item is clickable → navigates to
         *                         /newsapp/{newsId}
         *                         - Each item has its own thumbnail for display
         * @return notification response
         */
        PushNotificationResponseDto sendWeeklyDigest(String title, String body, String featuredImageUrl,
                        java.util.List<com.mmva.newsapp.infrastructure.push.dto.DigestNewsItemDto> newsItems);

        /**
         * Send promotional notification.
         * CRITICAL FOR MONETIZATION - used for sponsored content, app promotions.
         * 
         * @param title          notification title
         * @param body           notification body
         * @param thumbnailUrl   promotional image URL (1024x512 recommended)
         * @param clickActionUrl where to navigate on click (e.g., offer page)
         * @param segment        optional user segment to target (null for all)
         * @param campaignId     optional campaign ID for tracking
         * @return notification response
         */
        PushNotificationResponseDto sendPromotional(String title, String body, String thumbnailUrl,
                        String clickActionUrl, String segment, String campaignId);

        /**
         * Send trending news notification.
         * Alerts users about viral/trending content.
         * 
         * @param newsId       trending news ID
         * @param title        notification title
         * @param body         notification body
         * @param thumbnailUrl news thumbnail URL (1024x512 recommended)
         * @return notification response
         */
        PushNotificationResponseDto sendTrending(UUID newsId, String title, String body, String thumbnailUrl);

        /**
         * Send comment reply notification.
         * Notifies user when someone replies to their comment.
         * 
         * @param userId         user to notify
         * @param newsId         news article where comment was made
         * @param replierName    name of person who replied
         * @param commentSnippet snippet of the reply
         * @return notification response
         */
        PushNotificationResponseDto sendCommentReply(UUID userId, UUID newsId, String replierName,
                        String commentSnippet);

        /**
         * Send system announcement notification.
         * For app updates, maintenance notices, policy changes.
         * 
         * @param title          announcement title
         * @param body           announcement body
         * @param clickActionUrl optional URL to navigate (e.g., changelog page)
         * @return notification response
         */
        PushNotificationResponseDto sendSystemAnnouncement(String title, String body, String clickActionUrl);

        // ========================================
        // Scheduled Notifications
        // ========================================

        /**
         * Schedule notification for later delivery.
         * 
         * @param request notification request with scheduledAt set
         * @return notification response
         */
        PushNotificationResponseDto scheduleNotification(PushNotificationSendRequestDto request);

        /**
         * Cancel scheduled notification.
         * 
         * @param notificationId notification ID
         * @return true if cancelled successfully
         */
        boolean cancelScheduledNotification(UUID notificationId);

        /**
         * Process pending scheduled notifications.
         * Called by scheduler.
         */
        void processScheduledNotifications();

        // ========================================
        // Analytics & Tracking
        // ========================================

        /**
         * Record notification open (callback from app).
         * 
         * @param notificationId notification ID
         * @param deviceId       device that opened
         */
        void recordNotificationOpened(UUID notificationId, UUID deviceId);

        /**
         * Get notification by ID.
         * 
         * @param notificationId notification ID
         * @return notification details
         */
        PushNotificationResponseDto getNotification(UUID notificationId);

        /**
         * Get recent notifications.
         * 
         * @param pageable pagination
         * @return page of notifications
         */
        Page<PushNotificationResponseDto> getRecentNotifications(Pageable pageable);

        /**
         * Get notifications for a newsapp newsapp.
         * 
         * @param newsId newsapp ID
         * @return page of notifications
         */
        Page<PushNotificationResponseDto> getNotificationsForNews(UUID newsId, Pageable pageable);

        /**
         * Get notification statistics.
         * 
         * @param startDate start of period
         * @param endDate   end of period
         * @return statistics
         */
        NotificationStatistics getStatistics(Instant startDate, Instant endDate);

        // ========================================
        // Retry & Maintenance
        // ========================================

        /**
         * Retry failed notifications.
         * Called by scheduler.
         */
        void retryFailedNotifications();

        /**
         * Clean up old notification records.
         * 
         * @param daysToKeep number of days to keep records
         * @return count of deleted records
         */
        int cleanupOldNotifications(int daysToKeep);

        /**
         * Notification statistics DTO.
         */
        record NotificationStatistics(
                        long totalSent,
                        long totalDelivered,
                        long totalFailed,
                        long totalOpened,
                        double deliveryRate,
                        double openRate,
                        java.util.Map<String, Long> byType,
                        java.util.Map<String, Long> byStatus) {
        }

        /**
         * Check if FCM service is healthy.
         *
         * @return true if healthy
         */
        boolean isFcmServiceHealthy();

        /**
         * Check if database is healthy.
         *
         * @return true if healthy
         */
        boolean isDatabaseHealthy();

        /**
         * Check if queue is healthy.
         *
         * @return true if healthy
         */
        boolean isQueueHealthy();
}
