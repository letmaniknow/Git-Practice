package com.mmva.newsapp.infrastructure.common.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for bulk operation results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationResultDto {

    @Schema(description = "Number of successful operations", example = "10")
    private int successCount;

    @Schema(description = "Number of failed operations", example = "2")
    private int failureCount;

    @Schema(description = "List of successfully processed user IDs")
    private List<UUID> successfulIds;

    @Schema(description = "List of failed user IDs")
    private List<UUID> failedIds;

    @Schema(description = "Summary message for the bulk operation", example = "8 users updated successfully, 2 failed.")
    private String message;
}
