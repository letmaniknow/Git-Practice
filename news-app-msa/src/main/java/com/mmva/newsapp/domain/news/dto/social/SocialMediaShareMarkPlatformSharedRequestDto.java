package com.mmva.newsapp.domain.news.dto.social;

// ===============================
// Core Java Imports
// ===============================
import java.time.Instant;
import java.util.UUID;

// ===============================
// OpenAPI/Swagger Imports
// ===============================
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request DTO for marking a specific platform as shared/completed
 */
@Schema(description = "Request to mark a platform sharing as completed")
public class SocialMediaShareMarkPlatformSharedRequestDto {

    @Schema(description = "News article ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private UUID newsId;

    @Schema(description = "Platform that was shared", example = "WHATSAPP", required = true)
    private String platform;

    @Schema(description = "When the platform was shared", example = "2026-02-06T14:30:00Z")
    private Instant sharedAt;

    @Schema(description = "Optional notes about the sharing", example = "Shared to main WhatsApp channel")
    private String notes;

    @Schema(description = "ID of the admin/editor who performed the sharing", example = "456e7890-e89b-12d3-a456-426614174001")
    private UUID sharedBy;

    // ===============================
    // Constructors
    // ===============================

    public SocialMediaShareMarkPlatformSharedRequestDto() {
    }

    public SocialMediaShareMarkPlatformSharedRequestDto(UUID newsId, String platform, Instant sharedAt, String notes,
            UUID sharedBy) {
        this.newsId = newsId;
        this.platform = platform;
        this.sharedAt = sharedAt;
        this.notes = notes;
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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
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

    public UUID getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(UUID sharedBy) {
        this.sharedBy = sharedBy;
    }
}