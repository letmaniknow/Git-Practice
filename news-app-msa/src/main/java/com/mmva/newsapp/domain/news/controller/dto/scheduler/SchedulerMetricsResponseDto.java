package com.mmva.newsapp.domain.news.controller.dto.scheduler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for scheduler metrics endpoint.
 * Provides aggregated performance statistics for job scheduling system.
 * 
 * Used in admin dashboard to monitor job execution health and performance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulerMetricsResponseDto {

    /**
     * Total number of jobs executed in time period
     */
    private Long totalJobs;

    /**
     * Number of jobs that completed successfully
     */
    private Long successfulJobs;

    /**
     * Number of jobs that completed with failures
     */
    private Long failedJobs;

    /**
     * Number of jobs that partially succeeded (some articles failed, some
     * succeeded)
     */
    private Long partialSuccessJobs;

    /**
     * Number of jobs still running
     */
    private Long runningJobs;

    /**
     * Success rate percentage (successfulJobs / totalJobs * 100)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "#.##")
    private Double successRate;

    /**
     * Failure rate percentage (failedJobs / totalJobs * 100)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "#.##")
    private Double failureRate;

    /**
     * Partial success rate percentage (partialSuccessJobs / totalJobs * 100)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "#.##")
    private Double partialSuccessRate;

    /**
     * Total number of articles processed across all jobs
     */
    private Long totalArticlesProcessed;

    /**
     * Number of articles successfully published
     */
    private Long articlesSuccessfullyPublished;

    /**
     * Number of articles that failed publication
     */
    private Long articlesFailed;

    /**
     * Number of articles that were skipped
     */
    private Long articlesSkipped;

    /**
     * Average job duration in milliseconds
     */
    private Long avgJobDurationMs;

    /**
     * Minimum job duration in milliseconds
     */
    private Long minJobDurationMs;

    /**
     * Maximum job duration in milliseconds
     */
    private Long maxJobDurationMs;

    /**
     * Average number of articles per job
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT, pattern = "#.##")
    private Double avgArticlesPerJob;

    /**
     * Median job duration in milliseconds
     */
    private Long medianJobDurationMs;

    /**
     * 95th percentile job duration in milliseconds (99th percentile of slowest
     * jobs)
     */
    private Long p95JobDurationMs;

    /**
     * 99th percentile job duration in milliseconds (only 1% slower)
     */
    private Long p99JobDurationMs;

    /**
     * Number of jobs currently in retry queue
     */
    private Long jobsInRetryQueue;

    /**
     * Number of articles awaiting retry
     */
    private Long articlesAwaitingRetry;

    /**
     * Most common error code in failed attempts
     */
    private String mostCommonErrorCode;

    /**
     * Number of occurrences of most common error
     */
    private Long mostCommonErrorCount;

    /**
     * Timestamp when metrics were calculated
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant calculatedAt;

    /**
     * Time period for these metrics (e.g., "24h", "7d", "30d")
     */
    private String timePeriod;

    /**
     * Start of time period
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant periodStartAt;

    /**
     * End of time period
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Instant periodEndAt;
}
