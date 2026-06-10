package com.mmva.newsapp.domain.news.service.seo;

import com.mmva.newsapp.domain.news.dto.seo.*;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;

/**
 * Service interface for SEO optimization of news articles.
 *
 * <p>
 * Provides comprehensive SEO meta tag generation and structured data
 * creation for news articles to improve search engine visibility and
 * social media sharing.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsSeoService {

    /**
     * Generates complete SEO meta tags for a news article.
     *
     * <p>
     * Creates all necessary meta tags including basic SEO, Open Graph,
     * Twitter Cards, and other social media optimizations.
     * </p>
     *
     * @param news The news article entity
     * @return Complete SEO meta tags DTO
     */
    NewsSeoMetaTagsDto generateMetaTags(NewsMasterEntity news);

    /**
     * Generates structured data (JSON-LD) for a news article.
     *
     * <p>
     * Creates Schema.org compliant structured data for rich snippets
     * in search engine results.
     * </p>
     *
     * @param news The news article entity
     * @return Structured data as JSON-LD string
     */
    String generateStructuredDataJson(NewsMasterEntity news);

    /**
     * Generates Open Graph meta tags for social media sharing.
     *
     * @param news The news article entity
     * @return Open Graph meta tags DTO
     */
    NewsSeoOpenGraphDto generateOpenGraphTags(NewsMasterEntity news);

    /**
     * Generates Twitter Card meta tags.
     *
     * @param news The news article entity
     * @return Twitter Card meta tags DTO
     */
    NewsSeoTwitterCardDto generateTwitterCardTags(NewsMasterEntity news);

    /**
     * Generates SEO-optimized page title.
     *
     * @param news The news article entity
     * @return Optimized title string (50-60 characters)
     */
    String generatePageTitle(NewsMasterEntity news);

    /**
     * Generates SEO-optimized meta description.
     *
     * @param news The news article entity
     * @return Optimized description string (150-160 characters)
     */
    String generateMetaDescription(NewsMasterEntity news);

    /**
     * Generates canonical URL for the news article.
     *
     * @param news    The news article entity
     * @param baseUrl The base URL of the application
     * @return Canonical URL string
     */
    String generateCanonicalUrl(NewsMasterEntity news, String baseUrl);
}