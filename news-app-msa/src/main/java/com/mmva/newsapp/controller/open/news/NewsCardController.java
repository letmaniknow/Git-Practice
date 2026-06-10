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
 * REST controller for news card operations.
 *
 * <p>
 * Provides endpoints for retrieving news content in card format,
 * optimized for list views, feeds, and preview displays.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/news/cards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "News Cards API", description = "News card operations for lists and feeds")
public class NewsCardController {

    private final NewsService newsService;

    /**
     * Retrieves paginated news cards for general list views.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return ResponseEntity containing paginated news cards
     */
    @GetMapping
    @Operation(summary = "Get news cards", description = "Retrieves paginated news items in card format for list views and feeds")
    public ResponseEntity<Page<NewsCreateResponseDto>> getNewsCards(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.info("News cards request - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsCreateResponseDto> response = newsService.getNewsCards(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves trending news cards based on engagement.
     *
     * @param limit maximum number of cards to return
     * @return ResponseEntity containing trending news cards
     */
    @GetMapping("/trending")
    @Operation(summary = "Get trending news cards", description = "Retrieves trending news items in card format based on engagement metrics")
    public ResponseEntity<java.util.List<NewsCreateResponseDto>> getTrendingNewsCards(
            @Parameter(description = "Maximum number of items", example = "10") @RequestParam(defaultValue = "10") int limit) {

        log.info("Trending news cards request - limit: {}", limit);

        java.util.List<NewsCreateResponseDto> response = newsService.getTrendingNewsCards(limit);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves news cards filtered by category.
     *
     * @param category the news category
     * @param page     page number (0-based)
     * @param size     page size
     * @return ResponseEntity containing paginated category news cards
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get news cards by category", description = "Retrieves news cards filtered by specific category")
    public ResponseEntity<Page<NewsCreateResponseDto>> getNewsCardsByCategory(
            @Parameter(description = "News category", required = true, example = "Politics") @PathVariable String category,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.info("Category news cards request - category: {}, page: {}, size: {}", category, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsCreateResponseDto> response = newsService.getNewsCardsByCategory(category, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves featured news cards for homepage/highlight sections.
     *
     * @param limit maximum number of featured cards to return
     * @return ResponseEntity containing featured news cards
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured news cards", description = "Retrieves featured news items in card format for homepage highlights")
    public ResponseEntity<java.util.List<NewsCreateResponseDto>> getFeaturedNewsCards(
            @Parameter(description = "Maximum number of items", example = "5") @RequestParam(defaultValue = "5") int limit) {

        log.info("Featured news cards request - limit: {}", limit);

        // For now, use trending as featured. In production, this would have its own
        // logic
        java.util.List<NewsCreateResponseDto> response = newsService.getTrendingNewsCards(limit);

        return ResponseEntity.ok(response);
    }
}