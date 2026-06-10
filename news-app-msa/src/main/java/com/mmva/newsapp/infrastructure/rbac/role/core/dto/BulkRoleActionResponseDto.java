package com.mmva.newsapp.infrastructure.rbac.role.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for bulk role activation/deactivation")
public class BulkRoleActionResponseDto {

    @Schema(description = "Action performed", example = "BULK_ACTIVATE")
    private String action;

    @Schema(description = "Total roles processed")
    private Integer totalProcessed;

    @Schema(description = "Number of successful operations")
    private Integer successCount;

    @Schema(description = "Number of failed operations")
    private Integer failedCount;

    @Schema(description = "List of successfully processed role IDs")
    private List<UUID> successfulRoleIds;

    @Schema(description = "List of failed role IDs with reasons")
    private List<FailedRoleOperation> failedOperations;

    @Schema(description = "Timestamp of the operation")
    private String timestamp;

    @Schema(description = "User who performed the operation")
    private UUID performedBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedRoleOperation {
        private UUID roleId;
        private String reason;
    }
}
