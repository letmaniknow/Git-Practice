package com.mmva.newsapp.domain.news.service.validation;

import com.mmva.newsapp.domain.news.dto.core.NewsCreateRequestDto;
import com.mmva.newsapp.domain.news.dto.validation.NewsValidationResult;
import com.mmva.newsapp.domain.news.dto.validation.GeographicValidationResult;
import com.mmva.newsapp.domain.news.dto.validation.ContentValidationResult;
import com.mmva.newsapp.domain.news.dto.validation.WorkflowValidationResult;
import com.mmva.newsapp.domain.news.dto.validation.DuplicateDetectionResult;
import com.mmva.newsapp.domain.news.dto.seo.SeoValidationResult;

/**
 * Domain service interface for news validation and business rule enforcement.
 *
 * <p>
 * Provides comprehensive validation for news articles including business rules,
 * data integrity checks, and domain-specific validation logic for news
 * publishing.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>News creation request validation</li>
 * <li>Business rule enforcement</li>
 * <li>Geographic data validation for news</li>
 * <li>Content quality validation</li>
 * <li>Publishing workflow validation</li>
 * <li>Duplicate content detection</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsValidationService {

        /**
         * Validates a news creation request comprehensively.
         * Requires imageVideoFile to be present (mandatory for new articles).
         *
         * @param request the news creation request to validate
         * @return validation result with any issues found
         */
        NewsValidationResult validateNewsCreation(NewsCreateRequestDto request);

        /**
         * Validates a news update request comprehensively.
         * imageVideoFile is optional (user may keep existing media).
         * 
         * @param request the news update request to validate
         * @return validation result with any issues found
         */
        NewsValidationResult validateNewsUpdate(NewsCreateRequestDto request);

        /**
         * Validates geographic data for news articles.
         *
         * @param latitude     the latitude coordinate
         * @param longitude    the longitude coordinate
         * @param locationName the location name
         * @param country      the country name
         * @param state        the state/province name
         * @return geographic validation result
         */
        GeographicValidationResult validateGeographicData(Double latitude, Double longitude,
                        String locationName, String country, String state);

        /**
         * Validates news content quality and structure.
         *
         * @param title   the news title
         * @param content the news content
         * @param excerpt the news excerpt
         * @return content validation result
         */
        ContentValidationResult validateNewsContent(String title, String content, String excerpt);

        /**
         * Validates publishing workflow and scheduling.
         *
         * @param publishDateTime the scheduled publish date/time
         * @param urgencyLevel    the news urgency level
         * @param isDraft         whether the article is a draft
         * @return workflow validation result
         */
        WorkflowValidationResult validatePublishingWorkflow(java.time.LocalDateTime publishDateTime,
                        String urgencyLevel, boolean isDraft);

        /**
         * Checks for potential duplicate content.
         *
         * @param title          the news title
         * @param content        the news content
         * @param sourceAgencyId the source agency ID
         * @return duplicate detection result
         */
        DuplicateDetectionResult checkForDuplicates(String title, String content, Long sourceAgencyId);

        /**
         * Validates SEO metadata for news articles.
         *
         * @param title           the news title
         * @param metaDescription the meta description
         * @param keywords        the SEO keywords
         * @param slug            the URL slug
         * @return SEO validation result
         */
        SeoValidationResult validateSeoMetadata(String title, String metaDescription,
                        String[] keywords, String slug);
}