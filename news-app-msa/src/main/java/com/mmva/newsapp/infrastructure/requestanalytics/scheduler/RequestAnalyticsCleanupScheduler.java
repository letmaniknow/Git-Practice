package com.mmva.newsapp.infrastructure.requestanalytics.scheduler;

import com.mmva.newsapp.infrastructure.requestanalytics.config.RequestAnalyticsProperties;
import com.mmva.newsapp.infrastructure.requestanalytics.repository.RequestAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Scheduled job for cleaning up old request analytics data.
 * 
 * <p>
 * Automatically deletes analytics records older than the configured
 * retention period to prevent unbounded table growth.
 * </p>
 * 
 * <h3>Configuration:</h3>
 * 
 * <pre>
 * app:
 *   analytics:
 *     enabled: true
 *     retention:
 *       enabled: true
 *       period: 90d
 *       cleanup-cron: "0 0 2 * * ?"
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestAnalyticsCleanupScheduler {

    private final RequestAnalyticsRepository analyticsRepository;
    private final RequestAnalyticsProperties properties;

    /**
     * Scheduled cleanup job that runs based on configured cron expression.
     * Deletes analytics records older than the retention period.
     */
    @Scheduled(cron = "${app.analytics.retention.cleanup-cron:0 0 2 * * ?}")
    @Transactional
    public void cleanupOldAnalytics() {
        if (!properties.getRetention().isEnabled()) {
            return;
        }

        Instant cutoffDate = Instant.now().minus(properties.getRetention().getPeriod());

        log.info("Starting request analytics cleanup. Deleting records older than {}", cutoffDate);

        try {
            long startTime = System.currentTimeMillis();
            int deletedCount = analyticsRepository.deleteOlderThan(cutoffDate);
            long duration = System.currentTimeMillis() - startTime;

            log.info("Request analytics cleanup completed. Deleted {} records in {} ms",
                    deletedCount, duration);
        } catch (Exception e) {
            log.error("Error during request analytics cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual cleanup method for administrative purposes.
     * 
     * @param olderThan delete records older than this instant
     * @return number of deleted records
     */
    @Transactional
    public int manualCleanup(Instant olderThan) {
        log.info("Manual cleanup initiated. Deleting records older than {}", olderThan);
        int deleted = analyticsRepository.deleteOlderThan(olderThan);
        log.info("Manual cleanup completed. Deleted {} records", deleted);
        return deleted;
    }
}
