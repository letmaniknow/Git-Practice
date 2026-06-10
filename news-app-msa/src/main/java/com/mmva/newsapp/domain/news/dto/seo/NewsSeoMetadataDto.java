package com.mmva.newsapp.domain.news.dto.seo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for news SEO metadata.
 *
 * <p>
 * Contains SEO-related metadata for news articles including meta descriptions,
 * keywords, slugs, and search engine optimization data.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSeoMetadataDto {

    /**
     * SEO-optimized meta description in English.
     */
    private String metaDescriptionEn;

    /**
     * SEO-optimized meta description in Arabic.
     */
    private String metaDescriptionAr;

    /**
     * SEO keywords in English (comma-separated).
     */
    private String keywordsEn;

    /**
     * SEO keywords in Arabic (comma-separated).
     */
    private String keywordsAr;

    /**
     * URL-friendly slug in English.
     */
    private String slugEn;

    /**
     * URL-friendly slug in Arabic.
     */
    private String slugAr;

    /**
     * Overall SEO score (0-100).
     */
    private Double seoScore;

    /**
     * Whether the title is SEO-optimized.
     */
    private Boolean titleOptimized;

    /**
     * Whether the meta description is optimal.
     */
    private Boolean metaDescriptionOptimal;

    /**
     * Whether relevant keywords are present.
     */
    private Boolean keywordsPresent;

    /**
     * Whether the slug is SEO-friendly.
     */
    private Boolean slugOptimized;

    /**
     * Open Graph title for social media.
     */
    private String ogTitle;

    /**
     * Open Graph description for social media.
     */
    private String ogDescription;

    /**
     * Open Graph image URL for social media.
     */
    private String ogImage;

    /**
     * Twitter Card title.
     */
    private String twitterTitle;

    /**
     * Twitter Card description.
     */
    private String twitterDescription;

    /**
     * Twitter Card image URL.
     */
    private String twitterImage;

    /**
     * Last SEO analysis timestamp.
     */
    private java.time.LocalDateTime lastSeoAnalysis;
}