package com.mmva.newsapp.domain.newsrss.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for RSS feed autodiscovery information.
 * Provides feed URLs and HTML link tags for frontend integration.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsRssDiscoveryDto {

    /**
     * List of available RSS feeds with their metadata.
     */
    private List<NewsRssFeedInfo> feeds;

    /**
     * Pre-built HTML link tags for easy frontend integration.
     * Copy these into the &lt;head&gt; section of your HTML pages.
     */
    private String htmlLinkTags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsRssFeedInfo {
        private String title;
        private String url;
        private String type;
        private String language;
    }
}
