package com.mmva.newsapp.domain.newsrss.service.core;

import com.mmva.newsapp.domain.newsrss.dto.core.NewsRssFeedDto;

import java.util.UUID;

/**
 * Service interface for generating RSS feeds.
 * Provides RSS 2.0 compliant feeds for newsapp content distribution.
 * 
 * <h3>Supported Feeds:</h3>
 * <ul>
 * <li>Latest newsapp feed (all categories)</li>
 * <li>Category-specific feeds</li>
 * <li>Featured newsapp feed</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsRssFeedService {

    /**
     * Generates RSS feed for latest published newsapp.
     * 
     * @param language the language code ("en" or "es")
     * @param limit    maximum number of items (default: 20)
     * @return RSS feed DTO
     */
    NewsRssFeedDto getLatestNewsFeed(String language, int limit);

    /**
     * Generates RSS feed for a specific newscategory.
     * 
     * @param categoryId the newscategory UUID
     * @param language   the language code ("en" or "es")
     * @param limit      maximum number of items
     * @return RSS feed DTO
     */
    NewsRssFeedDto getCategoryFeed(UUID categoryId, String language, int limit);

    /**
     * Generates RSS feed for featured newsapp only.
     * 
     * @param language the language code ("en" or "es")
     * @param limit    maximum number of items
     * @return RSS feed DTO
     */
    NewsRssFeedDto getFeaturedNewsFeed(String language, int limit);

    /**
     * Converts RSS feed DTO to XML string.
     * 
     * @param feed the RSS feed DTO
     * @return XML string in RSS 2.0 format
     */
    String toXml(NewsRssFeedDto feed);
}
