package com.mmva.newsapp.controller.open.ads;

// OpenAPI/Swagger imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

// Spring imports
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Lombok imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Java imports
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

// Project imports
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.common.ratelimit.service.RateLimiterService;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdClickRecordRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdImpressionRecordRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.service.AdsService;
import com.mmva.newsapp.infrastructure.requestanalytics.service.RequestInfoService;

/**
 * Public Ads Controller - For anonymous and authenticated ad tracking.
 *
 * <p>
 * All endpoints are public and support both anonymous users (via session IDs)
 * and authenticated users (via user IDs). Used by frontend for ad impression
 * and click tracking.
 * </p>
 *
 * <ul>
 * <li>Path prefix: /api/v1/public/ads</li>
 * <li>API versioning in path</li>
 * <li>No authentication required (public endpoints)</li>
 * <li>CORS enabled for frontend calls</li>
 * <li>Rate limited: 100 impressions/minute, 50 clicks/minute per IP</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@CrossOrigin(origins = "*")
@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/public/ads")
@Tag(name = "Public Ads", description = "Public ad tracking APIs for impressions and clicks")
@RequiredArgsConstructor
public class PublicAdsController {

        // =============================
        // Constants
        // =============================
        private static final String DEFAULT_TENANT = "default";

        // Rate limiting constants
        private static final String RATE_LIMIT_IMPRESSION = "ad-impression";
        private static final String RATE_LIMIT_CLICK = "ad-click";

        // =============================
        // Dependencies
        // =============================
        private final AdsService adsService;
        private final RequestInfoService requestInfoService;
        private final RateLimiterService rateLimiterService;

        // =============================
        // Tracking Endpoints
        // =============================

        /**
         * 1. Record Ad Impression.
         *
         * <p>
         * Records when an ad is displayed to a user. Supports both anonymous
         * and authenticated users. For anonymous users, sessionId is required
         * for tracking. For authenticated users, userId provides additional context.
         * </p>
         *
         * <p>
         * <strong>Anonymous Tracking:</strong> Set userId to null, provide sessionId
         * <br>
         * <strong>Authenticated Tracking:</strong> Provide both userId and sessionId
         * </p>
         */
        @PostMapping("/impressions")
        @Operation(summary = "1. Record Ad Impression", description = "Records an ad impression for tracking (anonymous or authenticated)")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Impression recorded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
        })
        public ResponseEntity<ApiResponseDto<Map<String, String>>> recordImpression(
                        @Valid @org.springframework.web.bind.annotation.RequestBody AdImpressionRecordRequestDto request,
                        HttpServletRequest httpRequest) {

                log.debug("Public: Recording impression for campaign: {}, placement: {}",
                                request.getAdImpressionCampaignId(), request.getAdImpressionPlacementId());

                // Rate limiting: 100 impressions per minute per IP
                String clientIp = requestInfoService.getClientIpAddress(httpRequest);
                String rateLimitKey = clientIp + ":" + RATE_LIMIT_IMPRESSION;

                if (!rateLimiterService.isAllowed(rateLimitKey)) {
                        long retryAfter = rateLimiterService.getRemainingCooldownSeconds(rateLimitKey);
                        log.warn("Rate limit exceeded for impression tracking from IP: {}", clientIp);
                        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                                        ApiResponseDto.error(
                                                        "Rate limit exceeded. Please try again later. Retry after "
                                                                        + retryAfter + " seconds."));
                }

                // Extract request metadata for fraud detection and analytics
                String ipHash = hashIp(requestInfoService.getClientIpAddress(httpRequest));
                String userAgent = httpRequest.getHeader("User-Agent");

                // Record the impression
                UUID impressionId = adsService.recordImpression(request, DEFAULT_TENANT, ipHash, userAgent);

                // Record the request for rate limiting
                rateLimiterService.recordRequest(rateLimitKey);

                log.info("Public: Impression recorded - campaign: {}, placement: {}, user: {}, session: {}, impressionId: {}",
                                request.getAdImpressionCampaignId(),
                                request.getAdImpressionPlacementId(),
                                request.getAdImpressionUserId(),
                                request.getAdImpressionSessionId(),
                                impressionId);

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponseDto.success("Impression recorded successfully",
                                                Map.of("impressionId", impressionId.toString())));
        }

        /**
         * 2. Record Ad Click.
         *
         * <p>
         * Records when a user clicks on an ad. Supports both anonymous
         * and authenticated users. For anonymous users, sessionId is required
         * for tracking. For authenticated users, userId provides additional context.
         * </p>
         *
         * <p>
         * <strong>Anonymous Tracking:</strong> Set userId to null, provide sessionId
         * <br>
         * <strong>Authenticated Tracking:</strong> Provide both userId and sessionId
         * </p>
         */
        @PostMapping("/clicks")
        @Operation(summary = "2. Record Ad Click", description = "Records an ad click for tracking (anonymous or authenticated)")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Click recorded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
        })
        public ResponseEntity<ApiResponseDto<Map<String, String>>> recordClick(
                        @Valid @org.springframework.web.bind.annotation.RequestBody AdClickRecordRequestDto request,
                        HttpServletRequest httpRequest) {

                log.debug("Public: Recording click for campaign: {}, placement: {}",
                                request.getAdClickCampaignId(), request.getAdClickPlacementId());

                // Rate limiting: 50 clicks per minute per IP
                String clientIp = requestInfoService.getClientIpAddress(httpRequest);
                String rateLimitKey = clientIp + ":" + RATE_LIMIT_CLICK;

                if (!rateLimiterService.isAllowed(rateLimitKey)) {
                        long retryAfter = rateLimiterService.getRemainingCooldownSeconds(rateLimitKey);
                        log.warn("Rate limit exceeded for click tracking from IP: {}", clientIp);
                        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                                        ApiResponseDto.error(
                                                        "Rate limit exceeded. Please try again later. Retry after "
                                                                        + retryAfter + " seconds."));
                }

                // Extract request metadata for fraud detection and analytics
                String ipHash = hashIp(requestInfoService.getClientIpAddress(httpRequest));
                String userAgent = httpRequest.getHeader("User-Agent");

                // Record the click
                UUID clickId = adsService.recordClick(request, DEFAULT_TENANT, ipHash, userAgent);

                // Record the request for rate limiting
                rateLimiterService.recordRequest(rateLimitKey);

                log.info("Public: Click recorded - campaign: {}, placement: {}, user: {}, session: {}, clickId: {}",
                                request.getAdClickCampaignId(),
                                request.getAdClickPlacementId(),
                                request.getAdClickUserId(),
                                request.getAdClickSessionId(),
                                clickId);

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponseDto.success("Click recorded successfully",
                                                Map.of("clickId", clickId.toString())));
        }

        // =============================
        // Helper Methods
        // =============================

        /**
         * Hashes IP address for privacy and fraud detection.
         * Uses SHA-256 for one-way hashing.
         */
        private String hashIp(String ipAddress) {
                if (ipAddress == null || ipAddress.isEmpty()) {
                        return null;
                }

                try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(ipAddress.getBytes(StandardCharsets.UTF_8));
                        return HexFormat.of().formatHex(hash);
                } catch (Exception e) {
                        log.warn("Failed to hash IP address: {}", e.getMessage());
                        return null;
                }
        }
}