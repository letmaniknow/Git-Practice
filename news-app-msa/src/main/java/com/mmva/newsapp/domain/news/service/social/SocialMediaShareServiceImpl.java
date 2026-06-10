package com.mmva.newsapp.domain.news.service.social;

// ===============================
// Core Java Imports
// ===============================
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

// ===============================
// Spring Framework Imports
// ===============================
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ===============================
// Lombok Imports
// ===============================
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ===============================
// Project Imports
// ===============================
import com.mmva.newsapp.domain.news.dto.social.SocialMediaShareMarkPlatformSharedRequestDto;
import com.mmva.newsapp.domain.news.dto.social.SocialMediaShareDashboardItemDto;
import com.mmva.newsapp.domain.news.dto.social.SocialMediaShareDashboardResponseDto;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.model.core.SocialMediaShareEntity;
import com.mmva.newsapp.domain.news.model.core.SocialMediaPlatformStatusEntity;
import com.mmva.newsapp.domain.news.repository.core.NewsRepository;
import com.mmva.newsapp.domain.news.repository.core.SocialMediaShareRepository;
import com.mmva.newsapp.domain.news.repository.core.SocialMediaPlatformStatusRepository;

/**
 * Service for managing social media sharing operations with per-platform
 * tracking
 * Handles dashboard data, platform-specific status updates, and share text
 * generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SocialMediaShareServiceImpl implements SocialMediaShareService {

    private final NewsRepository newsRepository;
    private final SocialMediaShareRepository sharingRepository;
    private final SocialMediaPlatformStatusRepository platformStatusRepository;

    // ===============================
    // Constants
    // ===============================

    private static final int HIGH_PRIORITY_HOURS = 1; // Within 1 hour
    private static final int MEDIUM_PRIORITY_HOURS = 6; // Within 6 hours
    private static final int MAX_DASHBOARD_ITEMS = 50; // Max items per priority level

    // ===============================
    // Dashboard Methods
    // ===============================

    /**
     * Get social sharing dashboard data with per-platform tracking
     * Returns news items that need social sharing, organized by priority
     */
    @Transactional(readOnly = true)
    public SocialMediaShareDashboardResponseDto getSocialMediaSharingDashboard() {
        log.debug("Fetching social sharing dashboard data with platform tracking");

        Instant now = Instant.now();

        // Get all sharing records that are enabled and not fully shared
        List<SocialMediaShareEntity> incompleteSharingRecords = sharingRepository
                .findIncompleteSharingRecords();

        // Get the news entities for these sharing records
        Set<UUID> newsIds = incompleteSharingRecords.stream()
                .map(SocialMediaShareEntity::getNewsId)
                .collect(Collectors.toSet());

        List<NewsMasterEntity> newsEntities = newsRepository.findByNewsIds(new ArrayList<>(newsIds));

        // Create maps for efficient lookup
        Map<UUID, NewsMasterEntity> newsMap = newsEntities.stream()
                .collect(Collectors.toMap(NewsMasterEntity::getNewsNewsId, n -> n));

        Map<UUID, SocialMediaShareEntity> sharingMap = incompleteSharingRecords.stream()
                .collect(Collectors.toMap(SocialMediaShareEntity::getNewsId, s -> s));

        // Separate published and scheduled news
        List<NewsMasterEntity> publishedNeedingShares = new ArrayList<>();
        List<NewsMasterEntity> scheduledPublishedNeedingShares = new ArrayList<>();

        for (NewsMasterEntity news : newsEntities) {
            if ("PUBLISHED".equals(news.getNewsWorkflowStatus()) && Boolean.TRUE.equals(news.getNewsIsActive())) {
                publishedNeedingShares.add(news);
            } else if ("SCHEDULED".equals(news.getNewsWorkflowStatus())) {
                scheduledPublishedNeedingShares.add(news);
            }
        }

        // Combine and categorize by priority
        List<SocialMediaShareDashboardItemDto> highPriority = new ArrayList<>();
        List<SocialMediaShareDashboardItemDto> mediumPriority = new ArrayList<>();
        List<SocialMediaShareDashboardItemDto> lowPriority = new ArrayList<>();
        List<SocialMediaShareDashboardItemDto> scheduledReady = new ArrayList<>();

        // Process published news
        for (NewsMasterEntity news : publishedNeedingShares) {
            SocialMediaShareEntity sharing = sharingMap.get(news.getNewsNewsId());
            if (sharing != null && !sharing.isFullyShared()) {
                SocialMediaShareDashboardItemDto item = convertToDashboardItem(news, sharing, now);
                categorizeByPriority(item, highPriority, mediumPriority, lowPriority);
            }
        }

        // Process scheduled news
        for (NewsMasterEntity news : scheduledPublishedNeedingShares) {
            SocialMediaShareEntity sharing = sharingMap.get(news.getNewsNewsId());
            if (sharing != null && !sharing.isFullyShared()) {
                SocialMediaShareDashboardItemDto item = convertToDashboardItem(news, sharing, now);
                item.setPriority("SCHEDULED");
                scheduledReady.add(item);
            }
        }

        // Sort by priority and time
        sortDashboardItems(highPriority, mediumPriority, lowPriority, scheduledReady);

        SocialMediaShareDashboardResponseDto response = new SocialMediaShareDashboardResponseDto(
                highPriority, mediumPriority, lowPriority, scheduledReady);

        response.setLastUpdated(now.toString());

        log.debug("Social sharing dashboard: {} high, {} medium, {} low, {} scheduled",
                highPriority.size(), mediumPriority.size(), lowPriority.size(), scheduledReady.size());

        return response;
    }

    // ===============================
    // Platform Sharing Methods
    // ===============================

    /**
     * Mark a specific platform as shared for a news article
     */
    @Transactional
    public void markPlatformShared(SocialMediaShareMarkPlatformSharedRequestDto request) {
        log.debug("Marking platform {} as shared for news: {}", request.getPlatform(), request.getNewsId());

        SocialMediaShareEntity sharing = sharingRepository.findByNewsId(request.getNewsId())
                .orElseThrow(
                        () -> new IllegalStateException("Sharing record not found for news: " + request.getNewsId()));

        // Find or create the platform status record
        SocialMediaPlatformStatusEntity platformStatus = platformStatusRepository
                .findBySharing_SharingIdAndPlatform(sharing.getSharingId(), request.getPlatform());

        if (platformStatus == null) {
            // Create new platform status record
            platformStatus = SocialMediaPlatformStatusEntity.builder()
                    .sharing(sharing)
                    .platform(request.getPlatform())
                    .status(SocialMediaShareEntity.STATUS_COMPLETED)
                    .sharedAt(request.getSharedAt() != null ? request.getSharedAt() : Instant.now())
                    .sharedBy(request.getSharedBy())
                    .notes(request.getNotes())
                    .build();
        } else {
            // Update existing platform status
            platformStatus.markCompleted(
                    request.getSharedAt() != null ? request.getSharedAt() : Instant.now(),
                    request.getNotes());
            platformStatus.setSharedBy(request.getSharedBy());
        }

        platformStatusRepository.save(platformStatus);

        log.info("Marked platform {} as shared for news article {}", request.getPlatform(), request.getNewsId());
    }

    /**
     * Mark multiple platforms as shared for a news article
     */
    @Transactional
    public void markPlatformsShared(UUID newsId, List<String> platforms, UUID sharedBy) {
        log.debug("Marking platforms {} as shared for news: {}", platforms, newsId);

        SocialMediaShareEntity sharing = sharingRepository.findByNewsId(newsId)
                .orElseThrow(() -> new IllegalStateException("Sharing record not found for news: " + newsId));

        Instant now = Instant.now();
        List<SocialMediaPlatformStatusEntity> statusUpdates = new ArrayList<>();

        for (String platform : platforms) {
            SocialMediaPlatformStatusEntity platformStatus = platformStatusRepository
                    .findBySharing_SharingIdAndPlatform(sharing.getSharingId(), platform);

            if (platformStatus == null) {
                // Create new platform status record
                platformStatus = SocialMediaPlatformStatusEntity.builder()
                        .sharing(sharing)
                        .platform(platform)
                        .status(SocialMediaShareEntity.STATUS_COMPLETED)
                        .sharedAt(now)
                        .sharedBy(sharedBy)
                        .build();
            } else {
                // Update existing platform status
                platformStatus.markCompleted(now, null);
                platformStatus.setSharedBy(sharedBy);
            }

            statusUpdates.add(platformStatus);
        }

        platformStatusRepository.saveAll(statusUpdates);

        log.info("Marked {} platforms as shared for news article {}", platforms.size(), newsId);
    }

    /**
     * Create or ensure sharing record exists for a news article
     */
    @Transactional
    public void ensureSharingRecordExists(UUID newsId) {
        log.debug("Ensuring sharing record exists for news: {}", newsId);

        if (!sharingRepository.existsByNewsId(newsId)) {
            SocialMediaShareEntity sharing = SocialMediaShareEntity.builder()
                    .newsId(newsId)
                    .sharingEnabled(true)
                    .targetPlatforms("WHATSAPP,FACEBOOK,TWITTER,INSTAGRAM,LINKEDIN,TELEGRAM,TIKTOK,YOUTUBE")
                    .build();

            sharingRepository.save(sharing);

            // Initialize platform status records
            sharing.initializePlatformStatuses();
            sharingRepository.save(sharing);

            log.info("Created sharing record for news article {}", newsId);
        } else {
            log.debug("Sharing record already exists for news: {}", newsId);
        }
    }

    // ===============================
    // Statistics Methods
    // ===============================

    /**
     * Get sharing statistics for dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSharingStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<SocialMediaShareEntity> allEnabledRecords = sharingRepository.findEnabledSharingRecords();

        long totalSharingRecords = allEnabledRecords.size();
        long fullyShared = allEnabledRecords.stream()
                .mapToLong(record -> record.isFullyShared() ? 1L : 0L)
                .sum();
        long needingAttention = totalSharingRecords - fullyShared;

        stats.put("totalArticles", totalSharingRecords);
        stats.put("fullyShared", fullyShared);
        stats.put("needingAttention", needingAttention);
        stats.put("completionRate", totalSharingRecords > 0 ? (double) fullyShared / totalSharingRecords * 100 : 0);

        return stats;
    }

    // ===============================
    // Share Text Generation
    // ===============================

    /**
     * Generate share texts for a news article
     */
    public Map<String, String> generateShareTexts(NewsMasterEntity news) {
        Map<String, String> shareTexts = new HashMap<>();

        String title = news.getNewsTitleEn();
        String excerpt = news.getNewsExcerptEn() != null ? news.getNewsExcerptEn()
                : generateExcerpt(news.getNewsContentEn());
        String url = generateShortUrl(news.getNewsNewsId());
        boolean hasMedia = news.getNewsMediaFileUrl() != null && !news.getNewsMediaFileUrl().trim().isEmpty();

        // WhatsApp (most formatting support)
        shareTexts.put("WHATSAPP", String.format(
                "📰 *%s*\n\n%s\n\n📖 Read more: %s\n\n#%s #NewsUpdate%s",
                title, excerpt, url, generateHashtags(news),
                hasMedia ? "\n\n📎 Attach: " + news.getNewsMediaFileName() : ""));

        // Facebook (rich formatting)
        shareTexts.put("FACEBOOK", String.format(
                "📰 %s\n\n%s\n\nRead the full story: %s\n\n#%s%s",
                title, excerpt, url, generateHashtags(news),
                hasMedia ? "\n\n📎 Attach image/video when posting" : ""));

        // Twitter (character limit)
        String twitterText = String.format("%s %s #%s%s", title, url, generateHashtags(news),
                hasMedia ? " 📷" : "");
        if (twitterText.length() > 280) {
            twitterText = String.format("%.270s... %s #%s%s", title, url, generateHashtags(news),
                    hasMedia ? " 📷" : "");
        }
        shareTexts.put("TWITTER", twitterText);

        // LinkedIn (professional)
        shareTexts.put("LINKEDIN", String.format(
                "%s\n\n%s\n\nRead more: %s\n\n#%s #BusinessNews%s",
                title, excerpt, url, generateHashtags(news),
                hasMedia ? "\n\n📎 Attach featured image" : ""));

        // Instagram (visual focus)
        shareTexts.put("INSTAGRAM", String.format(
                "📰 %s\n\n%s\n\nLink in bio 🔗\n\n#%s #NewsUpdate%s",
                title, excerpt, generateHashtags(news),
                hasMedia ? "\n\n📎 REQUIRED: Attach image/video" : "\n\n📎 Add relevant image"));

        // Telegram (clean formatting)
        shareTexts.put("TELEGRAM", String.format(
                "📰 *%s*\n\n%s\n\n[Read more](%s)\n\n#%s%s",
                title, excerpt, url, generateHashtags(news),
                hasMedia ? "\n\n📎 Attach: " + news.getNewsMediaFileName() : ""));

        // TikTok (short and engaging)
        shareTexts.put("TIKTOK", String.format(
                "📰 %s\n\nBreaking news! Full story linked 🔗\n\n#%s #News%s",
                title, generateHashtags(news),
                hasMedia ? "\n\n📎 Use video/image for engagement" : ""));

        // YouTube (video focus)
        shareTexts.put("YOUTUBE", String.format(
                "📰 %s\n\n%s\n\nWatch our latest coverage: %s\n\n#%s%s",
                title, excerpt, url, generateHashtags(news),
                hasMedia ? "\n\n📎 Upload video thumbnail" : ""));

        return shareTexts;
    }

    // ===============================
    // Helper Methods
    // ===============================

    private SocialMediaShareDashboardItemDto convertToDashboardItem(NewsMasterEntity news,
            SocialMediaShareEntity sharing, Instant now) {
        SocialMediaShareDashboardItemDto item = new SocialMediaShareDashboardItemDto();

        item.setNewsId(news.getNewsNewsId().toString());
        item.setTitleEn(news.getNewsTitleEn());
        item.setTitleEs(news.getNewsTitleEs());
        item.setWorkflowStatus(news.getNewsWorkflowStatus().toString());
        item.setPublishedAt(news.getNewsPublishedAt());
        item.setScheduledPublishAt(news.getNewsScheduledPublishAt());
        item.setSocialSharingEnabled(sharing.getSharingEnabled());
        item.setTargetPlatforms(sharing.getTargetPlatformsList());
        item.setPlatformStatuses(getPlatformStatusesWithDetails(sharing));

        // Calculate completion stats
        int totalPlatforms = sharing.getTargetPlatformsList().size();
        int completedPlatforms = sharing.getCompletedPlatformsCount();
        item.setTotalPlatformsCount(totalPlatforms);
        item.setCompletedPlatformsCount(completedPlatforms);
        item.setCompletionPercentage(totalPlatforms > 0 ? (double) completedPlatforms / totalPlatforms * 100 : 0);

        // Set overall status
        if (completedPlatforms == 0) {
            item.setOverallStatus("NOT_STARTED");
        } else if (completedPlatforms == totalPlatforms) {
            item.setOverallStatus("FULLY_SHARED");
        } else {
            item.setOverallStatus("PARTIALLY_SHARED");
        }

        // Generate share texts
        item.setShareTexts(generateShareTexts(news));
        item.setShortUrl(generateShortUrl(news.getNewsNewsId()));
        item.setIsBreaking(news.getNewsIsBreaking());

        // Set media information for sharing
        item.setMediaFileUrl(news.getNewsMediaFileUrl());
        item.setMediaFileName(news.getNewsMediaFileName());
        item.setMediaFileType(news.getNewsMediaFileType());
        item.setMediaType(news.getNewsMediaType());
        item.setThumbnailUrl(news.getNewsThumbnailUrl());

        // Calculate time since published
        if (news.getNewsPublishedAt() != null) {
            Duration timeSince = Duration.between(news.getNewsPublishedAt(), now);
            item.setTimeSincePublished(formatDuration(timeSince));
        }

        return item;
    }

    private Map<String, SocialMediaShareDashboardItemDto.PlatformStatusDetail> getPlatformStatusesWithDetails(
            SocialMediaShareEntity sharing) {
        Map<String, SocialMediaShareDashboardItemDto.PlatformStatusDetail> details = new HashMap<>();

        // Create status map from platform status entities
        Map<String, SocialMediaPlatformStatusEntity> statusMap = new HashMap<>();
        if (sharing.getPlatformStatuses() != null) {
            for (SocialMediaPlatformStatusEntity status : sharing.getPlatformStatuses()) {
                statusMap.put(status.getPlatform(), status);
            }
        }

        for (String platform : sharing.getTargetPlatformsList()) {
            SocialMediaPlatformStatusEntity statusEntity = statusMap.get(platform);
            if (statusEntity != null) {
                details.put(platform, new SocialMediaShareDashboardItemDto.PlatformStatusDetail(
                        statusEntity.getStatus(),
                        statusEntity.getSharedAt(),
                        statusEntity.getNotes(),
                        statusEntity.getSharedBy() != null ? statusEntity.getSharedBy().toString() : null));
            } else {
                // Platform not yet initialized
                details.put(platform, new SocialMediaShareDashboardItemDto.PlatformStatusDetail(
                        SocialMediaShareEntity.STATUS_PENDING, null, null, null));
            }
        }

        return details;
    }

    private void categorizeByPriority(SocialMediaShareDashboardItemDto item,
            List<SocialMediaShareDashboardItemDto> highPriority,
            List<SocialMediaShareDashboardItemDto> mediumPriority,
            List<SocialMediaShareDashboardItemDto> lowPriority) {

        if (item.getPublishedAt() == null)
            return;

        Instant now = Instant.now();
        Duration timeSincePublished = Duration.between(item.getPublishedAt(), now);
        long hoursSincePublished = timeSincePublished.toHours();

        // Breaking news is always high priority
        if (Boolean.TRUE.equals(item.getIsBreaking()) && !"FULLY_SHARED".equals(item.getOverallStatus())) {
            item.setPriority("HIGH");
            highPriority.add(item);
            return;
        }

        // Items with low completion are higher priority
        double completion = item.getCompletionPercentage() != null ? item.getCompletionPercentage() : 0;
        if (completion < 50) {
            // Prioritize by time for incomplete items
            if (hoursSincePublished <= HIGH_PRIORITY_HOURS) {
                item.setPriority("HIGH");
                highPriority.add(item);
            } else if (hoursSincePublished <= MEDIUM_PRIORITY_HOURS) {
                item.setPriority("MEDIUM");
                mediumPriority.add(item);
            } else {
                item.setPriority("LOW");
                lowPriority.add(item);
            }
        } else {
            // Nearly complete items get lower priority
            item.setPriority("LOW");
            lowPriority.add(item);
        }
    }

    @SuppressWarnings({ "unchecked", "varargs" })
    private void sortDashboardItems(List<SocialMediaShareDashboardItemDto>... lists) {
        for (List<SocialMediaShareDashboardItemDto> list : lists) {
            // Sort by completion percentage (lowest first), then by published time (newest
            // first)
            list.sort((a, b) -> {
                // Breaking news first
                if (Boolean.TRUE.equals(a.getIsBreaking()) && !Boolean.TRUE.equals(b.getIsBreaking())) {
                    return -1;
                }
                if (!Boolean.TRUE.equals(a.getIsBreaking()) && Boolean.TRUE.equals(b.getIsBreaking())) {
                    return 1;
                }

                // Lower completion percentage first
                double completionA = a.getCompletionPercentage() != null ? a.getCompletionPercentage() : 0;
                double completionB = b.getCompletionPercentage() != null ? b.getCompletionPercentage() : 0;
                int completionCompare = Double.compare(completionA, completionB);
                if (completionCompare != 0) {
                    return completionCompare;
                }

                // Then by published time (newest first)
                if (a.getPublishedAt() != null && b.getPublishedAt() != null) {
                    return b.getPublishedAt().compareTo(a.getPublishedAt());
                }

                return 0;
            });

            // Limit to max items
            if (list.size() > MAX_DASHBOARD_ITEMS) {
                list.subList(MAX_DASHBOARD_ITEMS, list.size()).clear();
            }
        }
    }

    private String generateShortUrl(UUID newsId) {
        // In a real implementation, this would use a URL shortener service
        return "https://news.example.com/n/" + newsId.toString().substring(0, 8);
    }

    private String generateExcerpt(String content) {
        if (content == null)
            return "";
        String cleanContent = content.replaceAll("<[^>]*>", "").trim();
        return cleanContent.length() > 150 ? cleanContent.substring(0, 147) + "..." : cleanContent;
    }

    private String generateHashtags(NewsMasterEntity news) {
        // Generate relevant hashtags based on category and content
        // This is a simplified implementation
        return "News";
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        if (hours > 0) {
            return String.format("%d hours, %d minutes ago", hours, minutes);
        } else {
            return String.format("%d minutes ago", minutes);
        }
    }
}