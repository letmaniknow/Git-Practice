package com.mmva.newsapp.domain.newsrss.service.core;

import com.mmva.newsapp.domain.news.service.media.MediaUrlService;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.repository.core.NewsRepository;
import com.mmva.newsapp.domain.newsrss.dto.core.NewsRssFeedDto;
import com.mmva.newsapp.domain.newsrss.dto.core.NewsRssItemDto;
import com.mmva.newsapp.infrastructure.common.audit.repository.SoftDeleteSpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of RSS feed generation service.
 * Generates RSS 2.0 compliant XML feeds for newsapp distribution.
 *
 * <h3>Features:</h3>
 * <ul>
 * <li>Bilingual support (English/Spanish)</li>
 * <li>Category-specific feeds</li>
 * <li>Media enclosures for thumbnails</li>
 * <li>SEO-optimized metadata</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsRssFeedServiceImpl implements NewsRssFeedService {

    private static final String PUBLISHED_STATUS = "published";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    // RFC 822 date format required by RSS 2.0
    private static final DateTimeFormatter RFC_822_FORMATTER = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            .withZone(ZoneId.of("UTC"));

    private final NewsRepository newsRepository;
    private final MediaUrlService mediaUrlService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.name:News App}")
    private String appName;

    @Value("${app.description:Latest newsapp and updates}")
    private String appDescription;

    // ========================================
    // Public API Methods
    // ========================================

    @Override
    public NewsRssFeedDto getLatestNewsFeed(String language, int limit) {
        log.debug("NewsRssFeedService: Generating latest newsapp feed - language: {}, limit: {}", language, limit);

        int safeLimit = validateLimit(limit);
        String lang = validateLanguage(language);

        Specification<NewsMasterEntity> spec = SoftDeleteSpec.<NewsMasterEntity>notDeleted()
                .and((root, query, cb) -> cb.equal(root.get("newsWorkflowStatus"), PUBLISHED_STATUS))
                .and((root, query, cb) -> cb.isTrue(root.get("newsIsActive")));

        List<NewsMasterEntity> news = newsRepository.findAll(
                spec,
                PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "newsPublishedAt"))).getContent();

        return buildFeed(
                appName + " - Latest News",
                baseUrl,
                appDescription,
                lang,
                news,
                lang);
    }

    @Override
    public NewsRssFeedDto getCategoryFeed(UUID categoryId, String language, int limit) {
        log.debug("NewsRssFeedService: Generating newscategory feed - categoryId: {}, language: {}", categoryId,
                language);

        int safeLimit = validateLimit(limit);
        String lang = validateLanguage(language);

        Specification<NewsMasterEntity> spec = SoftDeleteSpec.<NewsMasterEntity>notDeleted()
                .and((root, query, cb) -> cb.equal(root.get("newsWorkflowStatus"), PUBLISHED_STATUS))
                .and((root, query, cb) -> cb.isTrue(root.get("newsIsActive")))
                .and((root, query, cb) -> cb.equal(root.get("newsNewsCategoryId"), categoryId));

        List<NewsMasterEntity> news = newsRepository.findAll(
                spec,
                PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "newsPublishedAt"))).getContent();

        return buildFeed(
                appName + " - Category Feed",
                baseUrl + "/newscategory/" + categoryId,
                "News from newscategory",
                lang,
                news,
                lang);
    }

    @Override
    public NewsRssFeedDto getFeaturedNewsFeed(String language, int limit) {
        log.debug("NewsRssFeedService: Generating featured newsapp feed - language: {}", language);

        int safeLimit = validateLimit(limit);
        String lang = validateLanguage(language);

        Specification<NewsMasterEntity> spec = SoftDeleteSpec.<NewsMasterEntity>notDeleted()
                .and((root, query, cb) -> cb.equal(root.get("newsWorkflowStatus"), PUBLISHED_STATUS))
                .and((root, query, cb) -> cb.isTrue(root.get("newsIsActive")))
                .and((root, query, cb) -> cb.isTrue(root.get("newsIsFeatured")));

        List<NewsMasterEntity> news = newsRepository.findAll(
                spec,
                PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "newsPublishedAt"))).getContent();

        return buildFeed(
                appName + " - Featured News",
                baseUrl + "/featured",
                "Featured and highlighted newsapp",
                lang,
                news,
                lang);
    }

    @Override
    public String toXml(NewsRssFeedDto feed) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append(
                "<newsrss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:media=\"http://search.yahoo.com/mrss/\">\n");
        xml.append("  <channel>\n");

        // Channel metadata
        xml.append("    <title>").append(escapeXml(feed.getTitle())).append("</title>\n");
        xml.append("    <link>").append(escapeXml(feed.getLink())).append("</link>\n");
        xml.append("    <description>").append(escapeXml(feed.getDescription())).append("</description>\n");
        xml.append("    <language>").append(escapeXml(feed.getLanguage())).append("</language>\n");
        xml.append("    <lastBuildDate>").append(feed.getLastBuildDate()).append("</lastBuildDate>\n");
        xml.append("    <generator>").append(escapeXml(feed.getGenerator())).append("</generator>\n");

        if (feed.getCopyright() != null) {
            xml.append("    <copyright>").append(escapeXml(feed.getCopyright())).append("</copyright>\n");
        }

        // Atom self-link for feed readers
        xml.append("    <atom:link href=\"").append(escapeXml(feed.getLink()))
                .append("/newsrss\" rel=\"self\" type=\"application/newsrss+xml\"/>\n");

        // Items
        if (feed.getItems() != null) {
            for (NewsRssItemDto item : feed.getItems()) {
                xml.append("    <item>\n");
                xml.append("      <title>").append(escapeXml(item.getTitle())).append("</title>\n");
                xml.append("      <link>").append(escapeXml(item.getLink())).append("</link>\n");
                xml.append("      <description><![CDATA[")
                        .append(item.getDescription() != null ? item.getDescription() : "")
                        .append("]]></description>\n");
                xml.append("      <pubDate>").append(item.getPubDate()).append("</pubDate>\n");
                xml.append("      <guid isPermaLink=\"true\">").append(escapeXml(item.getLink())).append("</guid>\n");

                if (item.getCategory() != null) {
                    xml.append("      <newscategory>").append(escapeXml(item.getCategory()))
                            .append("</newscategory>\n");
                }

                if (item.getAuthor() != null) {
                    xml.append("      <author>").append(escapeXml(item.getAuthor())).append("</author>\n");
                }

                // Media thumbnail for feed readers that support it
                if (item.getThumbnailUrl() != null && !item.getThumbnailUrl().isEmpty()) {
                    xml.append("      <media:thumbnail url=\"").append(escapeXml(item.getThumbnailUrl()))
                            .append("\"/>\n");
                }

                xml.append("    </item>\n");
            }
        }

        xml.append("  </channel>\n");
        xml.append("</newsrss>");

        return xml.toString();
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private NewsRssFeedDto buildFeed(String title, String link, String description,
            String language, List<NewsMasterEntity> news, String lang) {
        List<NewsRssItemDto> items = news.stream()
                .map(n -> mapToRssItem(n, lang))
                .collect(Collectors.toList());

        return NewsRssFeedDto.builder()
                .title(title)
                .link(link)
                .description(description)
                .language(language.equals("es") ? "es" : "en-us")
                .lastBuildDate(formatRfc822(Instant.now()))
                .copyright("© " + java.time.Year.now().getValue() + " " + appName)
                .generator(appName + " RSS Generator v1.0")
                .items(items)
                .build();
    }

    private NewsRssItemDto mapToRssItem(NewsMasterEntity news, String language) {
        boolean isSpanish = "es".equalsIgnoreCase(language);

        String title = isSpanish ? news.getNewsTitleEs() : news.getNewsTitleEn();
        String description = isSpanish ? truncateDescription(news.getNewsContentEs())
                : truncateDescription(news.getNewsContentEn());

        // Build thumbnail URL - always use the service to ensure proper HTTP URL
        // The stored value may be a filename, URL, or file path
        String thumbnailUrl = mediaUrlService.buildThumbnailUrl(news.getNewsThumbnailUrl());
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            // Fallback: use media file as thumbnail (for legacy newsapp without dedicated
            // thumbnail)
            thumbnailUrl = mediaUrlService.buildMediaUrl(news.getNewsMediaFileName());
        }

        return NewsRssItemDto.builder()
                .title(title != null ? title : "Untitled")
                .link(baseUrl + "/newsapp/" + news.getNewsSlug())
                .description(description)
                .pubDate(formatRfc822(
                        news.getNewsPublishedAt() != null ? news.getNewsPublishedAt() : news.getCreatedAt()))
                .guid(news.getNewsNewsId().toString())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    private String truncateDescription(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // Strip HTML tags and truncate to 300 characters
        String stripped = content.replaceAll("<[^>]*>", "").trim();
        if (stripped.length() > 300) {
            return stripped.substring(0, 297) + "...";
        }
        return stripped;
    }

    private String formatRfc822(Instant instant) {
        if (instant == null) {
            instant = Instant.now();
        }
        return RFC_822_FORMATTER.format(instant);
    }

    private int validateLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String validateLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "en";
        }
        return language.toLowerCase().startsWith("es") ? "es" : "en";
    }

    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
