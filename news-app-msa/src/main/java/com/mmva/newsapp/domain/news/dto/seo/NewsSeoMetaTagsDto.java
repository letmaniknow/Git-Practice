package com.mmva.newsapp.domain.news.dto.seo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Complete SEO meta tags DTO for news articles.
 *
 * <p>
 * Contains all meta tags needed for SEO optimization including
 * basic SEO, Open Graph, Twitter Cards, and structured data.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSeoMetaTagsDto {

    /**
     * SEO-optimized page title (50-60 characters).
     */
    private String title;

    /**
     * Meta description (150-160 characters).
     */
    private String description;

    /**
     * Keywords for search engines.
     */
    private String keywords;

    /**
     * Canonical URL to prevent duplicate content.
     */
    private String canonicalUrl;

    /**
     * Robots meta tag instructions.
     */
    @Builder.Default
    private String robots = "index, follow, max-snippet:-1, max-image-preview:large";

    /**
     * Open Graph meta tags for social media.
     */
    private NewsSeoOpenGraphDto openGraph;

    /**
     * Twitter Card meta tags.
     */
    private NewsSeoTwitterCardDto twitterCard;

    /**
     * Structured data as JSON-LD string.
     */
    private String structuredDataJson;

    /**
     * Article-specific meta tags.
     */
    private NewsSeoArticleDto article;

    /**
     * Language and locale information.
     */
    @Builder.Default
    private String language = "en-US";

    /**
     * Viewport meta tag for mobile optimization.
     */
    @Builder.Default
    private String viewport = "width=device-width, initial-scale=1.0";
}