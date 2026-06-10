package com.mmva.newsapp.domain.news.dto.seo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Open Graph meta tags DTO for social media sharing.
 *
 * <p>
 * Provides Facebook, LinkedIn, and other social platforms
 * with rich preview information for news articles.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSeoOpenGraphDto {

    /**
     * Open Graph title (same as page title, max 95 characters).
     */
    private String title;

    /**
     * Open Graph description (max 200 characters).
     */
    private String description;

    /**
     * Open Graph image URL (recommended: 1200x630px).
     */
    private String image;

    /**
     * Open Graph image width in pixels.
     */
    private Integer imageWidth;

    /**
     * Open Graph image height in pixels.
     */
    private Integer imageHeight;

    /**
     * Open Graph image alt text.
     */
    private String imageAlt;

    /**
     * Canonical URL of the article.
     */
    private String url;

    /**
     * Open Graph object type.
     */
    @Builder.Default
    private String type = "article";

    /**
     * Site name (brand name).
     */
    private String siteName;

    /**
     * Article published time.
     */
    private Instant publishedTime;

    /**
     * Article modified time.
     */
    private Instant modifiedTime;

    /**
     * Article author name.
     */
    private String author;

    /**
     * Article section/category.
     */
    private String section;

    /**
     * Article tags (comma-separated).
     */
    private String tags;

    /**
     * Content locale.
     */
    @Builder.Default
    private String locale = "en_US";
}