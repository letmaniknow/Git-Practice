package com.mmva.newsapp.domain.news.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data transfer object containing news content validation results.
 *
 * <p>
 * Encapsulates the results of content validation including any issues found,
 * warnings, and recommendations for news content quality and structure.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsContentValidationResult {

    /**
     * Whether the content is valid for publishing.
     */
    private boolean isValid;

    /**
     * List of validation errors that prevent publishing.
     */
    private List<String> errors;

    /**
     * List of validation warnings that should be reviewed.
     */
    private List<String> warnings;

    /**
     * List of recommendations for content improvement.
     */
    private List<String> recommendations;

    /**
     * Content quality score (0-100).
     */
    private double qualityScore;

    /**
     * Whether the content meets minimum length requirements.
     */
    private boolean meetsMinimumLength;

    /**
     * Whether the content has proper structure (paragraphs, etc.).
     */
    private boolean hasProperStructure;

    /**
     * Whether the content contains appropriate keywords.
     */
    private boolean hasRelevantKeywords;
}