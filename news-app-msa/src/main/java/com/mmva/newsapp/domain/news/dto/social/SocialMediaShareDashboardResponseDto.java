package com.mmva.newsapp.domain.news.dto.social;

// ===============================
// Core Java Imports
// ===============================
import java.util.List;

// ===============================
// OpenAPI/Swagger Imports
// ===============================
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for social sharing dashboard
 * Contains lists of news that need sharing at different priority levels
 */
@Schema(description = "Social sharing dashboard response")
public class SocialMediaShareDashboardResponseDto {

    @Schema(description = "High priority items (breaking news, just published)", example = "2")
    private List<SocialMediaShareDashboardItemDto> highPriority;

    @Schema(description = "Medium priority items (recently published)", example = "5")
    private List<SocialMediaShareDashboardItemDto> mediumPriority;

    @Schema(description = "Low priority items (older published news)", example = "8")
    private List<SocialMediaShareDashboardItemDto> lowPriority;

    @Schema(description = "Scheduled items ready for sharing (published but not shared yet)", example = "3")
    private List<SocialMediaShareDashboardItemDto> scheduledReady;

    @Schema(description = "Total count of items needing attention", example = "18")
    private Integer totalCount;

    @Schema(description = "Count of high priority items", example = "2")
    private Integer highPriorityCount;

    @Schema(description = "Count of medium priority items", example = "5")
    private Integer mediumPriorityCount;

    @Schema(description = "Count of low priority items", example = "8")
    private Integer lowPriorityCount;

    @Schema(description = "Count of scheduled items ready", example = "3")
    private Integer scheduledReadyCount;

    @Schema(description = "Last updated timestamp", example = "2026-02-06T14:30:00Z")
    private String lastUpdated;

    // ===============================
    // Constructors
    // ===============================

    public SocialMediaShareDashboardResponseDto() {
    }

    public SocialMediaShareDashboardResponseDto(
            List<SocialMediaShareDashboardItemDto> highPriority,
            List<SocialMediaShareDashboardItemDto> mediumPriority,
            List<SocialMediaShareDashboardItemDto> lowPriority,
            List<SocialMediaShareDashboardItemDto> scheduledReady) {

        this.highPriority = highPriority;
        this.mediumPriority = mediumPriority;
        this.lowPriority = lowPriority;
        this.scheduledReady = scheduledReady;

        this.highPriorityCount = highPriority != null ? highPriority.size() : 0;
        this.mediumPriorityCount = mediumPriority != null ? mediumPriority.size() : 0;
        this.lowPriorityCount = lowPriority != null ? lowPriority.size() : 0;
        this.scheduledReadyCount = scheduledReady != null ? scheduledReady.size() : 0;
        this.totalCount = this.highPriorityCount + this.mediumPriorityCount +
                this.lowPriorityCount + this.scheduledReadyCount;
    }

    // ===============================
    // Getters and Setters
    // ===============================

    public List<SocialMediaShareDashboardItemDto> getHighPriority() {
        return highPriority;
    }

    public void setHighPriority(List<SocialMediaShareDashboardItemDto> highPriority) {
        this.highPriority = highPriority;
        this.highPriorityCount = highPriority != null ? highPriority.size() : 0;
        updateTotalCount();
    }

    public List<SocialMediaShareDashboardItemDto> getMediumPriority() {
        return mediumPriority;
    }

    public void setMediumPriority(List<SocialMediaShareDashboardItemDto> mediumPriority) {
        this.mediumPriority = mediumPriority;
        this.mediumPriorityCount = mediumPriority != null ? mediumPriority.size() : 0;
        updateTotalCount();
    }

    public List<SocialMediaShareDashboardItemDto> getLowPriority() {
        return lowPriority;
    }

    public void setLowPriority(List<SocialMediaShareDashboardItemDto> lowPriority) {
        this.lowPriority = lowPriority;
        this.lowPriorityCount = lowPriority != null ? lowPriority.size() : 0;
        updateTotalCount();
    }

    public List<SocialMediaShareDashboardItemDto> getScheduledReady() {
        return scheduledReady;
    }

    public void setScheduledReady(List<SocialMediaShareDashboardItemDto> scheduledReady) {
        this.scheduledReady = scheduledReady;
        this.scheduledReadyCount = scheduledReady != null ? scheduledReady.size() : 0;
        updateTotalCount();
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getHighPriorityCount() {
        return highPriorityCount;
    }

    public void setHighPriorityCount(Integer highPriorityCount) {
        this.highPriorityCount = highPriorityCount;
    }

    public Integer getMediumPriorityCount() {
        return mediumPriorityCount;
    }

    public void setMediumPriorityCount(Integer mediumPriorityCount) {
        this.mediumPriorityCount = mediumPriorityCount;
    }

    public Integer getLowPriorityCount() {
        return lowPriorityCount;
    }

    public void setLowPriorityCount(Integer lowPriorityCount) {
        this.lowPriorityCount = lowPriorityCount;
    }

    public Integer getScheduledReadyCount() {
        return scheduledReadyCount;
    }

    public void setScheduledReadyCount(Integer scheduledReadyCount) {
        this.scheduledReadyCount = scheduledReadyCount;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // ===============================
    // Helper Methods
    // ===============================

    private void updateTotalCount() {
        this.totalCount = (highPriorityCount != null ? highPriorityCount : 0) +
                (mediumPriorityCount != null ? mediumPriorityCount : 0) +
                (lowPriorityCount != null ? lowPriorityCount : 0) +
                (scheduledReadyCount != null ? scheduledReadyCount : 0);
    }
}