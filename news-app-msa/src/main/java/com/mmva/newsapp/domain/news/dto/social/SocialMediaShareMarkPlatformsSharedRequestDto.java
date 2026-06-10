package com.mmva.newsapp.domain.news.dto.social;

// ===============================
// Core Java Imports
// ===============================
import java.util.List;
import java.util.UUID;

// ===============================
// OpenAPI/Swagger Imports
// ===============================
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/**
 * Request DTO for marking multiple platforms as shared for a news article
 */
@Schema(description = "Request to mark multiple social media platforms as shared")
public class SocialMediaShareMarkPlatformsSharedRequestDto {

    @Schema(description = "News article ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = RequiredMode.REQUIRED)
    private UUID newsId;

    @Schema(description = "List of platform names to mark as shared", example = "[\"FACEBOOK\", \"TWITTER\", \"INSTAGRAM\"]", requiredMode = RequiredMode.REQUIRED)
    private List<String> platforms;

    @Schema(description = "ID of the user performing the sharing action", example = "456e7890-e89b-12d3-a456-426614174001", requiredMode = RequiredMode.REQUIRED)
    private UUID sharedBy;

    @Schema(description = "Optional notes about the sharing", example = "Shared during morning batch at 9:15 AM")
    private String notes;

    // ===============================
    // Constructors
    // ===============================

    public SocialMediaShareMarkPlatformsSharedRequestDto() {
    }

    public SocialMediaShareMarkPlatformsSharedRequestDto(UUID newsId, List<String> platforms, UUID sharedBy) {
        this.newsId = newsId;
        this.platforms = platforms;
        this.sharedBy = sharedBy;
    }

    // ===============================
    // Getters and Setters
    // ===============================

    public UUID getNewsId() {
        return newsId;
    }

    public void setNewsId(UUID newsId) {
        this.newsId = newsId;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public UUID getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(UUID sharedBy) {
        this.sharedBy = sharedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}