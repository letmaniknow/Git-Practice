package com.mmva.newsapp.domain.appuser.dto.operations;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for bulk user operations (admindashboard use).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserBulkOperationDto {

    @Schema(description = "List of user IDs to operate on", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "User IDs list cannot be empty")
    private List<UUID> userIds;

    @Schema(description = "Operation to perform on users", example = "activate", requiredMode = Schema.RequiredMode.REQUIRED)
    private String operation; // activate, deactivate, delete, unlock
}
