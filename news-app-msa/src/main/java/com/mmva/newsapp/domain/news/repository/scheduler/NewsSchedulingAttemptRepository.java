package com.mmva.newsapp.domain.news.repository.scheduler;

import com.mmva.newsapp.domain.news.model.scheduler.NewsSchedulingAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * NewsSchedulingAttemptRepository - Data access for publication attempts
 *
 * Purpose:
 * - Query per-article attempt history
 * - Find failed articles for retry
 * - Track attempt patterns for debugging
 * - Support failure analysis and metrics
 */
@Repository
public interface NewsSchedulingAttemptRepository extends JpaRepository<NewsSchedulingAttempt, UUID> {

    /**
     * Find all attempts for a specific job
     * Used to view job details and per-article results
     */
    List<NewsSchedulingAttempt> findByNewsSchedulingJob_JobId(UUID jobId);

    /**
     * Find all failed attempts for a job
     * Used to identify which articles failed in a job
     */
    @Query("SELECT a FROM NewsSchedulingAttempt a WHERE a.newsSchedulingJob.jobId = :jobId AND a.status = 'FAILED'")
    List<NewsSchedulingAttempt> findFailedAttemptsByJob(@Param("jobId") UUID jobId);

    /**
     * Find all attempts for a specific article
     * Used to view retry history for an article
     */
    List<NewsSchedulingAttempt> findByArticleIdOrderByTimestampDesc(UUID articleId);

    /**
     * Find articles that should be retried
     * Used by retry service to process failed articles
     */
    @Query("SELECT a FROM NewsSchedulingAttempt a WHERE a.status = 'FAILED' AND a.shouldRetry = TRUE ORDER BY a.timestamp ASC")
    List<NewsSchedulingAttempt> findFailedArticlesForRetry();

    /**
     * Find failed articles within retry limit
     * Used to filter out articles that have exceeded max retries
     */
    @Query("SELECT a FROM NewsSchedulingAttempt a WHERE a.status = 'FAILED' AND a.shouldRetry = TRUE AND a.retryCount < :maxRetries ORDER BY a.timestamp ASC")
    List<NewsSchedulingAttempt> findFailedArticlesUnderRetryLimit(@Param("maxRetries") int maxRetries);

    /**
     * Count failed attempts for an article
     * Used to track retry history
     */
    long countByArticleIdAndStatus(UUID articleId, String status);

    /**
     * Count total failures in a job
     * Used for job statistics
     */
    long countByNewsSchedulingJob_JobIdAndStatus(UUID jobId, String status);

    /**
     * Get the most recent attempt for an article
     * Used to check last attempt details
     */
    Optional<NewsSchedulingAttempt> findFirstByArticleIdOrderByTimestampDesc(UUID articleId);

    /**
     * Count articles that can be retried in a job
     * Used for monitoring
     */
    @Query("SELECT COUNT(a) FROM NewsSchedulingAttempt a WHERE a.newsSchedulingJob.jobId = :jobId AND a.shouldRetry = TRUE")
    long countRetryableFailures(@Param("jobId") UUID jobId);

    /**
     * Find all skipped attempts
     * Used to identify articles that were skipped
     */
    List<NewsSchedulingAttempt> findByStatus(String status);

    /**
     * Find attempts within a time range
     * Used for metrics aggregation and historical queries
     */
    List<NewsSchedulingAttempt> findByTimestampBetween(java.time.Instant start, java.time.Instant end);
}
