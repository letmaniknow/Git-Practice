package com.mmva.newsapp.controller.admin.social;

// ===============================
// OpenAPI/Swagger Imports
// ===============================
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// ===============================
// Spring Framework Imports
// ===============================
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// ===============================
// Lombok Imports
// ===============================
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ===============================
// Jakarta Validation Imports
// ===============================
import jakarta.validation.Valid;

// ===============================
// Project Imports
// ===============================
import com.mmva.newsapp.domain.news.dto.social.SocialMediaShareDashboardResponseDto;
import com.mmva.newsapp.domain.news.dto.social.SocialMediaShareMarkPlatformSharedRequestDto;
import com.mmva.newsapp.domain.news.dto.social.SocialMediaShareMarkPlatformsSharedRequestDto;
import com.mmva.newsapp.domain.news.service.social.SocialMediaShareService;

import java.util.Map;

/**
 * Admin Social Media Sharing Controller
 * Provides endpoints for managing social media sharing of news articles
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/admin/social/sharing")
@PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Admin Social Media Sharing", description = "Social media sharing management for administrators and editors")
public class AdminSocialMediaSharingController {

        private final SocialMediaShareService socialMediaShareService;

        /**
         * Get social media sharing dashboard data
         * Returns news articles that need social sharing with platform status details
         */
        @GetMapping("/dashboard")
        @Operation(summary = "Get Social Media Sharing Dashboard", description = "Retrieves the social media sharing dashboard with news articles that need sharing, organized by priority levels")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN or EDITOR role"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<SocialMediaShareDashboardResponseDto> getSharingDashboard() {
                log.info("Admin requesting social media sharing dashboard");

                SocialMediaShareDashboardResponseDto dashboard = socialMediaShareService
                                .getSocialMediaSharingDashboard();

                log.debug("Returning dashboard with {} high priority, {} medium priority, {} low priority, {} scheduled items",
                                dashboard.getHighPriorityCount(),
                                dashboard.getMediumPriorityCount(),
                                dashboard.getLowPriorityCount(),
                                dashboard.getScheduledReadyCount());

                return ResponseEntity.ok(dashboard);
        }

        /**
         * Mark a specific platform as shared for a news article
         */
        @PostMapping("/mark-platform")
        @Operation(summary = "Mark Platform as Shared", description = "Marks a specific social media platform as shared for a news article")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Platform marked as shared successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN or EDITOR role"),
                        @ApiResponse(responseCode = "404", description = "News article or sharing record not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<Map<String, String>> markPlatformShared(
                        @Valid @RequestBody SocialMediaShareMarkPlatformSharedRequestDto request) {

                log.info("Admin marking platform {} as shared for news article: {}",
                                request.getPlatform(), request.getNewsId());

                socialMediaShareService.markPlatformShared(request);

                Map<String, String> response = Map.of(
                                "message", "Platform " + request.getPlatform() + " marked as shared for news article",
                                "status", "success");

                return ResponseEntity.ok(response);
        }

        /**
         * Mark multiple platforms as shared for a news article
         */
        @PostMapping("/mark-platforms")
        @Operation(summary = "Mark Multiple Platforms as Shared", description = "Marks multiple social media platforms as shared for a news article in a single operation")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Platforms marked as shared successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN or EDITOR role"),
                        @ApiResponse(responseCode = "404", description = "News article or sharing record not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<Map<String, Object>> markPlatformsShared(
                        @Valid @RequestBody SocialMediaShareMarkPlatformsSharedRequestDto request) {

                log.info("Admin marking platforms {} as shared for news article: {}", request.getPlatforms(),
                                request.getNewsId());

                socialMediaShareService.markPlatformsShared(request.getNewsId(), request.getPlatforms(),
                                request.getSharedBy());

                Map<String, Object> response = Map.of(
                                "message", "Platforms " + request.getPlatforms() + " marked as shared for news article",
                                "platformsMarked", request.getPlatforms().size(),
                                "status", "success");

                return ResponseEntity.ok(response);
        }

        /**
         * Get social media sharing statistics
         */
        @GetMapping("/statistics")
        @Operation(summary = "Get Sharing Statistics", description = "Retrieves statistics about social media sharing performance and completion rates")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN or EDITOR role"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<Map<String, Object>> getSharingStatistics() {
                log.info("Admin requesting social media sharing statistics");

                Map<String, Object> statistics = socialMediaShareService.getSharingStatistics();

                return ResponseEntity.ok(statistics);
        }
}