package com.mmva.newsapp.infrastructure.common.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object containing comprehensive content quality metrics.
 *
 * <p>
 * Encapsulates all quality-related measurements calculated for content,
 * including readability, sentiment, structural metrics, and overall quality
 * scores.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentQualityMetrics {

    /**
     * Overall content quality score (0-100, higher is better).
     */
    private double contentQualityScore;

    /**
     * Readability score using Flesch-Kincaid formula (0-100, higher is easier to
     * read).
     */
    private double readabilityScore;

    /**
     * Sentiment score (-1.0 to 1.0, negative=negative sentiment, positive=positive
     * sentiment).
     */
    private double sentimentScore;

    /**
     * Readability level assessment (e.g., "Elementary", "Intermediate",
     * "Advanced").
     */
    private String readabilityLevel;

    /**
     * Structural content metrics.
     */
    private ContentMetrics contentMetrics;
}