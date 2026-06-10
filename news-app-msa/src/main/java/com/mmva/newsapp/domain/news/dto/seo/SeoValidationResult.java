package com.mmva.newsapp.domain.news.dto.seo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data transfer object containing SEO validation results.
 *
 * <p>
 * Encapsulates validation results for SEO metadata including title
 * optimization,
 * meta description quality, keyword analysis, and slug validation.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeoValidationResult {

    /**
     * Whether the SEO metadata is valid.
     */
    private boolean isValid;

    /**
     * List of SEO validation errors.
     */
    private List<String> errors;

    /**
     * List of SEO validation warnings.
     */
    private List<String> warnings;

    /**
     * List of SEO improvement suggestions.
     */
    private List<String> suggestions;

    /**
     * Overall SEO score (0-100).
     */
    private double seoScore;

    /**
     * Whether the title is SEO-optimized.
     */
    private boolean titleOptimized;

    /**
     * Whether the meta description is optimal.
     */
    private boolean metaDescriptionOptimal;

    /**
     * Whether keywords are present and relevant.
     */
    private boolean keywordsPresent;

    /**
     * Whether the slug is SEO-friendly.
     */
    private boolean slugOptimized;

    /**
     * Suggested improved title.
     */
    private String suggestedTitle;

    /**
     * Suggested improved meta description.
     */
    private String suggestedMetaDescription;
}