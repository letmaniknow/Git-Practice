package com.mmva.newsapp.domain.news.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data transfer object containing content validation results.
 *
 * <p>
 * Encapsulates validation results for news content including quality checks,
 * structure validation, and content appropriateness assessments.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentValidationResult {

    /**
     * Whether the content is valid.
     */
    private boolean isValid;

    /**
     * List of content validation errors.
     */
    private List<String> errors;

    /**
     * List of content validation warnings.
     */
    private List<String> warnings;

    /**
     * List of content improvement suggestions.
     */
    private List<String> suggestions;

    /**
     * Content quality score (0-100).
     */
    private double qualityScore;

    /**
     * Whether content meets minimum length requirements.
     */
    private boolean meetsMinimumLength;

    /**
     * Whether content has proper structure.
     */
    private boolean hasProperStructure;

    /**
     * Whether content contains relevant keywords.
     */
    private boolean hasRelevantKeywords;

    /**
     * Content readability score (0-100).
     */
    private double readabilityScore;

    /**
     * Detected content language.
     */
    private String detectedLanguage;
}