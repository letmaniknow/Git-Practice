package com.mmva.newsapp.controller.user.engagement;

import com.mmva.newsapp.domain.newsengagement.likes.dto.NewsLikeRequestDto;
import com.mmva.newsapp.domain.newsengagement.likes.dto.NewsLikeResponseDto;
import com.mmva.newsapp.domain.newsengagement.likes.service.NewsLikeService;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.security.userdetails.AppUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * User Like Controller.
 * 
 * <p>
 * Handles all like operations for the authenticated user:
 * </p>
 * <ul>
 * <li>Like management (add, remove)</li>
 * <li>Like queries (list, check status)</li>
 * </ul>
 * 
 * <p>
 * Path prefix: /api/v1/me/likes
 * </p>
 */
@CrossOrigin(origins = "*", allowedHeaders = { "Range", "Accept", "Content-Type", "Authorization" }, exposedHeaders = {
                "Content-Length", "Content-Range", "Accept-Ranges" })
@RestController
@RequestMapping("/api/v1/me/likes")
@Tag(name = "User Likes", description = "Like management for authenticated users")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserLikeController {

        private final NewsLikeService likeService;

        // ==========================================
        // LIKE OPERATIONS (1-4)
        // ==========================================

        @GetMapping
        @Operation(summary = "1. Get my likes", description = "Retrieves all news articles liked by the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Likes retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<List<NewsLikeResponseDto>>> getMyLikes(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Like [{}]: Fetching all likes", userId);
                List<NewsLikeResponseDto> likes = likeService.getLikesByUserId(userId);
                log.debug("Like [{}]: Retrieved {} likes", userId, likes.size());
                return ResponseEntity.ok(ApiResponseDto.success("Likes retrieved successfully", likes));
        }

        @PostMapping
        @Operation(summary = "2. Like a news article", description = "Adds a like to a news article")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Like added"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "409", description = "Already liked")
        })
        public ResponseEntity<ApiResponseDto<NewsLikeResponseDto>> addLike(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Valid @RequestBody NewsLikeRequestDto dto) {
                UUID userId = userDetails.getUserId();
                log.info("Like [{}]: Liking news: {}", userId, dto.getNewsLikesNewsId());
                dto.setNewsLikesUserId(userId);
                NewsLikeResponseDto response = likeService.addLike(dto);
                log.info("Like [{}]: Liked news: {}", userId, dto.getNewsLikesNewsId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Like added successfully", response));
        }

        @GetMapping("/check/{newsId}")
        @Operation(summary = "3. Check if news is liked", description = "Checks if a news article is liked by the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Like status retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Boolean>> hasLikedNews(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "News ID to check", required = true) @PathVariable UUID newsId) {
                UUID userId = userDetails.getUserId();
                log.debug("Like [{}]: Checking like status for news: {}", userId, newsId);
                boolean liked = likeService.hasUserLikedNews(newsId, userId);
                log.debug("Like [{}]: News {} liked: {}", userId, newsId, liked);
                return ResponseEntity.ok(ApiResponseDto.success("Like status retrieved", liked));
        }

        @DeleteMapping("/{newsId}")
        @Operation(summary = "4. Unlike a news article", description = "Removes the like from a news article")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Like removed"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized"),
                        @ApiResponse(responseCode = "404", description = "Like not found")
        })
        public ResponseEntity<ApiResponseDto<Void>> removeLike(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "News ID to unlike", required = true) @PathVariable UUID newsId) {
                UUID userId = userDetails.getUserId();
                log.info("Like [{}]: Removing like for news: {}", userId, newsId);
                likeService.removeLikeByUser(newsId, userId);
                log.info("Like [{}]: Like removed for news: {}", userId, newsId);
                return ResponseEntity.ok(ApiResponseDto.success("Like removed successfully", null));
        }
}
