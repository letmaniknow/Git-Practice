package com.mmva.newsapp.controller.open.news;

import com.mmva.newsapp.domain.news.dto.core.NewsCreateResponseDto;
import com.mmva.newsapp.domain.news.service.core.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for web-optimized news operations.
 *
 * <p>
 * Provides endpoints for retrieving news content specifically formatted
 * and optimized for web browser consumption with SEO metadata.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/web/news")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Web News API", description = "Web-optimized news operations with SEO")
public class WebNewsController {

    private final NewsService newsService;

    /**
     * Retrieves a single news item optimized for web display with SEO metadata.
     *
     * @param newsId the unique identifier of the news item
     * @return ResponseEntity containing the web-optimized news data
     */
    @GetMapping("/{newsId}")
    @Operation(summary = "Get news by ID (Web Optimized)", description = "Retrieves a single news item with HTML content and SEO metadata for web browsers")
    public ResponseEntity<NewsCreateResponseDto> getNewsById(
            @Parameter(description = "News item ID", required = true, example = "1") @PathVariable Long newsId) {

        log.info("Web news request for ID: {}", newsId);

        NewsCreateResponseDto response = newsService.getNewsForWeb(newsId);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves paginated news cards for web list views.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return ResponseEntity containing paginated news cards
     */
    @GetMapping("/cards")
    @Operation(summary = "Get news cards for web lists", description = "Retrieves paginated news items in card format for web list views")
    public ResponseEntity<Page<NewsCreateResponseDto>> getNewsCards(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.info("Web news cards request - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsCreateResponseDto> response = newsService.getNewsCards(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves trending news cards for web display.
     *
     * @param limit maximum number of items to return
     * @return ResponseEntity containing trending news cards
     */
    @GetMapping("/trending")
    @Operation(summary = "Get trending news cards (Web)", description = "Retrieves trending news items in card format for web display")
    public ResponseEntity<java.util.List<NewsCreateResponseDto>> getTrendingNews(
            @Parameter(description = "Maximum number of items", example = "10") @RequestParam(defaultValue = "10") int limit) {

        log.info("Web trending news request - limit: {}", limit);

        java.util.List<NewsCreateResponseDto> response = newsService.getTrendingNewsCards(limit);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves news items by category for web display.
     *
     * @param category the news category
     * @param page     page number (0-based)
     * @param size     page size
     * @return ResponseEntity containing paginated category news
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get news by category (Web Optimized)", description = "Retrieves news items for a specific category optimized for web display")
    public ResponseEntity<Page<NewsCreateResponseDto>> getNewsByCategory(
            @Parameter(description = "News category", required = true, example = "Politics") @PathVariable String category,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.info("Web category news request - category: {}, page: {}, size: {}", category, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsCreateResponseDto> response = newsService.getNewsCardsByCategory(category, pageable);

        return ResponseEntity.ok(response);
    }
}