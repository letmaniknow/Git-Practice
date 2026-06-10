package com.mmva.newsapp.domain.news.dto.validation;

import com.mmva.newsapp.domain.news.dto.seo.SeoValidationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data transfer object containing comprehensive news validation results.
 *
 * <p>
 * Encapsulates all validation results for news creation including errors,
 * warnings, and validation status across different validation domains.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsValidationResult {

    /**
     * Whether the news creation request is valid for processing.
     */
    private boolean isValid;

    /**
     * List of critical errors that prevent news creation.
     */
    private List<String> errors;

    /**
     * List of warnings that should be reviewed but don't prevent creation.
     */
    private List<String> warnings;

    /**
     * List of suggestions for improvement.
     */
    private List<String> suggestions;

    /**
     * Geographic validation results.
     */
    private GeographicValidationResult geographicValidation;

    /**
     * Content validation results.
     */
    private ContentValidationResult contentValidation;

    /**
     * Workflow validation results.
     */
    private WorkflowValidationResult workflowValidation;

    /**
     * Duplicate detection results.
     */
    private DuplicateDetectionResult duplicateDetection;

    /**
     * SEO validation results.
     */
    private SeoValidationResult seoValidation;

    /**
     * Overall validation score (0-100).
     */
    private double overallScore;
}