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
 * REST controller for mobile-optimized news operations.
 *
 * <p>
 * Provides endpoints for retrieving news content specifically formatted
 * and optimized for mobile app consumption.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/mobile/news")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mobile News API", description = "Mobile-optimized news operations")
public class MobileNewsController {

    private final NewsService newsService;

    /**
     * Retrieves a single news item optimized for mobile display.
     *
     * @param newsId the unique identifier of the news item
     * @return ResponseEntity containing the mobile-optimized news data
     */
    @GetMapping("/{newsId}")
    @Operation(summary = "Get news by ID (Mobile Optimized)", description = "Retrieves a single news item with content formatted for mobile app consumption")
    public ResponseEntity<NewsCreateResponseDto> getNewsById(
            @Parameter(description = "News item ID", required = true, example = "1") @PathVariable Long newsId) {

        log.info("Mobile news request for ID: {}", newsId);

        NewsCreateResponseDto response = newsService.getNewsForMobile(newsId);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves paginated news items in card format for mobile lists.
     *
     * @param page     page number (0-based)
     * @param size     page size
     * @param category optional category filter
     * @param priority optional priority filter
     * @return ResponseEntity containing paginated news cards
     */
    @GetMapping("/cards")
    @Operation(summary = "Get news cards for mobile lists", description = "Retrieves paginated news items in card format optimized for mobile list views")
    public ResponseEntity<Page<NewsCreateResponseDto>> getNewsCards(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Category filter", example = "Politics") @RequestParam(required = false) String category,

            @Parameter(description = "Priority filter", example = "HIGH") @RequestParam(required = false) String priority) {

        log.info("Mobile news cards request - page: {}, size: {}, category: {}, priority: {}",
                page, size, category, priority);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsCreateResponseDto> response = newsService.getNewsCardsForMobile(pageable, category, priority);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves breaking news items optimized for mobile.
     *
     * @param limit maximum number of items to return
     * @return ResponseEntity containing breaking news items
     */
    @GetMapping("/breaking")
    @Operation(summary = "Get breaking news (Mobile Optimized)", description = "Retrieves breaking news items with mobile-optimized formatting")
    public ResponseEntity<Page<NewsCreateResponseDto>> getBreakingNews(
            @Parameter(description = "Maximum number of items", example = "10") @RequestParam(defaultValue = "10") int limit) {

        log.info("Mobile breaking news request - limit: {}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        Page<NewsCreateResponseDto> response = newsService.getBreakingNewsForMobile(pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves news items by category optimized for mobile.
     *
     * @param category the news category
     * @param page     page number (0-based)
     * @param size     page size
     * @return ResponseEntity containing paginated category news
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get news by category (Mobile Optimized)", description = "Retrieves news items for a specific category with mobile optimization")
    public ResponseEntity<Page<NewsCreateResponseDto>> getNewsByCategory(
            @Parameter(description = "News category", required = true, example = "Politics") @PathVariable String category,

            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.info("Mobile category news request - category: {}, page: {}, size: {}", category, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NewsCreateResponseDto> response = newsService.getNewsByCategoryForMobile(category, pageable);

        return ResponseEntity.ok(response);
    }
}