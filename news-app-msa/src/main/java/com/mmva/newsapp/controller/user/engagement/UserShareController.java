package com.mmva.newsapp.controller.user.engagement;

import com.mmva.newsapp.domain.newsengagement.shares.dto.NewsShareRequestDto;
import com.mmva.newsapp.domain.newsengagement.shares.dto.NewsShareResponseDto;
import com.mmva.newsapp.domain.newsengagement.shares.service.NewsShareService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * User Share Controller.
 * 
 * <p>
 * Handles all share operations for the authenticated user:
 * </p>
 * <ul>
 * <li>Share recording</li>
 * <li>Share queries (list, check status, count)</li>
 * </ul>
 * 
 * <p>
 * Path prefix: /api/v1/me/shares
 * </p>
 */
@CrossOrigin(origins = "*", allowedHeaders = { "Range", "Accept", "Content-Type", "Authorization" }, exposedHeaders = {
                "Content-Length", "Content-Range", "Accept-Ranges" })
@RestController
@RequestMapping("/api/v1/me/shares")
@Tag(name = "User Shares", description = "Share tracking for authenticated users")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserShareController {

        private final NewsShareService shareService;

        // ==========================================
        // SHARE OPERATIONS (1-4)
        // ==========================================

        @GetMapping
        @Operation(summary = "1. Get my shares", description = "Retrieves all news articles shared by the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Shares retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<List<NewsShareResponseDto>>> getMyShares(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Share [{}]: Fetching all shares", userId);
                List<NewsShareResponseDto> shares = shareService.getSharesByUserId(userId);
                log.debug("Share [{}]: Retrieved {} shares", userId, shares.size());
                return ResponseEntity.ok(ApiResponseDto.success("Shares retrieved successfully", shares));
        }

        @PostMapping
        @Operation(summary = "2. Record a share", description = "Records that the user shared a news article")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Share recorded"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<NewsShareResponseDto>> recordShare(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Valid @RequestBody NewsShareRequestDto dto) {
                UUID userId = userDetails.getUserId();
                log.info("Share [{}]: Adding share for news: {}", userId, dto.getNewsSharesNewsId());
                dto.setNewsSharesUserId(userId);
                NewsShareResponseDto response = shareService.addShare(dto);
                log.info("Share [{}]: Share added for news: {}", userId, dto.getNewsSharesNewsId());
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("Share recorded successfully", response));
        }

        @GetMapping("/check/{newsId}")
        @Operation(summary = "3. Check if news is shared", description = "Checks if a news article was shared by the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Share status retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Boolean>> hasSharedNews(
                        @AuthenticationPrincipal AppUserDetails userDetails,
                        @Parameter(description = "News ID to check", required = true) @PathVariable UUID newsId) {
                UUID userId = userDetails.getUserId();
                log.debug("Share [{}]: Checking share status for news: {}", userId, newsId);
                boolean shared = shareService.hasUserShared(newsId, userId);
                log.debug("Share [{}]: News {} shared: {}", userId, newsId, shared);
                return ResponseEntity.ok(ApiResponseDto.success("Share status retrieved", shared));
        }

        @GetMapping("/count")
        @Operation(summary = "4. Get my share count", description = "Gets the total number of shares by the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Share count retrieved"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponseDto<Long>> getMyShareCount(
                        @AuthenticationPrincipal AppUserDetails userDetails) {
                UUID userId = userDetails.getUserId();
                log.debug("Share [{}]: Getting share count", userId);
                Long count = shareService.getShareCountByUserId(userId);
                log.debug("Share [{}]: Share count: {}", userId, count);
                return ResponseEntity.ok(ApiResponseDto.success("Share count retrieved", count));
        }
}
