package com.mmva.newsapp.controller.open.news;

// OpenAPI/Swagger imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Lombok imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
// Spring imports
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mmva.newsapp.domain.news.dto.core.NewsCreateResponseDto;
import com.mmva.newsapp.domain.news.dto.core.NewsMediaFileRequestDto;
import com.mmva.newsapp.domain.news.service.core.NewsService;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentResponseDto;
import com.mmva.newsapp.domain.newsengagement.comments.service.NewsCommentService;
import com.mmva.newsapp.domain.newsengagement.likes.service.NewsLikeService;
import com.mmva.newsapp.domain.newsengagement.shares.service.NewsShareService;
import com.mmva.newsapp.domain.newsengagement.shares.dto.NewsShareRequestDto;
import com.mmva.newsapp.domain.newsengagement.shares.dto.NewsShareResponseDto;
import com.mmva.newsapp.domain.newsengagement.views.dto.NewsViewRequestDto;
import com.mmva.newsapp.domain.newsengagement.views.dto.NewsViewResponseDto;
import com.mmva.newsapp.domain.newsengagement.views.service.NewsViewService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.common.util.SortUtils;

// Java imports
import java.util.List;
import java.util.UUID;

/**
 * Public News Controller - For common users (read-only access to PUBLISHED
 * newsapp).
 * 
 * <p>
 * All endpoints only return newsapp with workflowStatus = 'published' AND
 * isActive
 * = true.
 * Following industry best practices:
 * </p>
 * <ul>
 * <li>Path prefix: /api/v1/public/news</li>
 * <li>API versioning in path</li>
 * <li>No authentication required (public endpoints)</li>
 * </ul>
 * 
 * @see com.mmva.newsapp.controller.admindashboard.AdminNewsController for
 *      admindashboard newsapp
 *      management
 */
@CrossOrigin(origins = "*")
@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/public/news")
@Tag(name = "Public News", description = "Public read-only APIs for published newsapp articles")
@RequiredArgsConstructor
public class PublicNewsController {

        // =============================
        // Constants
        // =============================
        private static final String DEFAULT_PAGE = "0";
        private static final String DEFAULT_SIZE = "10";
        // Use Java property name (newsPublishedAt), not DB column name
        // (news_published_at)
        private static final String DEFAULT_SORT = "newsPublishedAt,desc";
        @SuppressWarnings("unused") // Reserved for related articles feature
        private static final int DEFAULT_RELATED_LIMIT = 5;

        // =============================
        // Dependencies
        // =============================
        private final NewsService newsService;
        private final NewsCommentService commentService;
        private final NewsLikeService likeService;
        private final NewsShareService shareService;
        private final NewsViewService viewService;

        // =============================
        // News Retrieval Endpoints (1-9)
        // =============================

        /**
         * 1. Get all published newsapp (paginated).
         */
        @GetMapping
        @Operation(summary = "1. Get all published newsapp", description = "Retrieves paginated list of published and active newsapp articles")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Published newsapp fetched successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> getAllPublishedNews(
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) int size,
                        @Parameter(description = "Sort criteria (e.g., news_published_at,desc)") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                log.debug("Public: Fetching all published newsapp - page={}, size={}, sort={}", page, size, sort);

                Pageable pageable = PageRequest.of(page, size, Sort.by(SortUtils.parseSort(sort)));
                Page<NewsCreateResponseDto> news = newsService.getPublishedActiveNews(pageable);

                log.debug("Public: Retrieved {} published newsapp articles", news.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("Published newsapp fetched successfully", news));
        }

        /**
         * 2. Get published newsapp by ID.
         */
        @GetMapping("/{newsId}")
        @Operation(summary = "2. Get published newsapp by ID", description = "Retrieves a specific published newsapp newsapp by ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "News fetched successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found or not published")
        })
        public ResponseEntity<ApiResponseDto<NewsCreateResponseDto>> getPublishedNewsById(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                log.debug("Public: Fetching published newsapp by ID: {}", newsId);

                NewsCreateResponseDto response = newsService.getPublishedNewsById(newsId);

                log.debug("Public: Retrieved newsapp newsapp: {}", newsId);
                return ResponseEntity.ok(ApiResponseDto.success("News fetched successfully", response));
        }

        /**
         * 3. Get published newsapp by slug.
         */
        @GetMapping("/slug/{slug}")
        @Operation(summary = "3. Get published newsapp by slug", description = "Retrieves a published newsapp newsapp by its unique slug")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "News fetched successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found or not published")
        })
        public ResponseEntity<ApiResponseDto<NewsCreateResponseDto>> getPublishedNewsBySlug(
                        @Parameter(description = "News newsapp slug", required = true) @PathVariable String slug) {

                log.debug("Public: Fetching published newsapp by slug: {}", slug);

                NewsCreateResponseDto response = newsService.getPublishedNewsBySlug(slug);

                log.debug("Public: Retrieved newsapp newsapp by slug: {}", slug);
                return ResponseEntity.ok(ApiResponseDto.success("News fetched successfully", response));
        }

        /**
         * 4. Get published newsapp by newscategory.
         */
        @GetMapping("/newscategory/{newsCategoryId}")
        @Operation(summary = "4. Get published newsapp by newscategory", description = "Retrieves published newsapp articles by newscategory ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "News by newscategory fetched successfully"),
                        @ApiResponse(responseCode = "404", description = "Category not found")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> getPublishedNewsByCategory(
                        @Parameter(description = "Category ID", required = true) @PathVariable String newsCategoryId,
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) int size,
                        @Parameter(description = "Sort criteria (e.g., news_published_at,desc)") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                log.debug("Public: Fetching published newsapp by newscategory: {} - page={}, size={}",
                                newsCategoryId, page, size);

                Pageable pageable = PageRequest.of(page, size, Sort.by(SortUtils.parseSort(sort)));
                Page<NewsCreateResponseDto> news = newsService.getPublishedNewsByCategory(newsCategoryId, pageable);

                log.debug("Public: Retrieved {} newsapp articles for newscategory: {}",
                                news.getTotalElements(), newsCategoryId);
                return ResponseEntity.ok(ApiResponseDto.success("News by newscategory fetched successfully", news));
        }

        /**
         * 5. Search published newsapp.
         */
        @GetMapping("/search")
        @Operation(summary = "5. Search published newsapp", description = "Search published newsapp articles with filters")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Search completed successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> searchPublishedNews(
                        @Parameter(description = "Search query text") @RequestParam(required = false) String query,
                        @Parameter(description = "Filter by newscategory ID") @RequestParam(required = false) String newsCategoryId,
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) int size,
                        @Parameter(description = "Sort criteria (e.g., news_published_at,desc)") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                log.debug("Public: Searching published newsapp - query='{}', categoryId={}, page={}, size={}",
                                query, newsCategoryId, page, size);

                Pageable pageable = PageRequest.of(page, size, Sort.by(SortUtils.parseSort(sort)));
                Page<NewsCreateResponseDto> news = newsService.searchPublishedNews(query, newsCategoryId, pageable);

                log.debug("Public: Search returned {} results", news.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("Search completed successfully", news));
        }

        /**
         * 6. Get trending newsapp.
         */
        @GetMapping("/trending")
        @Operation(summary = "6. Get trending newsapp", description = "Fetches trending published newsapp based on views, likes, shares")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Trending newsapp fetched successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> getTrendingNews(
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) int size) {

                log.debug("Public: Fetching trending newsapp - page={}, size={}", page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<NewsCreateResponseDto> trending = newsService.getTrendingPublishedNews(pageable);

                log.debug("Public: Retrieved {} trending newsapp articles", trending.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("Trending newsapp fetched successfully", trending));
        }

        /**
         * 7. Get featured newsapp.
         */
        @GetMapping("/featured")
        @Operation(summary = "7. Get featured newsapp", description = "Retrieves featured/pinned published newsapp articles")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Featured newsapp fetched successfully")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> getFeaturedNews(
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) int size) {

                log.debug("Public: Fetching featured newsapp - page={}, size={}", page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<NewsCreateResponseDto> featured = newsService.getFeaturedPublishedNews(pageable);

                log.debug("Public: Retrieved {} featured newsapp articles", featured.getTotalElements());
                return ResponseEntity.ok(ApiResponseDto.success("Featured newsapp fetched successfully", featured));
        }

        /**
         * 8. Get related newsapp.
         */
        @GetMapping("/{newsId}/related")
        @Operation(summary = "8. Get related newsapp", description = "Retrieves published newsapp articles related to the given newsapp ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Related newsapp fetched successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<List<NewsCreateResponseDto>>> getRelatedNews(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId,
                        @Parameter(description = "Maximum number of related articles to return") @RequestParam(defaultValue = "5") int limit) {

                log.debug("Public: Fetching related newsapp for ID: {} - limit={}", newsId, limit);

                List<NewsCreateResponseDto> related = newsService.getRelatedPublishedNews(newsId, limit);

                log.debug("Public: Retrieved {} related newsapp articles for: {}", related.size(), newsId);
                return ResponseEntity.ok(ApiResponseDto.success("Related newsapp fetched successfully", related));
        }

        /**
         * 9. Get published newsapp by author.
         */
        @GetMapping("/author/{authorId}")
        @Operation(summary = "9. Get published newsapp by author", description = "Retrieves published newsapp articles by author ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "News by author fetched successfully"),
                        @ApiResponse(responseCode = "404", description = "Author not found")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCreateResponseDto>>> getNewsByAuthor(
                        @Parameter(description = "Author ID", required = true) @PathVariable String authorId,
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) int size,
                        @Parameter(description = "Sort criteria (e.g., news_published_at,desc)") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                log.debug("Public: Fetching published newsapp by author: {} - page={}, size={}", authorId, page, size);

                Pageable pageable = PageRequest.of(page, size, Sort.by(SortUtils.parseSort(sort)));
                Page<NewsCreateResponseDto> news = newsService.getPublishedNewsByAuthor(authorId, pageable);

                log.debug("Public: Retrieved {} newsapp articles by author: {}", news.getTotalElements(), authorId);
                return ResponseEntity.ok(ApiResponseDto.success("News by author fetched successfully", news));
        }

        /**
         * 10. Get newsapp recommendations.
         */
        @GetMapping("/recommendations")
        @Operation(summary = "10. Get newsapp recommendations", description = "Fetches personalized or default newsapp recommendations")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Recommendations fetched successfully")
        })
        public ResponseEntity<ApiResponseDto<List<NewsCreateResponseDto>>> getRecommendations(
                        @Parameter(description = "User ID for personalized recommendations (optional)") @RequestParam(required = false) UUID userId,
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) int size,
                        @Parameter(description = "Sort criteria (e.g., news_published_at,desc)") @RequestParam(defaultValue = DEFAULT_SORT) String sort) {

                log.debug("Public: Fetching recommendations - userId={}, page={}, size={}", userId, page, size);

                Pageable pageable = PageRequest.of(page, size, Sort.by(SortUtils.parseSort(sort)));
                List<NewsCreateResponseDto> recommendations;

                if (userId != null) {
                        log.debug("Public: Fetching personalized recommendations for user: {}", userId);
                        recommendations = newsService.getUserRecommendations(userId, pageable);
                } else {
                        log.debug("Public: Fetching default recommendations");
                        recommendations = newsService.getDefaultRecommendations(pageable);
                }

                log.debug("Public: Retrieved {} recommendations", recommendations.size());
                return ResponseEntity
                                .ok(ApiResponseDto.success("Recommendations fetched successfully", recommendations));
        }

        // =============================
        // Comment Endpoints (11-16)
        // =============================

        /**
         * 11. Get approved comments for newsapp (paginated).
         */
        @GetMapping("/{newsId}/comments")
        @Operation(summary = "11. Get approved comments for newsapp (paginated)", description = "Retrieves paginated approved comments for a published newsapp newsapp")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<Page<NewsCommentResponseDto>>> getNewsCommentsPaginated(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId,
                        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = DEFAULT_SIZE) int size) {

                log.debug("Public: Fetching paginated comments for newsapp: {} - page={}, size={}", newsId, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<NewsCommentResponseDto> comments = commentService.getApprovedCommentsByNewsId(
                                UUID.fromString(newsId), pageable);

                log.debug("Public: Retrieved {} comments for newsapp: {}", comments.getTotalElements(), newsId);
                return ResponseEntity.ok(ApiResponseDto.success("Comments retrieved successfully", comments));
        }

        /**
         * 12. Get all approved comments for newsapp (non-paginated, legacy).
         */
        @GetMapping("/{newsId}/comments/all")
        @Operation(summary = "12. Get all approved comments (legacy)", description = "Retrieves all approved comments for a published newsapp newsapp (legacy, not recommended for large threads)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<List<NewsCommentResponseDto>>> getNewsCommentsAll(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                log.debug("Public: Fetching ALL comments for newsapp: {} (legacy endpoint)", newsId);

                List<NewsCommentResponseDto> comments = commentService.getApprovedCommentsByNewsId(
                                UUID.fromString(newsId));

                log.debug("Public: Retrieved {} comments for newsapp: {}", comments.size(), newsId);
                return ResponseEntity.ok(ApiResponseDto.success("Comments retrieved successfully", comments));
        }

        /**
         * 13. Get approved comment count for newsapp.
         */
        @GetMapping("/{newsId}/comments/count")
        @Operation(summary = "13. Get approved comment count", description = "Returns the number of approved comments for a newsapp newsapp")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Comment count retrieved"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<Long>> getApprovedCommentCount(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                log.debug("Public: Fetching approved comment count for newsapp: {}", newsId);

                Long count = commentService.getApprovedCommentCountByNewsId(UUID.fromString(newsId));

                log.debug("Public: News {} has {} approved comments", newsId, count);
                return ResponseEntity.ok(ApiResponseDto.success("Comment count retrieved", count));
        }

        /**
         * 14. Get approved comment thread for newsapp.
         */
        @GetMapping("/{newsId}/comments/thread")
        @Operation(summary = "14. Get approved comment thread", description = "Fetches the full nested approved comment thread for a newsapp newsapp")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Comment thread fetched"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<List<NewsCommentResponseDto>>> getCommentThreadForNews(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable UUID newsId) {

                log.debug("Public: Fetching approved comment thread for newsapp: {}", newsId);

                List<NewsCommentResponseDto> thread = commentService.getApprovedCommentThreadForNews(newsId);

                log.debug("Public: Retrieved comment thread with {} top-level comments for newsapp: {}",
                                thread.size(), newsId);
                return ResponseEntity.ok(ApiResponseDto.success("Comment thread fetched", thread));
        }

        /**
         * 15. Get approved replies for a comment.
         */
        @GetMapping("/comments/{commentId}/replies")
        @Operation(summary = "15. Get approved replies for a comment", description = "Fetches all approved replies for a given comment ID (public)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Replies fetched"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<ApiResponseDto<List<NewsCommentResponseDto>>> getRepliesForComment(
                        @Parameter(description = "Comment ID", required = true) @PathVariable UUID commentId) {

                log.debug("Public: Fetching approved replies for comment: {}", commentId);

                List<NewsCommentResponseDto> replies = commentService.getApprovedReplies(commentId);

                log.debug("Public: Retrieved {} replies for comment: {}", replies.size(), commentId);
                return ResponseEntity.ok(ApiResponseDto.success("Replies fetched", replies));
        }

        /**
         * 16. Get like count for comment.
         */
        @GetMapping("/comments/{commentId}/likes/count")
        @Operation(summary = "16. Get like count for comment", description = "Returns the number of likes for a comment")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Like count retrieved"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<ApiResponseDto<Long>> getCommentLikeCount(
                        @Parameter(description = "Comment ID", required = true) @PathVariable UUID commentId) {

                log.debug("Public: Fetching like count for comment: {}", commentId);

                long count = commentService.getLikeCount(commentId);

                log.debug("Public: Comment {} has {} likes", commentId, count);
                return ResponseEntity.ok(ApiResponseDto.success("Like count retrieved", count));
        }

        /**
         * 17. Get report count for comment.
         */
        @GetMapping("/comments/{commentId}/reports/count")
        @Operation(summary = "17. Get report count for comment", description = "Returns the number of reports for a comment")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Report count retrieved"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<ApiResponseDto<Long>> getCommentReportCount(
                        @Parameter(description = "Comment ID", required = true) @PathVariable UUID commentId) {

                log.debug("Public: Fetching report count for comment: {}", commentId);

                long count = commentService.getReportCount(commentId);

                log.debug("Public: Comment {} has {} reports", commentId, count);
                return ResponseEntity.ok(ApiResponseDto.success("Report count retrieved", count));
        }

        /**
         * 18. Get users who liked a comment.
         */
        @GetMapping("/comments/{commentId}/likes/users")
        @Operation(summary = "18. Get users who liked a comment", description = "Returns a list of user IDs who liked the comment")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Users who liked comment retrieved"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        public ResponseEntity<ApiResponseDto<List<UUID>>> getUsersWhoLikedComment(
                        @Parameter(description = "Comment ID", required = true) @PathVariable UUID commentId) {

                log.debug("Public: Fetching users who liked comment: {}", commentId);

                List<UUID> users = commentService.getUsersWhoLikedComment(commentId);

                log.debug("Public: {} users liked comment: {}", users.size(), commentId);
                return ResponseEntity.ok(ApiResponseDto.success("Users who liked comment retrieved", users));
        }

        // =============================
        // Engagement Stats Endpoints (19-21)
        // =============================

        /**
         * 19. Get like count for newsapp.
         */
        @GetMapping("/{newsId}/likes/count")
        @Operation(summary = "19. Get like count", description = "Get the total number of likes for a newsapp newsapp")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Like count retrieved"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<Long>> getLikeCount(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                log.debug("Public: Fetching like count for newsapp: {}", newsId);

                Long count = likeService.getLikeCountByNewsId(UUID.fromString(newsId));

                log.debug("Public: News {} has {} likes", newsId, count);
                return ResponseEntity.ok(ApiResponseDto.success("Like count retrieved", count));
        }

        /**
         * 20. Get share count for newsapp.
         */
        @GetMapping("/{newsId}/shares/count")
        @Operation(summary = "20. Get share count", description = "Get the total number of shares for a newsapp newsapp")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Share count retrieved"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<Long>> getShareCount(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                log.debug("Public: Fetching share count for newsapp: {}", newsId);

                Long count = shareService.getShareCountByNewsId(UUID.fromString(newsId));

                log.debug("Public: News {} has {} shares", newsId, count);
                return ResponseEntity.ok(ApiResponseDto.success("Share count retrieved", count));
        }

        /**
         * 21. Get view count for newsapp.
         */
        @GetMapping("/{newsId}/views/count")
        @Operation(summary = "21. Get view count", description = "Get the total number of views for a newsapp newsapp")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "View count retrieved"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<Long>> getViewCount(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId) {

                log.debug("Public: Fetching view count for newsapp: {}", newsId);

                Long count = viewService.getViewCountByNewsId(UUID.fromString(newsId));

                log.debug("Public: News {} has {} views", newsId, count);
                return ResponseEntity.ok(ApiResponseDto.success("View count retrieved", count));
        }

        // =============================
        // View Tracking Endpoint (22)
        // =============================

        /**
         * 22. Track view for newsapp.
         */
        @PostMapping("/{newsId}/view")
        @Operation(summary = "22. Track view", description = "Records a view for a newsapp newsapp (can be called automatically)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "View tracked successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<NewsViewResponseDto>> trackView(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId,
                        @Parameter(description = "User ID for authenticated views (optional)") @RequestHeader(value = "X-User-Id", required = false) UUID userId) {

                log.info("Public: Tracking view for newsapp: {} by user: {}", newsId, userId);

                NewsViewRequestDto dto = NewsViewRequestDto.builder()
                                .newsViewsNewsId(UUID.fromString(newsId))
                                .newsViewsUserId(userId)
                                .build();
                NewsViewResponseDto response = viewService.addView(dto);

                log.info("Public: View added for newsapp: {} - viewId: {}", newsId, response.getNewsViewsId());
                return ResponseEntity.ok(ApiResponseDto.success("View tracked successfully", response));
        }

        // =============================
        // Share Tracking Endpoint (23)
        // =============================

        /**
         * 23. Track share for newsapp.
         * 
         * <p>
         * This endpoint allows both authenticated and anonymous share tracking.
         * When a user shares a news article via social media, email, or other channels,
         * this endpoint records the share event for analytics.
         * </p>
         * 
         * <p>
         * <strong>Design Note:</strong> Share is an EVENT (like View), not a STATE
         * (like Like/Bookmark).
         * Events cannot be undone by users - they represent a moment in time.
         * </p>
         * 
         * <ul>
         * <li><strong>Authenticated shares:</strong> Pass userId to track WHO
         * shared</li>
         * <li><strong>Anonymous shares:</strong> Omit userId for privacy-focused
         * analytics</li>
         * </ul>
         */
        @PostMapping("/{newsId}/share")
        @Operation(summary = "23. Track share", description = "Records a share event for a news article (anonymous or authenticated)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Share tracked successfully"),
                        @ApiResponse(responseCode = "404", description = "News not found")
        })
        public ResponseEntity<ApiResponseDto<NewsShareResponseDto>> trackShare(
                        @Parameter(description = "News newsapp ID", required = true) @PathVariable String newsId,
                        @Parameter(description = "User ID for authenticated shares (optional)") @RequestHeader(value = "X-User-Id", required = false) UUID userId,
                        @Parameter(description = "Share platform (e.g., FACEBOOK, TWITTER, EMAIL, WHATSAPP, LINKEDIN, COPY_LINK)", required = true) @RequestHeader(value = "X-Share-Platform") String platform) {

                log.info("Public: Tracking share for newsapp: {} by user: {} via platform: {}", newsId, userId,
                                platform);

                NewsShareRequestDto dto = NewsShareRequestDto.builder()
                                .newsSharesNewsId(UUID.fromString(newsId))
                                .newsSharesUserId(userId)
                                .newsSharesPlatform(platform)
                                .build();
                NewsShareResponseDto response = shareService.addShare(dto);

                log.info("Public: Share added for newsapp: {} - shareId: {}", newsId, response.getNewsSharesId());
                return ResponseEntity.ok(ApiResponseDto.success("Share tracked successfully", response));
        }

        // ===============================
        // Endpoint 24: Get Media File
        // ===============================

        /**
         * Retrieves a media file by filename.
         *
         * @param filename the media filename
         * @param adminId  the admindashboard UUID
         * @return the media resource
         */
        @GetMapping("/media/{filename}")
        @Operation(summary = "24. Get media file", description = "Retrieves a media file associated with newsapp")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Media file retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Media file not found")
        })
        public ResponseEntity<Resource> getMediaFile(
                        @Parameter(description = "Media filename", required = true) @PathVariable String filename) {

                log.debug("Admin: Fetching media file: {} ", filename);

                NewsMediaFileRequestDto mediaFile = newsService.getMediaFile(filename);

                log.debug("Admin: Retrieved media file: {}", filename);
                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(mediaFile.getContentType()))
                                .body(mediaFile.getResource());
        }

        // ===============================
        // Endpoint 25: Get Thumbnail File
        // ===============================

        /**
         * Retrieves a thumbnail file by filename.
         * Thumbnails have optimized caching for better performance.
         *
         * @param filename the thumbnail filename
         * @return the thumbnail resource
         */
        @GetMapping("/thumbnails/{filename}")
        @Operation(summary = "25. Get thumbnail file", description = "Retrieves a thumbnail file associated with newsapp")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Thumbnail retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Thumbnail not found")
        })
        public ResponseEntity<Resource> getThumbnailFile(
                        @Parameter(description = "Thumbnail filename", required = true) @PathVariable String filename) {

                log.debug("Public: Fetching thumbnail file: {}", filename);

                NewsMediaFileRequestDto thumbnailFile = newsService.getThumbnailFile(filename);

                log.debug("Public: Retrieved thumbnail file: {}", filename);
                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(thumbnailFile.getContentType()))
                                .body(thumbnailFile.getResource());
        }

        // ===============================
        // Endpoint 26: Get Small Image File
        // ===============================

        /**
         * Retrieves a card size image file by filename.
         * Card images are 400x225 pixels (16:9) for news list views - Inshorts style.
         *
         * @param filename the card image filename
         * @return the card image resource
         */
        @GetMapping("/images/card/{filename}")
        @Operation(summary = "26. Get card image file", description = "Retrieves a card size image file (400x225) - Inshorts style")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Card image retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Card image not found")
        })
        public ResponseEntity<Resource> getCardImageFile(
                        @Parameter(description = "Card image filename", required = true) @PathVariable String filename) {

                log.debug("Public: Fetching card image file: {}", filename);

                NewsMediaFileRequestDto cardImageFile = newsService.getCardImageFile(filename);

                log.debug("Public: Retrieved card image file: {}", filename);
                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(cardImageFile.getContentType()))
                                .body(cardImageFile.getResource());
        }

        // ===============================
        // Endpoint 27: Get Medium Image File
        // ===============================

        /**
         * Retrieves a hero size image file by filename.
         * Hero images are 800x450 pixels (16:9) for featured news - Inshorts style.
         *
         * @param filename the hero image filename
         * @return the hero image resource
         */
        @GetMapping("/images/hero/{filename}")
        @Operation(summary = "27. Get hero image file", description = "Retrieves a hero size image file (800x450) - Inshorts style")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Hero image retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Hero image not found")
        })
        public ResponseEntity<Resource> getHeroImageFile(
                        @Parameter(description = "Hero image filename", required = true) @PathVariable String filename) {

                log.debug("Public: Fetching hero image file: {}", filename);

                NewsMediaFileRequestDto heroImageFile = newsService.getHeroImageFile(filename);

                log.debug("Public: Retrieved hero image file: {}", filename);
                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(heroImageFile.getContentType()))
                                .body(heroImageFile.getResource());
        }

}
