package com.mmva.newsapp.domain.news.dto.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NewsSchedulingBulkRetryResponseDto
 *
 * Response DTO for bulk retry operations.
 * Provides summary of articles queued for retry.
 *
 * Used in:
 * - POST /api/v1/admin/scheduler/publish/failed-articles/retry-all - Bulk retry
 * endpoint
 *
 * @author MMVA News Team
 * @since 1.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSchedulingBulkRetryResponseDto {

    @JsonProperty("articles_to_retry_count")
    private Integer articlesToRetryCount;

    @JsonProperty("jobs_created_count")
    private Integer jobsCreatedCount;

    @JsonProperty("new_job_id")
    private String newJobId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("message")
    private String message;
}
