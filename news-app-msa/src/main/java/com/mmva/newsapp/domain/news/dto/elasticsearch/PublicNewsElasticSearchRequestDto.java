package com.mmva.newsapp.domain.news.dto.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for advanced news search operations.
 *
 * <p>
 * Supports comprehensive search filtering including text queries,
 * category filters, geographic filters, date ranges, and content types.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicNewsElasticSearchRequestDto {

    /**
     * Main search query string.
     * Supports full-text search across titles, content, and excerpts.
     */
    private String query;

    /**
     * Category IDs to filter by.
     */
    private List<UUID> categoryIds;

    /**
     * Country codes to filter by (ISO 2-letter codes).
     */
    private List<String> countryCodes;

    /**
     * Tags to filter by.
     */
    private List<String> tags;

    /**
     * Keywords to filter by.
     */
    private List<String> keywords;

    /**
     * Geographic bounding box for location-based search.
     */
    private GeographicBoundsDto bounds;

    /**
     * Start date for publication date filter.
     */
    private Instant startDate;

    /**
     * End date for publication date filter.
     */
    private Instant endDate;

    /**
     * Filter for breaking news only.
     */
    private Boolean breakingNewsOnly;

    /**
     * Filter for sponsored content only.
     */
    private Boolean sponsoredOnly;

    /**
     * Filter for premium content only.
     */
    private Boolean premiumOnly;

    /**
     * Urgency level filter.
     */
    private String urgencyLevel;

    /**
     * Target audience filter.
     */
    private String targetAudience;

    /**
     * Source agency ID filter.
     */
    private UUID sourceAgencyId;

    /**
     * Sort field for results.
     * Options: relevance, published_at, title, etc.
     */
    private String sortBy;

    /**
     * Sort direction.
     * Options: asc, desc
     */
    private String sortDirection;

    /**
     * Page number (0-based).
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * Page size.
     */
    @Builder.Default
    private Integer size = 20;

    /**
     * DTO for geographic bounding box.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeographicBoundsDto {
        private Double topLeftLatitude;
        private Double topLeftLongitude;
        private Double bottomRightLatitude;
        private Double bottomRightLongitude;
    }
}