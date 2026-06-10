package com.mmva.newsapp.infrastructure.common.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object containing Open Graph metadata for social media sharing.
 *
 * <p>
 * Encapsulates Open Graph protocol metadata used by social media platforms
 * like Facebook, Twitter, and LinkedIn for rich content previews.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenGraphMetadata {

    /**
     * Open Graph title for social media sharing.
     */
    private String ogTitle;

    /**
     * Open Graph description for social media sharing.
     */
    private String ogDescription;

    /**
     * Open Graph image URL for social media sharing.
     */
    private String ogImage;

    /**
     * Open Graph URL (canonical URL).
     */
    private String ogUrl;

    /**
     * Open Graph type (article, website, etc.).
     */
    private String ogType;

    /**
     * Open Graph site name.
     */
    private String ogSiteName;

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
}