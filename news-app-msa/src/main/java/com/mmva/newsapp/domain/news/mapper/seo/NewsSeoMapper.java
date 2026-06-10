package com.mmva.newsapp.domain.news.mapper.seo;

import com.mmva.newsapp.domain.news.dto.seo.*;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for SEO-related DTO transformations.
 *
 * <p>
 * Provides mapping between NewsMasterEntity and SEO DTOs,
 * with custom logic for SEO-optimized content generation.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsSeoMapper {

    /**
     * Maps NewsMasterEntity to NewsSeoMetaTagsDto.
     *
     * <p>
     * Note: This mapping creates a basic structure. The full SEO generation
     * should be handled by NewsSeoService for optimal SEO content.
     * </p>
     */
    @Mapping(target = "title", expression = "java(news.getNewsTitleEn())")
    @Mapping(target = "description", expression = "java(generateDescription(news))")
    @Mapping(target = "keywords", expression = "java(generateKeywords(news))")
    @Mapping(target = "canonicalUrl", ignore = true) // Handled by service
    @Mapping(target = "robots", constant = "index,follow")
    @Mapping(target = "language", constant = "en")
    @Mapping(target = "viewport", constant = "width=device-width, initial-scale=1.0")
    @Mapping(target = "openGraph", source = "news", qualifiedByName = "toOpenGraph")
    @Mapping(target = "twitterCard", source = "news", qualifiedByName = "toTwitterCard")
    @Mapping(target = "structuredDataJson", ignore = true) // Handled by service
    @Mapping(target = "article", source = "news", qualifiedByName = "toArticle")
    NewsSeoMetaTagsDto toMetaTagsDto(NewsMasterEntity news);

    /**
     * Maps NewsMasterEntity to NewsSeoOpenGraphDto.
     */
    @Named("toOpenGraph")
    @Mapping(target = "title", expression = "java(news.getNewsTitleEn())")
    @Mapping(target = "description", expression = "java(generateDescription(news))")
    @Mapping(target = "image", expression = "java(getImageUrl(news))")
    @Mapping(target = "imageWidth", constant = "1200")
    @Mapping(target = "imageHeight", constant = "630")
    @Mapping(target = "imageAlt", expression = "java(news.getNewsTitleEn() + \" - NewsApp\")")
    @Mapping(target = "url", ignore = true) // Handled by service
    @Mapping(target = "siteName", constant = "NewsApp")
    @Mapping(target = "type", constant = "article")
    @Mapping(target = "locale", constant = "en_US")
    @Mapping(target = "publishedTime", source = "newsPublishedAt")
    @Mapping(target = "modifiedTime", source = "createdAt")
    @Mapping(target = "author", source = "newsSourceAuthorName")
    @Mapping(target = "section", constant = "News")
    @Mapping(target = "tags", source = "newsTags")
    NewsSeoOpenGraphDto toOpenGraphDto(NewsMasterEntity news);

    /**
     * Maps NewsMasterEntity to NewsSeoTwitterCardDto.
     */
    @Named("toTwitterCard")
    @Mapping(target = "card", constant = "summary_large_image")
    @Mapping(target = "title", expression = "java(news.getNewsTitleEn())")
    @Mapping(target = "description", expression = "java(generateDescription(news))")
    @Mapping(target = "image", expression = "java(getImageUrl(news))")
    @Mapping(target = "imageAlt", expression = "java(news.getNewsTitleEn() + \" - NewsApp\")")
    @Mapping(target = "url", ignore = true) // Handled by service
    @Mapping(target = "site", constant = "@newsapp")
    @Mapping(target = "creator", ignore = true) // Not available in entity
    @Mapping(target = "domain", ignore = true) // Handled by service
    NewsSeoTwitterCardDto toTwitterCardDto(NewsMasterEntity news);

    /**
     * Maps NewsMasterEntity to NewsSeoArticleDto.
     */
    @Named("toArticle")
    @Mapping(target = "author", source = "newsSourceAuthorName")
    @Mapping(target = "section", constant = "News")
    @Mapping(target = "tags", expression = "java(parseTags(news.getNewsTags()))")
    @Mapping(target = "publishedTime", source = "newsPublishedAt")
    @Mapping(target = "modifiedTime", source = "createdAt")
    @Mapping(target = "wordCount", source = "newsWordCount")
    @Mapping(target = "readingTime", source = "newsReadTimeMinutes")
    @Mapping(target = "isBreaking", source = "newsIsBreaking")
    @Mapping(target = "contentRating", ignore = true) // Not available in entity
    @Mapping(target = "urgencyLevel", ignore = true) // Not available in entity
    @Mapping(target = "targetAudience", ignore = true) // Not available in entity
    NewsSeoArticleDto toArticleDto(NewsMasterEntity news);

    /**
     * Generates meta description from news content.
     */
    @Named("generateDescription")
    default String generateDescription(NewsMasterEntity news) {
        String description = news.getNewsExcerptEn();

        if (description == null || description.trim().isEmpty()) {
            String content = news.getNewsContentEn();
            if (content != null && !content.trim().isEmpty()) {
                description = content.replaceAll("<[^>]+>", "")
                        .replaceAll("\\s+", " ")
                        .trim();

                if (description.length() > 155) {
                    description = description.substring(0, 152) + "...";
                }
            } else {
                description = "Read the latest news article on NewsApp";
            }
        }

        return description;
    }

    /**
     * Generates keywords from news tags.
     */
    @Named("generateKeywords")
    default String generateKeywords(NewsMasterEntity news) {
        StringBuilder keywords = new StringBuilder();

        if (news.getNewsTags() != null && !news.getNewsTags().trim().isEmpty()) {
            keywords.append(news.getNewsTags());
        }

        if (Boolean.TRUE.equals(news.getNewsIsBreaking())) {
            if (keywords.length() > 0)
                keywords.append(", ");
            keywords.append("breaking news");
        }

        if (keywords.length() > 0)
            keywords.append(", ");
        keywords.append("news");

        return keywords.toString();
    }

    /**
     * Gets the best available image URL.
     */
    @Named("getImageUrl")
    default String getImageUrl(NewsMasterEntity news) {
        if (news.getNewsThumbnailUrl() != null && !news.getNewsThumbnailUrl().trim().isEmpty()) {
            return news.getNewsThumbnailUrl();
        }

        if (news.getNewsMediaFileUrl() != null && !news.getNewsMediaFileUrl().trim().isEmpty()) {
            return news.getNewsMediaFileUrl();
        }

        return "/images/default-news-image.jpg";
    }

    /**
     * Parses comma-separated tags into a list.
     */
    @Named("parseTags")
    default java.util.List<String> parseTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return java.util.List.of();
        }

        return java.util.Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }
}