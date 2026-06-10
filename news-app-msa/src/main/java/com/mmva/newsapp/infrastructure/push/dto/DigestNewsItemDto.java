package com.mmva.newsapp.infrastructure.push.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Individual news item for daily/weekly digest notifications.
 * Each item represents one clickable news story in the digest.
 * 
 * <h3>Mobile App Usage:</h3>
 * 
 * <pre>
 * When user taps on a news item thumbnail in the digest:
 * 1. App navigates to: /newsapp/{newsId}
 * 2. Shows the full news article
 * </pre>
 * 
 * <h3>Rich Notification Layout (Android/iOS):</h3>
 * 
 * <pre>
 * ┌─────────────────────────────────────┐
 * │ 📰 Today's Top Stories              │  ← Main digest title
 * │ 5 stories curated for you           │  ← Main digest body
 * ├─────────────────────────────────────┤
 * │ [📷] Breaking: Stock Market Rally   │  ← Item 1 (thumbnailUrl + title)
 * │ [📷] Tech Giants Report Earnings    │  ← Item 2
 * │ [📷] Sports: Championship Finals    │  ← Item 3
 * └─────────────────────────────────────┘
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigestNewsItemDto {

    /**
     * News article ID for deep linking.
     * When user clicks this item, navigate to /newsapp/{newsId}
     */
    @NotNull(message = "News ID is required")
    private UUID newsId;

    /**
     * News headline/title.
     * Should be concise for digest display (max 100 chars).
     */
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    /**
     * News thumbnail URL.
     * Recommended: 1024x512 for rich notifications.
     * Used as clickable thumbnail in digest list.
     */
    private String thumbnailUrl;

    /**
     * Optional category name for display context.
     * e.g., "Politics", "Sports", "Technology"
     */
    private String category;

    /**
     * Optional short summary/excerpt.
     * For expanded digest views.
     */
    @Size(max = 150, message = "Summary must not exceed 150 characters")
    private String summary;
}
