package com.mmva.newsapp.domain.news.dto.social;

// ===============================
// Core Java Imports
// ===============================
import java.time.Instant;
import java.util.List;
import java.util.Map;

// ===============================
// OpenAPI/Swagger Imports
// ===============================
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for social sharing dashboard data with per-platform tracking
 * Shows news articles that need social media sharing with detailed platform
 * status
 */
@Schema(description = "Social sharing dashboard item with platform tracking")
public class SocialMediaShareDashboardItemDto {

    @Schema(description = "News article ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String newsId;

    @Schema(description = "News title in English", example = "Breaking News: Major Event")
    private String titleEn;

    @Schema(description = "News title in Spanish", example = "Noticia de Última Hora: Evento Mayor")
    private String titleEs;

    @Schema(description = "News workflow status", example = "PUBLISHED")
    private String workflowStatus;

    @Schema(description = "When the news was published", example = "2026-02-06T10:30:00Z")
    private Instant publishedAt;

    @Schema(description = "When the news was scheduled to publish (for scheduled news)", example = "2026-02-06T09:00:00Z")
    private Instant scheduledPublishAt;

    @Schema(description = "Social sharing enabled for this news", example = "true")
    private Boolean socialSharingEnabled;

    @Schema(description = "Target platforms for sharing", example = "[\"WHATSAPP\", \"FACEBOOK\", \"TWITTER\", \"INSTAGRAM\"]")
    private List<String> targetPlatforms;

    @Schema(description = "Detailed status for each platform", example = "{\"WHATSAPP\": {\"status\": \"COMPLETED\", \"sharedAt\": \"2026-02-06T10:35:00Z\"}, \"FACEBOOK\": {\"status\": \"PENDING\"}}")
    private Map<String, PlatformStatusDetail> platformStatuses;

    @Schema(description = "Overall sharing completion status", example = "PARTIALLY_SHARED")
    private String overallStatus; // NOT_STARTED, PARTIALLY_SHARED, FULLY_SHARED

    @Schema(description = "Number of platforms completed", example = "3")
    private Integer completedPlatformsCount;

    @Schema(description = "Total number of target platforms", example = "8")
    private Integer totalPlatformsCount;

    @Schema(description = "Completion percentage", example = "37.5")
    private Double completionPercentage;

    @Schema(description = "Pre-generated share texts for each platform")
    private Map<String, String> shareTexts;

    @Schema(description = "Short URL for sharing", example = "https://news.example.com/n/abc123")
    private String shortUrl;

    @Schema(description = "Priority level for sharing", example = "HIGH")
    private String priority; // HIGH, MEDIUM, LOW

    @Schema(description = "Time elapsed since publishing (for published news)", example = "35 minutes ago")
    private String timeSincePublished;

    @Schema(description = "Is this breaking news", example = "true")
    private Boolean isBreaking;

    @Schema(description = "Main media file URL for sharing", example = "https://media.example.com/uploads/news-image.jpg")
    private String mediaFileUrl;

    @Schema(description = "Media file name", example = "earthquake-breaking-news.jpg")
    private String mediaFileName;

    @Schema(description = "Media file type", example = "image/jpeg")
    private String mediaFileType;

    @Schema(description = "Media type (image, video, etc.)", example = "image")
    private String mediaType;

    @Schema(description = "Thumbnail URL for preview", example = "https://media.example.com/thumbnails/news-thumb.jpg")
    private String thumbnailUrl;

    // ===============================
    // Inner Classes
    // ===============================

    /**
     * Details about a platform's sharing status
     */
    @Schema(description = "Platform sharing status details")
    public static class PlatformStatusDetail {

        @Schema(description = "Status of sharing to this platform", example = "COMPLETED")
        private String status; // PENDING, COMPLETED, FAILED, SKIPPED

        @Schema(description = "When this platform was shared", example = "2026-02-06T10:35:00Z")
        private Instant sharedAt;

        @Schema(description = "Notes about sharing to this platform", example = "Shared to main channel")
        private String notes;

        @Schema(description = "ID of the user who performed the sharing", example = "123e4567-e89b-12d3-a456-426614174000")
        private String sharedBy;

        // Constructors
        public PlatformStatusDetail() {
        }

        public PlatformStatusDetail(String status, Instant sharedAt, String notes) {
            this.status = status;
            this.sharedAt = sharedAt;
            this.notes = notes;
        }

        public PlatformStatusDetail(String status, Instant sharedAt, String notes, String sharedBy) {
            this.status = status;
            this.sharedAt = sharedAt;
            this.notes = notes;
            this.sharedBy = sharedBy;
        }

        // Getters and Setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Instant getSharedAt() {
            return sharedAt;
        }

        public void setSharedAt(Instant sharedAt) {
            this.sharedAt = sharedAt;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getSharedBy() {
            return sharedBy;
        }

        public void setSharedBy(String sharedBy) {
            this.sharedBy = sharedBy;
        }
    }

    // ===============================
    // Constructors
    // ===============================

    public SocialMediaShareDashboardItemDto() {
    }

    // ===============================
    // Getters and Setters
    // ===============================

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getTitleEs() {
        return titleEs;
    }

    public void setTitleEs(String titleEs) {
        this.titleEs = titleEs;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(String workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getScheduledPublishAt() {
        return scheduledPublishAt;
    }

    public void setScheduledPublishAt(Instant scheduledPublishAt) {
        this.scheduledPublishAt = scheduledPublishAt;
    }

    public Boolean getSocialSharingEnabled() {
        return socialSharingEnabled;
    }

    public void setSocialSharingEnabled(Boolean socialSharingEnabled) {
        this.socialSharingEnabled = socialSharingEnabled;
    }

    public List<String> getTargetPlatforms() {
        return targetPlatforms;
    }

    public void setTargetPlatforms(List<String> targetPlatforms) {
        this.targetPlatforms = targetPlatforms;
    }

    public Map<String, PlatformStatusDetail> getPlatformStatuses() {
        return platformStatuses;
    }

    public void setPlatformStatuses(Map<String, PlatformStatusDetail> platformStatuses) {
        this.platformStatuses = platformStatuses;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public Integer getCompletedPlatformsCount() {
        return completedPlatformsCount;
    }

    public void setCompletedPlatformsCount(Integer completedPlatformsCount) {
        this.completedPlatformsCount = completedPlatformsCount;
    }

    public Integer getTotalPlatformsCount() {
        return totalPlatformsCount;
    }

    public void setTotalPlatformsCount(Integer totalPlatformsCount) {
        this.totalPlatformsCount = totalPlatformsCount;
    }

    public Double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public Map<String, String> getShareTexts() {
        return shareTexts;
    }

    public void setShareTexts(Map<String, String> shareTexts) {
        this.shareTexts = shareTexts;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTimeSincePublished() {
        return timeSincePublished;
    }

    public void setTimeSincePublished(String timeSincePublished) {
        this.timeSincePublished = timeSincePublished;
    }

    public Boolean getIsBreaking() {
        return isBreaking;
    }

    public void setIsBreaking(Boolean isBreaking) {
        this.isBreaking = isBreaking;
    }

    public String getMediaFileUrl() {
        return mediaFileUrl;
    }

    public void setMediaFileUrl(String mediaFileUrl) {
        this.mediaFileUrl = mediaFileUrl;
    }

    public String getMediaFileName() {
        return mediaFileName;
    }

    public void setMediaFileName(String mediaFileName) {
        this.mediaFileName = mediaFileName;
    }

    public String getMediaFileType() {
        return mediaFileType;
    }

    public void setMediaFileType(String mediaFileType) {
        this.mediaFileType = mediaFileType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}