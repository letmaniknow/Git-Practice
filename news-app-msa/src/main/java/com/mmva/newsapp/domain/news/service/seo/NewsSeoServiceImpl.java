package com.mmva.newsapp.domain.news.service.seo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmva.newsapp.domain.news.dto.seo.*;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of SEO optimization service for news articles.
 *
 * <p>
 * Generates comprehensive SEO meta tags, Open Graph tags, Twitter Cards,
 * and Schema.org structured data to improve search engine visibility
 * and social media sharing for news articles.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSeoServiceImpl implements NewsSeoService {

    private final ObjectMapper objectMapper;

    @Value("${app.name:NewsApp}")
    private String appName;

    @Value("${app.url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.twitter.handle:@newsapp}")
    private String twitterHandle;

    @Override
    public NewsSeoMetaTagsDto generateMetaTags(NewsMasterEntity news) {
        log.debug("Generating SEO meta tags for news article: {}", news.getNewsNewsId());

        return NewsSeoMetaTagsDto.builder()
                .title(generatePageTitle(news))
                .description(generateMetaDescription(news))
                .keywords(generateKeywords(news))
                .canonicalUrl(generateCanonicalUrl(news, baseUrl))
                .openGraph(generateOpenGraphTags(news))
                .twitterCard(generateTwitterCardTags(news))
                .structuredDataJson(generateStructuredDataJson(news))
                .article(generateArticleTags(news))
                .build();
    }

    @Override
    public String generateStructuredDataJson(NewsMasterEntity news) {
        try {
            NewsStructuredDataArticle articleData = createStructuredDataArticle(news);
            return objectMapper.writeValueAsString(articleData);
        } catch (Exception e) {
            log.error("Failed to generate structured data JSON for news: {}", news.getNewsNewsId(), e);
            return "{}";
        }
    }

    @Override
    public NewsSeoOpenGraphDto generateOpenGraphTags(NewsMasterEntity news) {
        String imageUrl = generateImageUrl(news);

        return NewsSeoOpenGraphDto.builder()
                .title(generateOpenGraphTitle(news))
                .description(generateMetaDescription(news))
                .image(imageUrl)
                .imageWidth(1200)
                .imageHeight(630)
                .imageAlt(generateImageAlt(news))
                .url(generateCanonicalUrl(news, baseUrl))
                .siteName(appName)
                .publishedTime(news.getNewsPublishedAt())
                .modifiedTime(news.getCreatedAt()) // Using createdAt as modified time
                .author(news.getNewsSourceAuthorName())
                .section("News") // Could be made dynamic based on category
                .tags(news.getNewsTags())
                .build();
    }

    @Override
    public NewsSeoTwitterCardDto generateTwitterCardTags(NewsMasterEntity news) {
        return NewsSeoTwitterCardDto.builder()
                .title(generateTwitterTitle(news))
                .description(generateMetaDescription(news))
                .image(generateImageUrl(news))
                .imageAlt(generateImageAlt(news))
                .url(generateCanonicalUrl(news, baseUrl))
                .site(twitterHandle)
                .build();
    }

    @Override
    public String generatePageTitle(NewsMasterEntity news) {
        String baseTitle = news.getNewsTitleEn();

        // Truncate if too long (keep under 60 characters for optimal SEO)
        if (baseTitle.length() > 50) {
            baseTitle = baseTitle.substring(0, 47) + "...";
        }

        // Add breaking news indicator if applicable
        if (Boolean.TRUE.equals(news.getNewsIsBreaking())) {
            return "BREAKING: " + baseTitle + " - " + appName;
        }

        return baseTitle + " - " + appName;
    }

    @Override
    public String generateMetaDescription(NewsMasterEntity news) {
        // Use excerpt if available, otherwise create from content
        String description = news.getNewsExcerptEn();

        if (description == null || description.trim().isEmpty()) {
            // Create description from content (first 160 characters)
            String content = news.getNewsContentEn();
            if (content != null && !content.trim().isEmpty()) {
                description = content.replaceAll("<[^>]+>", "") // Remove HTML tags
                        .replaceAll("\\s+", " ") // Normalize whitespace
                        .trim();

                if (description.length() > 155) {
                    description = description.substring(0, 152) + "...";
                }
            } else {
                description = "Read the latest news article on " + appName;
            }
        }

        return description;
    }

    @Override
    public String generateCanonicalUrl(NewsMasterEntity news, String baseUrl) {
        return baseUrl + "/news/" + news.getNewsSlug();
    }

    /**
     * Generates keywords from news tags and category.
     */
    private String generateKeywords(NewsMasterEntity news) {
        StringBuilder keywords = new StringBuilder();

        // Add tags if available
        if (news.getNewsTags() != null && !news.getNewsTags().trim().isEmpty()) {
            keywords.append(news.getNewsTags());
        }

        // Add breaking news keyword if applicable
        if (Boolean.TRUE.equals(news.getNewsIsBreaking())) {
            if (keywords.length() > 0)
                keywords.append(", ");
            keywords.append("breaking news");
        }

        // Add category as keyword
        if (keywords.length() > 0)
            keywords.append(", ");
        keywords.append("news");

        return keywords.toString();
    }

    /**
     * Generates Open Graph optimized title.
     */
    private String generateOpenGraphTitle(NewsMasterEntity news) {
        String title = news.getNewsTitleEn();

        // Open Graph allows up to 95 characters
        if (title.length() > 90) {
            title = title.substring(0, 87) + "...";
        }

        if (Boolean.TRUE.equals(news.getNewsIsBreaking())) {
            return "BREAKING: " + title;
        }

        return title;
    }

    /**
     * Generates Twitter-optimized title.
     */
    private String generateTwitterTitle(NewsMasterEntity news) {
        String title = news.getNewsTitleEn();

        // Twitter allows up to 70 characters for title
        if (title.length() > 65) {
            title = title.substring(0, 62) + "...";
        }

        return title;
    }

    /**
     * Generates image URL for social media sharing.
     */
    private String generateImageUrl(NewsMasterEntity news) {
        if (news.getNewsThumbnailUrl() != null && !news.getNewsThumbnailUrl().trim().isEmpty()) {
            return news.getNewsThumbnailUrl();
        }

        if (news.getNewsMediaFileUrl() != null && !news.getNewsMediaFileUrl().trim().isEmpty()) {
            return news.getNewsMediaFileUrl();
        }

        // Default image
        return baseUrl + "/images/default-news-image.jpg";
    }

    /**
     * Generates alt text for images.
     */
    private String generateImageAlt(NewsMasterEntity news) {
        return news.getNewsTitleEn() + " - " + appName;
    }

    /**
     * Creates article-specific meta tags.
     */
    private NewsSeoArticleDto generateArticleTags(NewsMasterEntity news) {
        return NewsSeoArticleDto.builder()
                .author(news.getNewsSourceAuthorName())
                .section("News") // Could be made dynamic
                .tags(parseTags(news.getNewsTags()))
                .publishedTime(news.getNewsPublishedAt())
                .modifiedTime(news.getCreatedAt())
                .wordCount(news.getNewsWordCount())
                .readingTime(news.getNewsReadTimeMinutes())
                .isBreaking(news.getNewsIsBreaking())
                .build();
    }

    /**
     * Parses comma-separated tags into a list.
     */
    private List<String> parseTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }

    /**
     * Creates structured data for Schema.org Article.
     */
    private NewsStructuredDataArticle createStructuredDataArticle(NewsMasterEntity news) {
        return NewsStructuredDataArticle.builder()
                .context("https://schema.org")
                .type("NewsArticle")
                .headline(news.getNewsTitleEn())
                .description(generateMetaDescription(news))
                .image(List.of(generateImageUrl(news)))
                .datePublished(formatInstant(news.getNewsPublishedAt()))
                .dateModified(formatInstant(news.getCreatedAt()))
                .author(createStructuredDataPerson(news))
                .publisher(createStructuredDataOrganization())
                .mainEntityOfPage(createMainEntityOfPage(news))
                .articleSection("News")
                .keywords(parseTags(news.getNewsTags()))
                .speakable(createSpeakableSpec())
                .build();
    }

    /**
     * Creates structured data person for author.
     */
    private NewsStructuredDataPerson createStructuredDataPerson(NewsMasterEntity news) {
        return NewsStructuredDataPerson.builder()
                .type("Person")
                .name(news.getNewsSourceAuthorName() != null ? news.getNewsSourceAuthorName() : "NewsApp Staff")
                .build();
    }

    /**
     * Creates structured data organization for publisher.
     */
    private NewsStructuredDataOrganization createStructuredDataOrganization() {
        return NewsStructuredDataOrganization.builder()
                .type("Organization")
                .name(appName)
                .logo(createOrganizationLogo())
                .build();
    }

    /**
     * Creates organization logo structured data.
     */
    private NewsStructuredDataImageObject createOrganizationLogo() {
        return NewsStructuredDataImageObject.builder()
                .type("ImageObject")
                .url(baseUrl + "/images/logo.png")
                .width(600)
                .height(60)
                .build();
    }

    /**
     * Creates main entity of page structured data.
     */
    private NewsStructuredDataWebPage createMainEntityOfPage(NewsMasterEntity news) {
        return NewsStructuredDataWebPage.builder()
                .type("WebPage")
                .id(generateCanonicalUrl(news, baseUrl))
                .build();
    }

    /**
     * Creates speakable specification for voice search.
     */
    private NewsStructuredDataSpeakableSpec createSpeakableSpec() {
        return NewsStructuredDataSpeakableSpec.builder()
                .type("SpeakableSpecification")
                .cssSelector(List.of(".speakable-headline", ".speakable-summary"))
                .build();
    }

    /**
     * Formats Instant to ISO 8601 string.
     */
    private String formatInstant(Instant instant) {
        if (instant == null)
            return null;
        return instant.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}