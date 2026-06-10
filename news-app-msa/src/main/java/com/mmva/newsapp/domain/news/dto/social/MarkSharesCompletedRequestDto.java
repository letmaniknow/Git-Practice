package com.mmva.newsapp.domain.news.dto.social;

// ===============================
// Core Java Imports
// ===============================
import java.util.List;

// ===============================
// OpenAPI/Swagger Imports
// ===============================
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

/**
 * Request DTO for marking social shares as completed
 */
@Schema(description = "Request to mark social shares as completed")
public class MarkSharesCompletedRequestDto {

    @Schema(description = "List of news article IDs to mark as completed", example = "[\"123e4567-e89b-12d3-a456-426614174000\", \"456e7890-e89b-12d3-a456-426614174001\"]", requiredMode = RequiredMode.REQUIRED)
    private List<String> newsIds;

    @Schema(description = "Optional notes about the sharing", example = "Shared during morning batch at 9:15 AM")
    private String notes;

    // ===============================
    // Constructors
    // ===============================

    public MarkSharesCompletedRequestDto() {
    }

    public MarkSharesCompletedRequestDto(List<String> newsIds) {
        this.newsIds = newsIds;
    }

    // ===============================
    // Getters and Setters
    // ===============================

    public List<String> getNewsIds() {
        return newsIds;
    }

    public void setNewsIds(List<String> newsIds) {
        this.newsIds = newsIds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}