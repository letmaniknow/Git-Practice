package com.mmva.newsapp.domain.news.dto.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for news search results.
 *
 * <p>
 * Contains search results with metadata, pagination information,
 * and aggregated data for faceted search UI components.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicNewsElasticSearchResponseDto {

    /**
     * List of search result items.
     */
    private List<NewsElasticSearchResultDto> results;

    /**
     * Pagination metadata.
     */
    private PaginationMetadataDto pagination;

    /**
     * Search metadata.
     */
    private SearchMetadataDto metadata;

    /**
     * Aggregation data for faceted search UI.
     */
    private SearchAggregationsDto aggregations;

    /**
     * Individual search result item.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsElasticSearchResultDto {

        /**
         * News article ID.
         */
        private UUID newsId;

        /**
         * News slug for URL generation.
         */
        private String slug;

        /**
         * Title in English.
         */
        private String titleEn;

        /**
         * Title in Spanish.
         */
        private String titleEs;

        /**
         * Excerpt in English.
         */
        private String excerptEn;

        /**
         * Excerpt in Spanish.
         */
        private String excerptEs;

        /**
         * Category ID.
         */
        private UUID categoryId;

        /**
         * Category name.
         */
        private String categoryName;

        /**
         * Country code.
         */
        private String countryCode;

        /**
         * Region.
         */
        private String region;

        /**
         * City.
         */
        private String city;

        /**
         * Publication date.
         */
        private Instant publishedAt;

        /**
         * Whether this is breaking news.
         */
        private Boolean isBreaking;

        /**
         * Whether this is sponsored content.
         */
        private Boolean isSponsored;

        /**
         * Whether this is premium content.
         */
        private Boolean isPremium;

        /**
         * Urgency level.
         */
        private String urgencyLevel;

        /**
         * Thumbnail URL.
         */
        private String thumbnailUrl;

        /**
         * Media type.
         */
        private String mediaType;

        /**
         * Source agency name.
         */
        private String sourceAgencyName;

        /**
         * Search relevance score (0.0 to 1.0).
         */
        private Double relevanceScore;

        /**
         * Highlighted snippets from the content.
         */
        private List<String> highlights;
    }

    /**
     * Pagination metadata.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMetadataDto {

        /**
         * Current page number (0-based).
         */
        private Integer page;

        /**
         * Page size.
         */
        private Integer size;

        /**
         * Total number of results.
         */
        private Long totalElements;

        /**
         * Total number of pages.
         */
        private Integer totalPages;

        /**
         * Whether this is the first page.
         */
        private Boolean first;

        /**
         * Whether this is the last page.
         */
        private Boolean last;

        /**
         * Number of elements in current page.
         */
        private Integer numberOfElements;
    }

    /**
     * Search metadata.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchMetadataDto {

        /**
         * Original search query.
         */
        private String query;

        /**
         * Search execution time in milliseconds.
         */
        private Long executionTimeMs;

        /**
         * Total results found.
         */
        private Long totalResults;

        /**
         * Applied filters summary.
         */
        private List<String> appliedFilters;

        /**
         * Sort field used.
         */
        private String sortBy;

        /**
         * Sort direction used.
         */
        private String sortDirection;
    }

    /**
     * Aggregation data for faceted search.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchAggregationsDto {

        /**
         * Category aggregations (category ID -> count).
         */
        private java.util.Map<UUID, Long> categories;

        /**
         * Country aggregations (country code -> count).
         */
        private java.util.Map<String, Long> countries;

        /**
         * Tag aggregations (tag -> count).
         */
        private java.util.Map<String, Long> tags;

        /**
         * Content type aggregations.
         */
        private ContentTypeAggregationsDto contentTypes;

        /**
         * Date range aggregations.
         */
        private DateRangeAggregationsDto dateRanges;

        /**
         * Content type aggregations.
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ContentTypeAggregationsDto {
            private Long breakingNews;
            private Long sponsored;
            private Long premium;
        }

        /**
         * Date range aggregations.
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DateRangeAggregationsDto {
            private Long last24Hours;
            private Long lastWeek;
            private Long lastMonth;
            private Long lastYear;
        }
    }
}