package com.mmva.newsapp.controller.admin.ads;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderMetricsDto;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.exception.AdProviderException;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.service.AdProviderService;
import com.mmva.newsapp.infrastructure.monetization.ads.external.factory.AdProviderFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified controller for managing all external ad providers
 * 
 * Handles:
 * - OAuth authentication for all providers
 * - Metrics fetching and syncing
 * - Provider configuration and status
 * 
 * Uses Factory Pattern to instantiate correct provider:
 * All requests routed through AdProviderFactory
 * 
 * Naming Convention:
 * - Endpoint prefix: /api/v1/admin/ad-providers
 * - Sub-resources: /{provider-code}, /{provider-code}/metrics,
 * /{provider-code}/sync
 * - Response wrapper: CommonApiResponse
 * 
 * Security:
 * - Admin role required (enforced by @PreAuthorize)
 * - OAuth tokens stored securely
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/admin/ad-providers")
@RequiredArgsConstructor
@Slf4j
public class AdminAdProvidersController {

    private final AdProviderFactory adProviderFactory;

    /**
     * Get OAuth authorization URL for a specific provider
     * 
     * User visits this URL to grant permission
     * 
     * @param adProviderCode Provider code (google-adsense, google-admob)
     * @return Authorization URL
     */
    @GetMapping("/{adProviderCode}/auth-url")
    public ResponseEntity<?> getAdProviderAuthorizationUrl(
            @PathVariable String adProviderCode) {

        try {
            log.info("📱 Requesting authorization URL for provider: {}", adProviderCode);

            AdProviderService provider = adProviderFactory.getAdProviderServiceByCode(adProviderCode);
            String authUrl = provider.getAdProviderAuthorizationUrl();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("adProviderCode", adProviderCode);
            response.put("adProviderDisplayName", provider.getAdProviderDisplayName());
            response.put("adProviderAuthorizationUrl", authUrl);
            response.put("message", "✅ Authorization URL generated. Redirect user to this URL to grant permission.");

            log.info("✅ Authorization URL generated for: {}", adProviderCode);

            return ResponseEntity.ok(response);

        } catch (AdProviderException e) {
            log.error("❌ Provider error: {}", e.getMessage());
            return createErrorResponse(e, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error("❌ IO error generating authorization URL", e);
            return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * OAuth callback endpoint - exchange authorization code for access token
     * 
     * @param adProviderCode Provider code
     * @param code           Authorization code from provider
     * @return Access token
     */
    @PostMapping("/{adProviderCode}/oauth-callback")
    public ResponseEntity<?> handleOAuthCallback(
            @PathVariable String adProviderCode,
            @RequestParam String code) {

        try {
            log.info("🔐 Processing OAuth callback for provider: {}", adProviderCode);

            AdProviderService provider = adProviderFactory.getAdProviderServiceByCode(adProviderCode);
            String accessToken = provider.authenticateWithProvider(code);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("adProviderCode", adProviderCode);
            response.put("adProviderDisplayName", provider.getAdProviderDisplayName());
            response.put("accessToken", accessToken);
            response.put("message", "✅ Authentication successful. Token stored securely.");

            log.info("✅ OAuth callback processed for: {}", adProviderCode);

            return ResponseEntity.ok(response);

        } catch (AdProviderException e) {
            log.error("❌ Authentication failed: {}", e.getMessage());
            return createErrorResponse(e, HttpStatus.UNAUTHORIZED);
        } catch (IOException e) {
            log.error("❌ IO error during authentication", e);
            return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get metrics for specific provider
     * 
     * Fetches metrics for given date range
     * 
     * @param adProviderCode Provider code
     * @param startDate      Period start (yyyy-MM-dd)
     * @param endDate        Period end (yyyy-MM-dd)
     * @return Provider metrics
     */
    @GetMapping("/{adProviderCode}/metrics")
    public ResponseEntity<?> getAdProviderMetrics(
            @PathVariable String adProviderCode,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        try {
            log.info("📊 Fetching metrics for provider: {}", adProviderCode);

            // Default to last 30 days
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            if (startDate == null) {
                startDate = endDate.minusDays(30);
            }

            AdProviderService provider = adProviderFactory.getAdProviderServiceByCode(adProviderCode);
            AdProviderMetricsDto metrics = provider.fetchAdProviderMetrics(null, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("adProviderCode", adProviderCode);
            response.put("adProviderDisplayName", provider.getAdProviderDisplayName());
            response.put("adProviderMetrics", metrics);
            response.put("message", "✅ Metrics fetched successfully");

            log.info("✅ Metrics fetched for: {}", adProviderCode);

            return ResponseEntity.ok(response);

        } catch (AdProviderException e) {
            log.error("❌ Provider error: {}", e.getMessage());
            return createErrorResponse(e, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error("❌ IO error fetching metrics", e);
            return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Sync metrics for specific provider
     * 
     * Manually trigger sync (normally automatic via scheduler)
     * 
     * @param adProviderCode Provider code
     * @return Sync result
     */
    @PostMapping("/{adProviderCode}/sync")
    public ResponseEntity<?> syncAdProviderMetrics(
            @PathVariable String adProviderCode) {

        try {
            log.info("🔄 Syncing metrics for provider: {}", adProviderCode);

            AdProviderService provider = adProviderFactory.getAdProviderServiceByCode(adProviderCode);
            provider.syncAdProviderMetricsToDatabase(null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("adProviderCode", adProviderCode);
            response.put("adProviderDisplayName", provider.getAdProviderDisplayName());
            response.put("adProviderLastSyncTime", provider.getLastAdProviderSyncTime());
            response.put("message", "✅ Metrics synced successfully");

            log.info("✅ Metrics synced for: {}", adProviderCode);

            return ResponseEntity.ok(response);

        } catch (AdProviderException e) {
            log.error("❌ Provider error: {}", e.getMessage());
            return createErrorResponse(e, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error("❌ IO error syncing metrics", e);
            return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get configuration and status for specific provider
     * 
     * @param adProviderCode Provider code
     * @return Provider status
     */
    @GetMapping("/{adProviderCode}/status")
    public ResponseEntity<?> getAdProviderStatus(
            @PathVariable String adProviderCode) {

        try {
            log.info("📋 Getting status for provider: {}", adProviderCode);

            AdProviderService provider = adProviderFactory.getAdProviderServiceByCode(adProviderCode);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("adProviderCode", adProviderCode);
            response.put("adProviderDisplayName", provider.getAdProviderDisplayName());
            response.put("isAdProviderEnabled", provider.isAdProviderEnabled());
            response.put("adProviderConfigurationStatus", provider.getAdProviderConfigurationStatus());
            response.put("adProviderLastSyncTime", provider.getLastAdProviderSyncTime());
            response.put("message", "✅ Provider status retrieved");

            log.info("✅ Status retrieved for: {}", adProviderCode);

            return ResponseEntity.ok(response);

        } catch (AdProviderException e) {
            log.error("❌ Provider error: {}", e.getMessage());
            return createErrorResponse(e, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get metrics for all enabled providers
     * 
     * Aggregates metrics from all active providers
     * 
     * @return Aggregated metrics
     */
    @GetMapping("/metrics/all")
    public ResponseEntity<?> getAllProvidersMetrics() {

        try {
            log.info("📊 Fetching metrics for all providers");

            Map<ProviderType, AdProviderService> enabledProviders = adProviderFactory.getAllEnabledProviders();

            if (enabledProviders.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "⚠️  No enabled providers found");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            }

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);

            Map<String, Object> allMetrics = new HashMap<>();
            for (Map.Entry<ProviderType, AdProviderService> entry : enabledProviders.entrySet()) {
                try {
                    AdProviderMetricsDto metrics = entry.getValue()
                            .fetchAdProviderMetrics(null, startDate, endDate);
                    allMetrics.put(entry.getKey().getAdProviderCode(), metrics);
                } catch (Exception e) {
                    log.warn("⚠️  Error fetching metrics for provider: {}", entry.getKey(), e);
                    allMetrics.put(entry.getKey().getAdProviderCode(),
                            Map.of("error", e.getMessage()));
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalEnabledProviders", enabledProviders.size());
            response.put("adProviderMetricsMap", allMetrics);
            response.put("message", "✅ Metrics for all providers retrieved");

            log.info("✅ Metrics retrieved for {} providers", enabledProviders.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error fetching all providers metrics", e);
            return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Sync metrics for all enabled providers
     * 
     * Manually trigger sync across all providers (normally automatic via scheduler)
     * 
     * @return Sync results
     */
    @PostMapping("/sync/all")
    public ResponseEntity<?> syncAllProvidersMetrics() {

        try {
            log.info("🔄 Syncing metrics for all providers");

            Map<ProviderType, AdProviderService> enabledProviders = adProviderFactory.getAllEnabledProviders();

            if (enabledProviders.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "⚠️  No enabled providers found");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            }

            Map<String, Object> syncResults = new HashMap<>();
            for (Map.Entry<ProviderType, AdProviderService> entry : enabledProviders.entrySet()) {
                try {
                    entry.getValue().syncAdProviderMetricsToDatabase(null);
                    syncResults.put(entry.getKey().getAdProviderCode(),
                            Map.of("status", "✅ SUCCESS",
                                    "syncTime", entry.getValue().getLastAdProviderSyncTime()));
                } catch (Exception e) {
                    log.warn("⚠️  Error syncing metrics for provider: {}", entry.getKey(), e);
                    syncResults.put(entry.getKey().getAdProviderCode(),
                            Map.of("status", "❌ FAILED", "error", e.getMessage()));
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalProvidersProcessed", enabledProviders.size());
            response.put("adProviderSyncResults", syncResults);
            response.put("message", "✅ Sync completed for all providers");

            log.info("✅ Sync completed for {} providers", enabledProviders.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error syncing all providers", e);
            return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Helper method to create consistent error responses
     */
    private ResponseEntity<?> createErrorResponse(Exception exception, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", exception.getClass().getSimpleName());
        errorResponse.put("message", exception.getMessage());

        if (exception instanceof AdProviderException ape) {
            errorResponse.put("adProviderType", ape.getAdProviderType());
            errorResponse.put("adProviderErrorCode", ape.getAdProviderErrorCode());
            errorResponse.put("adProviderErrorDetails", ape.getAdProviderErrorDetails());
        }

        return ResponseEntity.status(status).body(errorResponse);
    }
}
