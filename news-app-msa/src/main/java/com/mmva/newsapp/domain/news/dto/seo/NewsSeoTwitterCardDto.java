package com.mmva.newsapp.domain.news.dto.seo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Twitter Card meta tags DTO for Twitter/X sharing.
 *
 * <p>
 * Provides Twitter with rich preview information for news articles
 * when shared on the platform.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSeoTwitterCardDto {

    /**
     * Twitter Card type.
     * Values: summary, summary_large_image, app, player
     */
    @Builder.Default
    private String card = "summary_large_image";

    /**
     * Twitter/X handle of the website (@username).
     */
    private String site;

    /**
     * Twitter/X handle of the content creator (@username).
     */
    private String creator;

    /**
     * Title for the card (max 70 characters).
     */
    private String title;

    /**
     * Description for the card (max 200 characters).
     */
    private String description;

    /**
     * URL of the image to display (recommended: 1200x675px for large image).
     */
    private String image;

    /**
     * Alt text for the image.
     */
    private String imageAlt;

    /**
     * URL of the content (canonical URL).
     */
    private String url;

    /**
     * Domain name for the app card (if using app card).
     */
    private String domain;
}