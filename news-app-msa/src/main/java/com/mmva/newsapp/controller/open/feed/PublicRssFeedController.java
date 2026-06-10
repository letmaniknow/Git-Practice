package com.mmva.newsapp.controller.open.feed;

import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.domain.newsrss.dto.core.NewsRssDiscoveryDto;
import com.mmva.newsapp.domain.newsrss.dto.core.NewsRssFeedDto;
import com.mmva.newsapp.domain.newsrss.service.core.NewsRssFeedService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.UUID;

/**
 * Public RSS Feed Controller.
 * Provides RSS 2.0 feeds for newsapp content distribution.
 * 
 * <h3>Available Feeds:</h3>
 * <ul>
 * <li>GET /newsrss - Latest newsapp (all categories)</li>
 * <li>GET /newsrss/newscategory/{id} - Category-specific feed</li>
 * <li>GET /newsrss/featured - Featured newsapp only</li>
 * </ul>
 * 
 * <h3>Query Parameters:</h3>
 * <ul>
 * <li>lang - Language code: "en" (default) or "es"</li>
 * <li>limit - Number of items: 1-100 (default: 20)</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/newsrss")
@RequiredArgsConstructor
@Tag(name = "RSS Feeds", description = "Public RSS 2.0 feeds for newsapp syndication")
@Slf4j
public class PublicRssFeedController {

    private static final String RSS_MEDIA_TYPE = "application/newsrss+xml";

    private final NewsRssFeedService newsRssFeedService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.name:News App}")
    private String appName;

    // ========================================
    // RSS Autodiscovery Endpoints
    // ========================================

    /**
     * Get RSS feed autodiscovery information.
     * Returns feed URLs and pre-built HTML link tags for frontend integration.
     * 
     * <h3>Usage:</h3>
     * <p>
     * Frontend developers should copy the HTML link tags from the response
     * and paste them into the &lt;head&gt; section of their HTML pages.
     * </p>
     * 
     * @return Discovery information with feed URLs and HTML tags
     */
    @GetMapping(value = "/discover", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get RSS autodiscovery info", description = "Returns feed URLs and HTML link tags for browser autodiscovery integration")
    public ResponseEntity<ApiResponseDto<NewsRssDiscoveryDto>> getDiscoveryInfo() {
        log.debug("RSS: Getting autodiscovery information");

        List<NewsRssDiscoveryDto.NewsRssFeedInfo> feeds = List.of(
                NewsRssDiscoveryDto.NewsRssFeedInfo.builder()
                        .title(appName + " - Latest News (English)")
                        .url(baseUrl + "/newsrss?lang=en")
                        .type("application/newsrss+xml")
                        .language("en")
                        .build(),
                NewsRssDiscoveryDto.NewsRssFeedInfo.builder()
                        .title(appName + " - Latest News (Spanish)")
                        .url(baseUrl + "/newsrss?lang=es")
                        .type("application/newsrss+xml")
                        .language("es")
                        .build(),
                NewsRssDiscoveryDto.NewsRssFeedInfo.builder()
                        .title(appName + " - Featured News (English)")
                        .url(baseUrl + "/newsrss/featured?lang=en")
                        .type("application/newsrss+xml")
                        .language("en")
                        .build(),
                NewsRssDiscoveryDto.NewsRssFeedInfo.builder()
                        .title(appName + " - Featured News (Spanish)")
                        .url(baseUrl + "/newsrss/featured?lang=es")
                        .type("application/newsrss+xml")
                        .language("es")
                        .build());

        String htmlTags = buildHtmlLinkTags(feeds);

        NewsRssDiscoveryDto discovery = NewsRssDiscoveryDto.builder()
                .feeds(feeds)
                .htmlLinkTags(htmlTags)
                .build();

        return ResponseEntity.ok(ApiResponseDto.success("RSS discovery info retrieved", discovery));
    }

    /**
     * Get HTML page with RSS autodiscovery link tags.
     * Browsers and feed readers can detect feeds from this page.
     * 
     * @return HTML page with autodiscovery meta tags
     */
    @GetMapping(value = "/discover.html", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Get RSS autodiscovery HTML page", description = "Returns an HTML page with proper link tags for browser feed detection")
    public ResponseEntity<String> getDiscoveryHtml() {
        log.debug("RSS: Generating autodiscovery HTML page");

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s - RSS Feeds</title>

                    <!-- RSS Autodiscovery Links -->
                    <link rel="alternate" type="application/newsrss+xml" title="%s - Latest News (EN)" href="%s/newsrss?lang=en" />
                    <link rel="alternate" type="application/newsrss+xml" title="%s - Latest News (ES)" href="%s/newsrss?lang=es" />
                    <link rel="alternate" type="application/newsrss+xml" title="%s - Featured (EN)" href="%s/newsrss/featured?lang=en" />
                    <link rel="alternate" type="application/newsrss+xml" title="%s - Featured (ES)" href="%s/newsrss/featured?lang=es" />

                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 800px; margin: 50px auto; padding: 20px; }
                        h1 { color: #333; }
                        .feed-list { list-style: none; padding: 0; }
                        .feed-list li { padding: 15px; margin: 10px 0; background: #f5f5f5; border-radius: 8px; }
                        .feed-list a { color: #e67e22; text-decoration: none; font-weight: bold; }
                        .feed-list a:hover { text-decoration: underline; }
                        .lang { font-size: 0.85em; color: #666; margin-left: 10px; }
                        code { background: #eee; padding: 2px 6px; border-radius: 4px; font-size: 0.9em; }
                        .code-block { background: #2d2d2d; color: #f8f8f2; padding: 15px; border-radius: 8px; overflow-x: auto; }
                        .code-block code { background: transparent; color: inherit; }
                    </style>
                </head>
                <body>
                    <h1>📰 %s RSS Feeds</h1>
                    <p>Subscribe to our newsapp feeds using any RSS reader:</p>

                    <h2>Available Feeds</h2>
                    <ul class="feed-list">
                        <li><a href="%s/newsrss?lang=en">Latest News</a> <span class="lang">English</span></li>
                        <li><a href="%s/newsrss?lang=es">Últimas Noticias</a> <span class="lang">Español</span></li>
                        <li><a href="%s/newsrss/featured?lang=en">Featured News</a> <span class="lang">English</span></li>
                        <li><a href="%s/newsrss/featured?lang=es">Noticias Destacadas</a> <span class="lang">Español</span></li>
                    </ul>

                    <h2>For Developers</h2>
                    <p>Add these tags to your HTML <code>&lt;head&gt;</code> for autodiscovery:</p>
                    <div class="code-block">
                        <code>&lt;link rel="alternate" type="application/newsrss+xml" title="Latest News" href="%s/newsrss" /&gt;</code>
                    </div>

                    <p style="margin-top: 30px; color: #666; font-size: 0.9em;">
                        API endpoint: <code>GET /newsrss/discover</code> returns JSON with all feed information.
                    </p>
                </body>
                </html>
                """
                .formatted(
                        appName, appName, baseUrl, appName, baseUrl, appName, baseUrl, appName, baseUrl,
                        appName, baseUrl, baseUrl, baseUrl, baseUrl, baseUrl);

        return ResponseEntity.ok(html);
    }

    // ========================================
    // RSS Feed Endpoints
    // ========================================

    /**
     * Get latest newsapp RSS feed.
     * 
     * @param language Language code ("en" or "es")
     * @param limit    Maximum number of items (1-100)
     * @return RSS XML feed
     */
    @GetMapping(produces = RSS_MEDIA_TYPE)
    @Operation(summary = "Get latest newsapp RSS feed", description = "Returns RSS 2.0 feed with latest published newsapp articles")
    public ResponseEntity<String> getLatestNewsFeed(
            @Parameter(description = "Language: 'en' (English) or 'es' (Spanish)") @RequestParam(defaultValue = "en") String lang,

            @Parameter(description = "Maximum items to return (1-100)") @RequestParam(defaultValue = "20") int limit) {

        log.debug("RSS: Fetching latest newsapp feed - lang: {}, limit: {}", lang, limit);

        NewsRssFeedDto feed = newsRssFeedService.getLatestNewsFeed(lang, limit);
        String xml = newsRssFeedService.toXml(feed);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(RSS_MEDIA_TYPE + ";charset=UTF-8"))
                .body(xml);
    }

    /**
     * Get newscategory-specific RSS feed.
     * 
     * @param categoryId Category UUID
     * @param language   Language code ("en" or "es")
     * @param limit      Maximum number of items
     * @return RSS XML feed
     */
    @GetMapping(value = "/newscategory/{categoryId}", produces = RSS_MEDIA_TYPE)
    @Operation(summary = "Get newscategory RSS feed", description = "Returns RSS 2.0 feed with newsapp from a specific newscategory")
    public ResponseEntity<String> getCategoryFeed(
            @Parameter(description = "Category UUID") @PathVariable UUID categoryId,

            @Parameter(description = "Language: 'en' (English) or 'es' (Spanish)") @RequestParam(defaultValue = "en") String lang,

            @Parameter(description = "Maximum items to return (1-100)") @RequestParam(defaultValue = "20") int limit) {

        log.debug("RSS: Fetching newscategory feed - categoryId: {}, lang: {}", categoryId, lang);

        NewsRssFeedDto feed = newsRssFeedService.getCategoryFeed(categoryId, lang, limit);
        String xml = newsRssFeedService.toXml(feed);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(RSS_MEDIA_TYPE + ";charset=UTF-8"))
                .body(xml);
    }

    /**
     * Get featured newsapp RSS feed.
     * 
     * @param language Language code ("en" or "es")
     * @param limit    Maximum number of items
     * @return RSS XML feed
     */
    @GetMapping(value = "/featured", produces = RSS_MEDIA_TYPE)
    @Operation(summary = "Get featured newsapp RSS feed", description = "Returns RSS 2.0 feed with featured/highlighted newsapp articles")
    public ResponseEntity<String> getFeaturedFeed(
            @Parameter(description = "Language: 'en' (English) or 'es' (Spanish)") @RequestParam(defaultValue = "en") String lang,

            @Parameter(description = "Maximum items to return (1-100)") @RequestParam(defaultValue = "20") int limit) {

        log.debug("RSS: Fetching featured newsapp feed - lang: {}", lang);

        NewsRssFeedDto feed = newsRssFeedService.getFeaturedNewsFeed(lang, limit);
        String xml = newsRssFeedService.toXml(feed);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(RSS_MEDIA_TYPE + ";charset=UTF-8"))
                .body(xml);
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * Build HTML link tags for RSS autodiscovery.
     */
    private String buildHtmlLinkTags(List<NewsRssDiscoveryDto.NewsRssFeedInfo> feeds) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- RSS Autodiscovery - Add these to your HTML <head> section -->\n");

        for (NewsRssDiscoveryDto.NewsRssFeedInfo feed : feeds) {
            sb.append(String.format(
                    "<link rel=\"alternate\" type=\"%s\" title=\"%s\" href=\"%s\" />\n",
                    feed.getType(), feed.getTitle(), feed.getUrl()));
        }

        return sb.toString();
    }
}
