package com.mmva.newsapp.domain.news.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for advanced admin news search operations.
 *
 * <p>
 * Encapsulates all search and filter parameters that can be applied to admin
 * news queries. All fields are optional, allowing flexible search combinations.
 * </p>
 *
 * <h3>Field Descriptions:</h3>
 * <ul>
 * <li><strong>query:</strong> Free-text search across title and content (case-insensitive)</li>
 * <li><strong>workflowStatuses:</strong> Filter by one or more status values</li>
 * <li><strong>categoryId:</strong> Filter by news category UUID</li>
 * <li><strong>fromDate:</strong> Start date for news creation date range (inclusive)</li>
 * <li><strong>toDate:</strong> End date for news creation date range (inclusive)</li>
 * </ul>
 *
 * <h3>Workflow Statuses Allowed:</h3>
 * <pre>
 * - DRAFT: Unpublished, not yet submitted
 * - SUBMITTED: Awaiting review
 * - REVIEWED: Reviewed by moderator
 * - APPROVED: Ready to publish
 * - SCHEDULED: Scheduled for future publication
 * - PUBLISHED: Currently live
 * - ARCHIVED: Archived/retired
 * </pre>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * GET /api/v1/admin/news/search-advanced?query=breaking&workflowStatuses=PUBLISHED,SCHEDULED&fromDate=2026-03-01&toDate=2026-03-31
 * </pre>
 *
 * @author MMVA Team
 * @version 1.0
 * @since 2026-03-04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request parameters for advanced admin news search")
public class AdminNewsSearchRequest {

    /**
     * Free-text search query.
     *
     * <p>
     * Searches across:
     * <ul>
     * <li>Title (English)</li>
     * <li>Title (Spanish)</li>
     * <li>Content (English)</li>
     * <li>Content (Spanish)</li>
     * </ul>
     * Case-insensitive partial matching (LIKE '%query%').
     * </p>
     *
     * @example "breaking news"
     */
    @Size(max = 500, message = "Search query must not exceed 500 characters")
    @Schema(
        description = "Free-text search query (searches title and content)",
        example = "breaking",
        maxLength = 500
    )
    private String query;

    /**
     * List of workflow statuses to filter by.
     *
     * <p>
     * Combines multiple statuses with OR logic (returns news matching ANY status).
     * Supports bulk filtering for multi-status queries needed by admin dashboards.
     * </p>
     *
     * @example ["PUBLISHED", "SCHEDULED"]
     */
    @Schema(
        description = "Workflow status values to filter by (OR logic)",
        example = "[\"PUBLISHED\", \"SCHEDULED\"]"
    )
    private List<String> workflowStatuses;

    /**
     * Category ID for filtering.
     *
     * <p>
     * Must be a valid UUID of an existing NewsCategory.
     * When specified, only news in this category will be returned.
     * </p>
     *
     * @example "123e4567-e89b-12d3-a456-426614174000"
     */
    @Schema(
        description = "UUID of the news category to filter by",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private String categoryId;

    /**
     * Start date for news creation date range (inclusive).
     *
     * <p>
     * Filters news created on or after this date.
     * Pairs with toDate to define a range; may be used alone for "from" queries.
     * </p>
     *
     * @example "2026-03-01"
     */
    @Schema(
        description = "Start date for news creation date range (inclusive)",
        example = "2026-03-01"
    )
    private LocalDate fromDate;

    /**
     * End date for news creation date range (inclusive).
     *
     * <p>
     * Filters news created on or before this date.
     * Pairs with fromDate to define a range; may be used alone for "until" queries.
     * </p>
     *
     * @example "2026-03-31"
     */
    @Schema(
        description = "End date for news creation date range (inclusive)",
        example = "2026-03-31"
    )
    private LocalDate toDate;

    /**
     * Validates that fromDate is not after toDate.
     *
     * @return true if date range is valid or no dates specified, false if fromDate > toDate
     */
    public boolean isValidDateRange() {
        if (fromDate == null || toDate == null) {
            return true; // Valid if only one date specified
        }
        return !fromDate.isAfter(toDate);
    }

    /**
     * Checks if any filter criteria has been specified.
     *
     * @return true if at least one filter field is non-null/non-empty
     */
    public boolean hasAnyFilter() {
        return (query != null && !query.isBlank()) ||
               (workflowStatuses != null && !workflowStatuses.isEmpty()) ||
               (categoryId != null && !categoryId.isBlank()) ||
               fromDate != null ||
               toDate != null;
    }

    /**
     * String representation for logging.
     *
     * @return formatted string showing all filter criteria
     */
    @Override
    public String toString() {
        return "AdminNewsSearchRequest{" +
               "query='" + query + '\'' +
               ", workflowStatuses=" + workflowStatuses +
               ", categoryId='" + categoryId + '\'' +
               ", fromDate=" + fromDate +
               ", toDate=" + toDate +
               '}';
    }
}
