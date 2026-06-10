package com.mmva.newsapp.domain.news.repository.scheduler;

import com.mmva.newsapp.domain.news.model.scheduler.NewsSchedulingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * NewsSchedulingJobRepository - Data access for scheduled publish jobs
 *
 * Purpose:
 * - Query job history and status
 * - Find jobs by status for monitoring
 * - Support job metrics and statistics
 * - Enable historical job lookups
 */
@Repository
public interface NewsSchedulingJobRepository extends JpaRepository<NewsSchedulingJob, UUID> {

    /**
     * Find jobs with specific status
     * Used for job monitoring and status checks
     */
    List<NewsSchedulingJob> findByStatus(String status);

    /**
     * Find the most recent job
     * Used to check if last job succeeded
     */
    Optional<NewsSchedulingJob> findFirstByOrderByStartedAtDesc();

    /**
     * Find jobs started within a time range
     * Used for job history queries
     */
    @Query("SELECT j FROM NewsSchedulingJob j WHERE j.startedAt BETWEEN :start AND :end ORDER BY j.startedAt DESC")
    List<NewsSchedulingJob> findJobsBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Find jobs with failures (failed or partial success)
     * Used for monitoring failed jobs
     */
    @Query("SELECT j FROM NewsSchedulingJob j WHERE j.status IN ('FAILED', 'PARTIAL_SUCCESS') ORDER BY j.startedAt DESC")
    List<NewsSchedulingJob> findJobsWithFailures();

    /**
     * Count total jobs
     * Used for metrics
     */
    long countByStatus(String status);

    /**
     * Find running jobs (should be 0 or 1 normally)
     * Used to detect stuck jobs
     */
    @Query("SELECT j FROM NewsSchedulingJob j WHERE j.status = 'RUNNING'")
    List<NewsSchedulingJob> findRunningJobs();

    /**
     * Calculate average job duration
     * Used for performance monitoring
     */
    @Query("SELECT AVG(j.durationMs) FROM NewsSchedulingJob j WHERE j.durationMs IS NOT NULL")
    Long getAverageJobDuration();

    /**
     * Calculate failure rate
     * Used for health monitoring
     */
    @Query("SELECT COUNT(j) FROM NewsSchedulingJob j WHERE j.status IN ('FAILED', 'PARTIAL_SUCCESS')")
    long getFailedJobCount();

    /**
     * Find jobs started within a time range ordered by start time descending
     * Used for metrics calculation over time periods (24h, 7d, 30d)
     */
    Optional<List<NewsSchedulingJob>> findByStartedAtBetweenOrderByStartedAtDesc(Instant start, Instant end);

    /**
     * Find jobs started within a time range (non-optional version)
     * Used for metrics controller queries
     */
    List<NewsSchedulingJob> findByStartedAtBetween(Instant start, Instant end);
}
