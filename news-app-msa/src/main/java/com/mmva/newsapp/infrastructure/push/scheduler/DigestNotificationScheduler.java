package com.mmva.newsapp.infrastructure.push.scheduler;

import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.repository.core.NewsRepository;
import com.mmva.newsapp.infrastructure.push.dto.DigestNewsItemDto;
import com.mmva.newsapp.infrastructure.push.dto.PushNotificationResponseDto;
import com.mmva.newsapp.infrastructure.push.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Automated Digest Notification Scheduler.
 * 
 * <h3>Industry Best Practice:</h3>
 * <ul>
 * <li><b>Daily Digest:</b> Sent every morning at configured time (default: 8
 * AM)</li>
 * <li><b>Weekly Digest:</b> Sent every Sunday at configured time (default: 9
 * AM)</li>
 * <li><b>Auto-selects:</b> Top trending news from past 24h/7d</li>
 * <li><b>Manual Override:</b> Admins can still send custom digests via API</li>
 * </ul>
 * 
 * <h3>Configuration (application.yaml):</h3>
 * 
 * <pre>
 * app:
 *   push:
 *     digest:
 *       enabled: true
 *       daily-cron: "0 0 8 * * ?"      # 8 AM daily
 *       weekly-cron: "0 0 9 ? * SUN"   # 9 AM Sunday
 *       daily-count: 5                  # Top 5 news for daily
 *       weekly-count: 10                # Top 10 news for weekly
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DigestNotificationScheduler {

    private final PushNotificationService pushNotificationService;
    private final NewsRepository newsRepository;

    @Value("${push.digest.enabled:true}")
    private boolean digestEnabled;

    @Value("${push.digest.daily-count:5}")
    private int dailyDigestCount;

    @Value("${push.digest.weekly-count:10}")
    private int weeklyDigestCount;

    @Value("${app.base-url:https://app.example.com}")
    private String baseUrl;

    // ========================================
    // Daily Digest - Every day at 8 AM
    // ========================================

    /**
     * Automatically sends daily digest notification every morning.
     * 
     * <p>
     * Cron: 0 0 8 * * ? = At 08:00:00 every day
     * </p>
     * 
     * <p>
     * Logic:
     * </p>
     * <ol>
     * <li>Fetches top N trending news from last 24 hours</li>
     * <li>Builds digest with individual thumbnails and deep links</li>
     * <li>Sends to 'daily_digest' topic subscribers</li>
     * </ol>
     */
    @Scheduled(cron = "${push.digest.daily-cron:0 0 8 * * ?}")
    @Transactional(readOnly = true)
    public void sendDailyDigestAutomatically() {
        if (!digestEnabled) {
            log.debug("DigestScheduler: Daily digest disabled, skipping");
            return;
        }

        log.info("DigestScheduler: Starting automatic daily digest generation");

        try {
            // Fetch top trending news
            List<NewsMasterEntity> trendingNews = newsRepository.findTrendingNews(
                    PageRequest.of(0, dailyDigestCount));

            if (trendingNews.isEmpty()) {
                log.warn("DigestScheduler: No trending news found for daily digest, skipping");
                return;
            }

            // Build digest items - FILTER OUT news without thumbnails (mandatory for rich
            // display)
            List<DigestNewsItemDto> newsItems = trendingNews.stream()
                    .filter(news -> news.getNewsThumbnailUrl() != null && !news.getNewsThumbnailUrl().isBlank())
                    .map(this::mapToDigestItem)
                    .collect(Collectors.toList());

            if (newsItems.isEmpty()) {
                log.warn("DigestScheduler: No news with thumbnails found for daily digest, skipping. " +
                        "News must have thumbnail images for digest notifications.");
                return;
            }

            // Use first news thumbnail as featured image
            String featuredImageUrl = newsItems.get(0).getThumbnailUrl();

            // Build title with date
            String title = "📰 Today's Top Stories";
            String body = String.format("%d stories curated for you • %s",
                    newsItems.size(),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d")));

            // Send digest
            PushNotificationResponseDto response = pushNotificationService.sendDailyDigest(
                    title, body, featuredImageUrl, newsItems);

            log.info("DigestScheduler: Daily digest sent successfully - notificationId={}, newsCount={}",
                    response.getNotificationId(), newsItems.size());

        } catch (Exception e) {
            log.error("DigestScheduler: Failed to send daily digest", e);
            // Don't throw - scheduler should continue running
        }
    }

    // ========================================
    // Weekly Digest - Every Sunday at 9 AM
    // ========================================

    /**
     * Automatically sends weekly digest notification every Sunday.
     * 
     * <p>
     * Cron: 0 0 9 ? * SUN = At 09:00:00 every Sunday
     * </p>
     * 
     * <p>
     * Logic:
     * </p>
     * <ol>
     * <li>Fetches top N trending news from last 7 days</li>
     * <li>Builds digest with individual thumbnails and deep links</li>
     * <li>Sends to 'weekly_digest' topic subscribers</li>
     * </ol>
     */
    @Scheduled(cron = "${push.digest.weekly-cron:0 0 9 ? * SUN}")
    @Transactional(readOnly = true)
    public void sendWeeklyDigestAutomatically() {
        if (!digestEnabled) {
            log.debug("DigestScheduler: Weekly digest disabled, skipping");
            return;
        }

        // Extra safety: only run on Sundays (in case cron misconfigured)
        if (LocalDate.now().getDayOfWeek() != DayOfWeek.SUNDAY) {
            log.debug("DigestScheduler: Not Sunday, skipping weekly digest");
            return;
        }

        log.info("DigestScheduler: Starting automatic weekly digest generation");

        try {
            // Fetch top trending news (more items for weekly)
            List<NewsMasterEntity> trendingNews = newsRepository.findTrendingNews(
                    PageRequest.of(0, weeklyDigestCount));

            if (trendingNews.isEmpty()) {
                log.warn("DigestScheduler: No trending news found for weekly digest, skipping");
                return;
            }

            // Build digest items - FILTER OUT news without thumbnails (mandatory for rich
            // display)
            List<DigestNewsItemDto> newsItems = trendingNews.stream()
                    .filter(news -> news.getNewsThumbnailUrl() != null && !news.getNewsThumbnailUrl().isBlank())
                    .map(this::mapToDigestItem)
                    .collect(Collectors.toList());

            if (newsItems.isEmpty()) {
                log.warn("DigestScheduler: No news with thumbnails found for weekly digest, skipping. " +
                        "News must have thumbnail images for digest notifications.");
                return;
            }

            // Use first news thumbnail as featured image
            String featuredImageUrl = newsItems.get(0).getThumbnailUrl();

            // Build title with week info
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.minusDays(6);
            String title = "📅 This Week's Highlights";
            String body = String.format("%d top stories • %s - %s",
                    newsItems.size(),
                    weekStart.format(DateTimeFormatter.ofPattern("MMM d")),
                    today.format(DateTimeFormatter.ofPattern("MMM d")));

            // Send digest
            PushNotificationResponseDto response = pushNotificationService.sendWeeklyDigest(
                    title, body, featuredImageUrl, newsItems);

            log.info("DigestScheduler: Weekly digest sent successfully - notificationId={}, newsCount={}",
                    response.getNotificationId(), newsItems.size());

        } catch (Exception e) {
            log.error("DigestScheduler: Failed to send weekly digest", e);
            // Don't throw - scheduler should continue running
        }
    }

    // ========================================
    // Manual Trigger Methods (for Admin API)
    // ========================================

    /**
     * Manually trigger daily digest (can be called from admin API).
     * Useful for testing or sending special editions.
     * 
     * @return notification response
     */
    public PushNotificationResponseDto triggerDailyDigestNow() {
        log.info("DigestScheduler: Manual daily digest triggered");

        List<NewsMasterEntity> trendingNews = newsRepository.findTrendingNews(
                PageRequest.of(0, dailyDigestCount));

        if (trendingNews.isEmpty()) {
            throw new IllegalStateException("No trending news available for digest");
        }

        // Filter out news without thumbnails (mandatory for rich notification display)
        List<DigestNewsItemDto> newsItems = trendingNews.stream()
                .filter(news -> news.getNewsThumbnailUrl() != null && !news.getNewsThumbnailUrl().isBlank())
                .map(this::mapToDigestItem)
                .collect(Collectors.toList());

        if (newsItems.isEmpty()) {
            throw new IllegalStateException("No trending news with thumbnails available for digest. " +
                    "News must have thumbnail images for push notifications.");
        }

        String featuredImageUrl = newsItems.get(0).getThumbnailUrl();
        String title = "📰 Special Edition: Today's Top Stories";
        String body = String.format("%d stories curated for you", newsItems.size());

        return pushNotificationService.sendDailyDigest(title, body, featuredImageUrl, newsItems);
    }

    /**
     * Manually trigger weekly digest (can be called from admin API).
     * 
     * @return notification response
     */
    public PushNotificationResponseDto triggerWeeklyDigestNow() {
        log.info("DigestScheduler: Manual weekly digest triggered");

        List<NewsMasterEntity> trendingNews = newsRepository.findTrendingNews(
                PageRequest.of(0, weeklyDigestCount));

        if (trendingNews.isEmpty()) {
            throw new IllegalStateException("No trending news available for digest");
        }

        // Filter out news without thumbnails (mandatory for rich notification display)
        List<DigestNewsItemDto> newsItems = trendingNews.stream()
                .filter(news -> news.getNewsThumbnailUrl() != null && !news.getNewsThumbnailUrl().isBlank())
                .map(this::mapToDigestItem)
                .collect(Collectors.toList());

        if (newsItems.isEmpty()) {
            throw new IllegalStateException("No trending news with thumbnails available for digest. " +
                    "News must have thumbnail images for push notifications.");
        }

        String featuredImageUrl = newsItems.get(0).getThumbnailUrl();
        String title = "📅 Special Edition: Week's Highlights";
        String body = String.format("%d top stories from this week", newsItems.size());

        return pushNotificationService.sendWeeklyDigest(title, body, featuredImageUrl, newsItems);
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Maps a NewsMasterEntity to DigestNewsItemDto.
     * Note: Category name would require a join/lookup - using null for now.
     * Consider adding categoryName to NewsMasterEntity or using a service.
     */
    private DigestNewsItemDto mapToDigestItem(NewsMasterEntity news) {
        return DigestNewsItemDto.builder()
                .newsId(news.getNewsNewsId())
                .title(truncateTitle(news.getNewsTitleEn(), 100))
                .thumbnailUrl(news.getNewsThumbnailUrl())
                .category(null) // TODO: Lookup category name by newsNewsCategoryId if needed
                .summary(truncateTitle(news.getNewsExcerptEn(), 150))
                .build();
    }

    /**
     * Truncates text to max length, adding ellipsis if needed.
     */
    private String truncateTitle(String text, int maxLength) {
        if (text == null)
            return null;
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
