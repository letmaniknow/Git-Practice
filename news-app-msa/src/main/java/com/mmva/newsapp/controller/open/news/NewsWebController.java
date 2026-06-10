package com.mmva.newsapp.controller.open.news;

// Spring imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

// Project imports
import com.mmva.newsapp.domain.news.dto.seo.NewsSeoMetaTagsDto;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.service.core.NewsService;
import com.mmva.newsapp.domain.news.service.seo.NewsSeoService;

/**
 * Web Controller for SEO-optimized news article pages.
 *
 * <p>
 * This controller demonstrates how to serve news articles as HTML pages
 * with comprehensive SEO meta tags, Open Graph tags, Twitter Cards,
 * and Schema.org structured data for optimal search engine visibility
 * and social media sharing.
 * </p>
 *
 * <p>
 * Note: This is a demonstration controller. In a production microservices
 * architecture, web page serving might be handled by a separate frontend
 * service.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Controller
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsWebController {

    private final NewsService newsService;
    private final NewsSeoService seoService;

    /**
     * Displays a news article page with full SEO optimization.
     *
     * <p>
     * This endpoint serves an HTML page containing:
     * - SEO meta tags (title, description, keywords)
     * - Open Graph tags for Facebook/LinkedIn sharing
     * - Twitter Card tags for Twitter sharing
     * - Schema.org structured data for rich snippets
     * - Article-specific meta tags
     * </p>
     *
     * @param slug  The unique slug identifier for the news article
     * @param model Spring MVC model to pass data to the Thymeleaf template
     * @return View name for the news article template
     */
    @GetMapping("/{slug}")
    public String viewNewsArticle(@PathVariable String slug, Model model) {
        log.debug("Web: Serving SEO-optimized news article page for slug: {}", slug);

        try {
            // Fetch the published news article
            NewsMasterEntity news = newsService.getPublishedNewsEntityBySlug(slug);

            // Generate comprehensive SEO meta tags
            NewsSeoMetaTagsDto seoMetaTags = seoService.generateMetaTags(news);

            // Add data to the model for Thymeleaf template
            model.addAttribute("news", news);
            model.addAttribute("seo", seoMetaTags);

            // Add individual SEO components for template flexibility
            model.addAttribute("pageTitle", seoMetaTags.getTitle());
            model.addAttribute("metaDescription", seoMetaTags.getDescription());
            model.addAttribute("canonicalUrl", seoMetaTags.getCanonicalUrl());
            model.addAttribute("keywords", seoMetaTags.getKeywords());

            // Open Graph and Twitter Card data
            model.addAttribute("openGraph", seoMetaTags.getOpenGraph());
            model.addAttribute("twitterCard", seoMetaTags.getTwitterCard());

            // Structured data as JSON-LD
            model.addAttribute("structuredDataJson", seoMetaTags.getStructuredDataJson());

            // Article-specific metadata
            model.addAttribute("articleMeta", seoMetaTags.getArticle());

            log.debug("Web: Successfully prepared SEO-optimized page for news: {}", news.getNewsNewsId());

            return "news/article"; // Thymeleaf template path

        } catch (Exception e) {
            log.error("Web: Failed to serve news article page for slug: {}", slug, e);
            model.addAttribute("error", "News article not found");
            return "error/404"; // Error template
        }
    }

    /**
     * Alternative endpoint using news ID instead of slug.
     *
     * <p>
     * This endpoint demonstrates SEO optimization using news ID.
     * Slug-based URLs are preferred for SEO.
     * </p>
     *
     * @param newsId The news article ID
     * @param model  Spring MVC model
     * @return View name for the news article template
     */
    @GetMapping("/id/{newsId}")
    public String viewNewsArticleById(@PathVariable String newsId, Model model) {
        log.debug("Web: Serving news article page by ID: {}", newsId);

        try {
            // Fetch the published news article by ID
            NewsMasterEntity news = newsService.getPublishedNewsEntityById(newsId);

            // Generate SEO meta tags
            NewsSeoMetaTagsDto seoMetaTags = seoService.generateMetaTags(news);

            // Add to model
            model.addAttribute("news", news);
            model.addAttribute("seo", seoMetaTags);

            log.debug("Web: Successfully prepared page for news ID: {}", newsId);

            return "news/article";

        } catch (Exception e) {
            log.error("Web: Failed to serve news article page for ID: {}", newsId, e);
            model.addAttribute("error", "News article not found");
            return "error/404";
        }
    }
}