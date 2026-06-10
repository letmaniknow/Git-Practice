package com.mmva.newsapp.domain.news.mapper.elasticsearch;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.mmva.newsapp.domain.news.dto.elasticsearch.PublicNewsElasticSearchResponseDto;
import com.mmva.newsapp.domain.news.model.elasticsearch.NewsSearchDocument;

import java.util.List;

/**
 * MapStruct mapper for NewsElasticSearch operations.
 *
 * <p>
 * Provides mapping between NewsSearchDocument (Elasticsearch) and
 * NewsElasticSearchResponseDto with proper field transformations.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsElasticSearchMapper {

    /**
     * Converts NewsSearchDocument to NewsElasticSearchResultDto.
     *
     * @param document the Elasticsearch search document
     * @return the response DTO
     */
    @Mappings({
            @Mapping(target = "newsId", source = "newsNewsId"),
            @Mapping(target = "slug", source = "newsSlug"),
            @Mapping(target = "titleEn", source = "newsTitleEn"),
            @Mapping(target = "titleEs", source = "newsTitleEs"),
            @Mapping(target = "excerptEn", source = "newsExcerptEn"),
            @Mapping(target = "excerptEs", source = "newsExcerptEs"),
            @Mapping(target = "categoryId", source = "newsNewsCategoryId"),
            @Mapping(target = "categoryName", source = "newsCategoryName"),
            @Mapping(target = "countryCode", source = "newsCountryCode"),
            @Mapping(target = "region", source = "newsRegion"),
            @Mapping(target = "city", source = "newsCity"),
            @Mapping(target = "publishedAt", source = "newsPublishedAt"),
            @Mapping(target = "isBreaking", source = "newsIsBreaking"),
            @Mapping(target = "isSponsored", source = "newsIsSponsored"),
            @Mapping(target = "isPremium", source = "newsIsPremium"),
            @Mapping(target = "urgencyLevel", source = "newsUrgencyLevel"),
            @Mapping(target = "thumbnailUrl", source = "newsThumbnailUrl"),
            @Mapping(target = "mediaType", source = "newsMediaType"),
            @Mapping(target = "sourceAgencyName", source = "newsSourceAgencyName"),
            @Mapping(target = "relevanceScore", expression = "java(calculateRelevanceScore(document))"),
            @Mapping(target = "highlights", expression = "java(extractHighlights(document))")
    })
    PublicNewsElasticSearchResponseDto.NewsElasticSearchResultDto toResultDto(NewsSearchDocument document);

    /**
     * Converts list of NewsSearchDocument to list of NewsElasticSearchResultDto.
     *
     * @param documents the list of Elasticsearch search documents
     * @return the list of response DTOs
     */
    List<PublicNewsElasticSearchResponseDto.NewsElasticSearchResultDto> toResultDtoList(
            List<NewsSearchDocument> documents);

    /**
     * Calculates relevance score for a search document.
     * Placeholder implementation - would be enhanced with actual scoring logic.
     *
     * @param document the search document
     * @return relevance score between 0.0 and 1.0
     */
    default Double calculateRelevanceScore(NewsSearchDocument document) {
        // Placeholder - actual implementation would use Elasticsearch scoring
        // or custom relevance algorithms
        return 1.0;
    }

    /**
     * Extracts highlighted snippets from search document.
     * Placeholder implementation - would be populated by Elasticsearch
     * highlighting.
     *
     * @param document the search document
     * @return list of highlighted text snippets
     */
    default List<String> extractHighlights(NewsSearchDocument document) {
        // Placeholder - actual implementation would extract highlights from
        // Elasticsearch response highlighting
        return List.of();
    }
}