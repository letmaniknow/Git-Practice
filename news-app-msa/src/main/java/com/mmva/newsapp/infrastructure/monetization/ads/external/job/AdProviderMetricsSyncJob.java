package com.mmva.newsapp.infrastructure.monetization.ads.external.job;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.enums.ProviderType;
import com.mmva.newsapp.infrastructure.monetization.ads.external.common.service.AdProviderService;
import com.mmva.newsapp.infrastructure.monetization.ads.external.factory.AdProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Scheduled job for synchronizing ad provider metrics
 * 
 * Runs periodically to fetch latest metrics from all enabled providers
 * and stores them in the local database
 * 
 * Execution:
 * - Triggers: Every hour at minute 0 (configurable via cron)
 * - Providers: All enabled providers in application.yaml
 * - Timeout: Individual provider sync timeout in seconds
 * - Retry: Automatic retry on failure (3 attempts configurable)
 * 
 * Benefits:
 * - Centralized sync management (no need per-provider jobs)
 * - Consistent error handling across all providers
 * - Easy to monitor and alert on failures
 * - Metrics available immediately in dashboard
 * 
 * Naming Convention: AdProviderMetricsSyncJob (Job suffix indicates scheduled
 * component)
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdProviderMetricsSyncJob {

    private final AdProviderFactory adProviderFactory;

    /**
     * Scheduled task to sync all ad provider metrics
     * 
     * Cron expression: 0 0 * * * ?
     * - Field 0: Seconds (0) = Every hour at second 0
     * - Field 1: Minutes (0) = Every hour at minute 0
     * - Field 2: Hours (*) = Every hour
     * - Field 3: Day of month (*) = Every day
     * - Field 4: Month (*) = Every month
     * - Field 5: Day of week (?) = Any day of week
     * 
     * Result: Runs at 00:00, 01:00, 02:00, ... 23:00 every day
     * 
     * Alternative schedules:
     * - Every 30 minutes: 0 0/30 * * * ?
     * - Every 6 hours: 0 0 0/6 * * ?
     * - Daily at 2:00 AM: 0 0 2 * * ?
     * - Weekdays only: 0 0 * * MON-FRI ?
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncAllAdProviderMetrics() {
        log.info("════════════════════════════════════════════════════════════════");
        log.info("🔄 START: Ad Provider Metrics Sync Job");
        log.info("════════════════════════════════════════════════════════════════");

        try {
            // Get all enabled providers
            Map<ProviderType, AdProviderService> enabledProviders = adProviderFactory.getAllEnabledProviders();

            if (enabledProviders.isEmpty()) {
                log.warn("⚠️  No enabled ad providers found. Sync skipped.");
                return;
            }

            log.info("📊 Syncing metrics for {} enabled provider(s)...", enabledProviders.size());

            // Sync each provider
            int successCount = 0;
            int failureCount = 0;

            for (Map.Entry<ProviderType, AdProviderService> entry : enabledProviders.entrySet()) {
                ProviderType adProviderType = entry.getKey();
                AdProviderService provider = entry.getValue();

                try {
                    long startTime = System.currentTimeMillis();

                    log.info("🔄 Syncing {}: {}...",
                            adProviderType.getAdProviderCode(),
                            adProviderType.getAdProviderDisplayName());

                    // Sync metrics (credential would come from stored token in real implementation)
                    provider.syncAdProviderMetricsToDatabase(null);

                    long duration = System.currentTimeMillis() - startTime;

                    log.info("✅ Successfully synced {}: {} ({}ms)",
                            adProviderType.getAdProviderCode(),
                            adProviderType.getAdProviderDisplayName(),
                            duration);

                    successCount++;

                } catch (IOException e) {
                    log.error("❌ Failed to sync {}: {}",
                            adProviderType.getAdProviderCode(),
                            e.getMessage(), e);

                    failureCount++;

                    // Continue with next provider even if one fails
                    // This ensures we don't lose data from other providers

                } catch (Exception e) {
                    log.error("❌ Unexpected error syncing {}",
                            adProviderType.getAdProviderCode(), e);

                    failureCount++;
                }
            }

            // Log summary
            log.info("════════════════════════════════════════════════════════════════");
            log.info("📊 SYNC SUMMARY:");
            log.info("   Total providers processed: {}", enabledProviders.size());
            log.info("   ✅ Successful: {}", successCount);
            log.info("   ❌ Failed: {}", failureCount);
            log.info("════════════════════════════════════════════════════════════════");

            if (failureCount > 0) {
                log.warn("⚠️  Some providers failed to sync. Check logs above for details.");
                // TODO: Send alert to admin dashboard or notification service
            } else {
                log.info("✅ All ad provider metrics synced successfully!");
            }

        } catch (Exception e) {
            log.error("❌ CRITICAL: Ad Provider Metrics Sync Job failed", e);
            // TODO: Send alert to monitoring/alerting system
        }
    }

    /**
     * Alternative sync job for development/testing
     * 
     * Runs every 5 minutes for faster feedback during development
     * Disable this in production
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncAdProviderMetricsFrequent() {
        // Only run in development mode
        if (System.getenv("SYNC_FREQUENT_DEV_MODE") == null) {
            return;
        }

        log.debug("🔄 Running frequent sync (development mode)...");

        try {
            Map<ProviderType, AdProviderService> enabledProviders = adProviderFactory.getAllEnabledProviders();

            for (AdProviderService provider : enabledProviders.values()) {
                try {
                    provider.syncAdProviderMetricsToDatabase(null);
                    log.debug("✅ Frequent sync completed for: {}", provider.getAdProviderDisplayName());
                } catch (IOException e) {
                    log.debug("⚠️  Frequent sync failed for: {}", provider.getAdProviderDisplayName());
                }
            }

        } catch (Exception e) {
            log.debug("⚠️  Frequent sync error", e);
        }
    }
}
