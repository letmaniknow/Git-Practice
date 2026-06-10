package com.mmva.newsapp.domain.news.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data transfer object containing workflow validation results.
 *
 * <p>
 * Encapsulates validation results for publishing workflow including scheduling
 * validation, urgency level checks, and workflow consistency.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowValidationResult {

    /**
     * Whether the workflow configuration is valid.
     */
    private boolean isValid;

    /**
     * List of workflow validation errors.
     */
    private List<String> errors;

    /**
     * List of workflow validation warnings.
     */
    private List<String> warnings;

    /**
     * Recommended publish date/time if current one is invalid.
     */
    private LocalDateTime recommendedPublishDateTime;

    /**
     * Whether the publish date/time is in the future.
     */
    private boolean isScheduledForFuture;

    /**
     * Whether the urgency level is appropriate for the content.
     */
    private boolean urgencyLevelAppropriate;

    /**
     * Suggested urgency level if current one is inappropriate.
     */
    private String suggestedUrgencyLevel;

    /**
     * Whether draft status is appropriate.
     */
    private boolean draftStatusAppropriate;
}