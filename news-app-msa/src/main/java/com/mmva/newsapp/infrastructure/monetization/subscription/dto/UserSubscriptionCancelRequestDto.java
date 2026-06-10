package com.mmva.newsapp.infrastructure.monetization.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for cancelling a user subscription.
 * 
 * <p>
 * Field naming follows entity pattern:
 * {@code userSubscriptionCancel{FieldName}}
 * per PROJECT_PRINCIPLES.md §6.1 Feature-Contextual Naming.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for cancelling a user subscription")
public class UserSubscriptionCancelRequestDto {

    @Schema(description = "Reason for cancellation", example = "Too expensive", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    private String userSubscriptionCancelReason;

    @Schema(description = "Whether to cancel immediately or at period end", example = "false", defaultValue = "false")
    @Builder.Default
    private Boolean userSubscriptionCancelImmediately = false;

    @Schema(description = "Additional feedback from user", example = "Service was good but not needed anymore")
    @Size(max = 1000, message = "Feedback must not exceed 1000 characters")
    private String userSubscriptionCancelFeedback;
}
